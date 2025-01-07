package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.config.Records;
import org.bukkit.SoundCategory;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Exit extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private final Owners owners = plugin.owners;
private static final CardSql cardSql = new CardSql();

public Exit () {
	super("exit");
}

@Override
public void onTicket (Player player, SignInfo info) {
	var lore = info.lore();
	var sign = info.sign();

	String station = info.station();
	boolean entryPunched = lore.get(0).contains("•");
	boolean exitPunched = lore.get(1).contains("•");
	boolean entryPunchRequired = plugin.getConfig().getBoolean("require-entry-punch");

	// Invalid Ticket
	if (entryPunched && exitPunched) {
		player.sendMessage(lang.getString("invalid-ticket"));
	}

	// Exit
	else if ((entryPunched || !entryPunchRequired) && (lore.get(1).equals(station) || owners.getOwners(station).contains(lore.get(1).replaceFirst("C:", "")))) {
		IciwiUtil.punchTicket(info.item(), 1);
		player.sendMessage(String.format(lang.getString("ticket-out"), station));
		// Log exit
		String entryStation = lore.get(0).replace(" •", "");
		String fareClass = lore.get(2);
		Fares fares = plugin.fares;

		cardSql.logMaster(player.getUniqueId().toString());
		cardSql.logExit(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), entryStation, station);
		cardSql.logJourney(fares.getFare(entryStation, station, fareClass), fares.getFare(entryStation, station, fareClass), fareClass);
		cardSql.logTicketUse(entryStation, station, fareClass);
		player.playSound(player, plugin.getConfig().getString("exit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
		super.setCloseGateArray(super.openGate());
	}

	// Ticket not used
	else if (!entryPunched && entryPunchRequired) {
		player.sendMessage(lang.getString("cannot-pass"));
	}

	else {
		player.sendMessage(lang.getString("invalid-ticket"));
	}
}

@Override
public void onCard (Player player, SignInfo info) {
	var sign = info.sign();

	// Get card from item
	IcCard icCard = IciwiUtil.IcCardFromItem(info.item());
	if (icCard == null) return;
	String serial = icCard.getSerial();
	final Records records = plugin.records;
	final Fares fares = plugin.fares;
	final String nStation = records.getStation(serial);
	final String station = info.station();

	// is the card not in the network?
	if (records.getStation(serial).isEmpty()) {
		if (plugin.getConfig().getBoolean("open-on-penalty")) {
			Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
			player.sendMessage(lang.getString("fare-evade"));
			super.setCloseGateArray(super.openGate());
		}
		else {
			player.sendMessage(lang.getString("cannot-pass"));
		}
		return;
	}

	// Calculate base fare
	double fare = fares.getCardFare(records.getStation(serial), station, records.getClass(serial));

	// Use transfer fare if applicable
	boolean osi = false;
	if (records.getTransfer(serial)) {
		double pFare = records.getPreviousFare(serial);
		double tFare = fares.getCardFare(records.getPreviousStation(serial), station, records.getClass(serial));
		fare = (tFare == 0d || tFare - pFare > fare) ? fare : Math.max(tFare - pFare, 0d);
		osi = true;
	}

	// Get cheapest rail pass
	List<String> nOwners = owners.getOwners(nStation);
	List<String> xOwners = owners.getOwners(station);
	HashSet<String> railPasses = new HashSet<>();
	for (String o : nOwners) railPasses.addAll(owners.getRailPassNames(o));
	for (String o : xOwners) railPasses.addAll(owners.getRailPassNames(o));
	railPasses.retainAll(cardSql.getAllDiscounts(serial).keySet());
	String finalRailPass = Collections.min(railPasses, (r, s) -> Double.compare(owners.getRailPassPercentage(r), owners.getRailPassPercentage(s)));
	double pp = owners.getRailPassPercentage(finalRailPass);

	// Set final base fare
	fare *= pp;

	if (icCard.getValue() < fare) {
		player.sendMessage(lang.getString("value-low"));
		return;
	}

	// Prepare each company's earnings
	double nEarning = fare / (2 * nOwners.size());
	double xEarning = fare / (2 * xOwners.size());

	// Handle fare caps and earnings
	double tFare = 0d;
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
	}

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
	}

	// Set up transfer information
	records.setTimestamp(serial, System.currentTimeMillis());
	records.setPreviousStation(serial, nStation);
	records.setStation(serial, null);
	records.setPreviousFare(serial, fare);

	// Messages and logs
	if (osi) player.sendMessage(lang.getString("osi"));
	if (icCard.withdraw(tFare))
		player.sendMessage(String.format(lang.getString("tapped-out"), station, fare, icCard.getValue()));

	// Logger
	cardSql.logMaster(player.getUniqueId().toString());
	cardSql.logExit(sign.getX(), sign.getY(), sign.getZ(), nStation, station);
	cardSql.logJourney(fares.getCardFare(nStation, station, records.getClass(serial)), fare, records.getClass(serial));
	cardSql.logCardUse(serial);
	icCard.getRailPasses().forEach((name, start) -> cardSql.logRailpassStore(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), start, owners.getRailPassDuration(name), owners.getRailPassOperator(name)));
	if (finalRailPass != null) {
		cardSql.logRailpassUse(finalRailPass, owners.getRailPassPrice(finalRailPass), owners.getRailPassPercentage(finalRailPass), cardSql.getStart(serial, finalRailPass), owners.getRailPassDuration(finalRailPass), owners.getRailPassOperator(finalRailPass));
	}

	// Finally exit
	player.playSound(player, plugin.getConfig().getString("exit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
	super.setCloseGateArray(super.openGate());
}

@Override
public void onRailPass (Player player, SignInfo info) {
	var lore = info.lore();
	var sign = info.sign();

	String station = info.station();
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
		player.playSound(player, plugin.getConfig().getString("exit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
		super.setCloseGateArray(super.openGate());
	}
}
}
