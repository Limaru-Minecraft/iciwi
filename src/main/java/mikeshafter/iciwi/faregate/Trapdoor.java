package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.api.ClosableFareGate;
import mikeshafter.iciwi.api.IcCard;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.*;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import mikeshafter.iciwi.util.IciwiUtil;

public class Trapdoor extends ClosableFareGate {

    private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
    private final Lang lang = new Lang();
    private final Owners owners = new Owners();
    private static final CardSql cardSql = new CardSql();

    public Trapdoor() {
        super(new Vector(0, -2, 0));
        super.setSignLine0(lang.getString("faregate"));
    }

    @Override public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
        if (!IciwiUtil.loreCheck(item)) return;

        // Get station
        String station = IciwiUtil.stripColor(signText[1]);

        // Wax sign
        sign.setWaxed(true);
        sign.update(true);

        // Force fare gate
        signText[0] = signText[0] + "F";
        List<String> lore = IciwiUtil.parseComponents(Objects.requireNonNull(item.getItemMeta().lore()));

        switch (item.getType()) {
            case PAPER:
                boolean entryPunched = lore.get(0).contains("•");
                boolean exitPunched	= lore.get(1).contains("•");
                boolean entryPunchRequired = plugin.getConfig().getBoolean("require-entry-punch");

                // Invalid Ticket
                if (entryPunched && exitPunched) {
                    player.sendMessage(lang.getString("invalid-ticket"));
                }

                // Exit
                else if ((entryPunched || !entryPunchRequired) && (lore.get(1).equals(station) || owners.getOwners(station).contains(lore.get(1).replaceFirst("C:", "")))) {
                    IciwiUtil.punchTicket(item, 1);
                    // Log exit
                    String entryStation = lore.get(0).replace(" •", "");
                    String fareClass = lore.get(2);
                    Fares fares = new Fares();

                    cardSql.logMaster(player.getUniqueId().toString());
                    cardSql.logExit(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), entryStation, station);
                    cardSql.logJourney(fares.getFare(entryStation, station, fareClass), fares.getFare(entryStation, station, fareClass), fareClass);
                    cardSql.logTicketUse(entryStation, station, fareClass);
                    player.sendMessage(String.format(lang.getString("ticket-out"), station));
                    super.setCloseGateArray(CardUtil.openGate(lang.getString("faregate"), signText, sign));
                }

                // Entry
                else if (lore.get(0).equals(station) || owners.getOwners(station).contains(lore.get(0).replaceFirst("C:", ""))) {
                    IciwiUtil.punchTicket(item, 0);
                    // Log entry

                    cardSql.logMaster(player.getUniqueId().toString());
                    cardSql.logEntry(sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ(), station);
                    player.sendMessage(String.format(lang.getString("ticket-in"), station));
                    super.setCloseGateArray(CardUtil.openGate(lang.getString("faregate"), signText, sign));
                }

                else {
                    player.sendMessage(lang.getString("invalid-ticket"));
                }
                break;

            case NAME_TAG:
                // Get card from item
                IcCard icCard = IciwiUtil.IcCardFromItem(item);
                if (icCard == null) return;

                // Vital information
                String serial = icCard.getSerial();
                Records records = new Records();

                // Determine entry or exit
                if (records.getStation(serial).isEmpty()) CardUtil.entry(player, icCard, station, sign.getLocation());
                else CardUtil.exit(player, icCard, station, sign.getLocation());

                // Open the fare gate in both cases
                super.setCloseGateArray(CardUtil.openGate(lang.getString("faregate"), signText, sign));
                break;


            case FILLED_MAP:
                String name = lore.get(0);
                String expiry = lore.get(1);

                try {
                // check if expired
                    long e = Long.parseLong(expiry);
                // if expired, return and do not open the gate
                    if (e < System.currentTimeMillis()) {
                        return;
                    }
                // otherwise, check if issuing TOC is one of the station's owners
                    List<String> tocs = owners.getOwners(station);
                    if (tocs.contains(owners.getRailPassOperator(name))) {
                // if yes, open the gate
                        super.setCloseGateArray(CardUtil.openGate(lang.getString("faregate"), signText, sign));
                    }
                }
                catch (Exception ignored) {
                    return;
                }
                break;
        }
    }

}
