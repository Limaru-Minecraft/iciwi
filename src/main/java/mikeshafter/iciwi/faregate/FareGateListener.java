package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.api.IciwiPlugin;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.config.Fares;
import net.kyori.adventure.text.Component;
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
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static mikeshafter.iciwi.util.IciwiUtil.any;
import static mikeshafter.iciwi.util.IciwiUtil.parseComponent;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

@Deprecated
public class FareGateListener implements Listener {
  private final Iciwi plugin = getPlugin(Iciwi.class);
  private final CardSql cardSql = new CardSql();
  private final Lang lang = new Lang();
  private final Records records = new Records();
  private final Owners owners = new Owners();
  private final Fares fares = new Fares();
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
        if (ChatColor.stripColor(parseComponent(sign.line(0))).contains(lang.getString("payment")) && isDouble(parseComponent(sign.line(1)))) {
    
          double price = Double.parseDouble(parseComponent(sign.line(1)));
          ItemStack item = player.getInventory().getItemInMainHand();
    
          // Check for Iciwi card
          // TODO: Other payment methods
          if (item.getType() == Material.NAME_TAG &&
                  item.hasItemMeta() &&
                  item.getItemMeta() != null &&
                  item.getItemMeta().hasLore() &&
                  item.getItemMeta().lore() != null) {

            // Iciwi-compatible plugins' cards must state their plugin name in lore[0]
            String cardPluginName = parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(0));
            PluginManager pluginManager = Bukkit.getServer().getPluginManager();

            // Get the plugin
            Plugin providingPlugin = pluginManager.getPlugin(cardPluginName);

            // check for plugin compatibility
            if (providingPlugin instanceof IciwiPlugin iciwiPlugin && iciwiPlugin.getFareCardClass() != null) {

              Class<?> icCardClass = iciwiPlugin.getFareCardClass();

              try {

                // Create new card instance using the provided constructor and the item
                IcCard card = (IcCard) icCardClass.getConstructor(ItemStack.class).newInstance(item);

                // Pay using Iciwi card
                pay(card, player, price);
                return;

              } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                pay(player, price);
              }
            }
      
