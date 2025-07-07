package mikeshafter.iciwi.faregate;
import mikeshafter.iciwi.api.SignInfo;
import org.bukkit.SoundCategory;

import net.kyori.adventure.text.Component;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Payment extends FareGate {

	private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
	private final Lang lang = plugin.lang;

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

		// Wax sign
		sign.setWaxed(true);
		sign.update(true);

		// Get price
		double price = Double.parseDouble(IciwiUtil.stripColor(signText[2]));

		// Pay
		Material cardMaterial = Material.valueOf(plugin.getConfig().getString("card.material"));
		if (item.getType() == cardMaterial && IciwiUtil.loreCheck(item)) {

			// Try paying with card
			IcCard icCard = IciwiUtil.IcCardFromItem(item);
			if (icCard != null && icCard.withdraw(price)) {
				player.sendMessage(String.format(lang.getString("pay-success-card"), price, icCard.getValue()));
			}

			// If there is no card, pay with cash
			else {
				Iciwi.economy.withdrawPlayer(player, price);
				player.sendMessage(lang.getString("cash-divert"));
				player.sendMessage(String.format(lang.getString("pay-success"), price));
			}
			player.playSound(player, plugin.getConfig().getString("payment-noise", "minecraft:block.amethyst_block.step"), SoundCategory.MASTER, 1f, 1f);
			// Receipt
			player.getInventory().addItem(IciwiUtil.makeItem(Material.BOOK, 0, Component.text("Receipt"), Component.text("Total: "+String.valueOf(price)) ));
		}

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
