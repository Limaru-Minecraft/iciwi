package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.IcLogger;
import mikeshafter.iciwi.api.SignInfo;
import org.bukkit.SoundCategory;

import net.kyori.adventure.text.Component;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.Optional;

public class Payment extends FareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private final IcLogger logger = plugin.icLogger;

public Payment() {
		super("payment");
	}

	@Override
	public void onInteract(Player player, SignInfo info) {
		var signText = info.signText();
		var sign = info.sign();
		var item = info.item();
		// Get station
		String station = info.station();
		String logSerial = "null";

		// Wax sign
		sign.setWaxed(true);
		sign.update(true);

		// Get price
		double price = Double.parseDouble(IciwiUtil.stripColor(signText[2]));

		boolean cashDivert;
		boolean payByCash;

		// Pay
		Material cardMaterial = Material.valueOf(plugin.getConfig().getString("card.material"));

		/**
 		* TO-DO: if pay by hand it can't check for item's lore
 		*/
		if (item == null || item.getType() == Material.AIR) { // if pay by hand
    		Iciwi.economy.withdrawPlayer(player, price);
			cashDivert = false;
			payByCash = true;

		} else if (item.getType() == cardMaterial && IciwiUtil.loreCheck(item)) { // if tap card

			// Try paying with card
			Optional<IcCard> cardOpt = IciwiUtil.IcCardFromItem(item);

			if (cardOpt.isPresent() && cardOpt.get().withdraw(price)) {
				cashDivert = false;
				payByCash = false;
				logSerial = cardOpt.get().getSerial();
			}
			// If there is no card, pay with cash
			else {
				Iciwi.economy.withdrawPlayer(player, price);
				cashDivert = true;
				payByCash = true;
			}

		} else { // else pay by cash
			Iciwi.economy.withdrawPlayer(player, price);
			cashDivert = false;
			payByCash = true;
		}

		player.sendRichMessage(lang.createRichMessage(
				"Payment",
				lang.getString("head-color"),
				lang.getString("body-color"),
				lang.getStringList("payment-message"),
				2,
				Map.of("station", station, "fare", Iciwi.economy.format(price), "cash-divert", String.valueOf(cashDivert))
			));
		player.playSound(player, plugin.getConfig().getString("payment-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);

		// Receipt if pay by cash
		if (payByCash) player.getInventory().addItem(IciwiUtil.makeItem(Material.FILLED_MAP, 0, Component.text("§7Receipt"), Component.text("Ticket/Receipt"), Component.text("Location: " + station), Component.text("Fare: " + price) ));

		// logger
		Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "price", String.valueOf(price), "station", station, "serial", logSerial);
		logger.info("payment", lMap);

		// Deposit money into owner's bank account
		var stationOwners = plugin.owners.getOwners(station);
		for (int i = 0; i < stationOwners.size(); i++) plugin.owners.deposit(stationOwners.get(i), price / stationOwners.size());
	}

@Override
public void onTicket (Player player, SignInfo info) {}

@Override
public void onCard (Player player, SignInfo info) {}

@Override
public void onRailPass (Player player, SignInfo info) {}

}
