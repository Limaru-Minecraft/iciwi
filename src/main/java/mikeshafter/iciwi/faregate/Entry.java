package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.faregate.util.RailPass;

import org.bukkit.SoundCategory;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;
import mikeshafter.iciwi.config.Owners;
import org.bukkit.entity.Player;
import java.util.Map;

public class Entry extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private final Owners owners = plugin.owners;

public Entry() {
    super("entry");
}

@Override
public void onTicket (Player player, SignInfo info) {
	var lore = info.lore();
    String station = info.station();
    boolean entryPunched = lore.get(0).contains("•");
    boolean exitPunched = lore.get(1).contains("•");

    // Invalid Ticket
    if (entryPunched || exitPunched) {
        player.sendMessage(lang.getString("invalid-ticket"));
    }

    // Entry
    else if (lore.get(0).equals(station) || owners.getOwners(station).contains(lore.get(0).replaceFirst("C:", ""))) {
        IciwiUtil.punchTicket(info.item(), 0);
        //player.sendMessage(String.format(lang.getString("ticket-in"), station));
        player.sendRichMessage(IciwiUtil.format("<green>=== Entry ===<br>  <yellow>{station}</yellow><br>=============</green>", Map.of("station", station)));

		//TODO: logger

        player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
        super.setCloseGateArray(super.openGate());
    }

    else {
        player.sendMessage(lang.getString("invalid-ticket"));
    }
}

@Override
public void onCard (Player player, SignInfo info) {
    final String station = info.station();
    final Records records = plugin.records;
    final IcCard icCard = IciwiUtil.IcCardFromItem(info.item());
    if (icCard == null) return;

    double value = icCard.getValue();
    String serial = icCard.getSerial();

    // don't parse if there is no serial
    if (serial == null || serial.isEmpty() || serial.isBlank()) return;

    // reject entry if card has less than the minimum value
    if (value < plugin.getConfig().getDouble("min-amount")) {
        player.sendMessage(lang.getString("value-low"));
        return;
    }

    // was the card already used to enter the network?
    if (!records.getStation(serial).isEmpty()) {
        if (plugin.getConfig().getBoolean("open-on-penalty")) {
            Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
            player.sendMessage(lang.getString("fare-evade"));
        }
        else {
            player.sendMessage(lang.getString("cannot-pass"));
            return;
        }
    }

    records.setStation(serial, station);
    records.setClass(serial, plugin.getConfig().getString("default-class"));
    records.setTransfer(serial, System.currentTimeMillis() - records.getTimestamp(serial) < plugin.getConfig().getLong("max-transfer-time"));

    //player.sendMessage(String.format(lang.getString("tapped-in"), station, value));
    player.sendRichMessage(IciwiUtil.format("<green>=== Entry ===<br>  <yellow>{station}</yellow><br>  <yellow>{value}</yellow><br>=============</green>", Map.of("station", station, "value", String.valueOf(value))));

	// TODO: logger

    player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
    super.setCloseGateArray(super.openGate());
}

@Override
public void onRailPass (Player player, SignInfo info) {
	RailPass railPass = new RailPass(player, info);
	if (railPass.onEntry()) {
		super.setCloseGateArray(super.openGate());
	}
}

}

