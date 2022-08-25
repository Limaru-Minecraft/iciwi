package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.JsonManager;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class TicketMachine {
  
  protected final Plugin plugin = getPlugin(Iciwi.class);
  protected final String station;
  protected final Player player;
  protected final CardSql app = new CardSql();
  protected final Owners owners = new Owners(plugin);
  protected final Lang lang = new Lang(plugin);
  
  public TicketMachine(Player player, String station) {
    this.player = player;
    this.station = station;
  }
  
  public Player getPlayer() {
    return player;
  }

  public void newTM_0() {
    Inventory i = plugin.getServer().createInventory(null, 9, lang.getString("ticket-machine"));
  
    // Single Journey Ticket
    i.setItem(1, makeButton(Material.PAPER, lang.getString("menu-new-ticket")));
    i.setItem(3, makeButton(Material.MAP, lang.getString("menu-adjust-fares")));
    i.setItem(5, makeButton(Material.NAME_TAG, lang.getString("card-operations")));
    i.setItem(7, makeButton(Material.BOOK, lang.getString("check-fares")));
  
    player.openInventory(i);
  }

  protected ItemStack makeButton(final Material material, final String displayName) {
    ItemStack item = new ItemStack(material, 1);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    item.setItemMeta(itemMeta);
    return item;
  }

  public void newTicket_1(double value) {
    Inventory i = plugin.getServer().createInventory(null, 36, String.format((lang.getString("menu-new-ticket")+lang.getString("currency")+"%.2f"), value));
    for (int[] ints : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      i.setItem(ints[0], makeButton(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(ints[1])));
    }
    i.setItem(30, makeButton(Material.RED_STAINED_GLASS_PANE, lang.getString("clear")));
    i.setItem(32, makeButton(Material.LIME_STAINED_GLASS_PANE, lang.getString("enter")));
  
    player.openInventory(i);
  }

  public void adjustFares_1() {
    // Ticket selection
    Inventory i = plugin.getServer().createInventory(null, 9, lang.getString("select-ticket"));
    player.openInventory(i);
  }

  public void adjustFares_2(double value, ItemStack item) {
    Inventory i = plugin.getServer().createInventory(null, 36, String.format((lang.getString("menu-adjust-fares")+lang.getString("currency")+"%.2f"), value));
    for (int[] ints : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      i.setItem(ints[0], makeButton(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(ints[1])));
    }
    i.setItem(0, item);
    i.setItem(30, makeButton(Material.RED_STAINED_GLASS_PANE, lang.getString("clear")));
    i.setItem(32, makeButton(Material.LIME_STAINED_GLASS_PANE, lang.getString("enter")));
  
    player.openInventory(i);
  }

  public void cardOperations_1() {
    // check if player has a card
    for (ItemStack i : player.getInventory().getContents()) {
      if (i != null && i.hasItemMeta() && i.getItemMeta() != null && i.getItemMeta().hasLore() && i.getItemMeta().getLore() != null && i.getItemMeta().getLore().get(0).equals(lang.getString("serial-number"))) {
        Inventory j = plugin.getServer().createInventory(null, 9, lang.getString("select-card"));
        player.openInventory(j);
        return;
      }
    }
    this.newCard_3();
  }

  public void newCard_3() {
    Inventory i = plugin.getServer().createInventory(null, 9, lang.getString("select-value"));
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    if (priceArray.size() == 0) {
      plugin.getConfig().set("price-array", new double[] {10d, 20d, 30d, 50d, 100d});
    }
    for (int j = 0; j < priceArray.size(); j++) {
      i.setItem(j, makeButton(Material.PURPLE_STAINED_GLASS_PANE, String.format(lang.getString("currency")+"%.2f", priceArray.get(j))));
    }
    player.openInventory(i);
  }

  public void cardOperations_2(String serial) {
    // this.serial = serial;
    Inventory i = plugin.getServer().createInventory(null, 9, String.format(lang.getString("card-operation")+"%s", serial));
    double cardValue = app.getCardValue(serial);
    i.setItem(0, makeButton(Material.NAME_TAG, lang.getString("card-details"), String.format(lang.getString("serial-number")+" %s", serial), String.format(lang.getString("remaining-value")+lang.getString("currency")+"%.2f", cardValue)));
    i.setItem(1, makeButton(Material.MAGENTA_WOOL, lang.getString("new-card")));
    i.setItem(2, makeButton(Material.CYAN_WOOL, lang.getString("top-up-card")));
    i.setItem(3, makeButton(Material.LIME_WOOL, lang.getString("menu-rail-pass"), owners.getOwner(this.station)));
    i.setItem(4, makeButton(Material.ORANGE_WOOL, lang.getString("refund-card")));
    player.openInventory(i);
  }

  public void topUp_3(String serial) {
    Inventory i = plugin.getServer().createInventory(null, 9, String.format(ChatColor.DARK_BLUE+"Top Up - %s", serial));
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    if (priceArray.size() == 0) {
      plugin.getConfig().set("price-array", new double[] {10d, 20d, 30d, 50d, 100d});
    }
    for (int j = 0; j < priceArray.size(); j++) {
      i.setItem(j, makeButton(Material.PURPLE_STAINED_GLASS_PANE, String.format(lang.getString("currency")+"%.2f", priceArray.get(j))));
    }
    player.openInventory(i);
  }

  public void railPass_3(String serial, String operator) {
    Inventory i = plugin.getServer().createInventory(null, 9, String.format(lang.getString("menu-rail-pass")+"%s", serial));
  
    // Show card's current rail passes and expiry dates
    i.addItem(makeButton(Material.NAME_TAG, lang.getString("card-discounts")));
  
    // Rail pass sales
    Set<String> daysSet = owners.getRailPassDays(operator);
    for (String days : daysSet) {
      double price = owners.getRailPassPrice(operator, Long.parseLong(days));
      i.addItem(makeButton(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN+days+lang.getString("days"), String.valueOf(price)));
    }
    player.openInventory(i);
  }

  public void checkFares_1(int page) {
    Map<String, Double> fares = JsonManager.getFares(this.station);
    if (fares != null) {
      int size = fares.size();
      List<String> stations = fares.keySet().stream().sorted().toList();
      TextComponent menu = Component.text().content("==== Fares from "+this.station+" ====\n").color(NamedTextColor.GOLD).build();
      for (int i = (page-1)*8; i < (page*8); i++) {
        menu = i < size ? menu.append(Component.text().content("\u00A76- \u00A7a"+stations.get(i)+"\u00a76 - "+String.format("\u00a7b[%.2f]\n", fares.get(stations.get(i)))).build())
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
    return "/iciwi:farechart "+this.station+" "+page;
  }
  
  public void generateTicket(ItemStack item, double value) {
    // get current fare on the ticket
    if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null) {
      String lore0 = item.getItemMeta().getLore().get(0);
      boolean validated = lore0.contains("•");
      String lore1 = item.getItemMeta().getLore().get(1);
      double val = (!lore1.contains("•") && isDouble(lore1)) ? Double.parseDouble(lore1) : 0;
      double parsedValue = value-val;
  
      if (Iciwi.economy.getBalance(player) >= parsedValue) {
        Iciwi.economy.withdrawPlayer(player, parsedValue);
      }
      player.sendMessage(String.format(lang.getString("generate-ticket"), station, value));
      player.getInventory().remove(item);
      player.getInventory().addItem(makeButton(Material.PAPER, lang.getString("train-ticket"), lore0, String.valueOf(value)));
    }
  }

  private boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }

  public String getStation() {
    return station;
  }
  
  public void generateTicket(double value) {
    if (Iciwi.economy.getBalance(player) >= value) {
      Iciwi.economy.withdrawPlayer(player, value);
    }
    player.sendMessage(String.format(lang.getString("generate-ticket"), station, value));
    player.getInventory().addItem(makeButton(Material.PAPER, lang.getString("train-ticket"), station, String.format("%.2f", value)));
  }
  
  protected ItemStack makeButton(final Material material, final String displayName, final String... lore) {
    ItemStack item = new ItemStack(material, 1);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    itemMeta.setLore(Arrays.asList(lore));
    item.setItemMeta(itemMeta);
    return item;
  }

}
