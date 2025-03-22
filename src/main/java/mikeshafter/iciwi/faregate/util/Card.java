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
 @return true if the player has clicked within the last 10 ticks, false otherwise
 */
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
    player.sendRichMessage(IciwiUtil.format("<green>=== Entry ===<br>  <yellow>{station}</yellow><br>  <yellow>{value}</yellow><br>=============</green>", Map.of("station", super.signInfo.station(), "value", String.valueOf(value))));

	// TODO: logger

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
	List<String> nOwners = owners.getOwners(nStation);
	List<String> xOwners = owners.getOwners(xStation);
	HashSet<String> railPasses = new HashSet<>();
	for (String o : nOwners) railPasses.addAll(owners.getRailPassNames(o));
	for (String o : xOwners) railPasses.addAll(owners.getRailPassNames(o));
	railPasses.retainAll(cardSql.getAllDiscounts(serial).keySet());
	double pp = 1f;
	String finalRailPass = null;
	if (!railPasses.isEmpty()) {
		finalRailPass = Collections.min(railPasses, (r, s) -> Double.compare(owners.getRailPassPercentage(r), owners.getRailPassPercentage(s)));
		pp = owners.getRailPassPercentage(finalRailPass);
	}

	// Set final base fare
	fare *= pp;

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

	// TODO: Logger

	player.playSound(player, plugin.getConfig().getString("onExit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
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
	Set<String> railPasses = cardSql.getAllDiscounts(serial).keySet();

	// Check if the card has a rail pass belonging to the station's operator
	if (railPasses.stream().anyMatch(r -> stationOwners.contains(owners.getRailPassOperator(r)))) {
		player.sendMessage(lang.getString("onMember-gate"));

		// TODO: Logger

		player.playSound(player, plugin.getConfig().getString("onMember-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
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
