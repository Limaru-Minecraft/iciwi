package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcCard;
import org.bukkit.block.Sign;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.*;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class Validator extends FareGate {

	private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
	private final Lang lang = new Lang(); 

	public Validator() {
		super();
		super.setSignLine0(lang.getString("validator"));
	}

	@Override
	public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
		// Get station
		String station = IciwiUtil.stripColor(signText[1]);

		// Wax sign
		sign.setWaxed(true);
		sign.update(true);

		// Paper ticket
		if (item.getType() == Material.valueOf(plugin.getConfig().getString("ticket.material")) && IciwiUtil.loreCheck(item)) {
			List<String> lore = IciwiUtil.parseComponents(Objects.requireNonNull(item.getItemMeta().lore()));
			boolean entryPunched = lore.get(0).contains("•");
			boolean exitPunched	= lore.get(1).contains("•");

			// Invalid Ticket
			if (entryPunched && exitPunched) {
				player.sendMessage(lang.getString("invalid-ticket"));
			}

			// Exit
			else if (entryPunched && lore.get(1).equals(station)) {
				IciwiUtil.punchTicket(item, 1);
				player.sendMessage(String.format(lang.getString("ticket-out"), station));
			}

			// Entry
			else if (lore.get(0).equals(station)) {
				IciwiUtil.punchTicket(item, 0);
				player.sendMessage(String.format(lang.getString("ticket-in"), station));
			}

			else {
				player.sendMessage(lang.getString("invalid-ticket"));
			}
		}


		// Card
		else if (item.getType() == Material.valueOf(plugin.getConfig().getString("card.material")) && IciwiUtil.loreCheck(item)) {

			// Get card from item
			IcCard icCard = IciwiUtil.IcCardFromItem(item);
			if (icCard == null) return;

			// Vital information
			String serial = icCard.getSerial();
			Records records = new Records();

			// Determine entry or exit
			if (records.getStation(serial).equals("")) CardUtil.entry(player, icCard, station, sign.getLocation().toVector());
			else CardUtil.exit(player, icCard, station, sign.getLocation().toVector());
		}
	}

}
