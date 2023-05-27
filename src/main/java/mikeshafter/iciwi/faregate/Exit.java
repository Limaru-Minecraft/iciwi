package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.api.ClosableFareGate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.*;
import org.bukkit.inventory.ItemStack;
import mikeshafter.iciwi.util.IciwiUtil;

public class Exit extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;

	public Exit() {
		super("");
		super.setSignLine0(lang.getString("exit"));
	}

	@Override
	public void onInteract(Player player, ItemStack item, String[] signText) {
		// Get station
		String station = ChatColor.stripColor(signText[1]);

	}

	@Override public void onPlayerInFareGate (Player player) {

	}
}
