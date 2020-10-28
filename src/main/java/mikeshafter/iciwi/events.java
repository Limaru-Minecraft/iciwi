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
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

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
  
  int x;
  int y;
  int z;
  Material gateMaterial;
  BlockData gateData;
  
  public void maxfare(double fare, Player player, String message){
    player.sendMessage(message+" "+ChatColor.GOLD+"Fare: "+fare);
    economy.withdrawPlayer(player, fare);
  }
  
  public void exit(String inSystem, String station, Player player){
    String playerName = player.getName();
//    double fare;
//    try{
//      // Get fare
//      fare = JSONmanager.getjson(station, inSystem);
//    } catch (Exception e){
//      // JSONmanager has a built-in exception catcher, this is placed here for redundancy
//      fare = 4.0;
//      player.sendMessage(ChatColor.RED+"Fare "+inSystem+"->"+station+" is not set up!");
//    }
    player.sendMessage(ChatColor.GREEN+"Entered "+inSystem+". Exited "+station+". ");
    //economy.withdrawPlayer(player, fare);
    player.getInventory().setItem(player.getInventory().getHeldItemSlot(), new ItemStack(Material.AIR, 0));
    plugin.getConfig().set(playerName, "");  // Sets config such that inSystem will be null the next time
  }
  
  @EventHandler
  public void CheckPlayerMove(PlayerMoveEvent event){
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
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x--;
      z--;
      
    } else if (face == BlockFace.NORTH){
      Location location = new Location(world, x+1, y, z+1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x++;
      z++;
      
    } else if (face == BlockFace.WEST){
      Location location = new Location(world, x+1, y, z-1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x++;
      z--;
      
    } else if (face == BlockFace.EAST){
      Location location = new Location(world, x-1, y, z+1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x--;
      z++;
      
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
  
        // Entry
        if (sign.getLine(0).equalsIgnoreCase("[Entry]")){
          String station = sign.getLine(1);
          String inSystem = plugin.getConfig().getString(player.getName());
          if (inSystem != null && !inSystem.isEmpty()){ // Max fare
            maxfare(4.0, player, ChatColor.RED+"You did not tap out of your previous journey! Maximum fare charged.");
            decideGate(signDirection, location);
            enter(station, player);
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
    
    
        } else if (sign.getLine(0).equalsIgnoreCase("[Exit]")){
          String station = sign.getLine(1);
          String inSystem = plugin.getConfig().getString(player.getName());
          double fare = JSONmanager.getjson(station, inSystem);
    
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(1) != null;
          if (inSystem != null && !inSystem.isEmpty() && // if inSystem has something
                  fare <= Double.parseDouble(player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1))){
            decideGate(signDirection, location); // open fare gates
            exit(inSystem, station, player);
          } else if (inSystem != null && !inSystem.isEmpty() && // if inSystem has something
                         fare > Double.parseDouble(player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1))){
            player.sendMessage(ChatColor.RED+"Wrong ticket! The fare for your journey is "+ChatColor.GOLD+fare+ChatColor.RED+".");
          } else { // Max fare
            maxfare(4.0, player, ChatColor.RED+"You did not tap in! Maximum fare charged.");
            decideGate(signDirection, location);
            exit(inSystem, station, player);
          }
        } else if (sign.getLine(0).equalsIgnoreCase("[Transfer]")){
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
    
          // === TICKET MACHINE ===
        } else if (sign.getLine(0).equalsIgnoreCase("[Tickets]")){
          CustomInventory inventory = new CustomInventory();
          String station = sign.getLine(1);
          inventory.newTicket(player, station);
        }
      } // === END OF SIGN CLICK ===
    }
  }
}
