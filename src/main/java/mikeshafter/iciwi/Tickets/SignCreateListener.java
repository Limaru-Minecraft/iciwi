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
    if (ChatColor.stripColor(lines[0]).contains(lang.TICKETS())) {
      if (player.hasPermission("iciwi.create")) {
        if (!(lines[0].contains("\u00A7") || lines[1].contains("\u00A7") || lines[2].contains("\u00A7") || lines[3].contains("\u00A7"))) {
          event.setLine(0, "\u00A7f"+lines[0]);
      
          if (lines[1].isBlank() && lines[2].isBlank() && lines[3].isBlank()) {
            lines[1] = lang.DEFAULT_TM_SIGN_LINE_2();
            lines[2] = lang.DEFAULT_TM_SIGN_LINE_3();
            lines[3] = lang.DEFAULT_TM_SIGN_LINE_4();
            event.setLine(1, "\u00A7l"+lines[1]);
            event.setLine(2, "\u00A7l"+lines[2]);
            event.setLine(3, "\u00A7b"+lines[2]);
          }
        }
        player.sendMessage(lang.CREATE_TICKET_MACHINE());
      } else event.setCancelled(true);
    }
  
  }
}
