package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.*;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import mikeshafter.iciwi.util.IciwiUtil;

public class Trapdoor extends ClosableFareGate {

	private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
	private final Lang lang = new Lang();

	public Trapdoor() {
		super(new Vector(0, -2, 0));
		super.setSignLine0(lang.getString("faregate"));
	}

	@Override
	public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
		// Get station
		String station = IciwiUtil.stripColor(signText[1]);

		// Wax sign
		sign.setWaxed(true);
		sign.update(true);

		// Force fare gate
		signText[0] = signText[0] + "F";

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
				super.setCloseGateArray(CardUtil.openGate(lang.getString("faregate"), signText, sign));
			}

			// Entry
			else if (lore.get(0).equals(station)) {
				IciwiUtil.punchTicket(item, 0);
				player.sendMessage(String.format(lang.getString("ticket-in"), station));
				super.setCloseGateArray(CardUtil.openGate(lang.getString("faregate"), signText, sign));
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

			// Open the fare gate in both cases
			super.setCloseGateArray(CardUtil.openGate(lang.getString("faregate"), signText, sign));
		}
	}

}
