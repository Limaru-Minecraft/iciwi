package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Transfer extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;

	public Transfer() {
		super("");
		super.setSignLine0(lang.getString("transfer"));
	}

	@Override
	public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
		// Get station
		String station = IciwiUtil.stripColor(signText[1]);

		// Paper ticket
		if (item.getType() == Material.valueOf(plugin.getConfig().getString("ticket.material")) && IciwiUtil.loreCheck(item)) {
			// Tickets are not valid at transfer signs
			player.sendMessage(lang.getString("tickets-not-valid"));
		}

		// Card
		else if (item.getType() == Material.valueOf(plugin.getConfig().getString("card.material")) && IciwiUtil.loreCheck(item)) {

			// Get card from item
			IcCard icCard = IciwiUtil.IcCardFromItem(item);
			if (icCard == null) return;

			// Call entry, and if successful, open fare gate
			if (CardUtil.transfer(player, icCard, station)) CardUtil.openGate(lang.getString("transfer"), signText, sign);

		}
	}

	@Override public void onPlayerInFareGate (int x, int y, int z) {
	}
}
