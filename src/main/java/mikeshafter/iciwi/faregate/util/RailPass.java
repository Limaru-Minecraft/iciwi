package mikeshafter.iciwi.faregate.util;

import mikeshafter.iciwi.IcLogger;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;

public class RailPass extends PayType {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Owners owners = plugin.owners;
private final IcLogger logger = plugin.icLogger;


public RailPass (Player player, SignInfo info) {
	super(player, info);
}

private boolean master (String string, String sound) {
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

	Map<String, Object> lMap = Map.of("player", player.getUniqueId().toString(), "station", station, "railPass", name, "rp-expiry", expiry);
	logger.info("railpass-use", lMap);

	player.playSound(player, sound, SoundCategory.MASTER, 1f, 1f);
	return true;

}

/**
 * Register entry
 *
 * @return Whether the operation was successful.
 */
@Override
public boolean onEntry () {
	final String string = "<green>=== Entry ===<br>  <yellow>{station}</yellow><br>  <yellow>{name}</yellow><br>=============</green>";
	final String sound = plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown");
	return master(string, sound);
}

/**
 * Register onExit
 *
 * @return Whether the operation was successful.
 */
@Override
public boolean onExit () {
	final String string = "<green>=== Exit ===<br>  <yellow>{station}</yellow><br>  <yellow>{name}</yellow><br>=============</green>";
	final String sound = plugin.getConfig().getString("exit-noise", "minecraft:block.amethyst_block.step");
	return master(string, sound);
}

/**
 * Check if a card has a railpass
 *
 * @return Whether the operation was successful.
 */
@Override
public boolean onMember () {
	final String string = "<green>=== Member ===<br>  <yellow>{station}</yellow><br>  <yellow>{name}</yellow><br>=============</green>";
	final String sound = plugin.getConfig().getString("member-noise", "minecraft:entity.allay.item_thrown");
	return master(string, sound);
}

/**
 * Stops and starts a journey without allowing for an OSI
 *
 * @return Whether the operation was successful.
 */
@Override
public boolean onTransfer () {
	final String string = "<green>=== Transfer ===<br>  <yellow>{station}</yellow><br>  <yellow>{name}</yellow><br>=============</green>";
	final String sound = plugin.getConfig().getString("transfer-noise", "minecraft:block.amethyst_block.step");
	return master(string, sound);
}

}
