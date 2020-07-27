package mikeshafter.iciwi;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

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
  
  @EventHandler
  public void signClick(PlayerInteractEvent event) {
    Action action = event.getAction();
    Player player = event.getPlayer();
    Block block = event.getClickedBlock();
    BlockState state = block.getState();
    
    // Check if sign is right clicked
    if (action == Action.RIGHT_CLICK_BLOCK && state instanceof Sign){
      Sign sign = (Sign) state;
  
      if (sign.getLine(0).equalsIgnoreCase("[Entry]")){
        String station = sign.getLine(1);
        String playerName = player.getName();
        String inSystem = plugin.getConfig().getString(playerName);
  
        // inSystem is the station at which the player previously entered
        // station is the station at which the player is currently at
  
        if (inSystem != null && !inSystem.isEmpty()){ // Max fare
          double fare = 8.0;
          player.sendMessage(ChatColor.RED+"You did not tap out of your previous journey! Maximum fare assumed. "+ChatColor.GOLD+"Fare: "+fare);
          EconomyResponse response = economy.withdrawPlayer(player, fare);
          
        // == Entry ==
        } else {
          if (!station.isEmpty()){
            plugin.getConfig().set(playerName, station);
            player.sendMessage("Entered "+station);
          }
        }
      }
      
  
      else if (sign.getLine(0).equalsIgnoreCase("[Exit]")){
        String station = sign.getLine(1);
        String playerName = player.getName();
        String inSystem = plugin.getConfig().getString(playerName);
    
        // inSystem is the station at which the player previously entered
        // station is the station at which the player is currently at
    
        // == Exit ==
        if (inSystem != null && !inSystem.isEmpty()){ // if inSystem has something
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
          EconomyResponse response = economy.withdrawPlayer(player, fare);
  
          plugin.getConfig().set(playerName, "");  // Sets config such that inSystem will be null the next time
          
        } else { // Max fare
          double fare = 8.0;
          player.sendMessage(ChatColor.RED+"You did not tap in! Maximum fare assumed. "+ChatColor.GOLD+"Fare: "+fare);
          EconomyResponse response = economy.withdrawPlayer(player, fare);
        }
      }
      
      
      else if (sign.getLine(0).equalsIgnoreCase("[Validator]")){
        String station = sign.getLine(1);
        String playerName = player.getName();
        String inSystem = plugin.getConfig().getString(playerName);
    
        // inSystem is the station at which the player entered the system
        // station is the station at which the player exited the system
    
        // == Exit ==
        if (inSystem != null && !inSystem.isEmpty()){
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
          EconomyResponse response = economy.withdrawPlayer(player, fare);
      
          plugin.getConfig().set(playerName, "");  // Sets config such that inSystem will be null the next time
          
          // == Entry ==
        } else {
          if (!station.isEmpty()){
            plugin.getConfig().set(playerName, station);
            player.sendMessage("Entered "+station);
          }
        }
      }
    }
  }
}
