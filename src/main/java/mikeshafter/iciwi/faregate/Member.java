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

import java.util.List;
import java.util.Objects;

import static mikeshafter.iciwi.faregate.CardUtil.plugin;


public class Member extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = new Lang();

public Member() {
	super();
	super.setSignLine0(lang.getString("member"));
}

@Override
public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
	if (!IciwiUtil.loreCheck(item)) return;

	// Get station
	String station = IciwiUtil.stripColor(signText[1]);

	// Wax sign
	sign.setWaxed(true);
	sign.update(true);

	TicketType ticketType = TicketType.asTicketType(item.getType());

	switch (Objects.requireNonNull(ticketType)) {
		case TICKET:
			plugin.sendAll("(☞ ͡° ͜ʖ ͡°)☞ dude tried to use a feature from the future! (ง ͡ʘ ͜ʖ ͡ʘ)ง FUTURE FEATURE FUTURE FEATURE (ง ͡ʘ ͜ʖ ͡ʘ)ง");
			break;
		case CARD:
			// Get card from item
			IcCard icCard = IciwiUtil.IcCardFromItem(item);
			if (icCard == null) return;

			// Call entry, and if successful, open fare gate
			if (CardUtil.member(player, icCard, station, sign.getLocation())) super.setCloseGateArray(CardUtil.openGate(lang.getString("Member"), signText, sign));
			break;
		case RAIL_PASS:
			List<String> lore = IciwiUtil.parseComponents(Objects.requireNonNull(item.getItemMeta().lore()));
	}
}

}
