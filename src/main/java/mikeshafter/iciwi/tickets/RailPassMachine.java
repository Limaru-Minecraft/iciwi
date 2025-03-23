package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.Clickable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.lang.Runnable;
import static mikeshafter.iciwi.util.IciwiUtil.*;

public class RailPassMachine implements Machine {

    // Attributes
    private Inventory inv;
    private Clickable[] clickables;
    private ItemStack selectedItem;
    private List<String> operators;
    private final Player player;
    private boolean bottomInv;

    // Constant helper classes
    private final CardSql cardSql = new CardSql();
    private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
    private final Owners owners = plugin.owners;
    private final Lang lang = plugin.lang;
    private Runnable clickInvItemRunnable;

    public RailPassMachine (Player player) { this.player = player; }
    public RailPassMachine (Player player, List<String> operators) {
        this.player = player;
        this.operators = operators;
    }

    @Override public Runnable getClickInvItemRunnable () { return this.clickInvItemRunnable; }

    public void init (String station) {
        // Set the operators
        this.operators = this.owners.getOwners(station);

        // setup inventory
        inv = this.plugin.getServer().createInventory(null, 9, this.lang.getComponent("ticket-machine"));
        this.clickables = new Clickable[9];

        // Create buttons
        this.clickables[2] = Clickable.of(
            makeItem(Material.PAPER, 0, this.lang.getComponent("menu-new-paper-pass"), Component.text("Buy a paper rail pass")),
            (event) -> paperPass()
        );
        this.clickables[6] = Clickable.of(
            makeItem(Material.NAME_TAG, 0, this.lang.getComponent("menu-insert-card"), Component.text("Apply a rail pass onto your card")),
            (event) -> selectCard()
        );

        setItems(clickables, inv);
        player.openInventory(inv);
    }

    // card selection menu. player clicks in their own inventory to select a card
    public void selectCard () {
        // Setup listener for bottom inventory selection
        // Create inventory
        this.inv = this.plugin.getServer().createInventory(null, 9, this.lang.getComponent("select-card"));
        // Swap flag
        this.bottomInv = true;
        // Set runnable after clicking the card
        this.clickInvItemRunnable = () -> railPass(this.selectedItem);
        // Start listening and open inventory
        this.player.openInventory(this.inv);
    }

    public void paperPass () {
        // get available railpasses
        List<String> railPassNames = new ArrayList<>();
        for (String railPassName : this.owners.getRailPassNamesFromList(this.operators)) {
            if (this.owners.getRailPassPercentage(railPassName) == 0d) {
                railPassNames.add(railPassName);
            }
        }
        
        int invSize = (railPassNames.size() / 9 + 1) * 9;
        this.inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
        clickables = new Clickable[invSize];

        // create all rail pass buttons
        for (int i = 0; i < railPassNames.size(); i++) {
            String rpName = railPassNames.get(i);
            clickables[i] = Clickable.of(
                makeItem(Material.LIME_STAINED_GLASS_PANE, 0, Component.text(rpName), Component.text(this.owners.getRailPassPrice(rpName))),
                (_) -> {
                double price = this.owners.getRailPassPrice(rpName);

                if (Iciwi.economy.getBalance(this.player) >= price) {
                    // take money from player
                    Iciwi.economy.withdrawPlayer(this.player, price);

                    // generate the rail pass paper ticket item
                    Material material = Material.valueOf(this.plugin.getConfig().getString("railpass.material"));
                    int customModelData = this.plugin.getConfig().getInt("railpass.custom-model-data");
                    long time = System.currentTimeMillis();

                    /*
                    Rail Pass
                    <name>
                    <expiry>
                     */
                    ItemStack item = makeItem(material, customModelData, lang.getComponent("paper-rail-pass"), Component.text(rpName), Component.text(String.valueOf(time + this.owners.getRailPassDuration(rpName))));

                    // give it to the player
                    this.player.getInventory().addItem(item);

                    // pay the TOC
                    this.owners.deposit(this.owners.getRailPassOperator(rpName), price);

                    // todo: log the transaction
                }
                else this.player.sendMessage(this.lang.getString("not-enough-money"));

                // close inventory
                this.player.closeInventory();SignInteractListener.removeMachine(player);
            });
        }
        setItems(clickables, inv);
        this.player.openInventory(this.inv);
    }

