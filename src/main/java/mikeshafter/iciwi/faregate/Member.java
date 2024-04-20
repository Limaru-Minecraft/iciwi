package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.config.Owners;
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
private final Owners owners = new Owners();

public Member() {
	super();
	super.setSignLine0(lang.getString("member"));
}

@Override
public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
	TicketType ticketType = TicketType.asTicketType(item.getType());
	if (!IciwiUtil.loreCheck(item) || ticketType == null) return;

	// Get station
	String station = IciwiUtil.stripColor(signText[1]);

	// Wax sign
	sign.setWaxed(true);
	sign.update(true);

	switch (ticketType) {
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
                String name = lore.get(0);
                String expiry = lore.get(1);

                try {
                // check if expired
                    long e = Long.parseLong(expiry);
                // if expired, return and do not open the gate
                    if (e < System.currentTimeMillis()) {
                        return;
                    }
                // otherwise, check if issuing TOC is one of the station's owners
                    List<String> tocs = owners.getOwners(station);
                    if (tocs.contains(owners.getRailPassOperator(name))) {
                // if yes, open the gate
                        super.setCloseGateArray(CardUtil.openGate(lang.getString("entry"), signText, sign));
                    }
                }
                catch (Exception ignored) {
                    return;
                }
                break;
			
			break;
	}
}

}
