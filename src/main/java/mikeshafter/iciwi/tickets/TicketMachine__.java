package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.*;

import static mikeshafter.iciwi.util.MachineUtil.componentToString;
import static mikeshafter.iciwi.util.MachineUtil.isDouble;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class TicketMachine {
  
  // Attributes
  private final String station;
  private final Player player;
  private Inventory i;
  private final Listener listener;

  // Constant helper classes
  private final Iciwi plugin = getPlugin(Iciwi.class);
  private final CardSql cardSql = new CardSql();
  private final Owners owners = new Owners();
  private final Lang lang = new Lang();
  
  public TicketMachine(Player player, String station) {
    this.player = player;
    this.station = station;
    this.listener = new OnTMCreationListener();
    
    i = plugin.getServer().createInventory(null, 9, lang.getComponent("ticket-machine"));
    i.setItem(1, makeItem(Material.PAPER, lang.getComponent("menu-new-ticket")));
    i.setItem(3, makeItem(Material.MAP, lang.getComponent("menu-adjust-fares")));
    i.setItem(5, makeItem(Material.NAME_TAG, lang.getComponent("card-operations")));
    i.setItem(7, makeItem(Material.BOOK, lang.getComponent("check-fares")));

    Bukkit.getPluginManager().registerEvents(this.listener, plugin);
    player.openInventory(i);
  }
  
  public Player getPlayer() {
    return player;
  }

  private class OnTMCreationListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
      Player player = (Player) event.getWhoClicked();
      Inventory inventory = event.getClickedInventory();
      ItemStack item = event.getCurrentItem();
      
      double value;
      if (inventoryName.equals(lang.getString("ticket-machine"))) {
        event.setCancelled(true);
    
        if (itemName.equals(lang.getString("menu-new-ticket"))) {
          if (Objects.equals(plugin.getConfig().getString("ticket-machine-type"), "GLOBAL")) {
            machine.generateTicket(plugin.getConfig().getDouble("global-ticket-price"));
            player.closeInventory();
          } 
          else machine.newTicket_1(0.0);
        } 
        else if (itemName.equals(lang.getString("menu-adjust-fares"))) machine.adjustFares_1();
        else if (itemName.equals(lang.getString("card-operations"))) machine.cardOperations_1();
        else if (itemName.equals(lang.getString("check-fares"))) {
          //machine.checkFares_1(1);
          player.closeInventory();
          new CustomMachine(this.machine.getPlayer(), this.machine.getStation());
        }
    
      }
      CommonUtil.unregisterListener(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
      CommonUtil.unregisterListener(this);
    }
  }
  
  public void newTicket_1(double value) {
    Inventory i = plugin.getServer().createInventory(null, 36, Component.text(String.format((lang.getString("menu-new-ticket")+lang.getString("currency")+"%.2f"), value)));
    for (int[] ints : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      i.setItem(ints[0], makeItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(ints[1])));
    }
    i.setItem(30, makeItem(Material.RED_STAINED_GLASS_PANE, lang.getComponent("clear")));
    i.setItem(32, makeItem(Material.LIME_STAINED_GLASS_PANE, lang.getComponent("enter")));
    
    player.openInventory(i);
  }

  public void adjustFares_1() {
    // Ticket selection
    Inventory i = plugin.getServer().createInventory(null, 9, lang.getComponent("select-ticket"));
    player.openInventory(i);
  }

  public void adjustFares_2(double value, ItemStack item) {
    Inventory i = plugin.getServer().createInventory(null, 36, Component.text(String.format((lang.getString("menu-adjust-fares")+lang.getString("currency")+"%.2f"), value)));
    for (int[] ints : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      i.setItem(ints[0], makeItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(ints[1])));
    }
    i.setItem(0, item);
    i.setItem(30, makeItem(Material.RED_STAINED_GLASS_PANE, lang.getComponent("clear")));
    i.setItem(32, makeItem(Material.LIME_STAINED_GLASS_PANE, lang.getComponent("enter")));
  
    player.openInventory(i);
  }

  public void cardOperations_1() {
    // check if player has a card
    for (ItemStack i : player.getInventory().getContents()) {
      if (i != null && i.hasItemMeta() && i.getItemMeta() != null && i.getItemMeta().hasLore() && i.getItemMeta().lore() != null && Objects.requireNonNull(i.getItemMeta().lore()).get(0).equals(lang.getComponent("serial-number"))) {
        Inventory j = plugin.getServer().createInventory(null, 9, lang.getComponent("select-card"));
        player.openInventory(j);
        return;
      }
    }
    this.newCard_3();
  }

  public void newCard_3() {
    Inventory i = plugin.getServer().createInventory(null, 9, lang.getComponent("select-value"));
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    if (priceArray.size() == 0) {
      plugin.getConfig().set("price-array", new double[] {10d, 20d, 30d, 50d, 100d});
    }
    for (int j = 0; j < priceArray.size(); j++) {
      i.setItem(j, makeItem(Material.PURPLE_STAINED_GLASS_PANE, Component.text(String.format(lang.getString("currency")+"%.2f", priceArray.get(j)))));
    }
    player.openInventory(i);
  }

  public void cardOperations_2(String serial) {
    // this.serial = serial;
    Inventory i = plugin.getServer().createInventory(null, 9, Component.text(String.format(lang.getString("card-operation")+"%s", serial)));
    double cardValue = cardSql.getCardValue(serial);
    i.setItem(0, makeItem(Material.NAME_TAG, lang.getComponent("card-details"), Component.text(String.format(lang.getString("serial-number")+" %s", serial)), Component.text(String.format(lang.getString("remaining-value")+lang.getString("currency")+"%.2f", cardValue))));
    i.setItem(1, makeItem(Material.MAGENTA_WOOL, lang.getComponent("new-card")));
    i.setItem(2, makeItem(Material.CYAN_WOOL, lang.getComponent("top-up-card")));
    i.setItem(3, makeItem(Material.LIME_WOOL, lang.getComponent("menu-rail-pass"), Component.text(owners.getOwner(this.station))));
    i.setItem(4, makeItem(Material.ORANGE_WOOL, lang.getComponent("refund-card")));
    player.openInventory(i);
  }

  public void topUp_3(String serial) {
    Inventory i = plugin.getServer().createInventory(null, 9, Component.text(String.format(ChatColor.DARK_BLUE+"Top Up - %s", serial)));
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    if (priceArray.size() == 0) {
      plugin.getConfig().set("price-array", new double[] {10d, 20d, 30d, 50d, 100d});
    }
    for (int j = 0; j < priceArray.size(); j++) {
      i.setItem(j, makeItem(Material.PURPLE_STAINED_GLASS_PANE, Component.text(String.format(lang.getString("currency")+"%.2f", priceArray.get(j)))));
    }
    player.openInventory(i);
  }

  public void checkFares_1(int page) {
    Map<String, Double> faresMap = new Fares().getFares(this.station);
    if (faresMap != null) {
      int size = faresMap.size();
      List<String> stations = faresMap.keySet().stream().sorted().toList();
      TextComponent menu = Component.text().content("==== Fares from "+this.station+" ====\n").color(NamedTextColor.GOLD).build();
      for (int i = (page-1)*8; i < (page*8); i++) {
        menu = i < size ? menu.append(Component.text().content("\u00A76- \u00A7a"+stations.get(i)+"\u00a76 - "+String.format("\u00a7b[%.2f]\n", faresMap.get(stations.get(i)))).build())
                   : menu.append(Component.text("\n"));
      }
      int maxPage = (size-1)/8+1;
      menu = menu.append((Component.text().content("== ").color(NamedTextColor.GOLD)).build());
      menu = page == 1 ? menu.append(Component.text().content("[###]").color(NamedTextColor.GOLD).build()) : menu.append(Component.text().clickEvent(ClickEvent.runCommand(checkFares(page-1))).content("[<<<]").color(NamedTextColor.GOLD).build());
      menu = menu.append(Component.text().content(String.format(" == Page %d of %d == ", page, maxPage)).color(NamedTextColor.GOLD).build());
      menu = page == maxPage ? menu.append(Component.text().content("[###]").color(NamedTextColor.GOLD).build()) : menu.append(Component.text().clickEvent(ClickEvent.runCommand(checkFares(page+1))).content("[>>>]").color(NamedTextColor.GOLD).build());
      menu = menu.append((Component.text().content(" ==").color(NamedTextColor.GOLD)).build());
      player.sendMessage(menu);
    }
  }
  
  private @NotNull String checkFares(int page) {
    return "/iciwi:iciwi farechart "+this.station+" "+page;
  }
  
  public void railPass_3(String serial, String operator) {
    Inventory i = plugin.getServer().createInventory(null, 9, Component.text(String.format(lang.getString("menu-rail-pass")+"%s", serial)));
  
    // Show card's current rail passes and expiry dates
    i.addItem(makeItem(Material.NAME_TAG, lang.getComponent("card-discounts")));
  
    // Rail pass sales
    Set<String> names = cardSql.getRailPassNames(operator);
    for (String name : names) {
      double price = owners.getRailPassPrice(name);
      i.addItem(makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text(name).color(TextColor.color(0, 255, 0)), Component.text(price)));
    }
      
    /*
    Set<String> daysSet = owners.getRailPassDays(operator);
    for (String days : daysSet) {
      double price = owners.getRailPassPrice(operator, Long.parseLong(days));
      i.addItem(makeItem(Material.LIME_STAINED_GLASS_PANE, Component.text(days).append(lang.getComponent("days")).color(TextColor.color(0, 255, 0)), Component.text(price)));
     */
    player.openInventory(i);
  }
  
  public String getStation() {
    return station;
  }
  
  public void generateTicket(ItemStack item, double value) {
    // get current fare on the ticket
    if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().lore() != null) {
      String lore0 = componentToString(Objects.requireNonNull(item.getItemMeta().lore()).get(0));
      String lore1 = componentToString(Objects.requireNonNull(item.getItemMeta().lore()).get(1));
      double val = (!lore1.contains("•") && isDouble(lore1)) ? Double.parseDouble(lore1) : 0;
      double parsedValue = value-val;
  
      if (Iciwi.economy.getBalance(player) >= parsedValue) {
        Iciwi.economy.withdrawPlayer(player, parsedValue);
        player.sendMessage(String.format(lang.getString("generate-ticket"), station, value));
        player.getInventory().remove(item);
        player.getInventory().addItem(makeItem(Material.PAPER, lang.getComponent("train-ticket"), Component.text(lore0), Component.text(value)));
      } else player.sendMessage(lang.getString("not-enough-money"));
    }
  }
  
  public void generateTicket(double value) {
    if (Iciwi.economy.getBalance(player) >= value) {
      Iciwi.economy.withdrawPlayer(player, value);
      player.sendMessage(String.format(lang.getString("generate-ticket"), station, value));
      player.getInventory().addItem(makeItem(Material.PAPER, lang.getComponent("train-ticket"), Component.text(station), Component.text(String.format("%.2f", value))));
    } else player.sendMessage(lang.getString("not-enough-money"));
  }
  
  public void generateCard(String serial, double value) {
    if (Iciwi.economy.getBalance(player) >= value) {
      cardSql.newCard(serial, value);
      player.getInventory().addItem(makeItem(Material.NAME_TAG, lang.getComponent("plugin-name"), lang.getComponent("serial-number"), Component.text(serial)));
    } else player.sendMessage(lang.getString("not-enough-money"));
  }
  
}