    // rail pass menu
    public void railPass (ItemStack item) {
        if (!loreCheck(item)) return;

        // get available railpasses
        List<String> railPassNames = new ArrayList<>(this.owners.getRailPassNamesFromList(this.operators));

        int invSize = (railPassNames.size() / 9 + 1) * 9;
        this.inv = this.plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
        this.clickables = new Clickable[invSize];

        // get serial number
        IcCard icCard = IcCardFromItem(item);
        if (icCard == null) {
            this.player.closeInventory();SignInteractListener.removeMachine(player);
            return;
        }
        String serial = icCard.getSerial();

        // rail pass viewer
        this.clickables[0] = Clickable.of(makeItem(Material.WHITE_STAINED_GLASS_PANE, 0, Component.text("View Rail Passes")), (event) -> {
            // print current rail passes
            // get current passes
            event.setCancelled(true);
            List<TextComponent> discountList = icCard.getRailPasses().entrySet().stream().sorted(Map.Entry.comparingByValue()).map(railPass -> Component.text().content(
                // Show expiry date
                "§6- §a" + railPass.getKey() + "§6 | Exp. " + String.format("§b%s\n", new Date(railPass.getValue() * 1000)))
                // Option to extend (currently disabled)
                // .append(Component.text().content("§6 | Extend
                // §a")).clickEvent(ClickEvent.runCommand("/iciwi railpass "+serial+"
                // "+railPass.getKey()))
                .build()).toList();
            // menu title
            TextComponent menu = Component.text().content("==== Rail Passes You Own ====\n").color(NamedTextColor.GOLD).build();
            // build content
            for (TextComponent displayEntry : discountList)
                menu = menu.append(displayEntry);
            menu = menu.append(Component.text("\n"));
            // send to player
            this.player.sendMessage(menu);
            this.player.closeInventory();SignInteractListener.removeMachine(player);
        });

        // create all rail pass buttons
        for (int i = 1; i < railPassNames.size(); i++) {
            String rpName = railPassNames.get(i);
            this.clickables[i] = Clickable.of(
                makeItem(Material.LIME_STAINED_GLASS_PANE, 0, Component.text(rpName), Component.text(this.owners.getRailPassPrice(rpName))),
                (_) -> {
                double price = this.owners.getRailPassPrice(rpName);

                if (Iciwi.economy.getBalance(this.player) >= price) {
                    // take money from player
                    Iciwi.economy.withdrawPlayer(this.player, price);

                    // check if the card already has the rail pass
                    if (this.cardSql.getAllDiscounts(serial).containsKey(rpName)) {
                        // Extend existing rail pass
                        icCard.setRailPass(rpName, icCard.getExpiry(rpName));
                        this.player.sendMessage(String.format(this.lang.getString("extended-rail-pass"), rpName, this.owners.getRailPassPrice(rpName)));
                    }
                    else {
                        // New rail pass
                        icCard.setRailPass(rpName, System.currentTimeMillis());
                        this.player.sendMessage(String.format(this.lang.getString("added-rail-pass"), rpName, this.owners.getRailPassPrice(rpName)));
                    }

                    // pay the TOC
                    this.owners.deposit(this.owners.getRailPassOperator(rpName), price);

                    // in any case, log into railpassExtend
                }
                else this.player.sendMessage(this.lang.getString("not-enough-money"));

                // close inventory
                this.player.closeInventory();SignInteractListener.removeMachine(player);
            });
        }

        // set items and open inventory
        setItems(this.clickables, this.inv);
        this.player.openInventory(this.inv);

    }

    @Override public Clickable[] getClickables () { return this.clickables; }

    @Override public boolean useBottomInv () { return this.bottomInv; }

    @Override public void setSelectedItem (ItemStack selectedItem) { this.selectedItem = selectedItem; }

    @Override public ItemStack getSelectedItem () { return this.selectedItem; }

    @Override public void setBottomInv (boolean b) { this.bottomInv = b; }
}
