package mikeshafter.iciwi.util;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;
import java.util.List;
import static mikeshafter.iciwi.util.IciwiUtil.parseComponents;
import static mikeshafter.iciwi.util.IciwiUtil.parseComponent;

public class GateCreateListener implements Listener {

  @EventHandler
  public void onGateCreate(SignChangeEvent event) {
    Lang lang = Iciwi.getPlugin(Iciwi.class).lang;
    final String line = parseComponent(event.line(0));
    final Player player = event.getPlayer();

    // Entry
    if (ChatColor.stripColor(line).contains(lang.getString("entry"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-entry-sign"));
      } else event.setCancelled(true);
    }

    // Exit
    else if (ChatColor.stripColor(line).contains(lang.getString("exit"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-exit-sign"));
      } else event.setCancelled(true);
    }

    // Member
    else if (ChatColor.stripColor(line).contains(lang.getString("member"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-member-sign"));
      } else event.setCancelled(true);
    }

    // Payment
    else if (ChatColor.stripColor(line).contains(lang.getString("payment"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-payment-sign"));
      } else event.setCancelled(true);
    }

    // HL-style faregate
    else if (ChatColor.stripColor(line).contains(lang.getString("faregate"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-faregate-sign"));
      } else event.setCancelled(true);
    }

    // HL-style validator
    else if (ChatColor.stripColor(line).contains(lang.getString("validator"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-validator-sign"));
      } else event.setCancelled(true);
    }

    // General Ticket machine
    else if (ChatColor.stripColor(line).contains(lang.getString("tickets"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-ticket-machine"));
      } else event.setCancelled(true);
    }

    // Rail Pass machine
    else if (ChatColor.stripColor(line).contains(lang.getString("passes"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-pass-machine"));
      } else event.setCancelled(true);
    }

    // Direct Ticket machine
    else if (ChatColor.stripColor(line).contains(lang.getString("custom-tickets"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-custom-machine"));
      } else event.setCancelled(true);
    }

  }
}
