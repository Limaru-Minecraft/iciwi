package mikeshafter.iciwi;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static mikeshafter.iciwi.Iciwi.economy;
import static org.bukkit.Bukkit.getServer;

public class Events implements Listener{
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  DecimalFormat currency = new DecimalFormat("##.00");
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event){  // Create ICIWI for new player
    Player player = event.getPlayer();
    if (!player.hasPlayedBefore()){
      plugin.getConfig().set(player.getName(), "");
    }
  }
  
  // Opening/Closing sequence in functions
  public void enter(String station, Player player){
    if (!station.isEmpty()){
      String playerName = player.getName();
      plugin.getConfig().set(playerName, station);
      player.sendMessage("Entered "+station);
    }
  }
  
  
  public void maxfare(double fare, Player player, String message){
    player.sendMessage(message+" "+ChatColor.GOLD+"Fare: "+fare);
    economy.withdrawPlayer(player, fare);
  }
  
  int x;
  int y;
  int z;
  Material gateMaterial;
  BlockData gateData;
  
  public void exit(String inSystem, String station, Player player, String ticketType, double fare){
    String playerName = player.getName();
    player.sendMessage(ChatColor.GREEN+"Entered "+inSystem+". Exited "+station+". Fare: "+ChatColor.YELLOW+fare);
    if (ticketType.equals(inSystem)){
      player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
    } else if (ticketType.equals("Remaining value:")){
      ItemMeta ticketMeta = player.getInventory().getItemInMainHand().getItemMeta();
      assert ticketMeta != null;
      List<String> lore = ticketMeta.getLore();
      assert lore != null;
      lore.set(1, currency.format(String.valueOf(Double.parseDouble(lore.get(1))-fare)));
      ticketMeta.setLore(lore);
      player.getInventory().getItemInMainHand().setItemMeta(ticketMeta);
    }
    plugin.getConfig().set(playerName, "");  // Sets config such that inSystem will be null the next time
  }
  
  
  @EventHandler
  public void checkPlayerMove(PlayerMoveEvent event){
    if (event.getFrom().getBlockX() == x && event.getFrom().getBlockY() == y && event.getFrom().getBlockZ() == z){
      Location location = new Location(event.getPlayer().getWorld(), x, y, z);
      Block block = location.getBlock();
      block.setType(gateMaterial);
      block.setBlockData(gateData);
    }
  }
  
  
  // Decide gate to open
  public void decideGate(BlockFace face, Location signLocation){
    World world = signLocation.getWorld();
    x = signLocation.getBlockX();
    y = signLocation.getBlockY();
    z = signLocation.getBlockZ();
    
    if (face == BlockFace.SOUTH){
      Location location = new Location(world, x-1, y, z-1);
      --x;
      --z;
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      BukkitScheduler scheduler = getServer().getScheduler();
      scheduler.scheduleSyncDelayedTask(plugin, new Runnable(){
        @Override
        public void run(){
          gate.setType(gateMaterial);
          gate.setBlockData(gateData);
        }
      }, 30L);
  
    } else if (face == BlockFace.NORTH){
      Location location = new Location(world, x+1, y, z+1);
      ++x;
      ++z;
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      BukkitScheduler scheduler = getServer().getScheduler();
      scheduler.scheduleSyncDelayedTask(plugin, new Runnable(){
        @Override
        public void run(){
          gate.setType(gateMaterial);
          gate.setBlockData(gateData);
        }
      }, 30L);
  
    } else if (face == BlockFace.WEST){
      Location location = new Location(world, x+1, y, z-1);
      ++x;
      --z;
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      BukkitScheduler scheduler = getServer().getScheduler();
      scheduler.scheduleSyncDelayedTask(plugin, new Runnable(){
        @Override
        public void run(){
          gate.setType(gateMaterial);
          gate.setBlockData(gateData);
        }
      }, 30L);
  
    } else if (face == BlockFace.EAST){
      Location location = new Location(world, x-1, y, z+1);
      --x;
      ++z;
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      BukkitScheduler scheduler = getServer().getScheduler();
      scheduler.scheduleSyncDelayedTask(plugin, new Runnable(){
        @Override
        public void run(){
          gate.setType(gateMaterial);
          gate.setBlockData(gateData);
        }
      }, 30L);
  
    }
  }
  
  @EventHandler
  public void signClick(PlayerInteractEvent event){
    if (event.getClickedBlock() != null){
      Action action = event.getAction();
      Player player = event.getPlayer();
      Block block = event.getClickedBlock();
      BlockState state = block.getState();
      BlockData blockData = block.getBlockData();
      
      if (action == Action.RIGHT_CLICK_BLOCK && state instanceof Sign && blockData instanceof WallSign){
        Sign sign = (Sign) state;
        WallSign wallSign = (WallSign) blockData;
        Location location = sign.getLocation();
        BlockFace signDirection = wallSign.getFacing();  // Get sign direction
  
        // === ENTRY ===
        if (sign.getLine(0).equalsIgnoreCase("[Entry]")){
          String station = sign.getLine(1);
          String inSystem = plugin.getConfig().getString(player.getName());
          if (inSystem != null && !inSystem.isEmpty()){ // Max fare
            maxfare(8.0, player, ChatColor.RED+"You did not tap out of your previous journey! Maximum fare charged.");
            decideGate(signDirection, location);
            enter(station, player);
          } else {
  
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(0) != null;
            if (player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).equals(station) || player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).equals("Remaining value:")){
              decideGate(signDirection, location); // open fare gates
              enter(station, player);
            } else {
              player.sendMessage(ChatColor.RED+"Wrong ticket!");
            }
          }
        }

        // === EXIT ===
        else if (sign.getLine(0).equalsIgnoreCase("[Exit]")){
          String station = sign.getLine(1);
          String inSystem = plugin.getConfig().getString(player.getName());
          double fare = JSONmanager.getjson(station, inSystem);
  
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(1) != null;
  
          String ticketType = player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0);
          String temp = player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1);
  
          if (inSystem != null && !inSystem.isEmpty() && (Double.parseDouble(temp) >= fare || temp.equals(station))){
            decideGate(signDirection, location); // open fare gates
            exit(inSystem, station, player, ticketType, fare);
          } else if (inSystem != null && !inSystem.isEmpty() && Double.parseDouble(temp) < fare){
            player.sendMessage(ChatColor.RED+"Wrong ticket! The fare for your journey is "+ChatColor.GOLD+fare+ChatColor.RED+".");
          } else if (inSystem != null && !inSystem.isEmpty()){
            player.sendMessage(ChatColor.RED+"Wrong ticket!");
          } else { // Max fare
            maxfare(8.0, player, ChatColor.RED+"You did not tap in! Maximum fare charged.");
            decideGate(signDirection, location);
            exit(inSystem, station, player, ticketType, 8.0);
          }
        }

        // === TRANSFER ===
        else if (sign.getLine(0).equalsIgnoreCase("[Transfer]")){
          String station = sign.getLine(1);
          String inSystem = plugin.getConfig().getString(player.getName());
  
          // If the player is already in the system
          if (inSystem != null && !inSystem.isEmpty()){
            player.sendMessage("Transfer: "+station);
            decideGate(signDirection, location);
          } else {
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(0) != null;
            if (player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).equals(station)){
              decideGate(signDirection, location); // open fare gates
              enter(station, player);
            } else {
              player.sendMessage(ChatColor.RED+"Wrong ticket!");
            }
          }
        }

        // === TICKET MACHINE ===
        else if (sign.getLine(0).equalsIgnoreCase("[Tickets]")){
          CustomInventory tm = new CustomInventory();
          String station = sign.getLine(1);
          tm.newTM(player, station);
        }

        // === PAYMENT ===
        else if (sign.getLine(0).equalsIgnoreCase("[Payment]")){
          double amt = Double.parseDouble(sign.getLine(1));
          String playerName = sign.getLine(2);
          OfflinePlayer offlinePlayer = getServer().getOfflinePlayer(UUID.fromString(playerName));
          economy.depositPlayer(offlinePlayer, amt);
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(0) != null;
          if (player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).equals("Remaining value:") && Double.parseDouble(player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1)) >= amt){
            player.getInventory().getItemInMainHand().getItemMeta().getLore().set(1, currency.format(String.valueOf(Double.parseDouble(player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1))-amt)));
          } else player.sendMessage(ChatColor.RED+"Requires ICIWI card with at least the amount set on the sign!");
        } // === END OF SIGN CLICK ===
      }
    }
  }
}
