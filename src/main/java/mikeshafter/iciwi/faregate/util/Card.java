package mikeshafter.iciwi.faregate.util;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.IcLogger;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.stream.Stream;

public class Card extends PayType {
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Records records = plugin.records;
private final Lang lang = plugin.lang;
private final Owners owners = plugin.owners;
private final LinkedHashSet<Player> clickBuffer = new LinkedHashSet<>();
private final IcLogger logger = plugin.icLogger;

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
 @return true if the player has clicked within the last 10 ticks, false otherwise
 */
private boolean onClick (Player player) {
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
    player.sendRichMessage(IciwiUtil.format("<green>=== Entry ===<br>  <yellow>{station}</yellow><br>  <yellow>{value}</yellow><br>=============</green>", Map.of("station", super.signInfo.station(), "value", String.valueOf(value))));

	Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "serial", serial, "value", String.valueOf(value), "nStation", nStation);
	logger.info("card-entry", lMap);

	player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
	return true;
}

/**
 Register onExit from a card
 @return Whether onExit was successful. If false, do not open the fare gate.
*/
public boolean onExit () {
	// don't parse if there is no serial
	if (onClick(player) || serial == null || serial.isEmpty() || serial.isBlank()) return false;

	Fares fares = plugin.fares;
	String xStation = super.signInfo.station();
	String nStation = records.getStation(serial);
	List<String> nOwners = owners.getOwners(nStation);
	List<String> xOwners = owners.getOwners(xStation);

	// is the card not in the network?
	if (records.getStation(serial).isEmpty()) {
		if (plugin.getConfig().getBoolean("open-on-penalty")) {
			Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
			player.sendMessage(lang.getString("fare-evade"));
			return true;
		}
		else {
			player.sendMessage(lang.getString("cannot-pass"));
		}
		return false;
	}

	// Calculate base fare
	double fare = fares.getCardFare(records.getStation(serial), xStation, records.getClass(serial));

	// Use transfer fare if applicable
	boolean osi = false;
	if (records.getTransfer(serial)) {
		double pFare = records.getPreviousFare(serial);
		double tFare = fares.getCardFare(records.getPreviousStation(serial), xStation, records.getClass(serial));
		if (tFare != 0d && !(tFare - pFare > fare)) {
			fare = Math.max(tFare - pFare, 0d);
			osi = true;
		}
	}

	// Get cheapest rail pass
	var tOwners = Stream.concat(owners.getOwners(nStation).stream(), owners.getOwners(xStation).stream()).toList();
	List<String> railPassNames = this.owners.getRailPassNamesFromList(tOwners);
	var myPasses = icCard.getRailPasses().keySet();
//	player.sendMessage("tOwners:"+ tOwners); //TODO: debug
//	player.sendMessage("rpNames:"+ railPassNames); //TODO: debug
	var finalPasses = myPasses.stream().filter(railPassNames::contains).toList();
//	player.sendMessage("rpNames (after retain):"+ finalPasses); //TODO: debug
	double pp = 1f;
	String finalRailPass = null;
	if (!finalPasses.isEmpty()) {
		if (finalPasses.size() == 1) {finalRailPass = finalPasses.get(0); pp = owners.getRailPassPercentage(finalRailPass);}
		else for (String railPassName : finalPasses) {
			if (pp >= owners.getRailPassPercentage(railPassName)) {
				pp = owners.getRailPassPercentage(railPassName);
				finalRailPass = railPassName;
			}
		}
//		player.sendMessage("FRP:"+ finalRailPass); //TODO: debug
//		player.sendMessage("PP:"+ pp); //TODO: debug
	}

	// Set final base fare
	fare *= pp;
//	player.sendMessage("Fare:"+ fare); //TODO: debug

	if (icCard.getValue() < fare) {
		player.sendMessage(lang.getString("value-low"));
		return false;
	}

	// Check for fare caps
	double tFare = 0d;

	if (!nOwners.isEmpty()) {
	double nEarning = fare / (2 * nOwners.size());
	for (var o : nOwners) {
		final double fcAmt = owners.getFareCapAmt(o);
		if (fcAmt == 0) {
			owners.deposit(o, nEarning);
			icCard.withdraw(nEarning);
			continue;
		}
		double remAmt = records.getCapRemAmt(serial, o);
		final long fcExp = records.getCapExpiry(serial, o);
		if (fcExp < System.currentTimeMillis()) {
			final long fcDur = owners.getFareCapDuration(o);
			remAmt = fcAmt;
			records.setCapExpiry(serial, o, fcDur + System.currentTimeMillis());
		}
		final double earning = Math.min(remAmt, nEarning);
		records.setCapRemAmt(serial, o, remAmt - earning);
		owners.deposit(o, earning);
		tFare += earning;
	}}

	if (!xOwners.isEmpty()) {
	double xEarning = fare / (2 * xOwners.size());
	for (var o : xOwners) {
		final double fcAmt = owners.getFareCapAmt(o);
		if (fcAmt == 0) {
			owners.deposit(o, xEarning);
			icCard.withdraw(xEarning);
			continue;
		}
		double remAmt = records.getCapRemAmt(serial, o);
		final long fcExp = records.getCapExpiry(serial, o);
		if (fcExp < System.currentTimeMillis()) {
			final long fcDur = owners.getFareCapDuration(o);
			remAmt = fcAmt;
			records.setCapExpiry(serial, o, fcDur + System.currentTimeMillis());
		}
		final double earning = Math.min(remAmt, xEarning);
		records.setCapRemAmt(serial, o, remAmt - earning);
		owners.deposit(o, earning);
		tFare += earning;
	}}

	// Set up transfer information
	records.setTimestamp(serial, System.currentTimeMillis());
	records.setPreviousStation(serial, nStation);
	records.setStation(serial, null);
	records.setPreviousFare(serial, fare);

	// Messages and logs
	if (osi) player.sendMessage(lang.getString("osi"));
	if (icCard.withdraw(tFare))
		player.sendRichMessage(IciwiUtil.format("<green>=== Exit ===<br>  <yellow>{entry} → {station}</yellow><br>  <yellow>{value}</yellow><br>  <red>{fare}</red><br>=============</green>", Map.of("entry", nStation,"station", xStation, "value", String.valueOf(icCard.getValue()), "fare", String.valueOf(fare) )));

	finalRailPass = finalRailPass == null ? "" : finalRailPass;
	Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "serial", serial, "value", String.valueOf(value), "nStation", nStation, "xStation", xStation, "osi", String.valueOf(osi), "fare", String.valueOf(tFare), "railPass", finalRailPass);
	logger.info("card-exit", lMap);

	player.playSound(player, plugin.getConfig().getString("exit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
	return true;
}

/**
 Check if a card has a railpass
 @return Whether checks were successful. If false, do not open the fare gate.
 */
public boolean onMember () {
	if (onClick(super.player)) return false;

	// Get the serial number of the card
	String serial = icCard.getSerial();
	String station = super.signInfo.station();

	// Get the owners of the station and the card's rail passes
	List<String> stationOwners = owners.getOwners(station);

	// Get the owners of the card's rail passes
	Set<String> railPasses = icCard.getRailPasses().keySet();

	// Check if the card has a rail pass belonging to the station's operator
	if (railPasses.stream().anyMatch(r -> stationOwners.contains(owners.getRailPassOperator(r)))) {
		player.sendMessage(lang.getString("member-gate"));

		Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "serial", serial, "value", String.valueOf(value), "station", station);
		logger.info("card-member", lMap);

		player.playSound(player, plugin.getConfig().getString("member-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
		return true;
	}
	// If the player does not have such a rail pass, return false
	return false;
}

/**
 Stops and starts a journey without allowing for an OSI
 @return Whether checks were successful. If false, do not open the fare gate.
 */
public boolean onTransfer () {
	if (onClick(super.player)) return false;

	Fares fares = plugin.fares;
	String station = super.signInfo.station();

	// don't parse if there is no serial
	if (serial == null || serial.isEmpty() || serial.isBlank()) return false;

	// If an OSI was detected, cancel OSI capability
//	if (records.getTransfer(serial)) {
//		records.setTransfer(serial, false);
//		player.sendMessage(lang.getString("transfer-cancel-osi"));
//		player.playSound(player, plugin.getConfig().getString("transfer-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);

//		Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "serial", serial, "value", value, "station", station);
//		logger.info("card-transfer", lMap);
//		return true;
//	}

	// Else perform normal onExit, then entry sequence
	String nStation = records.getStation(serial);
	double fare = fares.getCardFare(nStation, station, records.getClass(serial));

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
	List<String> entryStationOwners = owners.getOwners(nStation);
	List<String> exitStationOwners = owners.getOwners(station);
	String finalRailPass = null;
	double payPercentage = 1d;

	// Get cheapest discount
	for (var r : icCard.getRailPasses().keySet()) {
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
	records.setPreviousStation(serial, nStation);
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
	records.setStation(serial, nStation);
	records.setClass(serial, plugin.getConfig().getString("default-class"));

	// player has a transfer discount when they tap out and in within the time limit
	records.setTransfer(serial, System.currentTimeMillis() - records.getTimestamp(serial) < plugin.getConfig().getLong("max-transfer-time"));

	// confirmation
	player.sendMessage(String.format(lang.getString("tapped-out"), nStation, value));

	finalRailPass = finalRailPass == null ? "" : finalRailPass;
	Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "serial", serial, "value", String.valueOf(value), "nStation", nStation, "station", station, "fare", String.valueOf(fare), "railPass", finalRailPass);
	logger.info("card-transfer", lMap);

	player.playSound(player, plugin.getConfig().getString("transfer-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
	return true;
}

}
