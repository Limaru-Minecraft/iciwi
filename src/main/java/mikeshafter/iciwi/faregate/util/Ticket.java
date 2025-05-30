package mikeshafter.iciwi.faregate.util;

import mikeshafter.iciwi.IcLogger;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.IciwiUtil;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;

public class Ticket extends PayType {
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private final Owners owners = plugin.owners;
private final IcLogger logger = plugin.icLogger;

private final List<String> lore;
private final String station ;
private final boolean entryPunched ;
private final boolean exitPunched ;
private final boolean entryPunchRequired;

public Ticket (Player player, SignInfo info) {
	super(player, info);
	this.lore = info.lore();
	this.station = info.station();
	this.entryPunched = lore.get(0).contains("•");
	this.exitPunched = lore.get(1).contains("•");
	this.entryPunchRequired = plugin.getConfig().getBoolean("require-entry-punch");
}

/**
 * Register entry
 *
 * @return Whether the operation was successful.
 */
@Override public boolean onEntry () {
	boolean wrongStation = !lore.get(0).equals(station);
	String operator = lore.get(0).replaceFirst("C:", "");
	boolean wrongOperator = !owners.getOwners(station).contains(operator);

	// Invalid Ticket
	if (entryPunched || exitPunched || (wrongStation && wrongOperator)) {
		player.sendMessage(lang.getString("invalid-ticket"));
		return false;
	}

	IciwiUtil.punchTicket(super.signInfo.item(), 0);

	Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "nStation", station);
	logger.info("ticket-entry", lMap);

	player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
	player.sendRichMessage(IciwiUtil.format("<green>=== Entry ===<br>  <yellow>{station}</yellow><br>=============</green>", Map.of("station", station)));
	return true;
}

/**
 * Register onExit
 *
 * @return Whether the operation was successful.
 */
@Override public boolean onExit () {
	boolean usedTicket = entryPunched && exitPunched;
	boolean unvalidatedTicket = !entryPunched && entryPunchRequired;
	boolean wrongStation = !lore.get(1).equals(station);
	String operator = lore.get(1).replaceFirst("C:", "");
	boolean wrongOperator = !owners.getOwners(station).contains(operator);
	// Invalid ticket
	if (usedTicket || unvalidatedTicket || (wrongStation && wrongOperator) ) {
		player.sendMessage(lang.getString("invalid-ticket"));
		return false;
	}

	IciwiUtil.punchTicket(super.signInfo.item(), 1);
	String nStation = lore.get(0).replace(" •", "");

	Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "nStation", nStation, "xStation", station);
	logger.info("ticket-exit", lMap);

	player.playSound(player, plugin.getConfig().getString("exit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
	player.sendRichMessage(IciwiUtil.format("<green>=== Exit ===<br>  <yellow>{entry} → {station}</yellow><br>=============</green>", Map.of("entry", nStation, "station", station)));
	return true;
}

/**
 * Check if a card has a railpass
 *
 * @return Whether the operation was successful.
 */
@Override public boolean onMember () {
	return false;
}

/**
 * Stops and starts a journey without allowing for an OSI
 *
 * @return Whether the operation was successful.
 */
@Override public boolean onTransfer () {
	return false;
}
}
