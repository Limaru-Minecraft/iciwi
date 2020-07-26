package mikeshafter.iciwi;

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

//import java.io.File;
//import java.util.Objects;

public class events implements Listener{
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final Iciwi iciwi = Iciwi.getPlugin(Iciwi.class);
  //ConfigManager cfgm;
  
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
    if (action == Action.RIGHT_CLICK_BLOCK && state instanceof Sign){
      Sign sign = (Sign) state;
      if (sign.getLine(0).equalsIgnoreCase("[Validator]")){
        String station = sign.getLine(1);
        String playerName = player.getName();
        String inSystem = plugin.getConfig().getString(playerName);
    
        // inSystem is the station at which the player entered the system
        // station is the station at which the player exited the system
    
        // == Exit ==
        if (inSystem != null && !inSystem.isEmpty()){
          // iciwi.loadConfigManager();
          double fare;
          String farePath = "test1";
          try{
            //fare = cfgm.getFares().getDouble(farePath); //TODO
            fare = JSONmanager.getjson(station, inSystem);
          } catch (Exception e){
            fare = 0.0;
            player.sendMessage(ChatColor.RED+"Fare "+farePath+" is not set up!");
          }
          //TODO: == Payment ==
          player.sendMessage("Entered "+inSystem+". Exited "+station+". Fare: "+fare);
      
          plugin.getConfig().set(playerName, "");  // Sets config such that inSystem will be null the next time
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
