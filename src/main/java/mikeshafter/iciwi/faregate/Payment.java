package mikeshafter.iciwi.faregate;
import org.bukkit.SoundCategory;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.block.Sign;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Payment extends FareGate {

	private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
	private final Lang lang = plugin.lang;

	public Payment() {
		super();
		super.setSignLine0(lang.getString("payment"));
	}

	@Override
	public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
		// Get station
		String station = IciwiUtil.stripColor(signText[1]);

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
		}

		// Deposit money into owner's bank account
		var stationOwners = plugin.owners.getOwners(station);
		for (int i = 0; i < stationOwners.size(); i++) plugin.owners.deposit(stationOwners.get(i), price / stationOwners.size());
	}

}
