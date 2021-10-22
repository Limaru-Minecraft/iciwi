package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import mikeshafter.iciwi.Owners;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class TicketMachineListener implements Listener {
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final CardSql app = new CardSql();
  private final Owners owners = new Owners(plugin);
  private final Lang lang = new Lang(plugin);
  private double value = 0.0;
  private String serial, station;
  private String operator;
  private ArrayList<String[]> daysList; // {{days, price}, ...}
  private TicketMachine machine;
  
  
  @EventHandler
  public void TMSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign sign) {
      String signLine0 = ChatColor.stripColor(sign.getLine(0));
      String station = ChatColor.stripColor(sign.getLine(1)).replaceAll("\\s+", "");
      Player player = event.getPlayer();
      
      if ((signLine0.equalsIgnoreCase("[Tickets]") || signLine0.equalsIgnoreCase("-Tickets-") || signLine0.equalsIgnoreCase("[Ticket Machine]")) && !sign.getLine(1).equals(ChatColor.BOLD+"Buy/Top Up")) {
        // Figure out which ticket machine is to be used
        String machineType = plugin.getConfig().getString("ticket-machine-type");
        if (Objects.equals(machineType, "COMPANY")) {
          String company = owners.getOwner(station);
          machine = new CompanyTicketMachine(player, company);
          machine.newTM_0();
        } else if (Objects.equals(machineType, "GLOBAL")) {
          machine = new GlobalTicketMachine(player);
          machine.newTM_0();
        } else {
          machine = new TicketMachine(player, station);
          machine.newTM_0();
        }
      }
    }
  }
  
  @EventHandler
  public void TMClick(InventoryClickEvent event) {
    // Lang Constants
    String __TICKET_MACHINE = ChatColor.DARK_BLUE+"Ticket Machine";
    String NEW_TICKET = ChatColor.GREEN+"New Single Journey Ticket";
    String ADJUST_FARES = ChatColor.YELLOW+"Adjust Fares";
    String CARD_OPERATIONS = ChatColor.LIGHT_PURPLE+"ICIWI Card Operations";
    String CHECK_FARES = ChatColor.AQUA+"Check Fares";
  
    String __NEW_TICKET = ChatColor.DARK_BLUE+"New Ticket - ";
    String CLEAR = "CLEAR";
    String ENTER = "ENTER";
  
    String __SELECT_TICKET = ChatColor.DARK_BLUE+"Select Ticket...";
    String __ADJUST_FARES = ChatColor.DARK_BLUE+"Adjust Fare - ";
  
  
    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    if (inventory == null) return;
    event.setCancelled(true);
  
    if (item != null && item.hasItemMeta() && item.getItemMeta() != null) {
      String itemName = item.getItemMeta().getDisplayName();
      String inventoryName = event.getView().getTitle();
      player.closeInventory();
    
      // == Page 0 ==
      if (inventoryName.equals(__TICKET_MACHINE)) {
      
        if (itemName.equals(NEW_TICKET)) machine.newTicket_1(0.0);
        else if (itemName.equals(ADJUST_FARES)) machine.adjustFares_1();
        else if (itemName.equals(CARD_OPERATIONS)) machine.CardOperations_1();
        else if (itemName.equals(CHECK_FARES)) machine.checkFares_1();
      
      }
    
      // == Page 1: New Ticket ==
      else if (inventoryName.contains(__NEW_TICKET)) {
        value = Double.parseDouble(inventoryName.substring(__NEW_TICKET.length()));
        player.closeInventory();
      
        if (itemName.equals(CLEAR)) machine.newTicket_1(0.0);
      
        else if (itemName.equals(ENTER)) machine.generateTicket();
      
        else {
          double numberPressed = Integer.parseInt(itemName);
          value = Math.round((value*10.0+numberPressed)*100.0)/100.0;
          machine.newTicket_1(value);
        }
      
      }
    
      // == Page 1: Adjust Fares ==
      else if (inventoryName.equals(__SELECT_TICKET)) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null) {
          player.closeInventory();
          String station = item.getItemMeta().getLore().get(0);
          String ticketPrice = item.getItemMeta().getLore().get(1).substring(1);
          if (isDouble(ticketPrice)) machine.adjustFares_2(0.0, item);
          else player.sendMessage(ChatColor.RED+"Invalid ticket! Direct tickets cannot be adjusted!");
        }
      }
    
      // == Page 2: Adjust Fares ==
      else if (inventoryName.contains(__ADJUST_FARES)) {
        value = Double.parseDouble(inventoryName.substring(__ADJUST_FARES.length()));
        ItemStack item0 = inventory.getItem(0);
        player.closeInventory();
      
        if (itemName.equals(CLEAR)) machine.adjustFares_2(0.0, item0);
      
        else if (itemName.equals(ENTER)) {
          machine.generateTicket(item0);
        } else {
          double numberPressed = Integer.parseInt(itemName);
          value = Math.round((value*10.0+numberPressed)*100.0)/100.0;
          machine.adjustFares_2(value, item0);
        }
      
      }
    
      // == Page 1: Card Operations ==
    
    }
  }
  
  private boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }
}
