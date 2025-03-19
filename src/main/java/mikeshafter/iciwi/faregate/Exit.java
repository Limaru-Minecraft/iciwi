package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.faregate.util.RailPass;
import mikeshafter.iciwi.faregate.util.Ticket;
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
import java.util.Map;

public class Exit extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private final Owners owners = plugin.owners;
private static final CardSql cardSql = new CardSql();

public Exit () {
	super("onExit");
}

@Override
public void onTicket (Player player, SignInfo info) {
	Ticket ticket = new Ticket(player, info);
	if (ticket.onExit()) {
		super.setCloseGateArray(super.openGate());
	}
}

@Override
public void onCard (Player player, SignInfo info) {

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
		return;
	}

	// Prepare each company's earnings
	// TODO: Case where owners are not found
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
		player.sendRichMessage(IciwiUtil.format("<green>=== Exit ===<br>  <yellow>{entry} → {station}</yellow><br>  <yellow>{value}</yellow><br>  <red>{fare}</red><br>=============</green>", Map.of("entry", nStation,"station", station, "value", String.valueOf(icCard.getValue()), "fare", String.valueOf(fare) )));
		// player.sendMessage(String.format(lang.getString("tapped-out"), station, fare, icCard.getValue()));

	//TODO: Logger

	// Finally onExit
	player.playSound(player, plugin.getConfig().getString("onExit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
	super.setCloseGateArray(super.openGate());
}

@Override
public void onRailPass (Player player, SignInfo info) {
	RailPass railPass = new RailPass(player, info);
	if (railPass.onExit()) {
		super.setCloseGateArray(super.openGate());
	}
}
}
