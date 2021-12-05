package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import mikeshafter.iciwi.Owners;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class TicketMachine {

  protected final Plugin plugin = getPlugin(Iciwi.class);
  private final String station, operator;
  private final Player player;
  private final CardSql app = new CardSql();
  private final Owners owners = new Owners(plugin);
  private final Lang lang = new Lang(plugin);
  // private String serial;
  // private double value;

  public TicketMachine(Player player, String station) {
    this.player = player;
    this.station = station;

    // get operator
    this.operator = owners.getOwner(station);
  }

  public Player getPlayer() {
    return player;
  }

  public void newTM_0() {
    Inventory i = plugin.getServer().createInventory(null, 9, lang.__TICKET_MACHINE);

    // Single Journey Ticket
    i.setItem(1, makeButton(Material.PAPER, lang.NEW_TICKET));
    i.setItem(3, makeButton(Material.MAP, lang.ADJUST_FARES));
    i.setItem(5, makeButton(Material.NAME_TAG, lang.CARD_OPERATIONS));
    i.setItem(7, makeButton(Material.BOOK, lang.CHECK_FARES));

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
    Inventory i = plugin.getServer().createInventory(null, 36, String.format((lang.__NEW_TICKET+lang.CURRENCY+"%.2f"), value));
    for (int[] ints : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      i.setItem(ints[0], makeButton(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(ints[1])));
    }
    i.setItem(30, makeButton(Material.RED_STAINED_GLASS_PANE, lang.CLEAR));
    i.setItem(32, makeButton(Material.LIME_STAINED_GLASS_PANE, lang.ENTER));

    player.openInventory(i);
  }

  public void adjustFares_1() {
    // Ticket selection
    Inventory i = plugin.getServer().createInventory(null, 9, lang.__SELECT_TICKET);
    player.openInventory(i);
  }

  public void adjustFares_2(double value, ItemStack item) {
    Inventory i = plugin.getServer().createInventory(null, 36, String.format((lang.__ADJUST_FARES+lang.CURRENCY+"%.2f"), value));
    for (int[] ints : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      i.setItem(ints[0], makeButton(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(ints[1])));
    }
    i.setItem(0, item);
    i.setItem(30, makeButton(Material.RED_STAINED_GLASS_PANE, lang.CLEAR));
    i.setItem(32, makeButton(Material.LIME_STAINED_GLASS_PANE, lang.ENTER));

    player.openInventory(i);
  }

  public void cardOperations_1() {
    // check if player has a card
    for (ItemStack i : player.getInventory().getContents()) {
      if (i != null && i.hasItemMeta() && i.getItemMeta() != null && i.getItemMeta().hasLore() && i.getItemMeta().getLore() != null && i.getItemMeta().getLore().get(0).equals(lang.SERIAL_NUMBER)) {
        Inventory j = plugin.getServer().createInventory(null, 9, lang.__SELECT_CARD);
        player.openInventory(j);
        return;
      }
    }
    this.newCard_3();
  }

  public void newCard_3() {
    Inventory i = plugin.getServer().createInventory(null, 9, lang.__SELECT_VALUE);
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    if (priceArray.size() == 0) {
      plugin.getConfig().set("price-array", new double[] {10d, 20d, 30d, 50d, 100d});
    }
    for (int j = 0; j < priceArray.size(); j++) {
      i.setItem(j, makeButton(Material.PURPLE_STAINED_GLASS_PANE, String.format(lang.CURRENCY+"%.2f", priceArray.get(j))));
    }
    player.openInventory(i);
  }

  public void cardOperations_2(String serial) {
    // this.serial = serial;
    Inventory i = plugin.getServer().createInventory(null, 9, String.format(lang.__CARD_OPERATION+"%s", serial));
    double cardValue = app.getCardValue(serial);
    i.setItem(0, makeButton(Material.NAME_TAG, lang.CARD_DETAILS, String.format(lang.SERIAL_NUMBER+"%s", serial), String.format(lang.REMAINING_VALUE+lang.CURRENCY+"%.2f", cardValue)));
    i.setItem(1, makeButton(Material.MAGENTA_WOOL, lang.NEW_CARD));
    i.setItem(2, makeButton(Material.CYAN_WOOL, lang.TOP_UP_CARD));
    i.setItem(3, makeButton(Material.LIME_WOOL, lang.ADD_RAIL_PASS, owners.getOwner(this.station)));
    i.setItem(4, makeButton(Material.ORANGE_WOOL, lang.REFUND_CARD));
    player.openInventory(i);
  }

  public void topUp_3(String serial) {
    Inventory i = plugin.getServer().createInventory(null, 9, String.format(ChatColor.DARK_BLUE+"Top Up - %s", serial));
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    if (priceArray.size() == 0) {
      plugin.getConfig().set("price-array", new double[] {10d, 20d, 30d, 50d, 100d});
    }
    for (int j = 0; j < priceArray.size(); j++) {
      i.setItem(j, makeButton(Material.PURPLE_STAINED_GLASS_PANE, String.format(lang.CURRENCY+"%.2f", priceArray.get(j))));
    }
    player.openInventory(i);
  }

  public void railPass_3(String serial, String operator) {
    Inventory i = plugin.getServer().createInventory(null, 9, String.format(lang.__ADD_RAIL_PASS+"%s", serial));
    Set<String> daysSet = owners.getRailPassDays(operator);
    for (String days : daysSet) {
      double price = owners.getRailPassPrice(operator, Integer.parseInt(days));
      i.addItem(makeButton(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN+days+lang.DAYS, String.valueOf(price)));
    }
    player.openInventory(i);
  }

  public void checkFares_1() {
    this.player.sendMessage("fare checking not implemented, try again later.");
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
      player.sendMessage(String.format(lang.GENERATE_TICKET, station, value));
      player.getInventory().remove(item);
      player.getInventory().addItem(makeButton(Material.PAPER, lang.TRAIN_TICKET, lore0, String.valueOf(value)));
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

//  public String getOperator() {
//    return operator;
//  }
//
//  public String getSerial() {
//    return serial;
//  }
//
//  public void setSerial(String serial) {
//    this.serial = serial;
//  }
//
//  public double getValue() {
//    return value;
//  }
//
//  public void setValue(double value) {
//    this.value = value;
//  }
//
//  public Hashtable<Integer, Double> getDaysList() {
//    return daysList;
//  }
//
//  public void setDaysList(Hashtable<Integer, Double> daysList) {
//    this.daysList = daysList;
//  }
//
public CardSql getApp() {
  return app;
}
  
  public Owners getOwners() {
    return owners;
  }
  
  public Lang getLang() {
    return lang;
  }
  
  public void generateTicket(double value) {
    if (Iciwi.economy.getBalance(player) >= value) {
      Iciwi.economy.withdrawPlayer(player, value);
    }
    player.sendMessage(String.format(lang.GENERATE_TICKET, station, value));
    player.getInventory().addItem(makeButton(Material.PAPER, lang.TRAIN_TICKET, station, String.format("%.2f", value)));
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

//  protected ItemStack makeButton(final Material material, final String displayName, final List<String> lore) {
//    ItemStack item = new ItemStack(material, 1);
//    ItemMeta itemMeta = item.getItemMeta();
//    assert itemMeta != null;
//    itemMeta.setDisplayName(displayName);
//    itemMeta.setLore(lore);
//    item.setItemMeta(itemMeta);
//    return item;
//  }

}
