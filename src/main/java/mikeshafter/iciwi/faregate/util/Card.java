package mikeshafter.iciwi.faregate.util;

import java.util.*;
import java.util.stream.Stream;
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

public class Card extends PayType {
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Records records = plugin.records;
private final Lang lang = plugin.lang;
private final Owners owners = plugin.owners;
private final LinkedHashSet<Player> clickBuffer = new LinkedHashSet<>();
private final IcLogger logger = plugin.icLogger;

private final IcCard icCard;
private String serial = "";

public Card (Player player, SignInfo info) {
	super(player, info);
	Optional<IcCard> cardOption = IciwiUtil.IcCardFromItem(info.item());
	if (cardOption.isPresent()) {
		this.icCard = cardOption.get();
		this.serial = cardOption.get().getSerial();
	}
	else this.icCard = null;
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

	private record ExitDetails (double fare, String pass) {}

	private boolean handleEntry (String nStation) {
		// write the entry station and fare class
		records.setStation(serial, nStation);
		records.setClass(serial, plugin.getConfig().getString("default-class"));

		// player has a transfer discount when they tap out and in within the time limit
		long previousOutTime = records.getTimestamp(serial);
		records.setTransfer(serial);
		return (System.currentTimeMillis() - previousOutTime) < plugin.getConfig().getLong("max-transfer-time");
}

	/**
	  Register entry from a card
	  @return Whether entry was successful. If false, do not open the fare gate.
	 */
	public boolean onEntry() {
		if (onClick(player)) return false;
		final String nStation = super.signInfo.station();

		// don't parse if there is no serial
		if (serial == null || serial.isEmpty() || serial.isBlank()) return false;

		// reject entry if card has less than the minimum value
		if (this.icCard.getValue() < plugin.getConfig().getDouble("min-amount")) {
			player.sendMessage(lang.getString("value-low"));
			return false;
		}

		// was the card already used to enter the network?
		if (!records.getStation(serial).isEmpty()) {
			if (plugin.getConfig().getBoolean("open-on-penalty")) {
				Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
				player.sendMessage(lang.getString("fare-evade"));
			} else {
				player.sendMessage(lang.getString("cannot-pass"));
				return false;
			}
		}

	boolean osi = handleEntry(nStation);
		// confirmation
		player.sendRichMessage(
				lang.createRichMessage(
					"Entry",
					lang.getString("head-color"),
					lang.getString("body-color"),
					lang.getStringList("entry-message"),
					2,
					Map.of(
						"entry-station", nStation,
						"value", Iciwi.economy.format(this.icCard.getValue()),
						"osi", String.valueOf(osi)
						)
					)
				);

		Map<String, String> lMap = Map.of(
				"player", player.getUniqueId().toString(),
				"serial", serial,
				"value", this.icCard.getValueStr(),
				"nStation", nStation
				);
		logger.info("card-entry", lMap);

		player.playSound(
				player,
				plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"),
				SoundCategory.MASTER,
				1f,
				1f
				);
		return true;
	}

	private ExitDetails handleExit(double fare, String nStation, String xStation) {
		List<String> operators = Stream.concat(owners.getOwners(nStation).stream(), owners.getOwners(xStation).stream()).toList();

		// rail passes
		List<String> validPasses = owners.getRailPassNamesFromList(operators);
		double basePayout = fare / operators.size();
		Map<String, Long> railPasses = this.icCard.getRailPasses();

		// ascending order (first element has smallest percentage)
		double payout = basePayout;
		String pass = "";
		if (railPasses != null && !railPasses.isEmpty()) {
			List<String> sortedPasses = railPasses.keySet().stream().filter(validPasses::contains).sorted(Comparator.comparing(owners::getRailPassPercentage)).toList();

			double finalPercentage = owners.getRailPassPercentage(sortedPasses.getFirst());
			payout = Math.round(basePayout * finalPercentage);
			pass = sortedPasses.getFirst();
		}

		// fare caps
//		player.sendMessage("FARE-"+fare);//todo:debug
//		player.sendMessage("OPERATOR_COUNT-"+operators.size());//todo:debug
//		player.sendMessage("PAYOUT-"+payout);//todo:debug
		final List<Double> finalPayouts = records.deductCaps(this.serial, payout, operators);
		// pay operators
		double total = 0d;
		for (int i = 0; i < operators.size(); ++i) {
			double remapped = finalPayouts.get(i);
//			player.sendMessage(operators.get(i)+"-"+remapped);//todo:debug
			owners.deposit(operators.get(i), remapped);
			total += remapped;
		}

		// Set up transfer information
		records.setTimestamp(serial, System.currentTimeMillis());
		records.setPreviousStation(serial, nStation);
		records.setStation(serial, null);
		records.setPreviousFare(serial, fare);

		return new ExitDetails(total, pass);
	}

	/**
	  Register onExit from a card
	  @return Whether onExit was successful. If false, do not open the fare gate.
	 */
	public boolean onExit() {
		if (onClick(player) || serial == null || serial.isEmpty() || serial.isBlank()) return false;
		if (records.getStation(serial).isEmpty()) {
			if (plugin.getConfig().getBoolean("open-on-penalty")) {
				Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
				player.sendMessage(lang.getString("fare-evade"));
				return true;
			}
			else player.sendMessage(lang.getString("cannot-pass"));
			return false;
		}

		final Fares fares = plugin.fares;
		final String xStation = super.signInfo.station();
		final String nStation = records.getStation(serial);

		final double rFare = (fares.getCardFare( records.getPreviousStation(serial), xStation, records.getClass(serial)) - records.getPreviousFare(serial));
		final double xFare = fares.getCardFare(nStation, xStation, records.getClass(serial));

		final boolean osi = records.getTransfer(serial) && rFare > 0;
		final double fare = osi ? rFare : xFare;
		// =====
		final ExitDetails details = handleExit(fare, nStation, xStation);
		final double total = details.fare;
		final String pass = details.pass;
		// Confirmation
		if (icCard.withdraw(total)) {
			player.sendRichMessage(lang.createRichMessage("Exit", lang.getString("head-color"), lang.getString("body-color"), lang.getStringList("exit-message"), 2, Map.of("entry-station", nStation, "exit-station", xStation, "value", Iciwi.economy.format(this.icCard.getValue()), "fare", String.format("%.2f", total), "rail-pass", pass, "osi", String.valueOf(osi))));
			Map<String, String> lMap = Map.of(
				"player", player.getUniqueId().toString(),
				"serial", serial,
				"value", this.icCard.getValueStr(),
				"nStation", nStation,
				"xStation", xStation,
				"osi", String.valueOf(osi),
				"fare", String.format("%.2f", total),
				"rail-pass", pass
			);
			logger.info("card-exit", lMap);

			player.playSound(player, plugin.getConfig().getString("exit-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
			return true;
		}
		else {
			player.sendMessage("Ur card no has money. Fare is "+String.format("%.2f", total)+", ur card balance is "+icCard.getValueStr());
			return false;
		}

	}

/**
	  Check if a card has a railpass
	  @return Whether checks were successful. If false, do not open the fare gate.
	 */
	public boolean onMember() {
		if (onClick(super.player)) return false;

		// Get the serial number of the card
		String serial = icCard.getSerial();
		String station = super.signInfo.station();

		// Get the owners of the station and the card's rail passes
		List<String> stationOwners = owners.getOwners(station);

		// Get the owners of the card's rail passes
		if (icCard.getRailPasses() == null) {
			return false;
		}
		Set<String> railPasses = icCard.getRailPasses().keySet();

		// Check if the card has a rail pass belonging to the station's operator
		if (railPasses.stream().anyMatch(r -> stationOwners.contains(owners.getRailPassOperator(r)))) {
			player.sendMessage(lang.getString("member-gate"));
			player.sendRichMessage(
					lang.createRichMessage(
						"Member",
						lang.getString("head-color"),
						lang.getString("body-color"),
						lang.getStringList("member-message"),
						2,
						Map.of("station", station)
						)
					);

			Map<String, String> lMap = Map.of(
					"player",
					player.getUniqueId().toString(),
					"serial",
					serial,
					"value",
					this.icCard.getValueStr(),
					"station",
					station
					);
			logger.info("card-member", lMap);

			player.playSound(
					player,
					plugin.getConfig().getString("member-noise", "minecraft:entity.allay.item_thrown"),
					SoundCategory.MASTER,
					1f,
					1f
					);
			return true;
		}
		// If the player does not have such a rail pass, return false
		return false;
	}

	/**
	  Stops and starts a journey without allowing for an OSI
	  @return Whether checks were successful. If false, do not open the fare gate.
	 */
	public boolean onTransfer() {
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

		// reject entry if card has less than the minimum value
		if (this.icCard.getValue() < plugin.getConfig().getDouble("min-amount")) return false;
		// is the card not in the network?
		if (records.getStation(serial).isEmpty()) {
			if (plugin.getConfig().getBoolean("open-on-penalty")) {
				Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
				player.sendMessage(lang.getString("fare-evade"));
			} else {
				player.sendMessage(lang.getString("cannot-pass"));
				return false;
			}
		}

		final double fare = (fares.getCardFare(nStation, station, records.getClass(serial)));
		final ExitDetails details = handleExit(fare, nStation, station);

		// Perform entry sequence
		boolean osi = handleEntry(station);
		final double total = details.fare;
		final String pass = details.pass;

		// confirmation
		player.sendRichMessage(
				lang.createRichMessage(
					"Transfer",
					lang.getString("head-color"),
					lang.getString("body-color"),
					lang.getStringList("transfer-message"),
					2,
					Map.of(
						"entry-station", nStation,
						"transfer-station", station,
						"value", Iciwi.economy.format(this.icCard.getValue()),
						"fare", Iciwi.economy.format(total),
						"railPass", pass,
						"osi", String.valueOf(osi)
						)
					)
				);

		Map<String, String> lMap = Map.of(
				"player", player.getUniqueId().toString(),
				"serial", serial,
				"value", this.icCard.getValueStr(),
				"nStation", nStation,
				"station", station,
				"fare", String.format("%.2f", total),
				"railPass", pass
				);
		logger.info("card-transfer", lMap);

		player.playSound(
				player,
				plugin.getConfig().getString("transfer-noise", "minecraft:block.amethyst_block.step"),
				SoundCategory.MASTER,
				1f,
				1f
				);
		return true;
	}
}
