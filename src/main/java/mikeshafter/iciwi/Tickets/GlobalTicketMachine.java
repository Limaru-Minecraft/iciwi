package mikeshafter.iciwi.Tickets;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;


public class GlobalTicketMachine extends TicketMachine {
  public GlobalTicketMachine(Player player) {
    super(player, null);
  }
  
  public void newTM_0() {
    Inventory tm = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Ticket Machine");
    
    // Single Journey Ticket
    tm.setItem(1, makeButton(Material.PAPER, ChatColor.GREEN+"New Single Journey Ticket"));
    tm.setItem(4, makeButton(Material.NAME_TAG, ChatColor.LIGHT_PURPLE+"ICIWI Card Operations"));
    tm.setItem(7, makeButton(Material.BOOK, ChatColor.AQUA+"Check Fares"));
    
    getPlayer().openInventory(tm);
  }
  
  public void newSingleJourneyTicket_1() {
    getPlayer().sendMessage("change this");
  }
  
}
