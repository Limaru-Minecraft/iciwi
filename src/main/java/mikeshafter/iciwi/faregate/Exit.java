package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class Exit extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;

	public Exit() {
		super("");
		super.setSignLine0(lang.getString("exit"));
	}

  @Override
  public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
    // Get station
    String station = ChatColor.stripColor(signText[1]);

    // Paper ticket
    if (item.getType() == Material.valueOf(plugin.getConfig().getString("ticket.material")) && IciwiUtil.loreCheck(item)) {
      List<String> lore    = IciwiUtil.parseComponents(Objects.requireNonNull(item.getItemMeta().lore()));
      boolean entryPunched = lore.get(0).contains("•");
      boolean exitPunched  = lore.get(1).contains("•");

      // Invalid Ticket
      if (entryPunched && exitPunched) {
        player.sendMessage(lang.getString("invalid-ticket"));
      }

      // Exit
      else if (entryPunched && lore.get(1).equals(station)) {
        IciwiUtil.punchTicket(item, 1);
        player.sendMessage(String.format(lang.getString("ticket-out"), station));
        GateUtil.openGate(signText, sign);
      }

      // Ticket not used
      else if (!entryPunched) {
        player.sendMessage(lang.getString("cannot-pass"));
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

      // Call entry, and if successful, open fare gate
      if (CardUtil.exit(player, icCard, station)) GateUtil.openGate(signText, sign);

    }
  }
	@Override public void onPlayerInFareGate (Player player) {

	}

}
