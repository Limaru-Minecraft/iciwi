package mikeshafter.iciwi.faregate.util;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.IciwiUtil;
import java.util.List;
import java.util.Map;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class RailPass extends PayType {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private final Owners owners = plugin.owners;

public RailPass (Player player, SignInfo info) {
	super(player, info);
}

private boolean master (String string) {
	List<String> lore = super.signInfo.lore();
    String name = lore.get(0);
    String expiry = lore.get(1);
    String station = super.signInfo.station();
    List<String> tocs = owners.getOwners(station);

    // check if expired
    long e = Long.parseLong(expiry);
    // if expired, return and do not open the gate
    if (e < System.currentTimeMillis() || !tocs.contains(owners.getRailPassOperator(name))) return false;

	player.sendRichMessage(IciwiUtil.format(string, Map.of("station", station, "name", name)));

	player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
	return true;

}

/**
 * Register entry
 *
 * @return Whether the operation was successful.
 */
@Override
public boolean onEntry () {
	return master("<green>=== Entry ===<br>  <yellow>{station}</yellow><br>  <yellow>{name}</yellow><br>=============</green>");
}

/**
 * Register onExit
 *
 * @return Whether the operation was successful.
 */
@Override
public boolean onExit () {
	return master("<green>=== Exit ===<br>  <yellow>{station}</yellow><br>  <yellow>{name}</yellow><br>=============</green>");
}

/**
 * Check if a card has a railpass
 *
 * @return Whether the operation was successful.
 */
@Override
public boolean onMember () {
	return master("<green>=== Member ===<br>  <yellow>{station}</yellow><br>  <yellow>{name}</yellow><br>=============</green>");
}

/**
 * Stops and starts a journey without allowing for an OSI
 *
 * @return Whether the operation was successful.
 */
@Override
public boolean onTransfer () {
	return master("<green>=== Transfer ===<br>  <yellow>{station}</yellow><br>  <yellow>{name}</yellow><br>=============</green>");
}

}
