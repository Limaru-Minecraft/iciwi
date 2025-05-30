package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.faregate.util.RailPass;
import mikeshafter.iciwi.faregate.util.Ticket;
import mikeshafter.iciwi.faregate.util.Card;
import mikeshafter.iciwi.api.ClosableFareGate;
import org.bukkit.entity.Player;

public class Entry extends ClosableFareGate {

public Entry() {
    super("entry");
}

@Override
public void onTicket (Player player, SignInfo info) {
	Ticket ticket = new Ticket(player, info);
	if (ticket.onEntry()) {
        super.setCloseGateArray(super.openGate());
	}
}

@Override
public void onCard (Player player, SignInfo info) {
	Card card = new Card(player, info);
	if (card.onEntry()) {
        super.setCloseGateArray(super.openGate());
	}
}

@Override
public void onRailPass (Player player, SignInfo info) {
	RailPass railPass = new RailPass(player, info);
	if (railPass.onEntry()) {
		super.setCloseGateArray(super.openGate());
	}
}
}

