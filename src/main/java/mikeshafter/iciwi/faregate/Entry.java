package mikeshafter.iciwi.faregate;
import org.bukkit.SoundCategory;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.IciwiUtil;
import mikeshafter.iciwi.config.Owners;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class Entry extends ClosableFareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = new Lang();
private final Owners owners = new Owners();
private static final CardSql cardSql = new CardSql();

public Entry() {
    super();
    super.setSignLine0(lang.getString("entry"));
}

@Override public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
    if (!IciwiUtil.loreCheck(item)) return;

    // Get station
    String station = IciwiUtil.stripColor(signText[1]);

    // Wax sign
    sign.setWaxed(true);
    sign.update(true);

    List<String> lore = IciwiUtil.parseComponents(Objects.requireNonNull(item.getItemMeta().lore()));

    switch (IciwiUtil.getTicketType(item)) {
        case TICKET:
            boolean entryPunched = lore.get(0).contains("•");
            boolean exitPunched	= lore.get(1).contains("•");

            // Invalid Ticket
            if (entryPunched && exitPunched) {
                player.sendMessage(lang.getString("invalid-ticket"));
            }

            // Ticket already used
            else if (entryPunched) {
                player.sendMessage(lang.getString("cannot-pass"));
                if (plugin.getConfig().getBoolean("open-on-penalty")) {
                    Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
                    player.sendMessage(lang.getString("fare-evade"));
                }
            }

            // Entry
            else if (lore.get(0).equals(station) || owners.getOwners(station).contains(lore.get(0).replaceFirst("C:", ""))) {
                IciwiUtil.punchTicket(item, 0);
                player.sendMessage(String.format(lang.getString("ticket-in"), station));
                // Log entry

                cardSql.logMaster(player.getUniqueId().toString());
                cardSql.logEntry(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), station);
                player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
                super.setCloseGateArray(CardUtil.openGate(lang.getString("entry"), signText, sign));
            }

            else {
                player.sendMessage(lang.getString("invalid-ticket"));
            }
            break;

        case CARD:
            // Get card from item
            IcCard icCard = IciwiUtil.IcCardFromItem(item);
            if (icCard == null) return;

            // Call entry, and if successful, open fare gate
            if (CardUtil.entry(player, icCard, station, sign.getLocation())) {
                super.setCloseGateArray(CardUtil.openGate(lang.getString("entry"), signText, sign));
            }
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
                player.playSound(player, plugin.getConfig().getString("entry-noise", "minecraft:entity.allay.item_thrown"), SoundCategory.MASTER, 1f, 1f);
                super.setCloseGateArray(CardUtil.openGate(lang.getString("faregate"), signText, sign));
            }
    }
}
}