            // If there is no card, pay with cash
            pay(player, price);
          }
    
          return;
          // DO NOT PARSE THE REST OF THE CODE
        }
  
  
        // === Normal signs ===
  
        // Initialize fare gate; all signs point to this (normal fare gates)
        else if (ChatColor.stripColor(parseComponent(sign.line(0))).contains(lang.getString("entry")) || ChatColor.stripColor(parseComponent(sign.line(0))).contains(lang.getString("exit")) || ChatColor.stripColor(parseComponent(sign.line(0))).contains(lang.getString("validator")) || ChatColor.stripColor(parseComponent(sign.line(0))).contains(lang.getString("member"))) {
          gate = new FareGate(player, parseComponent(sign.line(0)), block.getLocation());
        }
      
      }
    
      // same thing, but for HL-style fare gates
      else if (data instanceof Openable) {
        if (location.add(0, -2, 0).getBlock().getState() instanceof Sign sign && ChatColor.stripColor(parseComponent(sign.line(0))).contains(lang.getString("faregate"))) {
          gate = new FareGate(player, parseComponent(sign.line(0)), sign.getLocation());
        }
      }
    
      // If it's not a fare gate, do nothing
      else return;
    
    
      // figuring out entry/exit. If it's a membership gate, this does not apply.
      if (gate != null && gate.getGateType() != GateType.MEMBER) {
        GateType gateType = gate.getGateType();
        /*
        Paper ticket    | • •
        Card            |     • •
        • in Lore?      | • ×
        Card in config? |     • ×
        ===========================
        Entry           |   x   x
        Exit            | x   x
         */
      
        // get ticket/card type
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        Sign sign = state instanceof Sign ? (Sign) state : location.getBlock().getState() instanceof Sign ? (Sign) location.getBlock().getState() : null;
        if (meta != null && meta.hasLore() && meta.lore() != null && sign != null) {
  
          String itemLore0 = parseComponent(Objects.requireNonNull(meta.lore()).get(0));
  
          // station. if the gatetype is a faregate, use all 3 lines, else, use the first line only.
          String station = ChatColor.stripColor((gateType == GateType.ENTRY || gateType == GateType.EXIT || gateType == GateType.VALIDATOR) ? parseComponent(sign.line(1)) : ChatColor.stripColor((gateType == GateType.FAREGATE) ? parseComponent(sign.line(1))+parseComponent(sign.line(2))+parseComponent(sign.line(3)) : parseComponent(sign.line(0)).split(" ", 2)[1])).replaceAll(" ", "").replace("]", "");
  
          // === Card ===
          if (item.getType() == Material.NAME_TAG && itemLore0.equals(lang.getString("serial-number"))) {
    
            String serial = parseComponent(Objects.requireNonNull(meta.lore()).get(1));
            // If there is nothing in records, set gateAction to ENTRY. Else, set it to EXIT.
            gateAction = records.getString("station."+serial) == null ? GateType.ENTRY : GateType.EXIT;
    
            // boolean flag to block gate opening as that causes an error
            boolean blockOpen = gateType == GateType.VALIDATOR || gateType == GateType.SPECIAL;
    
            // set gateType to gateAction for easier manipulation since they are ambiguous. Also removes the chance of a fine.
            if (gateType == GateType.FAREGATE || gateType == GateType.VALIDATOR || gateType == GateType.SPECIAL)
              gateType = gateAction;
          
            if (gateType == GateType.ENTRY) {
            
              records.set("station."+serial, station);
              // check whether the player tapped out and in within the time limit
              if (System.currentTimeMillis()-records.getLong("timestamp."+serial) < plugin.getConfig().getLong("max-transfer-time")) {
                records.set("has-transfer."+serial, station);
              }
              double value = cardSql.getCardValue(serial);
              player.sendMessage(String.format(lang.getString("tapped-in"), station, value));
            
              // open fare gate
              if (!blockOpen) {
                gates.add(gate);
                gate.open();
                gate.hold();
              }
            
            
            } else if (gateType == GateType.EXIT) {
  
              // get entry station and fare.
              String entryStation = records.getString("station."+serial);
              double fare = fares.getFare(entryStation, station);
  
              // get rail pass discounts
              Map<String, Long> discounts = cardSql.getAllDiscounts(serial);
              String entryStationOwner = owners.getOwners(entryStation).get(0);
              String exitStationOwner = owners.getOwners(station).get(0);
  
              // get transfer discount
              if (records.getBoolean("has-transfer."+serial)) {
                String prevStation = records.getString("previous-station."+serial);
                double noOsiFare = fares.getFare(prevStation, station);
                if (noOsiFare > fare) {
                  fare = noOsiFare;
                }
              }
  
              // record starting station for next transfer
              records.set("previous-station."+serial, entryStation);
  
              // remove fare from discounts
              double half = fare/2;
              if (discounts.containsKey(entryStationOwner)) fare -= half;
              if (discounts.containsKey(exitStationOwner)) fare -= half;
              owners.deposit(entryStationOwner, fare/2);
              owners.deposit(exitStationOwner, fare/2);
  
              // set remaining value and config
              // TODO: other payment methods
              cardSql.subtractValueFromCard(serial, fare);
              double value = cardSql.getCardValue(serial);
              player.sendMessage(String.format(lang.getString("remaining"), value));
  
              // set timestamp for future transfer
              records.set("timestamp."+serial, System.currentTimeMillis());
              records.set("station."+serial, null);
  
              // record starting station for next transfer
              records.set("previous-station."+serial, entryStation);
  
              player.sendMessage(String.format(lang.getString("tapped-out"), station, fare, value));
  
              // open fare gate
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
          
            // set gateType to gateAction for easier manipulation since they are ambiguous.
            if (gateType == GateType.FAREGATE || gateType == GateType.VALIDATOR || gateType == GateType.SPECIAL)
              gateType = gateAction;
          
            // Entry
            if (gateType == GateType.ENTRY && !itemLore0.contains("•") && !parseComponent(Objects.requireNonNull(meta.lore()).get(1)).contains("•")) {
            
              // check if the ticket is valid for that station
              if (itemLore0.equals(station) || itemLore0.equals(lang.getString("global-ticket"))) {
                // punch hole
                List<Component> lore = meta.lore();
                Objects.requireNonNull(lore).set(0, Component.text(itemLore0+" •"));
                meta.lore(lore);
                item.setItemMeta(meta);
    
                player.sendMessage(String.format(lang.getString("ticket-in"), station));
    
                // open gate
                gates.add(gate);
                gate.open();
                gate.hold();
              }
            }
          
            // Exit
            else if (gateType == GateType.EXIT && itemLore0.contains("•") && !parseComponent(Objects.requireNonNull(meta.lore()).get(1)).contains("•")) {
  
              // check fare
              String entryStation = itemLore0.substring(0, itemLore0.length()-2);
  
              // Station ticket
              if (!entryStation.equals(lang.getString("global-ticket"))) {
    
                // get fare
                double fare = fares.getFare(entryStation, station);
                String itemLore1 = parseComponent(Objects.requireNonNull(meta.lore()).get(1));
    
                // check if the station is the exit point, or if the fare works out
                if (itemLore1.equals(station) || Double.parseDouble(itemLore1) >= fare) {
      
                  // Punch out (station ticket)
                  List<Component> lore = meta.lore();
                  Objects.requireNonNull(lore).set(1, Component.text(itemLore1+" •"));
                  meta.lore(lore);
                  item.setItemMeta(meta);
                }
              } else {
                String itemLore1 = parseComponent(Objects.requireNonNull(meta.lore()).get(1));

                // Punch out (station ticket)
                List<Component> lore = meta.lore();
                Objects.requireNonNull(lore).set(1, Component.text(itemLore1+" •"));
                meta.lore(lore);
                item.setItemMeta(meta);
              }
              player.sendMessage(String.format(lang.getString("ticket-out"), station));
            
              // Open gates
              gates.add(gate);
              gate.open();
              gate.hold();
            
              // If nothing works out, player is probably holding an invalid ticket
            } else player.sendMessage(lang.getString("invalid-ticket"));
          }
        }
  
        // Check for fare evasion
        if (gateType != gateAction && (item.getType() == Material.NAME_TAG || item.getType() == Material.PAPER)) {
          player.sendMessage(lang.getString("fare-evade"));
          Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
        }
  
      }

      // membership gate
      else if (gate != null && gate.getGateType() == GateType.MEMBER) {
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        Sign sign = state instanceof Sign ? (Sign) state : location.getBlock().getState() instanceof Sign ? (Sign) location.getBlock().getState() : null;
        if (item.getType() == Material.NAME_TAG && meta != null && meta.hasLore() && meta.lore() != null && parseComponent(Objects.requireNonNull(meta.lore()).get(0)).equals(lang.getString("serial-number"))) {
    
          String serial = parseComponent(Objects.requireNonNull(meta.lore()).get(1));
          Map<String, Long> discounts = cardSql.getAllDiscounts(serial);
    
          if (sign != null && (discounts.containsKey(ChatColor.stripColor(parseComponent(sign.line(1)))) || any(discounts.keySet(), owners.getOwners(ChatColor.stripColor(parseComponent(sign.line(1))) )) )) {
            player.sendMessage(lang.getString("member-gate"));
      
            // Open gates
            gates.add(gate);
            gate.open();
            gate.hold();
          } else player.sendMessage(lang.getString("invalid-ticket"));
        }
      }
    
    }
  }
  
  
  @EventHandler  // Close gates
  public void CheckPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    for (FareGate g : this.gates) {
      int x = player.getLocation().getBlockX();
      int y = player.getLocation().getBlockY();
      int z = player.getLocation().getBlockZ();
      for (Location gateLocation : g.getGateLocations()) {
        if (g.getPlayer() == player && gateLocation.getBlockX() == x && gateLocation.getBlockY() == y && gateLocation.getBlockZ() == z) {
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            g.close();
            gates.remove(g);
          }, 10);
        }
      }
    }
  }


  // Util methods

  private boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }

  public void pay(IcCard card, Player player, double price) {
    if (card.getValue() >= price) {
      card.withdraw(price);
      double value = card.getValue();
      player.sendMessage(String.format(lang.getString("pay-success-card"), price, value));

//      if (Objects.equals(plugin.getConfig().getString("ticket-machine-type"), "GLOBAL")) {
//        Owners owners = new Owners(plugin);
//        owners.deposit(plugin.getConfig().getString("global-operator"), price);
//      }
    }
    else pay(player, price);
  }

  public void pay(Player player, double price) {
    Iciwi.economy.withdrawPlayer(player, price);
    player.sendMessage(lang.getString("cash-divert"));
    player.sendMessage(String.format(lang.getString("pay-success"), price));

//    if (Objects.equals(plugin.getConfig().getString("ticket-machine-type"), "GLOBAL")) {
//      Owners owners = new Owners(plugin);
//      owners.deposit(plugin.getConfig().getString("global-operator"), price);
//    }
  }
}
