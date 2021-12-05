package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import mikeshafter.iciwi.Owners;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Set;
import java.util.regex.Pattern;


public class GlobalTicketMachine extends TicketMachine {
  
  private final CardSql app = getApp();
  private final Owners owners = getOwners();
  private final Lang lang = getLang();
  
  public GlobalTicketMachine(Player player) {
    super(player, null);
  }
  
  public void newTM_0() {
    Inventory tm = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Ticket Machine");
    
    tm.setItem(1, super.makeButton(Material.PAPER, lang.NEW_TICKET, String.valueOf(plugin.getConfig().getDouble("global-ticket-price"))));
    tm.setItem(4, super.makeButton(Material.NAME_TAG, lang.CARD_OPERATIONS));
    tm.setItem(7, super.makeButton(Material.BOOK, lang.CHECK_FARES));
    
    getPlayer().openInventory(tm);
  }
  
  public void generateTicket(double value) {
    if (Iciwi.economy.getBalance(getPlayer()) >= value) {
      Iciwi.economy.withdrawPlayer(getPlayer(), value);
    }
    getPlayer().sendMessage(String.format(lang.GENERATE_TICKET_GLOBAL, value));
    getPlayer().getInventory().addItem(makeButton(Material.PAPER, lang.TRAIN_TICKET, lang.GLOBAL_TICKET, String.format("%.2f", value)));
  }
  
  public void newSingleJourneyTicket_1() {
    double value = plugin.getConfig().getDouble("global-ticket-price");
    if (Iciwi.economy.getBalance(super.getPlayer()) >= value) {
      Iciwi.economy.withdrawPlayer(super.getPlayer(), value);
    }
    super.getPlayer().getInventory().addItem(makeButton(Material.PAPER, lang.TRAIN_TICKET, lang.GLOBAL_TICKET, String.valueOf(value)));
  }
  
  public void railPass_3(String serial) {
    String operator = plugin.getConfig().getString("global-operator");
    Inventory i = plugin.getServer().createInventory(null, 9, String.format(lang.__ADD_RAIL_PASS+"%s", serial));
    Set<String> daysSet = owners.getRailPassDays(operator);
    for (String days : daysSet) {
      double price = owners.getRailPassPrice(operator, Integer.parseInt(days));
      i.addItem(makeButton(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN+days+lang.DAYS, String.valueOf(price)));
    }
    super.getPlayer().openInventory(i);
  }
  
  private boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }
  
}
