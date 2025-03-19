package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.faregate.util.Card;
import mikeshafter.iciwi.faregate.util.RailPass;
import mikeshafter.iciwi.faregate.util.Ticket;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import org.bukkit.entity.Player;

public class Exit extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);

public Exit () {
	super("exit");
}

@Override
public void onTicket (Player player, SignInfo info) {
	Ticket ticket = new Ticket(player, info);
	if (ticket.onExit()) {
		super.setCloseGateArray(super.openGate());
	}
}

@Override
public void onCard (Player player, SignInfo info) {
	Card card = new Card(player, info);
	if (card.onExit()) {
		super.setCloseGateArray(super.openGate());
	}
}

@Override
public void onRailPass (Player player, SignInfo info) {
	RailPass railPass = new RailPass(player, info);
	if (railPass.onExit()) {
		super.setCloseGateArray(super.openGate());
	}
}
}
