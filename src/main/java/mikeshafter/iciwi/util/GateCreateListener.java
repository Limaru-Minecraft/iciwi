package mikeshafter.iciwi.util;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class GateCreateListener implements Listener {
@EventHandler(priority = EventPriority.LOWEST) public void onGateCreate(SignChangeEvent event) {
	final Lang lang = Iciwi.getPlugin(Iciwi.class).lang;
	final String line = IciwiUtil.parseComponent(event.line(0));
	if (!(line.startsWith("[") && line.endsWith("]"))) return;
    final Player player = event.getPlayer();

    int createdSign = containsMany(line, lang.getString("entry"), lang.getString("exit"), lang.getString("member"), lang.getString("payment"), lang.getString("faregate"), lang.getString("validator"), lang.getString("tickets"), lang.getString("cards"), lang.getString("passes"), lang.getString("custom-tickets"));

	if (createdSign != -1 && !player.hasPermission("iciwi.create")) {
		event.setCancelled(true);
		return;
	}

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

/**
 * Check if any of the elements in a given array is a substring of another string.
 *
 * @param s          The string which contains a substring from checkArray
 * @param checkArray The array of strings in which a substring of s lies.
 * @return -1 if no string from checkArray is a substring of s, otherwise the index of the substring.
 */
private static int containsMany (final String s, final String... checkArray) {
	// loop through array
	for (int i = 0; i < checkArray.length; i++) if (s.contains(checkArray[i])) return i;
	// if nothing found output -1
	return -1;
}
}
