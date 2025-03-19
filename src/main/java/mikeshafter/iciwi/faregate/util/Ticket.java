package mikeshafter.iciwi.faregate.util;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.IciwiUtil;

import org.bukkit.SoundCategory;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;

public class Ticket extends PayType {
	private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
	private final Lang lang = plugin.lang;
	private final Owners owners = plugin.owners;

	private final List<String> lore;
	private final Sign sign;
	private final String station ;
	private final boolean entryPunched ;
	private final boolean exitPunched ;
	private final boolean entryPunchRequired;

	public Ticket (Player player, SignInfo info) {
		super(player, info);
		this.lore = info.lore();
		this.sign = info.sign();
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
		return false;
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
player.sendMessage(station); //TODO: debug
player.sendMessage(String.valueOf(usedTicket)); //TODO: debug
player.sendMessage(String.valueOf(unvalidatedTicket)); //TODO: debug
player.sendMessage(String.valueOf(wrongStation)); //TODO: debug
player.sendMessage(String.valueOf(wrongOperator)); //TODO: debug
		// Invalid ticket
		if (usedTicket || unvalidatedTicket || (wrongStation	&& wrongOperator) ) {
			player.sendMessage(lang.getString("invalid-ticket"));
			return false;
		}

		IciwiUtil.punchTicket(super.signInfo.item(), 1);
		player.sendMessage(String.format(lang.getString("ticket-out"), station));
		// Log onExit
		String entryStation = lore.get(0).replace(" •", "");
		String fareClass = lore.get(2);
		Fares fares = plugin.fares;

		// TODO: logger

		player.playSound(player, plugin.getConfig().getString("onExit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
		player.sendRichMessage(IciwiUtil.format("<green>=== Exit ===<br>  <yellow>{entry} → {station}</yellow><br>=============</green>", Map.of("entry", entryStation, "station", station)));
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
