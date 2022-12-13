package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import static mikeshafter.iciwi.util.MachineUtil.parseComponent;


public class SignCreateListener implements Listener {
  Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  Lang lang = new Lang(plugin);
  
  @EventHandler
  public void onGateCreate(SignChangeEvent event) {
    String line = parseComponent(event.line(0));
    Player player = event.getPlayer();
  
    // General Ticket machine
    if (ChatColor.stripColor(line).contains(lang.getString("tickets"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-ticket-machine"));
      } else event.setCancelled(true);
    }
  
    // Rail Pass machine
    if (ChatColor.stripColor(line).contains(lang.getString("passes"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-pass-machine"));
      } else event.setCancelled(true);
    }
  
    // Direct Ticket machine
    if (ChatColor.stripColor(line).contains(lang.getString("custom-tickets"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-custom-machine"));
      } else event.setCancelled(true);
    }
  
  }
}
