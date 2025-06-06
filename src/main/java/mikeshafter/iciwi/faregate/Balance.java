package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.entity.Player;

public class Balance extends FareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;

public Balance() {
	super("balance");
}

@Override
public void onTicket (Player player, SignInfo info) {}

@Override
public void onCard (Player player, SignInfo info) {
	final IcCard icCard = IciwiUtil.IcCardFromItem(info.item());
	if (icCard != null) player.sendMessage("Card value: " + icCard.getValue());
}

@Override
public void onRailPass (Player player, SignInfo info) {}

}
