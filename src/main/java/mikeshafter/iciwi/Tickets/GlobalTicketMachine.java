package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import mikeshafter.iciwi.Owners;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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
    
    tm.setItem(1, super.makeButton(Material.PAPER, lang.NEW_TICKET(), String.valueOf(plugin.getConfig().getDouble("global-ticket-price"))));
    tm.setItem(4, super.makeButton(Material.NAME_TAG, lang.CARD_OPERATIONS()));
    tm.setItem(7, super.makeButton(Material.BOOK, lang.CHECK_FARES()));
    
    getPlayer().openInventory(tm);
  }
  
  @Override
  public void cardOperations_2(String serial) {
    Inventory i = plugin.getServer().createInventory(null, 9, String.format(lang.__CARD_OPERATION()+"%s", serial));
    double cardValue = app.getCardValue(serial);
    i.setItem(0, makeButton(Material.NAME_TAG, lang.CARD_DETAILS(), String.format(lang.SERIAL_NUMBER()+"%s", serial), String.format(lang.REMAINING_VALUE()+lang.CURRENCY()+"%.2f", cardValue)));
    i.setItem(1, makeButton(Material.MAGENTA_WOOL, lang.NEW_CARD()));
    i.setItem(2, makeButton(Material.CYAN_WOOL, lang.TOP_UP_CARD()));
    i.setItem(3, makeButton(Material.LIME_WOOL, lang.ADD_RAIL_PASS(), plugin.getConfig().getString("global-operator")));
    i.setItem(4, makeButton(Material.ORANGE_WOOL, lang.REFUND_CARD()));
    super.getPlayer().openInventory(i);
  }
  
  @Override
  public void railPass_3(String serial, String operator) {
    super.railPass_3(serial, operator);
  }
  
  @Override
  public void cardOperations_1() {
    super.cardOperations_1();
  }
  
  @Override
  public void newCard_3() {
    super.newCard_3();
  }
  
  public void generateTicket(double value) {
    if (Iciwi.economy.getBalance(getPlayer()) >= value) {
      Iciwi.economy.withdrawPlayer(getPlayer(), value);
    }
    getPlayer().sendMessage(String.format(lang.GENERATE_TICKET_GLOBAL(), value));
    getPlayer().getInventory().addItem(makeButton(Material.PAPER, lang.TRAIN_TICKET(), lang.GLOBAL_TICKET(), String.format("%.2f", value)));
  }
  
  @Override
  public void topUp_3(String serial) {
    super.topUp_3(serial);
  }
  
  @Override
  public void checkFares_1() {
    super.checkFares_1();
  }
  
  private boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }
  
}
