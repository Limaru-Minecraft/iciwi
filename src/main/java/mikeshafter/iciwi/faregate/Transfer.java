package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.faregate.util.Card;
import mikeshafter.iciwi.faregate.util.RailPass;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.config.Lang;
import org.bukkit.entity.Player;

public class Transfer extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;

public Transfer () {
    super("transfer");
}

@Override
public void onTicket (Player player, SignInfo info) {player.sendMessage(lang.getString("tickets-not-valid"));}

@Override
public void onCard (Player player, SignInfo info) {
    Card card = new Card(player, info);
    if (card.onTransfer()) {
        super.setCloseGateArray(super.openGate());
    }
}

@Override
public void onRailPass (Player player, SignInfo info) {
	RailPass railPass = new RailPass(player, info);
	if (railPass.onTransfer()) {
		super.setCloseGateArray(super.openGate());
	}
}
}
