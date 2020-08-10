package mikeshafter.iciwi;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static mikeshafter.iciwi.Iciwi.economy;

public class events implements Listener{
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
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
  
  public void exit(String inSystem, String station, Player player){
    String playerName = player.getName();
    double fare;
    try{
      // Get fare
      fare = JSONmanager.getjson(station, inSystem);
    } catch (Exception e){
      // JSONmanager has a built-in exception catcher, this is placed here for redundancy
      fare = 4.0;
      player.sendMessage(ChatColor.RED+"Fare "+inSystem+"->"+station+" is not set up!");
    }
    player.sendMessage(ChatColor.GREEN+"Entered "+inSystem+". Exited "+station+". "+ChatColor.GOLD+"Fare: "+fare);
    economy.withdrawPlayer(player, fare);
    plugin.getConfig().set(playerName, "");  // Sets config such that inSystem will be null the next time
  }
  
  public void maxfare(double fare, Player player, String message){
    player.sendMessage(message+" "+ChatColor.GOLD+"Fare: "+fare);
    economy.withdrawPlayer(player, fare);
  }
  
  public void runnableFareGate(Location gateLocation, Material material, BlockData data){
    new BukkitRunnable(){
      @Override
      public void run(){
        gateLocation.getBlock().setType(material);
        gateLocation.getBlock().setBlockData(data);
      }
    }.runTaskLater(plugin, 60);
  }
  
  
  // Decide gate to open
  public void decideGate(BlockFace face, Location signLocation){
    World world = signLocation.getWorld();
    int x = signLocation.getBlockX();
    int y = signLocation.getBlockY();
    int z = signLocation.getBlockZ();
    if (face == BlockFace.SOUTH){
      Location location = new Location(world, x-1, y, z-1);
      Block gate = location.getBlock();
      BlockData data = gate.getBlockData();
      Material material = gate.getType();
      gate.setType(Material.AIR);
      runnableFareGate(location, material, data);
    } else if (face == BlockFace.NORTH){
      Location location = new Location(world, x+1, y, z+1);
      Block gate = location.getBlock();
      BlockData data = gate.getBlockData();
      Material material = gate.getType();
      gate.setType(Material.AIR);
      runnableFareGate(location, material, data);
    } else if (face == BlockFace.WEST){
      Location location = new Location(world, x+1, y, z-1);
      Block gate = location.getBlock();
      BlockData data = gate.getBlockData();
      Material material = gate.getType();
      gate.setType(Material.AIR);
      runnableFareGate(location, material, data);
    } else if (face == BlockFace.EAST){
      Location location = new Location(world, x-1, y, z+1);
      Block gate = location.getBlock();
      BlockData data = gate.getBlockData();
      Material material = gate.getType();
      gate.setType(Material.AIR);
      runnableFareGate(location, material, data);
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
      
      // Check if sign is right clicked
      if (action == Action.RIGHT_CLICK_BLOCK && state instanceof Sign && blockData instanceof WallSign){
        Sign sign = (Sign) state;
        WallSign wallSign = (WallSign) blockData;
        Location location = sign.getLocation();
        BlockFace signDirection = wallSign.getFacing();  // Get sign direction
        
        
        if (sign.getLine(0).equalsIgnoreCase("[Entry]")){
          String station = sign.getLine(1);
          String inSystem = plugin.getConfig().getString(player.getName());
          if (inSystem != null && !inSystem.isEmpty()){ // Max fare
            maxfare(4.0, player, ChatColor.RED+"You did not tap out of your previous journey! Maximum fare assumed.");
            decideGate(signDirection, location);
          } else {
            if (economy.getBalance(player) >= 5.0){
              decideGate(signDirection, location); // open fare gates
              enter(station, player);
            }
          }
        } else if (sign.getLine(0).equalsIgnoreCase("[Exit]")){
          String station = sign.getLine(1);
          String inSystem = plugin.getConfig().getString(player.getName());
          if (inSystem != null && !inSystem.isEmpty()){ // if inSystem has something
            decideGate(signDirection, location); // open fare gates
            exit(inSystem, station, player);
          } else { // Max fare
            maxfare(4.0, player, ChatColor.RED+"You did not tap in! Maximum fare assumed.");
            decideGate(signDirection, location);
          }
        } else if (sign.getLine(0).equalsIgnoreCase("[Validator]")){
          String station = sign.getLine(1);
          String inSystem = plugin.getConfig().getString(player.getName());
          if (inSystem != null && !inSystem.isEmpty()){
            decideGate(signDirection, location); // open fare gates
            exit(inSystem, station, player);
          } else {
            if (economy.getBalance(player) >= 5.0){
              decideGate(signDirection, location); // open fare gates
              enter(station, player);
            }
          }
        }
      } // === END OF SIGN CLICK ===
      
      
      // Check if trapdoor is right clicked
      else if (action == Action.RIGHT_CLICK_BLOCK && state instanceof Gate){
        Location location = block.getLocation();
        if (location.add(1, 0, 1).getBlock() instanceof WallSign ||
                location.add(-1, 0, -1).getBlock() instanceof WallSign ||
                location.add(-1, 0, 1).getBlock() instanceof WallSign ||
                location.add(1, 0, -1).getBlock() instanceof WallSign){
          Openable closeGate = (Openable) block.getBlockData();
          closeGate.setOpen(false);
        }
      }
    }
  }
}
