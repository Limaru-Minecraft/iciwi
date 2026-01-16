package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;

import java.util.Map;

import org.bukkit.entity.Player;
import java.util.Optional;

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
	final Optional<IcCard> icCard = IciwiUtil.IcCardFromItem(info.item());
	// Confirmation
	if (icCard.isEmpty()) return;

	player.sendRichMessage(lang.createRichMessage(
		"Balance",
		lang.getStringList("balance-message"),
		Map.of("value", icCard.get().getValue())
	));
}

@Override
public void onRailPass (Player player, SignInfo info) {}

}
