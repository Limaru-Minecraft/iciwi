package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import org.bukkit.SoundCategory;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.entity.Player;
import java.util.List;

public class Transfer extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private static final CardSql cardSql = new CardSql();
private final Owners owners = plugin.owners;
private final Lang lang = plugin.lang;

public Transfer () {
    super("transfer");
}

@Override
public void onTicket (Player player, SignInfo info) {player.sendMessage(lang.getString("tickets-not-valid"));}

@Override
public void onCard (Player player, SignInfo info) {
    var item = info.item();
    var station = info.station();
    var sign = info.sign();
    // Get card from item
    IcCard icCard = IciwiUtil.IcCardFromItem(item);
    if (icCard == null) return;

    // Call transfer, and if successful, open fare gate
    if (CardUtil.transfer(player, icCard, station, sign.getLocation())) {
        super.setCloseGateArray(super.openGate());
    }
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
        cardSql.logFreePass(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), station, "transfer");
        cardSql.logRailpassUse(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), e - owners.getRailPassDuration(name), owners.getRailPassDuration(name), owners.getRailPassOperator(name));
        player.sendMessage(String.format(lang.getString("used-paper-pass"), name));
        player.playSound(player, plugin.getConfig().getString("transfer-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
        super.setCloseGateArray(super.openGate());
    }
}

}
