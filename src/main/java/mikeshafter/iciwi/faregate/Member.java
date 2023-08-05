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


public class Member extends ClosableFareGate {

	private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
	private final Lang lang = new Lang();

	public Member() {
		super();
		super.setSignLine0(lang.getString("member"));
	}

	@Override
	public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
		// Get station
		String station = IciwiUtil.stripColor(signText[1]);

		// Paper ticket
		if (item.getType() == Material.valueOf(plugin.getConfig().getString("ticket.material")) && IciwiUtil.loreCheck(item)) {
			// FUTURE: Paper rail passes
		}

		// Card
		else if (item.getType() == Material.valueOf(plugin.getConfig().getString("card.material")) && IciwiUtil.loreCheck(item)) {

			// Get card from item
			IcCard icCard = IciwiUtil.IcCardFromItem(item);
			if (icCard == null) return;

			// Call entry, and if successful, open fare gate
			if (CardUtil.member(player, icCard, station)) CardUtil.openGate(lang.getString("Member"), signText, sign);

		}
	}

}
