package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Owners;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class TicketMachine {
  
  protected final Plugin plugin = getPlugin(Iciwi.class);
  private final String station, operator;
  private final Player player;
  private final CardSql app = new CardSql();
  private final Owners owners = new Owners(plugin);
  private String serial;
  private double value;
  private Hashtable<Integer, Double> daysList; // [{days, price}, ...]
  
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
    Inventory i = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Ticket Machine");
  
    // Single Journey Ticket
    i.setItem(1, makeButton(Material.PAPER, ChatColor.GREEN+"New Single Journey Ticket"));
    i.setItem(3, makeButton(Material.MAP, ChatColor.YELLOW+"Adjust Fares"));
    i.setItem(5, makeButton(Material.NAME_TAG, ChatColor.LIGHT_PURPLE+"ICIWI Card Operations"));
    i.setItem(7, makeButton(Material.BOOK, ChatColor.AQUA+"Check Fares"));
  
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
    Inventory i = plugin.getServer().createInventory(null, 36, String.format(ChatColor.DARK_BLUE+"New Ticket - £%.2f", value));
    for (int[] ints : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      i.setItem(ints[0], makeButton(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(ints[1])));
    }
    i.setItem(30, makeButton(Material.RED_STAINED_GLASS_PANE, "CLEAR"));
    i.setItem(32, makeButton(Material.LIME_STAINED_GLASS_PANE, "ENTER"));
  }
  
  public void adjustFares_1() {
    // Ticket selection
    Inventory i = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select Ticket...");
    player.openInventory(i);
  }
  
  public void adjustFares_2(double value, ItemStack item) {
    Inventory i = plugin.getServer().createInventory(null, 36, String.format(ChatColor.DARK_BLUE+"Adjust Fare - £%.2f", value));
    for (int[] ints : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      i.setItem(ints[0], makeButton(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(ints[1])));
    }
    i.setItem(0, item);
    i.setItem(30, makeButton(Material.RED_STAINED_GLASS_PANE, "CLEAR"));
    i.setItem(32, makeButton(Material.LIME_STAINED_GLASS_PANE, "ENTER"));
  }
  
  public void CardOperations_1() {
    // check if player has a card
    for (ItemStack i : player.getInventory().getContents()) {
      if (i != null && i.hasItemMeta() && i.getItemMeta() != null && i.getItemMeta().hasLore() && i.getItemMeta().getLore() != null && i.getItemMeta().getLore().get(0).equals("Serial number:")) {
        Inventory j = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select Card...");
        player.openInventory(j);
        return;
      }
    }
    //this.newCard_3();
  }
  
  public void cardOperations_2(String serial) {
    this.serial = serial;
  }
  
  public void checkFares_1() {
    this.player.sendMessage("fare checking under test");
  }
  
  public void generateTicket(ItemStack item) {
    // TODO: do the money stuff
    player.getInventory().remove(item);
    player.getInventory().addItem(makeButton(Material.PAPER, ChatColor.GREEN+"Train Ticket", station, String.valueOf(value)));
  }
  
  public void generateTicket() {
    // TODO: do the money stuff
    player.getInventory().addItem(makeButton(Material.PAPER, ChatColor.GREEN+"Train Ticket", station, String.valueOf(value)));
  }
  
  public String getStation() {
    return station;
  }
  
  public String getOperator() {
    return operator;
  }
  
  public String getSerial() {
    return serial;
  }
  
  public void setSerial(String serial) {
    this.serial = serial;
  }
  
  public double getValue() {
    return value;
  }
  
  public void setValue(double value) {
    this.value = value;
  }
  
  public Hashtable<Integer, Double> getDaysList() {
    return daysList;
  }
  
  public void setDaysList(Hashtable<Integer, Double> daysList) {
    this.daysList = daysList;
  }
  
  public CardSql getApp() {
    return app;
  }
  
  public Owners getOwners() {
    return owners;
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
  
  protected ItemStack makeButton(final Material material, final String displayName, final List<String> lore) {
    ItemStack item = new ItemStack(material, 1);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    itemMeta.setLore(lore);
    item.setItemMeta(itemMeta);
    return item;
  }
  
}
