package mikeshafter.iciwi.util;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import static mikeshafter.iciwi.util.IciwiUtil.parseComponent;
import static mikeshafter.iciwi.util.IciwiUtil.stripColor;


public class GateCreateListener implements Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  public void onGateCreate(SignChangeEvent event) {
    Lang lang = Iciwi.getPlugin(Iciwi.class).lang;
    final String line = parseComponent(event.line(0));
    final Player player = event.getPlayer();

    // Wax sign
    if (!player.hasPermission("iciwi.create")) {
      event.setCancelled(true);
      return;
    }
    
    final Sign sign = (Sign) event.getBlock().getState();
    sign.setWaxed(true);
    sign.update();

    // Entry
    if (stripColor(line).contains(lang.getString("entry"))) player.sendMessage(lang.getString("create-entry-sign"));

    // Exit
    else if (stripColor(line).contains(lang.getString("exit"))) player.sendMessage(lang.getString("create-exit-sign"));

    // Member
    else if (stripColor(line).contains(lang.getString("member"))) player.sendMessage(lang.getString("create-member-sign"));

    // Payment
    else if (stripColor(line).contains(lang.getString("payment"))) player.sendMessage(lang.getString("create-payment-sign"));

    // HL-style faregate
    else if (stripColor(line).contains(lang.getString("faregate"))) player.sendMessage(lang.getString("create-faregate-sign"));

    // HL-style validator
    else if (stripColor(line).contains(lang.getString("validator"))) player.sendMessage(lang.getString("create-validator-sign"));

    // General Ticket machine
    else if (stripColor(line).contains(lang.getString("tickets"))) player.sendMessage(lang.getString("create-ticket-machine"));

    // Card machine
    else if (stripColor(line).contains(lang.getString("cards"))) player.sendMessage(lang.getString("create-card-machine"));

    // Rail Pass machine
    else if (stripColor(line).contains(lang.getString("passes"))) player.sendMessage(lang.getString("create-pass-machine"));

    // Direct Ticket machine
    else if (stripColor(line).contains(lang.getString("custom-tickets"))) player.sendMessage(lang.getString("create-custom-machine"));

  }
}
