package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class Transfer extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = new Lang();

public Transfer() {
    super();
    super.setSignLine0(lang.getString("transfer"));
}

@Override
public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
    TicketType ticketType = TicketType.asTicketType(item.getType());
    if (!IciwiUtil.loreCheck(item) || ticketType == null) return;

    // Get station
    String station = IciwiUtil.stripColor(signText[1]);

    // Wax sign
    sign.setWaxed(true);
    sign.update(true);


    switch (ticketType) {
        case TICKET:
        player.sendMessage(lang.getString("tickets-not-valid"));
        break;

        case CARD:
        // Get card from item
        IcCard icCard = IciwiUtil.IcCardFromItem(item);
        if (icCard == null) return;

        // Call transfer, and if successful, open fare gate
        if (CardUtil.transfer(player, icCard, station, sign.getLocation())) super.setCloseGateArray(CardUtil.openGate(lang.getString("transfer"), signText, sign));
        break;

        case RAIL_PASS:
        List<String> lore = IciwiUtil.parseComponents(Objects.requireNonNull(item.getItemMeta().lore()));
        break;
    }
}
}
