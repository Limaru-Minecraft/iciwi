package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class Transfer extends ClosableFareGate {

private final Lang lang = new Lang();
private final Owners owners = new Owners();
private static final CardSql cardSql = new CardSql();


public Transfer() {
    super();
    super.setSignLine0(lang.getString("transfer"));
}

@Override
public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
    if (!IciwiUtil.loreCheck(item)) return;

    // Get station
    String station = IciwiUtil.stripColor(signText[1]);

    // Wax sign
    sign.setWaxed(true);
    sign.update(true);

    List<String> lore = IciwiUtil.parseComponents(Objects.requireNonNull(item.getItemMeta().lore()));

    switch (IciwiUtil.getTicketType(item)) {
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
            String name = lore.get(0);
            String expiry = lore.get(1);

            // check if expired
            long e = Long.parseLong(expiry);
            // if expired, return and do not open the gate
            if (e < System.currentTimeMillis()) return;
            // otherwise, check if issuing TOC is one of the station's owners
            List<String> tocs = owners.getOwners(station);
            if (tocs.contains(owners.getRailPassOperator(name))) {
                // log
                cardSql.logMaster(player.getUniqueId().toString());
                cardSql.logFreePass(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), station, "transfer");
                cardSql.logRailpassUse(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), e - owners.getRailPassDuration(name), owners.getRailPassDuration(name), owners.getRailPassOperator(name));

                super.setCloseGateArray(CardUtil.openGate(lang.getString("faregate"), signText, sign));
            }
    }
}
}
