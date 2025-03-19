package mikeshafter.iciwi.faregate.util;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.SoundCategory;
import mikeshafter.iciwi.config.*;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.SignInfo;

import java.util.*;

import org.bukkit.entity.Player;

public class Card extends PayType {
private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private static final Records records = plugin.records;
private static final Lang lang = plugin.lang;
private static final Owners owners = plugin.owners;
private static final CardSql cardSql = new CardSql();
private static final LinkedHashSet<Player> clickBuffer = new LinkedHashSet<>();

private final IcCard icCard;
private double value = 0;
private String serial = "";

public Card (Player player, SignInfo info) {
	super(player, info);
	this.icCard = IciwiUtil.IcCardFromItem(info.item());
	if (icCard != null) {
		this.value = this.icCard.getValue();
		this.serial = this.icCard.getSerial();
	}
}

/**
 Prevent code from registering multiple accidental clicks

 @param player Player who clicked
 @return true if the player has clicked within the last 10 ticks, false otherwise */
private static boolean onClick (Player player) {
	plugin.getServer().getScheduler().runTaskLater(plugin, () -> clickBuffer.remove(player), 10);
	return !clickBuffer.add(player);
}

/**
 Register entry from a card

 @return Whether entry was successful. If false, do not open the fare gate.
 */
public boolean onEntry () {
	if (onClick(player)) return false;
	final String nStation = super.signInfo.station();

	// don't parse if there is no serial
	if (serial == null || serial.isEmpty() || serial.isBlank()) return false;

	// reject entry if card has less than the minimum value
	if (value < plugin.getConfig().getDouble("min-amount")) {
		player.sendMessage(lang.getString("value-low"));
		return false;
	}

	// was the card already used to enter the network?
	if (!records.getStation(serial).isEmpty()) {
		if (plugin.getConfig().getBoolean("open-on-penalty")) {
			Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
			player.sendMessage(lang.getString("fare-evade"));
		}
		else {
			player.sendMessage(lang.getString("cannot-pass"));
			return false;
		}
	}

	// write the entry station and fare class
	records.setStation(serial, nStation);
	records.setClass(serial, plugin.getConfig().getString("default-class"));

	// player has a transfer discount when they tap out and in within the time limit
	records.setTransfer(serial, System.currentTimeMillis() - records.getTimestamp(serial) < plugin.getConfig().getLong("max-transfer-time"));

	// confirmation
	player.sendMessage(String.format(lang.getString("tapped-in"), nStation, value));

	// TODO: logger

	player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
	return true;
}

/**
 Register onExit from a card

 @return Whether onExit was successful. If false, do not open the fare gate. */
public boolean onExit () {
	if (onClick(player)) return false;

	Fares fares = plugin.fares;
	String xStation = super.signInfo.station();

	// don't parse if there is no serial
	if (serial == null || serial.isEmpty() || serial.isBlank()) return false;

	String nStation = records.getStation(serial);
	double fare = fares.getCardFare(nStation, xStation, records.getClass(serial));

	// is the card not in the network?
	if (records.getStation(serial).isEmpty()) {
		if (plugin.getConfig().getBoolean("open-on-penalty")) {
			Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
			player.sendMessage(lang.getString("fare-evade"));
			return true;
		}
		else {
			player.sendMessage(lang.getString("cannot-pass"));
			return false;
		}
	}

	// If an OSI is applicable, use the fare from the first entry station until the onExit station
	if (records.getTransfer(serial)) {
		// fare if the player did not tap out
		double longFare = fares.getCardFare(records.getPreviousStation(serial), xStation, records.getClass(serial));
		// the previous charged fare
		double previousFare = records.getPreviousFare(serial);
		// if the difference between the fares is less than the current fare, change the fare to that difference.
		if (longFare - previousFare < fare) fare = longFare - previousFare;
		// send confirmation
		player.sendMessage(lang.getString("osi"));
	}

	// Get the owners of stations and rail passes
	List<String> nStationOwners = owners.getOwners(nStation);
	List<String> xStationOwners = owners.getOwners(xStation);
	String finalRailPass = null;
	double payPercentage = 1d;

	// Get cheapest discount
	for (var r : cardSql.getAllDiscounts(serial).keySet()) {
		if ((nStationOwners.contains(owners.getRailPassOperator(r)) || xStationOwners.contains(owners.getRailPassOperator(r))) && owners.getRailPassPercentage(r) < payPercentage) {
			finalRailPass = r;
			payPercentage = owners.getRailPassPercentage(r);
		}
	}

	// Set final fare
	fare *= payPercentage;

	// check if card value is low
	if (value < fare) {
		player.sendMessage(lang.getString("value-low"));
		return false;
	}

	// Check for fare caps

	//TODO: check each owner for their respective fare caps
	// if an owner has a fare cap, and records.yml says that the fare cap has not been reached, deposit into the TOC's coffer
	// if the fare cap has been reached, do not deposit into the TOC's coffer, and remove that amount of money from the final fare.

	// withdraw fare from card
	if (!icCard.withdraw(fare)) {
		player.sendMessage("Error tapping out");
		return false;
	}

	// set details for future transfer
	records.setTimestamp(serial, System.currentTimeMillis());
	records.setPreviousStation(serial, nStation);
	records.setStation(serial, null);
	records.setPreviousFare(serial, fare);

	// send (value - fare) as the value variable is not updated
	player.sendMessage(String.format(lang.getString("tapped-out"), xStation, fare, value - fare));

	// TODO: Logger

	player.playSound(player, plugin.getConfig().getString("onExit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
	return true;
}

/**
 Check if a card has a railpass
 @return Whether checks were successful. If false, do not open the fare gate. */
public boolean onMember () {
	if (onClick(super.player)) return false;

	// Get the serial number of the card
	String serial = icCard.getSerial();
	String station = super.signInfo.station();

	// Get the owners of the station and the card's rail passes
	List<String> stationOwners = owners.getOwners(station);

	// Get the owners of the card's rail passes
	Set<String> railPasses = cardSql.getAllDiscounts(serial).keySet();

	// Check if the card has a rail pass belonging to the station's operator
	for (String r : railPasses) {
		if (stationOwners.contains(owners.getRailPassOperator(r))) {
			player.sendMessage(lang.getString("onMember-gate"));

			// TODO: Logger

			player.playSound(player, plugin.getConfig().getString("onMember-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
			return true;
		}
	}

	// If the player does not have such a rail pass, return false
	return false;
}

/**
 Stops and starts a journey without allowing for an OSI

 @return Whether checks were successful. If false, do not open the fare gate. */
public boolean onTransfer () {
	if (onClick(super.player)) return false;

	Fares fares = plugin.fares;
	String station = super.signInfo.station();

	// don't parse if there is no serial
	if (serial == null || serial.isEmpty() || serial.isBlank()) return false;

	// If an OSI was detected, cancel OSI capability
	if (records.getTransfer(serial)) {
		records.setTransfer(serial, false);
		player.sendMessage(lang.getString("transfer-cancel-osi"));
		player.playSound(player, plugin.getConfig().getString("transfer-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
		return true;
	}

	// Else perform normal onExit, then entry sequence
	String entryStation = records.getStation(serial);
	double fare = fares.getCardFare(entryStation, station, records.getClass(serial));

	// is the card not in the network?
	if (records.getStation(serial).isEmpty()) {
		if (plugin.getConfig().getBoolean("open-on-penalty")) {
			Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
			player.sendMessage(lang.getString("fare-evade"));
		}
		else {
			player.sendMessage(lang.getString("cannot-pass"));
			return false;
		}
	}

	// Get the owners of stations and rail passes
	List<String> entryStationOwners = owners.getOwners(entryStation);
	List<String> exitStationOwners = owners.getOwners(station);
	String finalRailPass = null;
	double payPercentage = 1d;

	// Get cheapest discount
	for (var r : cardSql.getAllDiscounts(serial).keySet()) {
		if ((entryStationOwners.contains(owners.getRailPassOperator(r)) || exitStationOwners.contains(owners.getRailPassOperator(r))) && owners.getRailPassPercentage(r) < payPercentage) {
			finalRailPass = r;
			payPercentage = owners.getRailPassPercentage(r);
		}
	}

	// Set final fare
	fare *= payPercentage;

	// check if card value is low
	if (value < fare) {
		player.sendMessage(lang.getString("value-low"));
		return false;
	}

	if (!icCard.withdraw(fare)) {
		player.sendMessage("Error tapping out");
		return false;
	}

	// set details for future transfer
	records.setTimestamp(serial, System.currentTimeMillis());
	records.setPreviousStation(serial, entryStation);
	records.setStation(serial, null);
	records.setPreviousFare(serial, fare);

	// Perform entry sequence
	// reject entry if card has less than the minimum value
	if (value < plugin.getConfig().getDouble("min-amount")) return false;

	// was the card already used to enter the network?
	if (records.getStation(serial).isEmpty()) {
		player.sendMessage(lang.getString("cannot-pass"));
		if (plugin.getConfig().getBoolean("open-on-penalty")) {
			Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
			player.sendMessage(lang.getString("fare-evade"));
		}
		else return false;
	}

	// write the entry station
	records.setStation(serial, entryStation);
	records.setClass(serial, plugin.getConfig().getString("default-class"));

	// player has a transfer discount when they tap out and in within the time limit
	records.setTransfer(serial, System.currentTimeMillis() - records.getTimestamp(serial) < plugin.getConfig().getLong("max-transfer-time"));

	// confirmation
	player.sendMessage(String.format(lang.getString("tapped-out"), entryStation, value));

	// TODO: Logger

	player.playSound(player, plugin.getConfig().getString("transfer-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
	return true;
}

}
