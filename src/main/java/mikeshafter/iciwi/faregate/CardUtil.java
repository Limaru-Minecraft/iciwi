package mikeshafter.iciwi.faregate;
import org.bukkit.SoundCategory;

import mikeshafter.iciwi.config.*;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.IcCard;
import java.util.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CardUtil {
private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private static final Records records = plugin.records;
private static final Lang lang = plugin.lang;
private static final Owners owners = plugin.owners;
private static final CardSql cardSql = new CardSql();
private static final LinkedHashSet<Player> clickBuffer = new LinkedHashSet<>();

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

 @param player       Player who used the card
 @param icCard       The card to register
 @param entryStation The station at which to enter
 @return Whether entry was successful. If false, do not open the fare gate. */
protected static boolean entry (Player player, IcCard icCard, String entryStation, Location signLocation) {
	if (onClick(player)) return false;

	double value = icCard.getValue();
	String serial = icCard.getSerial();

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
	records.setStation(serial, entryStation);
	records.setClass(serial, plugin.getConfig().getString("default-class"));

	// player has a transfer discount when they tap out and in within the time limit
	records.setTransfer(serial, System.currentTimeMillis() - records.getTimestamp(serial) < plugin.getConfig().getLong("max-transfer-time"));

	// confirmation
	player.sendMessage(String.format(lang.getString("tapped-in"), entryStation, value));

	// logger
	cardSql.logMaster(player.getUniqueId().toString());
	cardSql.logEntry(signLocation.getBlockX(), signLocation.getBlockY(), signLocation.getBlockZ(), entryStation);
	cardSql.logCardUse(serial);
	icCard.getRailPasses().forEach((name, start) -> cardSql.logRailpassStore(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), start, owners.getRailPassDuration(name), owners.getRailPassOperator(name)));
	if (records.getTransfer(icCard.getSerial())) {
		cardSql.logPrevJourney(records.getPreviousStation(serial), records.getPreviousFare(serial), records.getClass(serial), records.getTimestamp((serial)));
	}

	player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
	return true;
}

/**
 Register exit from a card

 @param player      Player who used the card
 @param icCard      The card to register
 @param exitStation The station at which to exit
 @return Whether exit was successful. If false, do not open the fare gate. */
