package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
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

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static mikeshafter.iciwi.util.MachineUtil.*;

public class TicketMachine implements Machine {

  // Attributes
  private Inventory inv;
  private Clickable[] clickables;
  private ItemStack selectedItem;
  private List<String> operators;
  private final Player player;
  private boolean bottomInv;

  // Constant helper classes
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final CardSql cardSql = new CardSql();
  private final Owners owners = plugin.owners;
  private final Lang lang = plugin.lang;

  // Constructor and Menu Display
  public TicketMachine(Player player) {
    this.player = player;
  }

  // getters
  public Clickable[] getClickables() {
    return clickables;
  }

  public ItemStack getSelectedItem() {
    return selectedItem;
  }

  public boolean useBottomInv() {
    return bottomInv;
  }

  // setters
  @Override
  public void setSelectedItem(ItemStack selectedItem) {
    this.selectedItem = selectedItem;
  }

  // initial menu
  public void init(String station) {
    // setup inventory
    inv = plugin.getServer().createInventory(this.player, 9, lang.getComponent("ticket-machine"));
    this.clickables = new Clickable[9];

    // Create buttons
    this.clickables[2] = Clickable.of(makeItem(Material.PAPER, 0, lang.getComponent("menu-new-ticket"),
        Component.text("Tickets are non-refundable")), (event) -> new CustomMachine(player, station));
    this.clickables[4] = Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-new-card")),
        (event) -> newCard());
    this.clickables[6] = Clickable.of(makeItem(Material.NAME_TAG, 0, lang.getComponent("menu-insert-card")),
        (event) -> selectCard());

