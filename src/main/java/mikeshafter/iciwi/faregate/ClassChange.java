package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;

import java.util.Map;

import org.bukkit.SoundCategory;

import mikeshafter.iciwi.IcLogger;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.entity.Player;

public class ClassChange extends FareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private final Records records = plugin.records;
private final IcLogger logger = plugin.icLogger;

public ClassChange() {
    super("classchange");
}

@Override
public void onTicket (Player player, SignInfo info) {}

@Override
public void onCard (Player player, SignInfo info) {
    String newClass = info.station();
    var sign = info.sign();
    var item = info.item();

    // Wax sign
    sign.setWaxed(true);
    sign.update(true);

    final IcCard icCard = IciwiUtil.IcCardFromItem(item);
	if (icCard == null) return;
	String serial = icCard.getSerial();
	records.setClass(serial, newClass);

	Map<String, Object> lMap = Map.of("player", player.getUniqueId(), "card", icCard, "newClass", newClass);
	logger.info("classChange", lMap);

	player.sendMessage(String.format(lang.getString("class-changed"), newClass));
	player.playSound(player, plugin.getConfig().getString("classchange-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
}

@Override
public void onRailPass (Player player, SignInfo info) {}

}
