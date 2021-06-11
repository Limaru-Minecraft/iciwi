package mikeshafter.iciwi;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;


public class TBarrier implements Listener {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final CardSql cardSql = new CardSql();
  
  HashMap<Player, Location> gateLocationMap = new HashMap<>();
  HashMap<Player, Material> gateTypeMap = new HashMap<>();
  HashMap<Player, BlockData> gateDataMap = new HashMap<>();
  
  @EventHandler
  public void TicketBarrierSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null) {
      
      Player player = event.getPlayer();
      Block block = event.getClickedBlock();
      Action action = event.getAction();
      BlockState state = block.getState();
      BlockData data = block.getBlockData();
      
      if (state instanceof Sign && data instanceof WallSign && action == Action.RIGHT_CLICK_BLOCK) {

// Get variables
        Sign sign = (Sign) state;
        WallSign wallSign = (WallSign) data;
        Location location = sign.getLocation();
        String signLine0 = ChatColor.stripColor(sign.getLine(0));
        String eStation = ChatColor.stripColor(sign.getLine(1));
        String face = wallSign.getFacing().toString();
        ItemStack heldItem = player.getInventory().getItemInMainHand();


// === Entry ===
        if (signLine0.equals("[Entry]") || signLine0.equals("[EntryV]")) {
// Flag for Entry
          boolean flag = false;


// Pay first
// Paper ticket
          if (heldItem.getType() == Material.PAPER && heldItem.getItemMeta() != null && heldItem.getItemMeta().getLore() != null) {

// Get lore
            ItemMeta meta = heldItem.getItemMeta();
            String ticketStation = meta.getLore().get(0);
            if (ticketStation.equals(eStation)) {
// Let player enter
              flag = true;
            }
          }

// Iciwi card
          else if (heldItem.getType() == Material.NAME_TAG && heldItem.getItemMeta() != null && heldItem.getItemMeta().getLore() != null) {

// Get lore
            ItemMeta meta = heldItem.getItemMeta();
            String lore0 = meta.getLore().get(0);
            if (lore0.equals("Serial number:")) {
// Give confirmation to player
              double cardVal = cardSql.getCardValue(meta.getLore().get(1));
              player.sendMessage(String.format(ChatColor.GREEN+"Remaining value: "+ChatColor.YELLOW+"£%.2f", cardVal));
// Let player enter
              flag = true;
            }
          }

// Common code
// Do not run if player is still a key in the HashMaps
          if (flag && gateLocationMap.get(player) == null && gateTypeMap.get(player) == null) {
            plugin.getConfig().set(player.getName(), eStation);
            player.sendMessage(String.format(ChatColor.GREEN+"Entry point: %s. Welcome aboard!", eStation));
// Check which direction the gate is facing
            switch (face) {
              case "SOUTH":
                location.add(-1, 0, -1);
                break;
              
              case "NORTH":
                location.add(1, 0, 1);
                break;
              
              case "WEST":
                location.add(1, 0, -1);
                break;
              
              case "EAST":
                location.add(-1, 0, 1);
                break;
            }
            Block gate = location.getBlock();
            gateTypeMap.put(player, gate.getType());
            gateDataMap.put(player, gate.getBlockData());
            gateLocationMap.put(player, location);
            
            
            if (signLine0.equals("[Entry]")) {
              gate.setType(Material.AIR);

// Close gate
              BukkitRunnable closeGates = new BukkitRunnable() {
                @Override
                public void run() {
                  location.getBlock().setType(gateTypeMap.get(player));
                  location.getBlock().setBlockData(gateDataMap.get(player));
                  gateLocationMap.remove(player);
                  gateTypeMap.remove(player);
                }
              };
              closeGates.runTaskLater(plugin, 100);
            }
          }
        }

// === Exit ===
        else if (signLine0.equals("[Exit]") || signLine0.equals("[ExitV]")) {
          boolean flag = false;
// Get fare from entryStation to eStation
          String entryStation = plugin.getConfig().getString(player.getName());
          double fare = JsonManager.getFare(eStation, entryStation);


// Paper ticket
          if (heldItem.getType() == Material.PAPER && heldItem.getItemMeta() != null && heldItem.getItemMeta().getLore() != null) {
// Get lore
            ItemMeta meta = heldItem.getItemMeta();
            List<String> lore = meta.getLore();


// Get value of ticket
            String allowedFare = lore.get(1);  // this can be name of the station as well
            
            if (allowedFare.equals(eStation) || fare >= Double.parseDouble(allowedFare)) {
// remove ticket from player inventory
              player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);

// Exit allowed
              flag = true;
            } else {
// Exit not allowed
              player.sendMessage(String.format(ChatColor.RED+"The value on your ticket is too low! The journey costs "+ChatColor.YELLOW+"£%.2f", fare));
            }
          }

// iciwi card
          else if (heldItem.getType() == Material.NAME_TAG && heldItem.getItemMeta() != null && heldItem.getItemMeta().getLore() != null) {
// Get lore
            ItemMeta meta = heldItem.getItemMeta();
            List<String> lore = meta.getLore();


// Check if it is an Iciwi card
            if (lore.get(0).equals("Serial number:")) {
// Get serial number
              String serial = lore.get(1);
              
              if (cardSql.getCardValue(serial) >= fare) {
// deduct fare
                cardSql.addValueToCard(serial, -1*fare);
                player.sendMessage(String.format(ChatColor.GREEN+"Fare: "+ChatColor.YELLOW+"£%.2f"+ChatColor.GREEN+". Remaining value: "+ChatColor.YELLOW+"£%.2f", fare, cardSql.getCardValue(serial)));
// Exit allowed
                flag = true;
              } else {
// Exit not allowed
                player.sendMessage(String.format(ChatColor.RED+"The value on your card is too low! The journey costs "+ChatColor.YELLOW+"£%.2f", fare));
              }
            }
          }

// Common code
// Do not run if player is still a key in the HashMaps
          if (flag && gateLocationMap.get(player) == null && gateTypeMap.get(player) == null) {
            plugin.getConfig().set(player.getName(), "");
            player.sendMessage(String.format(ChatColor.GREEN+"Travelled from %s to %s. Thank you for travelling!", entryStation, eStation));
// Check which direction the gate is facing
            switch (face) {
              case "SOUTH":
                location.add(-1, 0, -1);
                break;
              
              case "NORTH":
                location.add(1, 0, 1);
                break;
              
              case "WEST":
                location.add(1, 0, -1);
                break;
              
              case "EAST":
                location.add(-1, 0, 1);
                break;
            }
            Block gate = location.getBlock();
            
            gateTypeMap.put(player, gate.getType());
            gateDataMap.put(player, gate.getBlockData());
            gateLocationMap.put(player, location);
            
            if (signLine0.equals("[Exit]")) {
              gate.setType(Material.AIR);

// Close gate
              BukkitRunnable closeGates = new BukkitRunnable() {
                @Override
                public void run() {
                  location.getBlock().setType(gateTypeMap.get(player));
                  location.getBlock().setBlockData(gateDataMap.get(player));
                  gateLocationMap.remove(player);
                  gateTypeMap.remove(player);
                }
              };
              closeGates.runTaskLater(plugin, 100);
            }
          }
        }
      }
    }
  }
  
  @EventHandler  // If player walked through fare gate, close it
  public void CheckPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();

// Wait 0.4s
    if (gateLocationMap.containsKey(player) && sameBlockLocation(event.getFrom(), gateLocationMap.get(player))) {
      Location location = gateLocationMap.get(player);
      BukkitRunnable closeGates = new BukkitRunnable() {
        @Override
        public void run() {
          location.getBlock().setType(gateTypeMap.get(player));
          location.getBlock().setBlockData(gateDataMap.get(player));
          gateLocationMap.remove(player);
          gateTypeMap.remove(player);
        }
      };
      closeGates.runTaskLater(plugin, 8);
    }
  }
  
  private boolean sameBlockLocation(Location a, Location b) {
    return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ() && a.getWorld() == b.getWorld();
  }
}
