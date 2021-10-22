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
import java.util.regex.Pattern;

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
    Inventory tm = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Ticket Machine");
    
    // Single Journey Ticket
    tm.setItem(1, makeButton(Material.PAPER, ChatColor.GREEN+"New Single Journey Ticket"));
    tm.setItem(3, makeButton(Material.MAP, ChatColor.YELLOW+"Adjust Fares"));
    tm.setItem(5, makeButton(Material.NAME_TAG, ChatColor.LIGHT_PURPLE+"ICIWI Card Operations"));
    tm.setItem(7, makeButton(Material.BOOK, ChatColor.AQUA+"Check Fares"));
    
    player.openInventory(tm);
  }
  
  protected ItemStack makeButton(final Material material, final String displayName) {
    ItemStack item = new ItemStack(material, 1);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    item.setItemMeta(itemMeta);
    return item;
  }
  
  public void newSingleJourneyTicket_1() {
    this.player.sendMessage("new ticket under test");
  }
  
  public void adjustFares_1() {
    this.player.sendMessage("adjust fares under test");
  }
  
  public void iciwiCardOperations_1() {
    this.player.sendMessage("iciwi card ops under test");
  }
  
  public void checkFares_1() {
    this.player.sendMessage("fare checking under test");
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
  
  protected boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }
}