protected static boolean exit (Player player, IcCard icCard, String exitStation, Location signLocation) {
	if (onClick(player)) return false;

	Fares fares = plugin.fares;
	String serial = icCard.getSerial();

	// don't parse if there is no serial
	if (serial == null || serial.isEmpty() || serial.isBlank()) return false;

	String entryStation = records.getStation(serial);
	double value = icCard.getValue();
	double fare = fares.getCardFare(entryStation, exitStation, records.getClass(serial));

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

	// If an OSI is applicable, use the fare from the first entry station until the exit station
	if (records.getTransfer(serial)) {
		// fare if the player did not tap out
		double longFare = fares.getCardFare(records.getPreviousStation(serial), exitStation, records.getClass(serial));
		// the previous charged fare
		double previousFare = records.getPreviousFare(serial);
		// if the difference between the fares is less than the current fare, change the fare to that difference.
		if (longFare - previousFare < fare) fare = longFare - previousFare;
		// send confirmation
		player.sendMessage(lang.getString("osi"));
	}

	// Get the owners of stations and rail passes
	List<String> entryStationOwners = owners.getOwners(entryStation);
	List<String> exitStationOwners = owners.getOwners(exitStation);
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
	records.setPreviousStation(serial, entryStation);
	records.setStation(serial, null);
	records.setPreviousFare(serial, fare);

	// send (value - fare) as the value variable is not updated
	player.sendMessage(String.format(lang.getString("tapped-out"), exitStation, fare, value - fare));

	// Logger
	cardSql.logMaster(player.getUniqueId().toString());
	cardSql.logExit(signLocation.getBlockX(), signLocation.getBlockY(), signLocation.getBlockZ(), entryStation, exitStation);
	cardSql.logJourney(fares.getCardFare(entryStation, exitStation, records.getClass(serial)), fare, records.getClass(serial));
	cardSql.logCardUse(serial);
	icCard.getRailPasses().forEach((name, start) -> cardSql.logRailpassStore(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), start, owners.getRailPassDuration(name), owners.getRailPassOperator(name)));
	if (finalRailPass != null) {
		cardSql.logRailpassUse(finalRailPass, owners.getRailPassPrice(finalRailPass), owners.getRailPassPercentage(finalRailPass), cardSql.getStart(serial, finalRailPass), owners.getRailPassDuration(finalRailPass), owners.getRailPassOperator(finalRailPass));
	}

	player.playSound(player, plugin.getConfig().getString("exit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
	return true;
}

/**
 Check if a card has a railpass

 @param player  Player who used the card
 @param icCard  The card used
 @param station The station at which the sign is placed
 @return Whether checks were successful. If false, do not open the fare gate. */
protected static boolean member (Player player, IcCard icCard, String station, Location signLocation) {
	if (onClick(player)) return false;

	// Get the serial number of the card
	String serial = icCard.getSerial();

	// Get the owners of the station and the card's rail passes
	List<String> stationOwners = owners.getOwners(station);

	// Get the owners of the card's rail passes
	Set<String> railPasses = cardSql.getAllDiscounts(serial).keySet();

	// Check if the card has a rail pass belonging to the station's operator
	for (String r : railPasses) {
		if (stationOwners.contains(owners.getRailPassOperator(r))) {
			player.sendMessage(lang.getString("member-gate"));

			// Logger
			cardSql.logMaster(player.getUniqueId().toString());
			cardSql.logMember(signLocation.getBlockX(), signLocation.getBlockY(), signLocation.getBlockZ(), station);
			cardSql.logCardUse(serial);
			cardSql.logRailpassUse(r, owners.getRailPassPrice(r), owners.getRailPassPercentage(r), cardSql.getStart(serial, r), owners.getRailPassDuration(r), owners.getRailPassOperator(r));
			icCard.getRailPasses().forEach((name, start) -> cardSql.logRailpassStore(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), start, owners.getRailPassDuration(name), owners.getRailPassOperator(name)));

			player.playSound(player, plugin.getConfig().getString("member-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
			return true;
		}
	}

	// If the player does not have such a rail pass, return false
	return false;
}

/**
 Stops and starts a journey without allowing for an OSI

 @param player  Player who used the card
 @param icCard  The card used
 @param station The station at which the sign is placed
 @return Whether checks were successful. If false, do not open the fare gate. */
protected static boolean transfer (Player player, IcCard icCard, String station, Location signLocation) {
	if (onClick(player)) return false;

	Fares fares = plugin.fares;
	String serial = icCard.getSerial();

	// don't parse if there is no serial
	if (serial == null || serial.isEmpty() || serial.isBlank()) return false;

	// If an OSI was detected, cancel OSI capability
	if (records.getTransfer(serial)) {
		records.setTransfer(serial, false);
		player.sendMessage(lang.getString("transfer-cancel-osi"));
		player.playSound(player, plugin.getConfig().getString("transfer-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
		return true;
	}

	// Else perform normal exit, then entry sequence
	String entryStation = records.getStation(serial);
	double value = icCard.getValue();
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

	// Logger
	cardSql.logMaster(player.getUniqueId().toString());
	cardSql.logTransfer(signLocation.getBlockX(), signLocation.getBlockY(), signLocation.getBlockZ(), entryStation, station);
	cardSql.logJourney(fares.getCardFare(entryStation, station, records.getClass(serial)), fare, records.getClass(serial));
	cardSql.logCardUse(serial);
	icCard.getRailPasses().forEach((name, start) -> cardSql.logRailpassStore(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), start, owners.getRailPassDuration(name), owners.getRailPassOperator(name)));
	if (finalRailPass != null) {
		cardSql.logRailpassUse(finalRailPass, owners.getRailPassPrice(finalRailPass), owners.getRailPassPercentage(finalRailPass), cardSql.getStart(serial, finalRailPass), owners.getRailPassDuration(finalRailPass), owners.getRailPassOperator(finalRailPass));
	}
	player.playSound(player, plugin.getConfig().getString("transfer-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
	return true;
}

}
