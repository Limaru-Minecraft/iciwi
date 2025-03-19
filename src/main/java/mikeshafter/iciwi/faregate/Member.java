package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.faregate.util.Card;
import mikeshafter.iciwi.faregate.util.RailPass;
import mikeshafter.iciwi.api.ClosableFareGate;
import org.bukkit.entity.Player;

public class Member extends ClosableFareGate {

public Member() {
	super("member");
}

@Override
public void onTicket (Player player, SignInfo info) {}

@Override
public void onCard (Player player, SignInfo info) {
	Card card = new Card(player, info);
	if (card.onMember()) {
		super.setCloseGateArray(super.openGate());
	}
}
@Override
public void onRailPass (Player player, SignInfo info) {
	RailPass railPass = new RailPass(player, info);
	if (railPass.onMember()) {
		super.setCloseGateArray(super.openGate());
	}
}

}
