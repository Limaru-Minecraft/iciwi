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

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class FareGateListener implements Listener {
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final CardSql cardSql = new CardSql();
  private final Lang lang = new Lang(plugin);
  private final Records records = new Records(plugin);
  private final Owners owners = new Owners(plugin);
  private final LinkedList<FareGate> gates = new LinkedList<>();


  @EventHandler
  public void TicketBarrierSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
  
      Player player = event.getPlayer();
      Block block = event.getClickedBlock();
      BlockState state = block.getState();
      Location location = block.getLocation();
      Action action = event.getAction();
      BlockData data = block.getBlockData();
      FareGate gate = null;
  
      if (state instanceof Sign sign && data instanceof WallSign) {
        // Initialise fare gate; all signs point to this
        player.sendMessage(ChatColor.stripColor(sign.getLine(0)));  // TODO: DEBUG
        player.sendMessage(lang.ENTRY);  // TODO: DEBUG
        if (ChatColor.stripColor(sign.getLine(0)).contains(lang.ENTRY) || ChatColor.stripColor(sign.getLine(0)).contains(lang.EXIT) || ChatColor.stripColor(sign.getLine(0)).contains(lang.VALIDATOR)) {
          gate = new FareGate(player, sign.getLine(0), block.getLocation());
          player.sendMessage("DEBUG 1a");  // TODO: DEBUG
        }
    
        // same thing, but for HL-style fare gates
      } else if (data instanceof Openable) {
        if (location.add(0, -2, 0).getBlock().getState() instanceof Sign sign && ChatColor.stripColor(sign.getLine(0)).contains(lang.FAREGATE)) {
          player.sendMessage(lang.FAREGATE);  // TODO: DEBUG
          gate = new FareGate(player, sign.getLine(0), sign.getLocation());
          player.sendMessage("DEBUG 1b");  // TODO: DEBUG
        }
      } else return;

      // figuring out entry/exit
      if (gate != null) {
        player.sendMessage("DEBUG 0a");  // TODO: DEBUG
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
        Sign sign = state instanceof Sign ? (Sign) state : data instanceof Openable && location.getBlock().getState() instanceof Sign ? (Sign) location.getBlock().getState() : null;
        if (meta != null && meta.hasLore() && meta.getLore() != null && sign != null) {
    
          String itemLore0 = meta.getLore().get(0);
          GateType gateAction;  // reusing GateType to save on enums. This value can only be ENTRY or EXIT.
          // station
          String station = (gateType == GateType.ENTRY || gateType == GateType.EXIT || gateType == GateType.VALIDATOR) ? sign.getLine(1) : sign.getLine(1)+sign.getLine(2)+sign.getLine(3);
          player.sendMessage("DEBUG 2");  // TODO: DEBUG
    
    
          if (item.getType() == Material.NAME_TAG && itemLore0.equals(lang.SERIAL_NUMBER)) {
            // Card
            String serial = meta.getLore().get(1);
            gateAction = records.getString("station."+serial) == null ? GateType.ENTRY : GateType.EXIT;
            player.sendMessage("DEBUG 3a"+serial);  // TODO: DEBUG

            // set gateType to gateAction for easier manipulation since they are ambiguous.
            if (gateType == GateType.FAREGATE || gateType == GateType.VALIDATOR) gateType = gateAction;

            if (gateType == GateType.ENTRY) {

              records.set("station."+serial, station);
              // check whether the player tapped out and in within the time limit
              if (System.currentTimeMillis()-records.getLong("timestamp."+serial) < plugin.getConfig().getLong("max-transfer-time")) {
                records.set("transfer."+serial, true);
                player.sendMessage("DEBUG 4 ENTRY");  // TODO: DEBUG
              }

              gates.add(gate);
              gate.open();
              gate.hold();


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
  
              player.sendMessage("DEBUG 4 EXIT");  // TODO: DEBUG
  
              gates.add(gate);
              gate.open();
              gate.hold();
            }

          } else if (item.getType() == Material.PAPER) {

            player.sendMessage("DEBUG 5");  // TODO: DEBUG
            // Ticket
            gateAction = itemLore0.contains("•") ? GateType.EXIT : GateType.ENTRY;

            // set gateType to gateAction for easier manipulation since they are ambiguous.
            if (gateType == GateType.FAREGATE || gateType == GateType.VALIDATOR) gateType = gateAction;

            if (gateType == GateType.ENTRY && !itemLore0.contains("•")) {

              // check if the ticket is valid for that station
              if (itemLore0.equals(station)) {
                // punch hole
                List<String> lore = meta.getLore();
                lore.set(0, itemLore0+" •");
                meta.setLore(lore);
                item.setItemMeta(meta);
  
                player.sendMessage("DEBUG 6 ENTRY");  // TODO: DEBUG
  
                gates.add(gate);
                gate.open();
                gate.hold();
              }
            } else if (gateType == GateType.EXIT && itemLore0.contains("•")) {
              // check fare
              String exitStation = itemLore0.substring(0, itemLore0.length()-2);
              double fare = JsonManager.getFare(exitStation, station);
              String itemLore1 = meta.getLore().get(1);
              if (!itemLore1.contains("•") && (Objects.equals(itemLore1, station) || Double.parseDouble(itemLore1) >= fare)) {
                List<String> lore = meta.getLore();
                lore.set(1, itemLore1+" •");
                meta.setLore(lore);
                item.setItemMeta(meta);
    
                player.sendMessage("DEBUG 6 EXIT");  // TODO: DEBUG
    
                gates.add(gate);
                gate.open();
                gate.hold();
              }
  
            }

          } else gateAction = null;

          // Check for fare evasion
          if (!(gateType == gateAction) && (item.getType() == Material.NAME_TAG || item.getType() == Material.PAPER)) {
            player.sendMessage(lang.FARE_EVADE);
            Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
            player.sendMessage("DEBUG 8");  // TODO: DEBUG
          }

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
        plugin.getServer().getLogger().info(gateLocation.toString());
        if (g.getPlayer() == player && gateLocation[0] == x && gateLocation[1] == y && gateLocation[2] == z) {
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            g.close();
            gates.remove(g);
            player.sendMessage("DEBUG 9");  // TODO: DEBUG
          }, 10L);
      
        }
      }
    }
  }
}
