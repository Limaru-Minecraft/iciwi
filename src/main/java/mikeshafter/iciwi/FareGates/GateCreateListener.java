package mikeshafter.iciwi.FareGates;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;


public class GateCreateListener implements Listener {
  Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  Lang lang = new Lang(plugin);
  
  @EventHandler
  public void onGateCreate(SignChangeEvent event) {
    String[] lines = event.getLines();
    Player player = event.getPlayer();
    if (ChatColor.stripColor(lines[0]).contains(lang.ENTRY)) {
      if (player.hasPermission("iciwi.create")) {
        if (!(lines[0].contains("\u00A7") || lines[1].contains("\u00A7") || lines[2].contains("\u00A7") || lines[3].contains("\u00A7"))) {
          event.setLine(0, "\u00A7a"+lines[0]);
          event.setLine(1, "\u00A7e"+lines[1]);
          if (lines[2].isBlank() && lines[3].isBlank()) {
            if (lines[0].contains("V")) {
              lines[2] = lang.DEFAULT_ENTRY_SIGN_LINE_3;
              lines[3] = lang.DEFAULT_ENTRY_SIGN_LINE_4;
            } else if (lines[0].contains("L")) lines[2] = "-->";
            else lines[2] = "<--";
            event.setLine(2, "\u00A7f"+lines[2]);
            event.setLine(3, "\u00A7f"+lines[3]);
          }
        }
        player.sendMessage(lang.CREATE_ENTRY_SIGN);
      } else event.setCancelled(true);
    }
    
    else if (ChatColor.stripColor(lines[0]).contains(lang.EXIT)) {
      if (player.hasPermission("iciwi.create")) {
        if (!(lines[0].contains("\u00A7") || lines[1].contains("\u00A7") || lines[2].contains("\u00A7") || lines[3].contains("\u00A7"))) {
          event.setLine(0, "\u00A7c"+lines[0]);
          event.setLine(1, "\u00A7e"+lines[1]);
          if (lines[2].isBlank() && lines[3].isBlank()) {
            if (lines[0].contains("V")) {
              lines[2] = lang.DEFAULT_EXIT_SIGN_LINE_3;
              lines[3] = lang.DEFAULT_EXIT_SIGN_LINE_4;
            } else if (lines[0].contains("L")) lines[2] = "-->";
            else lines[2] = "<--";
            event.setLine(2, "\u00A7f"+lines[2]);
            event.setLine(3, "\u00A7f"+lines[3]);
          }
        }
        player.sendMessage(lang.CREATE_EXIT_SIGN);
      } else event.setCancelled(true);
    }

    else if (ChatColor.stripColor(lines[0]).contains(lang.FAREGATE)) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.CREATE_FAREGATE_SIGN);
      } else event.setCancelled(true);
    }

    else if (ChatColor.stripColor(lines[0]).contains(lang.VALIDATOR)) {
      if (player.hasPermission("iciwi.create")) {
        if (!(lines[0].contains("\u00A7") || lines[1].contains("\u00A7") || lines[2].contains("\u00A7") || lines[3].contains("\u00A7"))) {
          event.setLine(0, "\u00A7f"+lines[0]);
          
          if (lines[1].isBlank() && lines[2].isBlank() && lines[3].isBlank()) {
            lines[1] = lang.DEFAULT_VALIDATOR_SIGN_LINE_2;
            lines[2] = lang.DEFAULT_VALIDATOR_SIGN_LINE_3;
            lines[3] = lang.DEFAULT_VALIDATOR_SIGN_LINE_4;
            event.setLine(1, "\u00A7l"+lines[1]);
            event.setLine(2, "\u00A7l"+lines[2]);
            event.setLine(3, "\u00A7b"+lines[3]);
          }
        }
        player.sendMessage(lang.CREATE_VALIDATOR_SIGN);
      } else event.setCancelled(true);
    }
    
  }
}
