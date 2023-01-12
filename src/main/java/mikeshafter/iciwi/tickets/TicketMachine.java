package mikeshafter.iciwi.tickets;

import com.bergerkiller.bukkit.common.utils.CommonUtil;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.Clickable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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

  // Constant helper classes
  private final CardSql cardSql = new CardSql();
  private final Owners owners = new Owners();
  private final Lang lang = new Lang();
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);

  // Constructor and Menu Display
  public TicketMachine (Player player) { this.player = player; }

  // initial menu
  public void init (String station) {
    // Setup listener
    Listener listener = new EventListener();
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
      nextMachine.selectCard(player);
    });

    // Get operators
    operators = this.owners.getOwners(station);
    // Set items
    inv = setItems(clickables, inv);
    // Start listening and open inventory
    Bukkit.getPluginManager().registerEvents(listener, plugin);
    player.openInventory(inv);
  }

  // card selection menu. player clicks in their own inventory to select a card
  public void selectCard (Player player) {
    // Setup listener for bottom inventory selection
    Listener listener = new EventListener((byte) 1);
    // Create inventory
    inv = this.plugin.getServer().createInventory(null, 9, this.lang.getComponent("select-card"));
    // Start listening and open inventory
    Bukkit.getPluginManager().registerEvents(listener, plugin);
    player.openInventory(inv);
  }

  // main menu after inserting iciwi card
  public void cardMenu (Player player) {
    // Setup listener
    TicketMachine nextMachine = new TicketMachine(player);
    Listener listener = new EventListener();
    // setup inventory
    inv = plugin.getServer().createInventory(null, 9, lang.getComponent("ticket-machine"));
    this.clickables = new Clickable[9];

    // Create buttons
    this.clickables[2] = Clickable.of(makeItem(Material.PURPLE_WOOL, Component.text("New Iciwi Card")), (event) -> nextMachine.newCard());
    this.clickables[3] = Clickable.of(makeItem(Material.LIGHT_BLUE_WOOL, Component.text("Top Up Iciwi Card")), (event) -> nextMachine.topUpCard(player, this.selectedItem));
    this.clickables[4] = Clickable.of(makeItem(Material.LIME_WOOL, Component.text("Rail Passes")), (event) -> nextMachine.railPass(player, this.selectedItem));
    this.clickables[5] = Clickable.of(makeItem(Material.ORANGE_WOOL, Component.text("Refund Card")), (event) -> nextMachine.refundCard(player, this.selectedItem));
    this.clickables[6] = Clickable.of(makeItem(Material.PURPLE_WOOL, Component.text("Select Another Card")), (event) -> nextMachine.selectCard(player));

    // Set items
    inv = setItems(this.clickables, inv);
    // Start listening and open inventory
    Bukkit.getPluginManager().registerEvents(listener, plugin);
    player.openInventory(inv);
  }

  // new iciwi card menu
  public void newCard ()
  {
    // Setup listener
    Listener listener = new EventListener();
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
    Bukkit.getPluginManager().registerEvents(listener, plugin);
    player.openInventory(inv);
  }

  // top up menu
  public void topUpCard (Player player, ItemStack item) {
    // Setup listener
    Listener listener = new EventListener();
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
    Bukkit.getPluginManager().registerEvents(listener, plugin);
    player.openInventory(inv);
  }

  // rail pass menu
  public void railPass (Player player, ItemStack item)
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
  public void refundCard (Player player, ItemStack item)
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

  private class EventListener implements Listener {

    private final byte flags;

    public EventListener (byte flags) { 
      this.flags = flags;
      System.out.println("DEBUG: Registered a new listener!");
    }

    public EventListener () { 
      this.flags = 0; 
      System.out.println("DEBUG: Registered a new listener!");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void TicketMachineListener (InventoryClickEvent event) {
      System.out.println("DEBUG: Registered a click!");

      // Cancel unwanted clicks
      // Restrict putting items from the bottom inventory into the top inventory
      Inventory clickedInventory = event.getClickedInventory();
      Player player = (Player) event.getWhoClicked();

      if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
        event.setCancelled(true);
        return;
      }

      if (event.getAction() == InventoryAction.NOTHING && event.getClick() != ClickType.MIDDLE) {
        event.setCancelled(true);
        return;
      }

      if (clickedInventory == player.getOpenInventory().getBottomInventory()) {
        event.setCancelled(true);
        // close the previous inventory
        //player.closeInventory();
        // player inventory item selection code
        if (flags == (byte) 1) {
          selectedItem = event.getCurrentItem();
          // there can only be 1 action here, which is to open the card menu
          cardMenu(player);
        }
        return;
      }

      if (clickedInventory == player.getOpenInventory().getTopInventory()) {
        event.setCancelled(true);
        // close the previous inventory
        //player.closeInventory();
        // get contents of actual inventory
        ItemStack[] contents = clickedInventory.getContents();
        // get slot
        int clickedSlot = event.getRawSlot();
        // get clicked item
        Clickable clickedItem = clickables[clickedSlot];
        // compare items and run
        if (clickedItem.getItem().equals(contents[clickedSlot])) clickedItem.run(event);
        // don't need to test for more
        return;
      }
      if (clickedInventory != null) {
        clickedInventory.close();
        // end of event, therefore we unregister this
        CommonUtil.unregisterListener(this);
      }

    }

    @EventHandler
    public void onInvClose (InventoryCloseEvent event) {
      CommonUtil.unregisterListener(this);
      selectedItem = null;
    }
  }

}
