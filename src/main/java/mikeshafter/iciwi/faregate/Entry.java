package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.config.Records;
import org.bukkit.SoundCategory;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;
import mikeshafter.iciwi.config.Owners;
import org.bukkit.entity.Player;
import java.util.List;

public class Entry extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private final Owners owners = plugin.owners;
private static final CardSql cardSql = new CardSql();

public Entry() {
    super("entry");
}

@Override
public void onTicket (Player player, SignInfo info) {
    var signText = info.signText();
    var lore = info.lore();
    var sign = info.sign();
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
        player.sendMessage(String.format(lang.getString("ticket-in"), station));
        cardSql.logMaster(player.getUniqueId().toString());
        cardSql.logEntry(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), station);
        player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
        super.setCloseGateArray(CardUtil.openGate(lang.getString("entry"), signText, sign));
    }

    else {
        player.sendMessage(lang.getString("invalid-ticket"));
    }
}

@Override
public void onCard (Player player, SignInfo info) {
    var signText = info.signText();
    var sign = info.sign();
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

    player.sendMessage(String.format(lang.getString("tapped-in"), station, value));
    cardSql.logMaster(player.getUniqueId().toString());
    cardSql.logEntry(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), station);
    cardSql.logCardUse(serial);
    icCard.getRailPasses().forEach((name, start) -> cardSql.logRailpassStore(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), start, owners.getRailPassDuration(name), owners.getRailPassOperator(name)));
    if (records.getTransfer(icCard.getSerial())) {
        cardSql.logPrevJourney(records.getPreviousStation(serial), records.getPreviousFare(serial), records.getClass(serial), records.getTimestamp((serial)));
    }

    player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
    super.setCloseGateArray(CardUtil.openGate(lang.getString("entry"), signText, sign));
}

@Override
public void onRailPass (Player player, SignInfo info) {
    var signText = info.signText();
    var lore = info.lore();
    var sign = info.sign();
    String name = lore.get(0);
    String expiry = lore.get(1);
    String station = info.station();

    // check if expired
    long e = Long.parseLong(expiry);
    // if expired, return and do not open the gate
    if (e < System.currentTimeMillis()) return;
    // otherwise, check if issuing TOC is one of the station's owners
    List<String> tocs = owners.getOwners(station);
    if (tocs.contains(owners.getRailPassOperator(name))) {
        // log
        cardSql.logMaster(player.getUniqueId().toString());
        cardSql.logFreePass(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), station, "transfer");
        cardSql.logRailpassUse(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), e - owners.getRailPassDuration(name), owners.getRailPassDuration(name), owners.getRailPassOperator(name));
        player.sendMessage(String.format(lang.getString("used-paper-pass"), name));
        player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
        super.setCloseGateArray(CardUtil.openGate(lang.getString("entry"), signText, sign));
    }
}

}

