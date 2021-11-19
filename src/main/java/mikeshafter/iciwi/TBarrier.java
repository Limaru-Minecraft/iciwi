//package mikeshafter.iciwi;
//
//import org.bukkit.ChatColor;
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.block.Block;
//import org.bukkit.block.BlockState;
//import org.bukkit.block.Sign;
//import org.bukkit.block.data.BlockData;
//import org.bukkit.block.data.type.WallSign;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.block.Action;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.event.player.PlayerMoveEvent;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.plugin.Plugin;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//
//import static org.bukkit.plugin.java.JavaPlugin.getPlugin;
//
//
//public class TBarrier implements Listener {
//  private final Plugin plugin = getPlugin(Iciwi.class);
//  private final CardSql cardSql = new CardSql();
//
//  private final Iciwi iciwi = new Iciwi();
//  private final Owners owners = iciwi.owners;
//
//  HashMap<Player, Location> gateLocationMap = new HashMap<>();
//  HashMap<Player, Material> gateTypeMap = new HashMap<>();
//  HashMap<Player, BlockData> gateDataMap = new HashMap<>();
//
//  @EventHandler
//  public void TicketBarrierSignClick(PlayerInteractEvent event) {
//    if (event.getClickedBlock() != null) {
//
//      Player player = event.getPlayer();
//      Block block = event.getClickedBlock();
//      Action action = event.getAction();
//      BlockState state = block.getState();
//      BlockData data = block.getBlockData();
//
//      if (state instanceof Sign sign && data instanceof WallSign wallSign && action == Action.RIGHT_CLICK_BLOCK) {
//
//// Get variables
//        Location location = sign.getLocation();
//        String signLine0 = ChatColor.stripColor(sign.getLine(0));
//        String eStation = ChatColor.stripColor(sign.getLine(1));
//        String face = wallSign.getFacing().toString();
//        ItemStack heldItem = player.getInventory().getItemInMainHand();
//
//
//// === Entry ===
//        if (signLine0.equals("[Entry]") || signLine0.equals("[EntryV]")) {
//// Flag for Entry
//          boolean flag = false;
//
//
//// Pay first
//// Paper ticket
//          if (heldItem.getType() == Material.PAPER && heldItem.getItemMeta() != null && heldItem.getItemMeta().getLore() != null) {
//
//// Get lore
//            ItemMeta meta = heldItem.getItemMeta();
//            String ticketStation = meta.getLore().get(0);
//            if (ticketStation.equals(eStation)) {
//// Let player enter
//              flag = true;
//            }
//          }
//
//// Iciwi card
//          else if (heldItem.getType() == Material.NAME_TAG && heldItem.getItemMeta() != null && heldItem.getItemMeta().getLore() != null) {
//
//// Get lore
//            ItemMeta meta = heldItem.getItemMeta();
//            String lore0 = meta.getLore().get(0);
//            if (lore0.equals("Serial number:")) {
//              String serial = meta.getLore().get(1);
//
//              // Get timestamp
//              /*code to get timestamp from the config file */
//              long timestamp = 0L;
//              long maxTransferTime = plugin.getConfig().getLong("max-transfer-time"); // TODO: change this to a config value
//              if (System.currentTimeMillis() - timestamp < maxTransferTime) {
//                plugin.getConfig().set("transfer."+serial, true);
//                plugin.saveConfig();
//              }
//
//
//// Give confirmation to player
//              double cardVal = cardSql.getCardValue(serial);
//              player.sendMessage(String.format(ChatColor.GREEN+"Remaining value: "+ChatColor.YELLOW+"£%.2f", cardVal));
//// Let player enter
//              flag = true;
//            }
//          }
//
//// == Common code
//// Do not run if player is still a key in the HashMaps
//          if (flag && gateLocationMap.get(player) == null && gateTypeMap.get(player) == null) {
//            plugin.getConfig().set(player.getName(), eStation);
//            player.sendMessage(String.format(ChatColor.GREEN+"Entry point: %s. Welcome aboard!", eStation));
//// Check which direction the gate is facing
//            switch (face) {
//              case "SOUTH" -> location.add(-1, 0, -1);
//              case "NORTH" -> location.add(1, 0, 1);
//              case "WEST" -> location.add(1, 0, -1);
//              case "EAST" -> location.add(-1, 0, 1);
//            }
//            Block gate = location.getBlock();
//            gateTypeMap.put(player, gate.getType());
//            gateDataMap.put(player, gate.getBlockData());
//            gateLocationMap.put(player, location);
//
//
//            if (signLine0.equals("[Entry]")) {  // if the sign is not a validator
//              gate.setType(Material.AIR);
//
//// Close gate
//              BukkitRunnable closeGates = new BukkitRunnable() {
//                @Override
//                public void run() {
//                  if (gateLocationMap.containsKey(player) && gateTypeMap.containsKey(player)) {
//                    gate.setType(gateTypeMap.get(player));
//                    gate.setBlockData(gateDataMap.get(player));
//                    gateLocationMap.remove(player);
//                    gateTypeMap.remove(player);
//                  }
//                }
//              };
//              closeGates.runTaskLater(plugin, 100);
//            }
//          }
//        }
//
//// === Exit ===
//        else if (signLine0.equals("[Exit]") || signLine0.equals("[ExitV]")) {
//          boolean flag = false;
//// Get fare from entryStation to eStation
//          String entryStation = plugin.getConfig().getString(player.getName());
//          double fare = JsonManager.getFare(entryStation, eStation);
//
//
//// Paper ticket
//          if (heldItem.getType() == Material.PAPER && heldItem.getItemMeta() != null && heldItem.getItemMeta().getLore() != null) {
//// Get lore
//            ItemMeta meta = heldItem.getItemMeta();
//            List<String> lore = meta.getLore();
//
//
//// Get value of ticket
//            String allowedFare = lore.get(1);  // this can be name of the station as well
//
//            if (allowedFare.equals(eStation) || (fare <= Double.parseDouble(allowedFare))) {
//// remove ticket from player inventory
//              player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
//
//// Money has already been put in the operator's account so we don't do that again
//
//// Exit allowed
//              flag = true;
//            } else {
//// Exit not allowed
//              player.sendMessage(String.format(ChatColor.RED+"The value on your ticket is too low! The journey costs "+ChatColor.YELLOW+"£%.2f", fare));
//            }
//          }
//
//// iciwi card
//          else if (heldItem.getType() == Material.NAME_TAG && heldItem.getItemMeta() != null && heldItem.getItemMeta().getLore() != null) {
//// Get lore
//            ItemMeta meta = heldItem.getItemMeta();
//            List<String> lore = meta.getLore();
//
//
//// Check if it is an Iciwi card
//            if (lore.get(0).equals("Serial number:")) {
//// Get serial number
//              String serial = lore.get(1);
//
//// Get discounts associated with the card and deduct fare accordingly
//              HashSet<String> discounts = cardSql.getDiscountedOperators(serial);
//              String entryStationOwner = owners.getOwner(entryStation);
//              String exitStationOwner = owners.getOwner(eStation);
//              double half = fare/2;
//
//              // Check for transfer
//              if (plugin.getConfig().getBoolean("transfer."+serial)) { // true in config
//                fare -= plugin.getConfig().getDouble("transfer-discount"); // config value;
//              }
//              if (discounts.contains(entryStationOwner)) fare -= half;
//              if (discounts.contains(exitStationOwner)) fare -= half;
//
//              if (cardSql.getCardValue(serial) >= fare) {
//// deduct fare
//                cardSql.addValueToCard(serial, -1*fare);
//                player.sendMessage(String.format(ChatColor.GREEN+"Fare: "+ChatColor.YELLOW+"£%.2f"+ChatColor.GREEN+". Remaining value: "+ChatColor.YELLOW+"£%.2f", fare, cardSql.getCardValue(serial)));
//// give the money to the station operators
//                owners.deposit(entryStationOwner, fare/2);
//                owners.deposit(exitStationOwner, fare/2);
//
//                // TRANSFER: Set the time of exit
//                long timestamp = System.currentTimeMillis();
//
//                // Log timestamp to some config file
//                /* code */
//
//// Log and Exit allowed
//                cardSql.log(serial, entryStation, eStation, fare);
//                flag = true;
//              } else {
//// Exit not allowed
//                player.sendMessage(String.format(ChatColor.RED+"The value on your card is too low! The journey costs "+ChatColor.YELLOW+"£%.2f", fare));
//              }
//            }
//          }
//
//// == Common code
//// Do not run if player is still a key in the HashMaps
//          if (flag && gateLocationMap.get(player) == null && gateTypeMap.get(player) == null) {
//            plugin.getConfig().set(player.getName(), "");
//            player.sendMessage(String.format(ChatColor.GREEN+"Travelled from %s to %s. Thank you for travelling!", entryStation, eStation));
//// Check which direction the gate is facing
//            switch (face) {
//              case "SOUTH" -> location.add(-1, 0, -1);
//              case "NORTH" -> location.add(1, 0, 1);
//              case "WEST" -> location.add(1, 0, -1);
//              case "EAST" -> location.add(-1, 0, 1);
//            }
//            Block gate = location.getBlock();
//
//            gateTypeMap.put(player, gate.getType());
//            gateDataMap.put(player, gate.getBlockData());
//            gateLocationMap.put(player, location);
//
//            if (signLine0.equals("[Exit]")) {
//              gate.setType(Material.AIR);
//
//// Close gate
//              BukkitRunnable closeGates = new BukkitRunnable() {
//                @Override
//                public void run() {
//                  if (gateLocationMap.containsKey(player) && gateTypeMap.containsKey(player)) {
//                    gate.setType(gateTypeMap.get(player));
//                    gate.setBlockData(gateDataMap.get(player));
//                    gateLocationMap.remove(player);
//                    gateTypeMap.remove(player);
//                  }
//                }
//              };
//              closeGates.runTaskLater(plugin, 100);
//            }
//          }
//        }
//      }
//    }
//  }
//
//  @EventHandler  // If player walked through fare gate, close it
//  public void CheckPlayerMove(PlayerMoveEvent event) {
//    Player player = event.getPlayer();
//
//// Wait 0.4s
//    if (gateLocationMap.containsKey(player) && sameBlockLocation(event.getFrom(), gateLocationMap.get(player))) {
//      BukkitRunnable closeGates = new BukkitRunnable() {
//        @Override
//        public void run() {
//          if (gateLocationMap.containsKey(player) && gateTypeMap.containsKey(player)) {
//            Location location = gateLocationMap.get(player);
//            location.getBlock().setType(gateTypeMap.get(player));
//            location.getBlock().setBlockData(gateDataMap.get(player));
//            gateLocationMap.remove(player);
//            gateTypeMap.remove(player);
//          }
//        }
//      };
//      closeGates.runTaskLater(plugin, 8);
//    }
//  }
//
//  private boolean sameBlockLocation(Location a, Location b) {
//    return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ() && a.getWorld() == b.getWorld();
//  }
//}
