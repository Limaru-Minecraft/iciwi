package mikeshafter.iciwi.FareGates;

import mikeshafter.iciwi.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class FareGateListener implements Listener {
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final CardSql cardSql = new CardSql();
  private final Lang lang = new Lang(plugin);
  private final Records records = new Records(plugin);
  private final Owners owners = new Owners(plugin);
  private final LinkedList<FareGate> gates = new LinkedList<>();
  boolean canClick = true;


  @EventHandler
  public void TicketBarrierSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && canClick) {

      Player player = event.getPlayer();
      Block block = event.getClickedBlock();
      BlockState state = block.getState();
      Location location = block.getLocation();
      BlockData data = block.getBlockData();
      FareGate gate = null;
      GateType gateAction = null;  // reusing GateType to save on enums. This value can only be ENTRY or EXIT.
      canClick = false;
      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> canClick = true, 10);
  
      if (state instanceof Sign sign && data instanceof WallSign) {
        
        // Payment sign
        if (ChatColor.stripColor(sign.getLine(0)).contains(lang.PAYMENT) && isDouble(sign.getLine(1))) {
    
          double price = Double.parseDouble(sign.getLine(1));
          ItemStack item = player.getInventory().getItemInMainHand();
    
          if (item.getType() == Material.NAME_TAG &&
                  item.hasItemMeta() &&
                  item.getItemMeta() != null &&
                  item.getItemMeta().hasLore() &&
                  item.getItemMeta().getLore() != null &&
                  item.getItemMeta().getLore().get(0).equals(lang.SERIAL_NUMBER)) {
            String serial = item.getItemMeta().getLore().get(1);
            Payment.pay(serial, player, price);
          }
          else {
            Payment.pay(player, price);
          }
    
          return;
          // DO NOT PARSE THE REST OF THE CODE
        }

        // Initialise fare gate; all signs point to this
        else if (ChatColor.stripColor(sign.getLine(0)).contains(lang.ENTRY) || ChatColor.stripColor(sign.getLine(0)).contains(lang.EXIT) || ChatColor.stripColor(sign.getLine(0)).contains(lang.VALIDATOR)) {
          gate = new FareGate(player, sign.getLine(0), block.getLocation());
        }
        
      }
  
      // same thing, but for HL-style fare gates
      else if (data instanceof Openable) {
        if (location.add(0, -2, 0).getBlock().getState() instanceof Sign sign && ChatColor.stripColor(sign.getLine(0)).contains(lang.FAREGATE)) {
          gate = new FareGate(player, sign.getLine(0), sign.getLocation());
        }
      } else return;

      // figuring out entry/exit
      if (gate != null) {
        GateType gateType = gate.getGateType();
        /*
        Paper ticket    | Y Y
        Card            |     Y Y
        • in Lore?      | Y N
        Card in config? |     Y N
        ==========================
        Entry           |   x   x
        Exit            | x   x
         */

        // get ticket/card type
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        Sign sign = state instanceof Sign ? (Sign) state : location.getBlock().getState() instanceof Sign ? (Sign) location.getBlock().getState() : null;
        if (meta != null && meta.hasLore() && meta.getLore() != null && sign != null) {

          String itemLore0 = meta.getLore().get(0);

          // station. if the gatetype is a faregate, use all 3 lines, else, use the first line only.
          String station = ChatColor.stripColor((gateType == GateType.ENTRY || gateType == GateType.EXIT || gateType == GateType.VALIDATOR) ? sign.getLine(1) : ChatColor.stripColor((gateType == GateType.FAREGATE) ? sign.getLine(1)+sign.getLine(2)+sign.getLine(3) : sign.getLine(0).split(" ", 2)[1] )).replaceAll(" ", "").replace("]", "");

          // === Card ===
          if (item.getType() == Material.NAME_TAG && itemLore0.equals(lang.SERIAL_NUMBER)) {

            String serial = meta.getLore().get(1);
            // If there is nothing in records, set gateAction to ENTRY. Else, set it to EXIT.
            gateAction = records.getString("station."+serial) == null ? GateType.ENTRY : GateType.EXIT;
            
            // boolean flag to block gate opening as that causes an error
            boolean blockOpen = gateType == GateType.VALIDATOR || gateType == GateType.SPECIAL;

            // set gateType to gateAction for easier manipulation since they are ambiguous. Also removes the chance of a fine.
            if (gateType == GateType.FAREGATE || gateType == GateType.VALIDATOR || gateType == GateType.SPECIAL) gateType = gateAction;

            if (gateType == GateType.ENTRY) {

              records.set("station."+serial, station);
              // check whether the player tapped out and in within the time limit
              if (System.currentTimeMillis()-records.getLong("timestamp."+serial) < plugin.getConfig().getLong("max-transfer-time")) {
                records.set("transfer."+serial, true);
              }
              double value = cardSql.getCardValue(serial);
              player.sendMessage(String.format(lang.TAPPED_IN, station, value));

              if (!blockOpen) {
                gates.add(gate);
                gate.open();
                gate.hold();
              }


            } else if (gateType == GateType.EXIT) {

              // get entry station and fare.
              String entryStation = records.getString("station."+serial);
              double fare = JsonManager.getFare(entryStation, station);

              // get rail pass discounts
              HashSet<String> discounts = cardSql.getDiscountedOperators(serial);
              String entryStationOwner = owners.getOwner(entryStation);
              String exitStationOwner = owners.getOwner(station);

              // get transfer discount
              if (records.getBoolean("transfer."+serial)) {
                fare -= plugin.getConfig().getDouble("transfer-discount");
              }

              // remove fare from discounts
              double half = fare/2;
              if (discounts.contains(entryStationOwner)) fare -= half;
              if (discounts.contains(exitStationOwner)) fare -= half;
              owners.deposit(entryStationOwner, fare/2);
              owners.deposit(exitStationOwner, fare/2);

              // set remaining value and config
              cardSql.subtractValueFromCard(serial, fare);
              double value = cardSql.getCardValue(serial);
              player.sendMessage(String.format(lang.REMAINING, value));
              records.set("timestamp."+serial, System.currentTimeMillis());
              records.set("station."+serial, null);

              player.sendMessage(String.format(lang.TAPPED_OUT, station, fare, value));
  
              if (!blockOpen) {
                gates.add(gate);
                gate.open();
                gate.hold();
              }
              
            }
          }


          // === Ticket ===
          else if (item.getType() == Material.PAPER) {

            // Ticket
            gateAction = itemLore0.contains("•") ? GateType.EXIT : GateType.ENTRY;
            if (meta.getLore().get(1).contains("•")) gateType = null;

            // set gateType to gateAction for easier manipulation since they are ambiguous.
            if (gateType == GateType.FAREGATE || gateType == GateType.VALIDATOR || gateType == GateType.SPECIAL) gateType = gateAction;

            if (gateType == GateType.ENTRY && !itemLore0.contains("•")) {

              // check if the ticket is valid for that station
              if (itemLore0.equals(station) || itemLore0.equals(lang.GLOBAL_TICKET)) {
                // punch hole
                List<String> lore = meta.getLore();
                lore.set(0, itemLore0+" •");
                meta.setLore(lore);
                item.setItemMeta(meta);
    
                player.sendMessage(String.format(lang.TICKET_IN, station));
    
                gates.add(gate);
                gate.open();
                gate.hold();
              }
            } else if (gateType == GateType.EXIT && itemLore0.contains("•")) {
              // check fare
              String entryStation = itemLore0.substring(0, itemLore0.length()-2);
              if (!entryStation.equals(lang.GLOBAL_TICKET)) {
                double fare = JsonManager.getFare(entryStation, station);
                String itemLore1 = meta.getLore().get(1);
                if (!itemLore1.contains("•") && (Objects.equals(itemLore1, station) || Double.parseDouble(itemLore1) >= fare)) {
                  List<String> lore = meta.getLore();
                  lore.set(1, itemLore1+" •");
                  meta.setLore(lore);
                  item.setItemMeta(meta);
                }
              } else {
                List<String> lore = meta.getLore();
                lore.set(1, meta.getLore().get(1)+" •");
                meta.setLore(lore);
                item.setItemMeta(meta);
              }
              player.sendMessage(String.format(lang.TICKET_OUT, station));
  
              gates.add(gate);
              gate.open();
              gate.hold();
            }
          }
        }
  
        // Check for fare evasion
        if (gateType != gateAction && (item.getType() == Material.NAME_TAG || item.getType() == Material.PAPER)) {
          player.sendMessage(lang.FARE_EVADE);
          Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
        }
  
      }
      }
    }
  
  
  @EventHandler
  public void CheckPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    for (FareGate g : gates) {
      int x = player.getLocation().getBlockX();
      int y = player.getLocation().getBlockY();
      int z = player.getLocation().getBlockZ();
      for (int[] gateLocation : g.getGateLocations()) {
        if (g.getPlayer() == player && gateLocation[0] == x && gateLocation[1] == y && gateLocation[2] == z) {
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            g.close();
            gates.remove(g);
          }, 10L);

        }
      }
    }
  }
  
  private boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }
}
