package mikeshafter.iciwi;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class events implements Listener{
  private Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
  @EventHandler
  public void signClick(PlayerInteractEvent event) {
    Action action = event.getAction();
    Player player = event.getPlayer();
    Block block = event.getClickedBlock();
    BlockState state = block.getState();
    if (state instanceof Sign) {
      Sign sign = (Sign) state;
      if (sign.getLine(0).equalsIgnoreCase("[Validator]")) {
        player.setGameMode(GameMode.CREATIVE);
        
        player.sendMessage(ChatColor.GREEN + "You clicked on a validator sign");
        if (plugin.getConfig().getBoolean("EntryStates." + player.getUniqueId(), false)) {
          plugin.getConfig().set("EntryStates." + player.getUniqueId(), true);
        } else {
          plugin.getConfig().set("EntryStates." + player.getUniqueId(), false);
        }
        plugin.saveConfig();
      }
    }
  }
}
