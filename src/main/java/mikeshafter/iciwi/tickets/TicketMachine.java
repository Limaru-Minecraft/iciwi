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
  private boolean flag;

  // Constant helper classes
  private final CardSql cardSql = new CardSql();
  private final Owners owners = new Owners();
  private final Lang lang = new Lang();
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);

  // Constructor and Menu Display
  public TicketMachine (Player player) { this.player = player; }

  // getters
  public Clickable[] getClickables () { return clickables; }
  public ItemStack getSelectedItem () { return selectedItem; }
  public boolean useBottomInventory () { return flag; }
  
  // setters
  public void setSelectedItem(ItemStack selectedItem) { this.selectedItem = selectedItem; }

  // initial menu
  public void init (String station) {
    // setup inventory
    inv = plugin.getServer().createInventory(null, 9, lang.getComponent("ticket-machine"));
    this.clickables = new Clickable[9];

    // Create buttons
    this.clickables[2] = Clickable.of(makeItem(Material.PAPER, Component.text("New Single Journey Ticket"), Component.text("Tickets are non-refundable")), (event) -> new CustomMachine(player, station));
    this.clickables[4] = Clickable.of(makeItem(Material.PURPLE_WOOL, Component.text("New Iciwi Card")), (event) -> {
      TicketMachine nextMachine = new TicketMachine(player);
      nextMachine.newCard();
    });
    this.clickables[6] = Clickable.of(makeItem(Material.NAME_TAG, Component.text("Insert Card")), (event) -> {
      TicketMachine nextMachine = new TicketMachine(player);
      nextMachine.selectCard();
    });

    // Get operators
    operators = this.owners.getOwners(station);
    // Set items
    inv = setItems(clickables, inv);
    // Start listening and open inventory
    player.openInventory(inv);
  }

  // card selection menu. player clicks in their own inventory to select a card
  public void selectCard () {
    // Setup listener for bottom inventory selection
    // Create inventory
    inv = this.plugin.getServer().createInventory(null, 9, this.lang.getComponent("select-card"));
    // Start listening and open inventory
    player.openInventory(inv);
  }

  // main menu after inserting iciwi card
  public void cardMenu () {
    // Setup listener
    TicketMachine nextMachine = new TicketMachine(player);
    // setup inventory
    inv = plugin.getServer().createInventory(null, 9, lang.getComponent("ticket-machine"));
    this.clickables = new Clickable[9];

    // Create buttons
    this.clickables[2] = Clickable.of(makeItem(Material.PURPLE_WOOL, Component.text("New Iciwi Card")), (event) -> nextMachine.newCard());
    this.clickables[3] = Clickable.of(makeItem(Material.LIGHT_BLUE_WOOL, Component.text("Top Up Iciwi Card")), (event) -> nextMachine.topUpCard(this.selectedItem));  // todo: fix this next
    this.clickables[4] = Clickable.of(makeItem(Material.LIME_WOOL, Component.text("Rail Passes")), (event) -> nextMachine.railPass(this.selectedItem));  // todo: fix this next
    this.clickables[5] = Clickable.of(makeItem(Material.ORANGE_WOOL, Component.text("Refund Card")), (event) -> nextMachine.refundCard(this.selectedItem));  // todo: fix this next
    this.clickables[6] = Clickable.of(makeItem(Material.PURPLE_WOOL, Component.text("Select Another Card")), (event) -> nextMachine.selectCard());

    // Set items
    inv = setItems(this.clickables, inv);
    // Start listening and open inventory
    player.openInventory(inv);
  }

  // new iciwi card menu
  public void newCard ()
  {
    // Setup listener
    // setup inventory
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    int invSize = roundUp(priceArray.size(), 9);
    inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
    this.clickables = new Clickable[priceArray.size()];

    for (int i = 0; i < priceArray.size(); i++)
    {
      this.clickables[i] = Clickable.of(makeItem(Material.PURPLE_STAINED_GLASS_PANE, Component.text(String.format(lang.getString("currency")+"%.2f", priceArray.get(i)))), (event) -> {
        double value = Double.parseDouble(parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName()).replaceAll("[^\\d.]", ""));
        double deposit = plugin.getConfig().getDouble("deposit");

        event.setCancelled(true);

        if (Iciwi.economy.getBalance(player) >= deposit+value)
        {
          // Take money from player and send message
          Iciwi.economy.withdrawPlayer(player, deposit+value);

          // Prepare card
          int s = new SecureRandom().nextInt(100000);
          char sum = new char[] {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[((s%10)*2+(s/10%10)*3+(s/100%10)*5+(s/1000%10)*7+(s/10000)*9)%19];
          String serial = lang.getString("serial-prefix")+sum+"-"+s;

          // Generate card
          cardSql.newCard(serial, value);
          player.getInventory().addItem(makeItem(Material.NAME_TAG, lang.getComponent("plugin-name"), lang.getComponent("serial-number"), Component.text(serial)));

          // Send confirmation message
          player.sendMessage(String.format(lang.getString("new-card-created"), deposit, value));
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
    inv = setItems(this.clickables, inv);
    // Start listening and open inventory
    player.openInventory(inv);
  }

  // top up menu
  public void topUpCard (ItemStack item) {
    // Setup listener
    // setup inventory
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    int invSize = roundUp(priceArray.size(), 9);
    inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
    clickables = new Clickable[invSize];

    // get serial number
    String serial = parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1));

    for (int i = 0; i < priceArray.size(); i++)
    {
      clickables[i] = Clickable.of(makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text(String.format(lang.getString("currency")+"%.2f", priceArray.get(i)))), (event) -> {
        double value = Double.parseDouble(parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName()).replaceAll("[^\\d.]", ""));

        player.closeInventory();

        if (Iciwi.economy.getBalance(player) >= value)
        {
          // Take money from player and send message
          Iciwi.economy.withdrawPlayer(player, value);

          // Update value in SQL
          cardSql.addValueToCard(serial, value);
        }
      });
    }

    // Set items
    inv = setItems(this.clickables, inv);
    // Start listening and open inventory
    player.openInventory(inv);
  }

  // rail pass menu
  public void railPass (ItemStack item)
  {
    // get available railpasses
    ArrayList<String> railPassNames = new ArrayList<>();
    this.operators.forEach((o) -> railPassNames.addAll(cardSql.getRailPassNames(o)));

    int invSize = (railPassNames.size() / 9 + 1) * 9;
    inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
    clickables = new Clickable[invSize];

    // get serial number
    String serial = parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1));

    // rail pass viewer
    clickables[0] = Clickable.of(makeItem(Material.WHITE_STAINED_GLASS_PANE, Component.text("View Rail Passes")), (event) -> {
      //print current rail passes
      // get current passes
      List<TextComponent> discountList = cardSql.getAllDiscounts(serial).entrySet().stream()
              .sorted(Map.Entry.comparingByValue())
              .map(railPass -> Component.text().content(
                              // Show expiry date
                              "\u00A76- \u00A7a"+railPass.getKey()+"\u00a76 | Exp. "+String.format("\u00a7b%s\n", new Date(railPass.getValue()*1000)))
                      // Option to extend (currently disabled)
                      // .append(Component.text().content("\u00a76 | Extend \u00a7a")).clickEvent(ClickEvent.runCommand("/iciwi railpass "+serial+" "+railPass.getKey()))
                      .build()).toList();
      // menu title
      TextComponent menu = Component.text().content("==== Rail Passes You Own ====\n").color(NamedTextColor.GOLD).build();
      // build content
      for (TextComponent displayEntry : discountList) menu = menu.append(displayEntry);
      menu = menu.append(Component.text("\n"));
      // send to player
      player.sendMessage(menu);
    });

    // create all rail pass buttons
    for (int i = 1; i <= railPassNames.size(); i++)
    {
      clickables[i] = Clickable.of(makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text(railPassNames.get(i))), (event) -> {
        String name = parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName());
        double price = this.owners.getRailPassPrice(name);

        if (Iciwi.economy.getBalance(player) >= price)
        {
          // take money from player
          Iciwi.economy.withdrawPlayer(player, price);

          // check if the card already has the rail pass
          if (this.cardSql.getAllDiscounts(serial).containsKey(name))
            // Extend by the duration of the rail pass (change start time to the current expiry time)
            this.cardSql.setDiscount(serial, name, this.cardSql.getExpiry(serial, name));
          else
            // New rail pass
            this.cardSql.setDiscount(serial, name, Instant.now().getEpochSecond());

          // pay the TOC
          this.owners.deposit(this.owners.getRailPassOperator(name), price);
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

  // refunds the card
  public void refundCard (ItemStack item)
  {
    // get serial number
    String serial = parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1));
    for (ItemStack itemStack : player.getInventory().getContents())
    {
      // get loreStack
      if (loreCheck(itemStack) && Objects.requireNonNull(itemStack.getItemMeta().lore()).get(0).equals(lang.getComponent("serial-number")))
      {
        // get serialNumber
        if (Objects.requireNonNull(item.getItemMeta().lore()).get(1).equals(Component.text(serial)))
        {
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
          player.sendMessage(String.format(lang.getString("card-refunded"), serial, remainingValue+deposit));
          break;
        }
      }
    }
  }

  // puts the items of a clickable[] into an inventory
  public Inventory setItems (Clickable[] clickables, Inventory inventory)
  {
    Function<Clickable[], ItemStack[]> getItems = (c) -> {
      ItemStack[] items = new ItemStack[c.length];
      for (int i = 0; i < c.length; i++) if (c[i] != null) items[i] = c[i].getItem();
      return items;
    };
    inventory.setStorageContents(getItems.apply(clickables));
    return inventory;
  }
}
