package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;


public class SignCreateListener implements Listener {
  Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  Lang lang = new Lang(plugin);

  @EventHandler
  public void onGateCreate(SignChangeEvent event) {
    String[] lines = event.getLines();
    Player player = event.getPlayer();
  
    // General Ticket machine
    if (ChatColor.stripColor(lines[0]).contains(lang.getString("tickets"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-ticket-machine"));
      } else event.setCancelled(true);
    }
  
    // Rail Pass machine
    if (ChatColor.stripColor(lines[0]).contains(lang.getString("passes"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-pass-machine"));
      } else event.setCancelled(true);
    }
  
    // Direct Ticket machine
    if (ChatColor.stripColor(lines[0]).contains(lang.getString("custom-tickets"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-custom-machine"));
      } else event.setCancelled(true);
    }
  
  }
}
