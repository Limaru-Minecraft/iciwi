package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
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
  
    // Ticket machine
    if (ChatColor.stripColor(lines[0]).contains(lang.TICKETS())) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.CREATE_TICKET_MACHINE());
      } else event.setCancelled(true);
    }
  
  }
}
