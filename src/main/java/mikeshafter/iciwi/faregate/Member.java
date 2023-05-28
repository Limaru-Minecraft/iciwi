package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.api.ClosableFareGate;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.*;
import org.bukkit.inventory.ItemStack;
import mikeshafter.iciwi.util.IciwiUtil;

public class Member extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;

	public Member() {
		super("");
		super.setSignLine0(lang.getString("member"));
	}

	@Override
	public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
		// Get station
		String station = ChatColor.stripColor(signText[1]);

	}

	@Override public void onPlayerInFareGate (Player player) {

	}
}