    // Get operators
    operators = this.owners.getOwners(station);
    // Set items
    setItems(clickables, inv);
    // Start listening and open inventory
    player.openInventory(inv);
  }

  // card selection menu. player clicks in their own inventory to select a card
  public void selectCard() {
    // Setup listener for bottom inventory selection
    // Create inventory
    inv = this.plugin.getServer().createInventory(null, 9, lang.getComponent("select-card"));
    // Swap flag
    bottomInv = true;
    // Start listening and open inventory
    player.openInventory(inv);
  }

  @Override
  public void onCardSelection() {
    cardMenu();
  }

  // main menu after inserting iciwi card
  public void cardMenu() {
    // setup inventory
    inv = plugin.getServer().createInventory(null, 9, lang.getComponent("ticket-machine"));
    this.clickables = new Clickable[9];

    // Create buttons
    this.clickables[2] = Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-new-card")),
        (event) -> newCard());
    this.clickables[3] = Clickable.of(makeItem(Material.LIGHT_BLUE_WOOL, 0, lang.getComponent("menu-top-up-card")),
        (event) -> topUpCard(this.selectedItem)); // todo: fix this next
    this.clickables[4] = Clickable.of(makeItem(Material.LIME_WOOL, 0, lang.getComponent("menu-rail-pass")),
        (event) -> railPass(this.selectedItem)); // todo: fix this next
    this.clickables[5] = Clickable.of(makeItem(Material.ORANGE_WOOL, 0, lang.getComponent("menu-refund-card")),
        (event) -> refundCard(this.selectedItem));
    this.clickables[6] = Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-select-other-card")),
        (event) -> selectCard());

    // Set items
    setItems(this.clickables, inv);
    // Start listening and open inventory
    player.openInventory(inv);
  }

  // new iciwi card menu
  public void newCard() {
    // Setup listener
    // setup inventory
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    int invSize = roundUp(priceArray.size(), 9);
    inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
    this.clickables = new Clickable[priceArray.size()];

    for (int i = 0; i < priceArray.size(); i++) {
      this.clickables[i] = Clickable.of(makeItem(Material.PURPLE_STAINED_GLASS_PANE, 0,
          Component.text(String.format(lang.getString("currency") + "%.2f", priceArray.get(i)))), (event) -> {
            double value = Double
                .parseDouble(parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName())
                    .replaceAll("[^\\d.]", ""));
            double deposit = plugin.getConfig().getDouble("deposit");

            event.setCancelled(true);

            if (Iciwi.economy.getBalance(player) >= deposit + value) {
              // Take money from player and send message
              Iciwi.economy.withdrawPlayer(player, deposit + value);

              // Prepare card
              int s = new SecureRandom().nextInt(100000);
              char sum = new char[] { 'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R',
                  'S', 'T',
                  'Y' }[((s % 10) * 2 + (s / 10 % 10) * 3 + (s / 100 % 10) * 5 + (s / 1000 % 10) * 7 + (s / 10000) * 9)
                      % 19];
              String serial = lang.getString("serial-prefix") + sum + "-" + s;

              // Get card generator
              Material cardMaterial = Material.valueOf(plugin.getConfig().getString("card.material"));
              int customModelData = plugin.getConfig().getInt("card.custom-model-data");
              // Generate card
              cardSql.newCard(serial, value);
              player.getInventory().addItem(makeItem(cardMaterial, customModelData, lang.getComponent("plugin-name"),
                  lang.getComponent("serial-number"), Component.text(serial)));

              // Send confirmation message
              player.sendMessage(String.format(lang.getString("new-card-created"), deposit, value));
              player.closeInventory();
            } else {
              player.closeInventory();
              player.sendMessage(lang.getString("not-enough-money"));
            }
          });
    }

    // Set items
    setItems(this.clickables, inv);
    // Start listening and open inventory
    player.openInventory(inv);
  }

  // top up menu
  public void topUpCard(ItemStack item) {
    // Setup listener
    // setup inventory
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    int invSize = roundUp(priceArray.size(), 9);
    inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
    clickables = new Clickable[invSize];

    // get serial number
    String serial = parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1));

    for (int i = 0; i < priceArray.size(); i++) {
      clickables[i] = Clickable.of(makeItem(Material.LIME_STAINED_GLASS_PANE, 0,
          Component.text(String.format(lang.getString("currency") + "%.2f", priceArray.get(i)))), (event) -> {
            double value = Double
                .parseDouble(parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName())
                    .replaceAll("[^\\d.]", ""));

            if (Iciwi.economy.getBalance(player) >= value) {
              // Take money from player and send message
              Iciwi.economy.withdrawPlayer(player, value);
              player.sendMessage(String.format(lang.getString("card-topped-up"), value));

              // Update value in SQL
              cardSql.addValueToCard(serial, value);
              player.closeInventory();
            } 
            else 
            {
              player.closeInventory();
              player.sendMessage(lang.getString("not-enough-money"));
            }
          });
    }

    // Set items
    setItems(this.clickables, inv);
    // Start listening and open inventory
    player.openInventory(inv);
  }

  // rail pass menu
  public void railPass(ItemStack item) {
    // get available railpasses
    ArrayList<String> railPassNames = new ArrayList<>();
    this.operators.forEach((o) -> railPassNames.addAll(owners.getRailPassNames(o)));

    int invSize = (railPassNames.size() / 9 + 1) * 9;
    inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
    clickables = new Clickable[invSize];

    // get serial number
    String serial = parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1));

    // rail pass viewer
    clickables[0] = Clickable.of(makeItem(Material.WHITE_STAINED_GLASS_PANE, 0, lang.getComponent("menu-view-rail-pass")),
        (event) -> {
          // print current rail passes
          // get current passes
          List<TextComponent> discountList = cardSql.getAllDiscounts(serial).entrySet().stream()
              .sorted(Map.Entry.comparingByValue())
              .map(railPass -> Component.text().content(
                  // Show expiry date
                  "\u00A76- \u00A7a" + railPass.getKey() + "\u00a76 | Exp. "
                      + String.format("\u00a7b%s\n", new Date(railPass.getValue() * 1000)))
                  // Option to extend (currently disabled)
                  // .append(Component.text().content("\u00a76 | Extend
                  // \u00a7a")).clickEvent(ClickEvent.runCommand("/iciwi railpass "+serial+"
                  // "+railPass.getKey()))
                  .build())
              .toList();
          // menu title
          TextComponent menu = Component.text().content("==== Rail Passes You Own ====\n").color(NamedTextColor.GOLD)
              .build();
          // build content
          for (TextComponent displayEntry : discountList)
            menu = menu.append(displayEntry);
          menu = menu.append(Component.text("\n"));
          // send to player
          player.sendMessage(menu);

          // close inventory so that the player can see the message
          player.closeInventory();
        });

    // create all rail pass buttons
    for (int i = 1; i < railPassNames.size() + 1; i++) {
      clickables[i] = Clickable.of(makeItem(Material.LIME_STAINED_GLASS_PANE, 0, Component.text(railPassNames.get(i-1))),
          (event) -> {
            String name = parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName());
            double price = this.owners.getRailPassPrice(name);

            if (Iciwi.economy.getBalance(player) >= price) {
              // take money from player
              Iciwi.economy.withdrawPlayer(player, price);

              // check if the card already has the rail pass
              if (this.cardSql.getAllDiscounts(serial).containsKey(name))
                // Extend by the duration of the rail pass (change start time to the current
                // expiry time)
              {
                this.cardSql.setDiscount(serial, name, this.cardSql.getExpiry(serial, name));
                player.sendMessage(this.lang.getString("added-rail-pass"));
              }
              else
                // New rail pass
              {
                this.cardSql.setDiscount(serial, name, Instant.now().getEpochSecond());
                player.sendMessage(this.lang.getString("extended-rail-pass"));
              }

              // pay the TOC
              this.owners.deposit(this.owners.getRailPassOperator(name), price);
            }
            else
              player.sendMessage(this.lang.getString("not-enough-money"));

            // close inventory
            player.closeInventory();
          });
    }

    // set items and open inventory
    setItems(clickables, inv);
    player.openInventory(inv);

  }


  // refunds the card
  public void refundCard(ItemStack item) 
  {  
    // get serial number
    String serial = parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1));
    for (ItemStack itemStack : player.getInventory().getContents()) {
      // check if the lore matches
      if (loreCheck(itemStack) && Objects.requireNonNull(itemStack.getItemMeta().lore()).get(0).equals(lang.getComponent("serial-number")) && Objects.requireNonNull(itemStack.getItemMeta().lore()).get(1).equals(Component.text(serial))) {

        // return remaining value to the player
        double remainingValue = this.cardSql.getCardValue(serial);
        Iciwi.economy.depositPlayer(player, remainingValue);

        // return the deposit to the player
        double deposit = this.plugin.getConfig().getDouble("deposit");
        Iciwi.economy.depositPlayer(player, deposit);

        // remove card from the inventory and from the database
        player.getInventory().remove(itemStack);
        this.cardSql.deleteCard(serial);

        // send message and break out of loop
        player.sendMessage(String.format(lang.getString("card-refunded"), serial, remainingValue + deposit));

        // close inventory
        player.closeInventory();
        break;
      }
    }
  }

  /**
   * Puts the items of a clickable[] into an inventory.
   *
   * @param clickables The clickable[] stated above.
   * @param inventory  The inventory stated above.
   */
  private void setItems(Clickable[] clickables, Inventory inventory) {
    Function<Clickable[], ItemStack[]> getItems = (c) -> {
      ItemStack[] items = new ItemStack[c.length];
      for (int i = 0; i < c.length; i++)
        if (c[i] != null)
          items[i] = c[i].getItem();
      return items;
    };
    inventory.setStorageContents(getItems.apply(clickables));
  }

  @Override
  public void setBottomInv(boolean b) {
    this.bottomInv = b;
  }

}
