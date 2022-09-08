package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.regex.Pattern;


public class GlobalTicketMachine extends TicketMachine {

  public GlobalTicketMachine(Player player) {
    super(player, null);
  }

  public void newTM_0() {
    Inventory tm = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Ticket Machine");
  
    tm.setItem(1, super.makeButton(Material.PAPER, lang.getString("menu-new-ticket"), String.valueOf(plugin.getConfig().getDouble("global-ticket-price"))));
    tm.setItem(4, super.makeButton(Material.NAME_TAG, lang.getString("card-operations")));
    tm.setItem(7, super.makeButton(Material.BOOK, lang.getString("check-fares")));
  
    getPlayer().openInventory(tm);
  }
  
  @Override
  public void cardOperations_2(String serial) {
    Inventory i = plugin.getServer().createInventory(null, 9, String.format(lang.getString("card-operation")+"%s", serial));
    double cardValue = app.getCardValue(serial);
    i.setItem(0, makeButton(Material.NAME_TAG, lang.getString("card-details"), String.format(lang.getString("serial-number")+"%s", serial), String.format(lang.getString("remaining-value")+lang.getString("currency")+"%.2f", cardValue)));
    i.setItem(1, makeButton(Material.MAGENTA_WOOL, lang.getString("new-card")));
    i.setItem(2, makeButton(Material.CYAN_WOOL, lang.getString("top-up-card")));
    i.setItem(3, makeButton(Material.LIME_WOOL, lang.getString("menu-rail-pass"), plugin.getConfig().getString("global-operator")));
    i.setItem(4, makeButton(Material.ORANGE_WOOL, lang.getString("refund-card")));
    super.getPlayer().openInventory(i);
  }
  
  public void generateTicket(double value) {
    if (Iciwi.economy.getBalance(getPlayer()) >= value) {
      Iciwi.economy.withdrawPlayer(getPlayer(), value);
    }
    getPlayer().sendMessage(String.format(lang.getString("generate-ticket-global"), value));
    getPlayer().getInventory().addItem(makeButton(Material.PAPER, lang.getString("train-ticket"), lang.getString("global-ticket"), String.format("%.2f", value)));
  }
  
  private boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }
  
}
