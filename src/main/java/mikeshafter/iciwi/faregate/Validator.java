package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import org.bukkit.SoundCategory;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcCard;
import org.bukkit.entity.Player;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.*;
import mikeshafter.iciwi.util.IciwiUtil;
import java.util.List;

public class Validator extends FareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private final Owners owners = plugin.owners;
private static final CardSql cardSql = new CardSql();

public Validator () {
    super("validator");
}

@Override
public void onTicket (Player player, SignInfo info) {
    var lore = info.lore();
    var station = info.station();
    var item = info.item();
    var sign = info.sign();

    boolean entryPunched = lore.get(0).contains("•");
    boolean exitPunched = lore.get(1).contains("•");
    boolean entryPunchRequired = plugin.getConfig().getBoolean("require-entry-punch");

    // Invalid Ticket
    if (entryPunched && exitPunched) {
        player.sendMessage(lang.getString("invalid-ticket"));
    }

    // Exit
    else if ((entryPunched || !entryPunchRequired) && (lore.get(1).equals(station) || owners.getOwners(station).contains(lore.get(1).replaceFirst("C:", "")))) {
        IciwiUtil.punchTicket(item, 1);
        // Log exit
        String entryStation = lore.get(0).replace(" •", "");
        String fareClass = lore.get(2);
        Fares fares = plugin.fares;

        cardSql.logMaster(player.getUniqueId().toString());
        cardSql.logExit(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), entryStation, station);
        cardSql.logJourney(fares.getFare(entryStation, station, fareClass), fares.getFare(entryStation, station, fareClass), fareClass);
        cardSql.logTicketUse(entryStation, station, fareClass);
        player.playSound(player, plugin.getConfig().getString("exit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
        player.sendMessage(String.format(lang.getString("ticket-out"), station));
    }

    // Entry
    else if (lore.get(0).equals(station) || owners.getOwners(station).contains(lore.get(0).replaceFirst("C:", ""))) {
        IciwiUtil.punchTicket(item, 0);
        // Log entry

        cardSql.logMaster(player.getUniqueId().toString());
        cardSql.logEntry(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), station);
        player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
        player.sendMessage(String.format(lang.getString("ticket-in"), station));
    }

    else {
        player.sendMessage(lang.getString("invalid-ticket"));
    }
}

@Override
public void onCard (Player player, SignInfo info) {
    var station = info.station();
    var item = info.item();
    var sign = info.sign();

    IcCard icCard = IciwiUtil.IcCardFromItem(item);
    if (icCard == null) return;

    // Vital information
    String serial = icCard.getSerial();
    Records records = plugin.records;

    // Determine entry or exit
    if (records.getStation(serial).isEmpty()) CardUtil.entry(player, icCard, station, sign.getLocation());
    else CardUtil.exit(player, icCard, station, sign.getLocation());
}

@Override
public void onRailPass (Player player, SignInfo info) {
    var lore = info.lore();
    var station = info.station();
    var sign = info.sign();

    String name = lore.get(0);
    String expiry = lore.get(1);

    // check if expired
    long e = Long.parseLong(expiry);
    // if expired, return and do not open the gate
    if (e < System.currentTimeMillis()) return;
    // otherwise, check if issuing TOC is one of the station's owners
    List<String> tocs = owners.getOwners(station);
    if (tocs.contains(owners.getRailPassOperator(name))) {
        // log
        cardSql.logMaster(player.getUniqueId().toString());
        cardSql.logMaster(player.getUniqueId().toString());
        cardSql.logFreePass(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), station, "validator");
        cardSql.logRailpassUse(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), e - owners.getRailPassDuration(name), owners.getRailPassDuration(name), owners.getRailPassOperator(name));
        player.sendMessage(String.format(lang.getString("used-paper-pass"), name));
        player.playSound(player, plugin.getConfig().getString("member-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
    }
}

}


