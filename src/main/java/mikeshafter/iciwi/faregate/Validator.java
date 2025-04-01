package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.faregate.util.Card;
import mikeshafter.iciwi.faregate.util.RailPass;
import mikeshafter.iciwi.faregate.util.Ticket;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcCard;
import org.bukkit.entity.Player;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.*;
import mikeshafter.iciwi.util.IciwiUtil;

public class Validator extends FareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private final Owners owners = plugin.owners;

public Validator () {
    super("validator");
}

@Override
public void onTicket (Player player, SignInfo info) {
	Ticket ticket = new Ticket(player, info);

    var lore = info.lore();
    var station = info.station();

    boolean entryPunched = lore.get(0).contains("•");
    boolean exitPunched = lore.get(1).contains("•");
    boolean entryPunchRequired = plugin.getConfig().getBoolean("require-entry-punch");
    boolean canEnter = lore.get(0).equals(station) || owners.getOwners(station).contains(lore.get(0).replaceFirst("C:", ""));
    boolean canExit = (entryPunched || !entryPunchRequired) && (lore.get(1).equals(station) || owners.getOwners(station).contains(lore.get(1).replaceFirst("C:", "")));

    // Invalid Ticket
    if (entryPunched && exitPunched) {
        player.sendMessage(lang.getString("invalid-ticket"));
    }
    else if (!entryPunched && canEnter) ticket.onEntry();
    else if (!exitPunched && canExit) ticket.onExit();
    else {
        player.sendMessage(lang.getString("invalid-ticket"));
    }
}

@Override
public void onCard (Player player, SignInfo info) {
    IcCard icCard = IciwiUtil.IcCardFromItem(info.item());
    if (icCard == null) return;
    String serial = icCard.getSerial();
    Card card = new Card(player, info);
    if (plugin.records.getStation(serial).isEmpty()) card.onEntry();
    else card.onExit();
}

@Override
public void onRailPass (Player player, SignInfo info) {
	RailPass railPass = new RailPass(player, info);
	railPass.onMember();
}

}
