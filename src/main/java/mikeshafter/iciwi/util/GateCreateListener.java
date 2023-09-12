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
import static mikeshafter.iciwi.util.IciwiUtil.containsMany;

public class GateCreateListener implements Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  public void onGateCreate(SignChangeEvent event) {
    Lang lang = Iciwi.getPlugin(Iciwi.class).lang;
    final String line = parseComponent(event.line(0));
    final Player player = event.getPlayer();

    if (!player.hasPermission("iciwi.create")) {
      event.setCancelled(true);
      return;
    }

    int createdSign = containsMany(line, lang.getString("entry"), lang.getString("exit"), lang.getString("member"), lang.getString("payment"), lang.getString("faregate"), lang.getString("validator"), lang.getString("tickets"), lang.getString("cards"), lang.getString("passes"), lang.getString("custom-tickets"));

    switch (createdSign) {
      case 0 -> player.sendMessage(lang.getString("create-entry-sign"));
      case 1 -> player.sendMessage(lang.getString("create-exit-sign"));
      case 2 -> player.sendMessage(lang.getString("create-member-sign"));
      case 3 -> player.sendMessage(lang.getString("create-payment-sign"));
      case 4 -> player.sendMessage(lang.getString("create-faregate-sign"));
      case 5 -> player.sendMessage(lang.getString("create-validator-sign"));
      case 6 -> player.sendMessage(lang.getString("create-ticket-machine"));
      case 7 -> player.sendMessage(lang.getString("create-card-machine"));
      case 8 -> player.sendMessage(lang.getString("create-pass-machine"));
      case 9 -> player.sendMessage(lang.getString("create-custom-machine"));
      // stop processing if containsMany outputs a "not found"
      default -> {return;}
    }
    
    // wax sign
    final Sign sign = (Sign) event.getBlock().getState();
    sign.setWaxed(true);
    sign.update(true);
  }
}
