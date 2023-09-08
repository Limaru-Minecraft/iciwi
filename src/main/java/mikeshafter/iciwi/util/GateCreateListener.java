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

    int createdSign = containsMany(stripColor(line), lang.getString("entry"), lang.getString("exit"), lang.getString("member"), lang.getString("payment"), lang.getString("faregate"), lang.getString("validator"), lang.getString("tickets"), lang.getString("cards"), lang.getString("passes"), lang.getString("custom-tickets"));
    
    // stop processing if containsMany doesn't output a valid value
    if (createdSign == -1) return;
    
    switch (createdSign) {
      case 0: 
        player.sendMessage(lang.getString("create-entry-sign"));break;
      case 1:
        player.sendMessage(lang.getString("create-exit-sign"));break;
      case 2: 
        player.sendMessage(lang.getString("create-member-sign"));break;
      case 3:
        player.sendMessage(lang.getString("create-payment-sign"));break;
      case 4:
        player.sendMessage(lang.getString("create-faregate-sign"));break;
      case 5:
        player.sendMessage(lang.getString("create-validator-sign"));break;
      case 6:
        player.sendMessage(lang.getString("create-ticket-machine"));break;
      case 7:
        player.sendMessage(lang.getString("create-card-machine"));break;
      case 8:
        player.sendMessage(lang.getString("create-pass-machine"));break;
      case 9:
        player.sendMessage(lang.getString("create-custom-machine"));break;
      default: 
        break;
    }
    
    // wax sign
    final Sign sign = (Sign) event.getBlock().getState();
    sign.setWaxed(true);
    sign.update(true);
  }
}
