package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;


public class GateCreateListener implements Listener {
  Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  Lang lang = plugin.lang;

  @EventHandler
  public void onGateCreate(SignChangeEvent event) {
    List<String> lines = parseComponents(event.lines());
    Player player = event.getPlayer();

    // Entry
    if (ChatColor.stripColor(lines.get(0)).contains(lang.getString("entry"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-entry-sign"));
      } else event.setCancelled(true);
    }

    // Exit
    else if (ChatColor.stripColor(lines.get(0)).contains(lang.getString("exit"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-exit-sign"));
      } else event.setCancelled(true);
    }

    // HL-style faregate
    else if (ChatColor.stripColor(lines.get(0)).contains(lang.getString("faregate"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-faregate-sign"));
      } else event.setCancelled(true);
    }

    // HL-style validator
    else if (ChatColor.stripColor(lines.get(0)).contains(lang.getString("validator"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-validator-sign"));
      } else event.setCancelled(true);
    }


  }
}
