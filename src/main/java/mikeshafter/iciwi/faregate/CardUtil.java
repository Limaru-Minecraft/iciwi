package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.config.*;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.IcCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import mikeshafter.iciwi.util.IciwiUtil;


public class CardUtil {
  private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private static final Records records = plugin.records;
  private static final Lang lang = plugin.lang;
  private static final Fares fares = plugin.fares;
  private static final Owners owners = plugin.owners;
  private static final CardSql cardSql = new CardSql();

  /**
   * Register entry from a card
   * @param player Player who used the card
   * @param icCard The card to register
   * @param entryStation The station at which to enter
   * @return Whether entry was successful. If false, do not open the fare gate.
   */
  protected static boolean entry(Player player, IcCard icCard, String entryStation) {
    double value = icCard.getValue();
    String serial = icCard.getSerial();

    // reject entry if card has less than the minimum value
    if (value < plugin.getConfig().getDouble("min-amount")) return false;

    // was the card already used to enter the network?
		if (records.getString("station."+serial) == null) {
			player.sendMessage(lang.getString("cannot-pass"));
			if (plugin.getConfig().getBoolean("open-on-penalty")) {
				Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
				player.sendMessage(lang.getString("fare-evade"));
			} else return false;
		}

    // write the entry station to records
    records.set("station." + serial, entryStation);

    // check whether the player tapped out and in within the time limit
    if (System.currentTimeMillis() - records.getLong("timestamp." + serial) < plugin.getConfig().getLong("max-transfer-time"))
      records.set("has-transfer." + serial, entryStation);
    
    // confirmation
    player.sendMessage(String.format(lang.getString("tapped-in"), entryStation, value));
    return true;
  }

	/**
	* Register exit from a card
	* @param player Player who used the card
	* @param icCard The card to register
	* @param exitStation The station at which to exit
	* @return Whether exit was successful. If false, do not open the fare gate.
	*/
	protected static boolean exit(Player player, IcCard icCard, String exitStation) {
		String serial = icCard.getSerial();
		String entryStation = records.getString("station."+serial);
		double value = icCard.getValue();
		double fare = fares.getFare(entryStation, exitStation, plugin.getConfig().getString("default-class") /* TODO: Change this to use actual fare classes */);

		// is the card already in the network?
		if (records.getString("station."+serial) == null) {
			player.sendMessage(lang.getString("cannot-pass"));
			if (plugin.getConfig().getBoolean("open-on-penalty")) {
				Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
				player.sendMessage(lang.getString("fare-evade"));
			} else return false;
		}

		// Get the owners of stations and rail passes
		List<String> entryStationOwners = owners.getOwners(entryStation);
		List<String> exitStationOwners  = owners.getOwners(exitStation);
		Set<String> railPasses = cardSql.getAllDiscounts(serial).keySet();
		List<String> railPassOwners = new ArrayList<>();
		railPasses.forEach(r -> railPassOwners.add(owners.getRailPassOperator(r)));

		if (IciwiUtil.any(railPassOwners, entryStationOwners) && IciwiUtil.any(railPassOwners, exitStationOwners)) {
			// Get cheapest discount
			double payPercentage = 1d;
			for (var r: railPasses) {
				if (payPercentage > owners.getRailPassPercentage(r)) payPercentage = owners.getRailPassPercentage(r);
			}

		// Set final fare
		fare *= payPercentage;
		}

		if (value < fare) {
			player.sendMessage(lang.getString("value-low"));
			return false;
		}

		icCard.withdraw(fare);
		return true;
  }
}
