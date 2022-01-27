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
  
    // Entry
    if (ChatColor.stripColor(lines[0]).contains(lang.ENTRY())) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.CREATE_ENTRY_SIGN());
      } else event.setCancelled(true);
    }
  
    // Exit
    else if (ChatColor.stripColor(lines[0]).contains(lang.EXIT())) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.CREATE_EXIT_SIGN());
      } else event.setCancelled(true);
    }
  
    // HL-style faregate
    else if (ChatColor.stripColor(lines[0]).contains(lang.FAREGATE())) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.CREATE_FAREGATE_SIGN());
      } else event.setCancelled(true);
    }
  
    // HL-style validator
    else if (ChatColor.stripColor(lines[0]).contains(lang.VALIDATOR())) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.CREATE_VALIDATOR_SIGN());
      } else event.setCancelled(true);
    }
  
  
  }
}
