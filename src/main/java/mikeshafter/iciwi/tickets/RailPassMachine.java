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
import java.lang.Runnable ;
import java.util.function.Function;
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

    public RailPassMachine (Player player) {
        this.player = player;
    }
    public RailPassMachine (Player player, List<String> operators) {
        this.player = player;
        this.operators = operators;
    }

    @Override public Runnable getClickInvItemRunnable () {return clickInvItemRunnable;}

    public void init (String station) {
        // Set the operators
        this.operators = new Owners().getOwners(station);

        // setup inventory
        inv = this.plugin.getServer().createInventory(null, 9, this.lang.getComponent("railpass-machine"));
        this.clickables = new Clickable[9];

        // Create buttons
        this.clickables[2] = Clickable.of(
            makeItem(Material.PAPER, 0, lang.getComponent("menu-new-paper-pass"), Component.text("Buy a paper rail pass")),
            (event) -> paperPass()
        );
        this.clickables[6] = Clickable.of(
            makeItem(Material.NAME_TAG, 0, lang.getComponent("menu-insert-card"), Component.text("Apply a rail pass onto your card")),
            (event) -> selectCard()
        );


        // phase this out
//        initOld(station);
    }

    // card selection menu. player clicks in their own inventory to select a card
    public void selectCard () {
        // Setup listener for bottom inventory selection
        // Create inventory
        inv = this.plugin.getServer().createInventory(null, 9, lang.getComponent("select-card"));
        // Swap flag
        bottomInv = true;
        // Set runnable after clicking the card
        clickInvItemRunnable = () -> railPass(selectedItem);
        // Start listening and open inventory
        player.openInventory(inv);
    }

    // card selection menu. player clicks in their own inventory to select a card
    @Deprecated public void initOld (String station) {
        // Create inventory
        inv = this.plugin.getServer().createInventory(null, 9, this.lang.getComponent("select-card"));
        // Set next action
        clickInvItemRunnable = () -> railPass(selectedItem);
        // Start listening and open inventory
        player.openInventory(inv);
    }

    public void paperPass () {
        // get available railpasses
        ArrayList<String> railPassNames = new ArrayList<>();
        this.operators.forEach((o) -> railPassNames.addAll(owners.getRailPassNames(o)));
        
        int invSize = (railPassNames.size() / 9 + 1) * 9;
        inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
        clickables = new Clickable[invSize];

        // create all rail pass buttons
        for (int i = 0; i <= railPassNames.size(); i++) {
            clickables[i] = Clickable.of(
                makeItem(Material.LIME_STAINED_GLASS_PANE, 0, Component.text(railPassNames.get(i)), Component.text(this.owners.getRailPassPrice(railPassNames.get(i)))),
                (event) -> {
                String name = parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName());
                double price = this.owners.getRailPassPrice(name);

                if (Iciwi.economy.getBalance(player) >= price) {
                    // take money from player
                    Iciwi.economy.withdrawPlayer(player, price);

                    // generate the rail pass paper ticket item
                    Material material = Material.valueOf(plugin.getConfig().getString("railpass.material"));
                    int customModelData = plugin.getConfig().getInt("railpass.custom-model-data");
                    long time = System.currentTimeMillis();
                    ItemStack item = makeItem(material, customModelData, "Rail Pass", name, String.valueOf(time + owners.getRailPassDuration(name)));

                    // give it to the player
                    player.getInventory().addItem(item);

                    // pay the TOC
                    this.owners.deposit(this.owners.getRailPassOperator(name), price);

                    // log the transaction

                }
                else player.sendMessage(this.lang.getString("not-enough-money"));

                // close inventory
                player.closeInventory();
            });
        }
    }

    // rail pass menu
    public void railPass (ItemStack item) {
        if (!loreCheck(item)) return;

        // get available railpasses
        ArrayList<String> railPassNames = new ArrayList<>();
        this.operators.forEach((o) -> railPassNames.addAll(owners.getRailPassNames(o)));

        int invSize = (railPassNames.size() / 9 + 1) * 9;
        inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
        clickables = new Clickable[invSize];

        // get serial number
        IcCard icCard = IcCardFromItem(item);
        if (icCard == null) {
            player.closeInventory();
            return;
        }
        String serial = icCard.getSerial();

        // rail pass viewer
        clickables[0] = Clickable.of(makeItem(Material.WHITE_STAINED_GLASS_PANE, 0, Component.text("View Rail Passes")), (event) -> {
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
            player.sendMessage(menu);
            player.closeInventory();
        });

        // create all rail pass buttons
        for (int i = 1; i <= railPassNames.size(); i++) {
            clickables[i] = Clickable.of(
                makeItem(Material.LIME_STAINED_GLASS_PANE, 0, Component.text(railPassNames.get(i)), Component.text(this.owners.getRailPassPrice(railPassNames.get(i)))),
                (event) -> {
                String name = parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName());
                double price = this.owners.getRailPassPrice(name);

                if (Iciwi.economy.getBalance(player) >= price) {
                // take money from player
                Iciwi.economy.withdrawPlayer(player, price);

                // check if the card already has the rail pass
                if (this.cardSql.getAllDiscounts(serial).containsKey(name)) {
                // Extend existing rail pass
                icCard.setRailPass(name, icCard.getExpiry(name));
                player.sendMessage(this.lang.getString("extended-rail-pass"));

                }
                else {
                // New rail pass
                icCard.setRailPass(name, System.currentTimeMillis());
                player.sendMessage(this.lang.getString("added-rail-pass"));
                }
                // pay the TOC
                this.owners.deposit(this.owners.getRailPassOperator(name), price);

                // in any case, log into railpassExtend

                cardSql.logMaster(player.getUniqueId().toString());
                cardSql.logRailpassExtend(serial, name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), cardSql.getStart(serial, name), owners.getRailPassDuration(name), owners.getRailPassOperator(name));

                }
                else player.sendMessage(this.lang.getString("not-enough-money"));

                // close inventory
                player.closeInventory();
            });
        }

        // set items and open inventory
        inv = setItems(clickables, inv);
        player.openInventory(inv);

    }

    @Override public Clickable[] getClickables () { return this.clickables; }

    @Override public boolean useBottomInv () { return bottomInv; }

    @Override public void setSelectedItem (ItemStack selectedItem) { this.selectedItem = selectedItem; }

    @Override public ItemStack getSelectedItem () { return selectedItem; }

    // puts the items of a clickable[] into an inventory
    public Inventory setItems (Clickable[] clickables, Inventory inventory) {
        Function<Clickable[], ItemStack[]> getItems = (c) -> {
            ItemStack[] items = new ItemStack[c.length];
            for (int i = 0; i < c.length; i++)
                if (c[i] != null) items[i] = c[i].getItem();
            return items;
        };
        inventory.setStorageContents(getItems.apply(clickables));
        return inventory;
    }

    @Override public void setBottomInv (boolean b) { this.bottomInv = b; }
}
