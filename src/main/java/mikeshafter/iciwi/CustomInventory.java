package mikeshafter.iciwi;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class CustomInventory implements Listener{
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
  //  enum Route {A,B,C}
  // Number Pad
//  public void numPad(Player player, Route route) {
//    Inventory inventory = plugin.getServer().createInventory(null, 36, ChatColor.GOLD+"Ticket Machine");
//    ItemStack button = new ItemStack(Material.STONE_BUTTON,1);
//    ItemMeta buttonMeta = button.getItemMeta();
//    assert buttonMeta != null;
//
//    int j = 1;
//    for (int i = 0; i < 27; i++){
//      if (i%9 > 3 && i%9 < 7) {
//        buttonMeta.setDisplayName(Integer.toString(j));
//        inventory.setItem(i, button);
//        j++;
//      }
//
//      buttonMeta.setDisplayName(".");
//      inventory.setItem(31, button);
//
//      buttonMeta.setDisplayName("0");
//      inventory.setItem(32, button);
//
//      buttonMeta.setDisplayName("Enter");
//      inventory.setItem(33, button);
//    }
//
//  }
  public void newTM(Player player){
    Inventory inventory = plugin.getServer().createInventory(null, 9, ChatColor.GOLD+"Ticket Machine");
    // New paper ticket
    ItemStack newTicket = new ItemStack(Material.PAPER, 1);
    ItemMeta newTicketMeta = newTicket.getItemMeta();
    assert newTicketMeta != null;
    newTicketMeta.setDisplayName("Buy New Train Ticket");
    inventory.setItem(1, newTicket);
    
    // New ICIWI
    ItemStack newICIWI = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta newICIWIMeta = newICIWI.getItemMeta();
    assert newICIWIMeta != null;
    newICIWIMeta.setDisplayName("Buy New ICIWI Card");
    inventory.setItem(4, newICIWI);
    
    // Top up ICIWI
    ItemStack topICIWI = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta topICIWIMeta = newICIWI.getItemMeta();
    assert topICIWIMeta != null;
    topICIWIMeta.setDisplayName("Top up existing ICIWI Card");
    inventory.setItem(4, topICIWI);
  }
  
  public void newTicket(Player player, String station){
    Inventory inventory = plugin.getServer().createInventory(null, 54, ChatColor.GOLD+"Ticket Machine");
    
    int invSize = 45;
    
    // First invSize slots are tickets
    for (int i = 0; i < invSize; i++){
      ItemStack ticket = new ItemStack(Material.PAPER, 1);
      ItemMeta ticketMeta = ticket.getItemMeta();
      assert ticketMeta != null;
      ticketMeta.setDisplayName("Train Ticket");
      ArrayList<String> lore = new ArrayList<>();
      // Set starting station
      lore.add(station);
      // Set price
      lore.add(Double.toString(Math.ceil((0.70+(double) i/10)*10)/10));
      ticketMeta.setLore(lore);
      ticket.setItemMeta(ticketMeta);
      inventory.setItem(i, ticket);
    }
    
    // Slot 47 gets you a new ICIWI card
    ItemStack newICIWI = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta newICIWIMeta = newICIWI.getItemMeta();
    assert newICIWIMeta != null;
    newICIWIMeta.setDisplayName("Buy New ICIWI Card");
    newICIWI.setItemMeta(newICIWIMeta);
    inventory.setItem(47, newICIWI);
    
    // Slot 51 is a top up slot
    ItemStack topUp = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta topUpMeta = topUp.getItemMeta();
    assert topUpMeta != null;
    topUpMeta.setDisplayName("Top Up ICIWI Card");
    topUp.setItemMeta(topUpMeta);
    inventory.setItem(51, topUp);
    
    player.openInventory(inventory);
  }
}
