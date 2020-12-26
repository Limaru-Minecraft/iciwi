package mikeshafter.iciwi;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Objects;

import static mikeshafter.iciwi.Iciwi.economy;

public class CustomInventory implements Listener{
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
  
  String station;
  double val;
  ItemStack cardToCharge;
  
  
  public void newTM(Player player, String sta){
    station = sta;
    Inventory tm = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Ticket Machine");
    
    ItemStack newTicket = new ItemStack(Material.PAPER, 1);
    ItemMeta newTicketMeta = newTicket.getItemMeta();
    assert newTicketMeta != null;
    newTicketMeta.setDisplayName(ChatColor.GREEN+"Buy a single journey ticket");
    ArrayList<String> lore = new ArrayList<>();
    lore.add(station);
    lore.add("");
    newTicketMeta.setLore(lore);
    newTicket.setItemMeta(newTicketMeta);
    tm.setItem(1, newTicket);
    
    ItemStack newICIWI = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta newICIWIMeta = newICIWI.getItemMeta();
    assert newICIWIMeta != null;
    newICIWIMeta.setDisplayName(ChatColor.LIGHT_PURPLE+"Buy New ICIWI Card");
    newICIWI.setItemMeta(newICIWIMeta);
    tm.setItem(4, newICIWI);
    
    ItemStack topUp = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta topUpMeta = topUp.getItemMeta();
    assert topUpMeta != null;
    topUpMeta.setDisplayName(ChatColor.YELLOW+"Top Up ICIWI Card");
    topUp.setItemMeta(topUpMeta);
    tm.setItem(7, topUp);
    
    player.openInventory(tm);
  }
  
  
  @EventHandler
  public void TMClick(InventoryClickEvent event){
    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    if (inventory == null){
      return;
    }
    
    // newTM method
    if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Ticket Machine")){
      val = 0;
      if (item == null || !item.hasItemMeta()){
        return;
      }
      String temp = "";
      try{
        temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      } catch (Exception e){
        e.printStackTrace();
      }
      if (temp.equals(ChatColor.GREEN+"Buy a single journey ticket")){
        player.closeInventory();
        station = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getLore()).get(0);
        newKeypad(player, 1, 0.00, station);
      } else if (temp.equals(ChatColor.LIGHT_PURPLE+"Buy New ICIWI Card")){
        player.closeInventory();
        newKeypad(player, 2, 0.00, ChatColor.BLUE+"ICIWI Card");
      } else if (temp.equals(ChatColor.YELLOW+"Top Up ICIWI Card")){
        player.closeInventory();
        Inventory selectCard = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select ICIWI Card...");
        player.openInventory(selectCard);
      }
    }
    
    
    // selectCard
    else if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Select ICIWI Card...")){
      event.setCancelled(true);
      
      if (item == null || !item.hasItemMeta()){
        return;
      }
      String temp = "";
      try{
        temp = Objects.requireNonNull(Objects.requireNonNull(item.getItemMeta()).getLore()).get(0);
      } catch (Exception ignored){
      }
      if (temp.equals("Remaining value:")){
        player.closeInventory();
        cardToCharge = item;
        newKeypad(player, 3, 0.0, ChatColor.BLUE+"Amount to top up:");
      }
    } // end of select card
    
    
    // newKeypad method: New ICIWI Card
    else if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"New ICIWI Card")){
      event.setCancelled(true);
      if (item == null || !item.hasItemMeta()){
        return;
      }
      String temp = "";
      try{
        temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      } catch (Exception e){
        e.printStackTrace();
      }
      if (temp.equals("CLEAR")){
        newKeypad(player, 2, 0.00, ChatColor.BLUE+"ICIWI Card");
        val = 0.0d;
      } else if (temp.equals("ENTER")){
        economy.withdrawPlayer(player, 5.0+val);
        player.sendMessage(ChatColor.GREEN+"Fare of card: "+ChatColor.YELLOW+"£5.00"+ChatColor.GREEN+". Current card value: "+ChatColor.YELLOW+val);
        ItemStack card = new ItemStack(Material.NAME_TAG, 1);
        ItemMeta cardMeta = card.getItemMeta();
        assert cardMeta != null;
        cardMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Remaining value:");
        lore.add(Double.toString(val));
        cardMeta.setLore(lore);
        card.setItemMeta(cardMeta);
        player.getInventory().addItem(card);
        player.closeInventory();
        val = 0.0d;
      } else {
        try{
          float value = Float.parseFloat(temp)/100.0f;
          val = Math.round((val*10.0+value)*100.0)/100.0;
          newKeypad(player, 2, val, ChatColor.BLUE+"ICIWI Card - £"+val);
        } catch (NumberFormatException ignored){
        }
      }
    } // End of New ICIWI card
    
    
    // newKeypad Method: New ticket
    else if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"New Single Journey Ticket")){
      event.setCancelled(true);
      if (item == null || !item.hasItemMeta()){
        return;
      }
      String temp = "";
      try{
        temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      } catch (Exception e){
        e.printStackTrace();
      }
      if (temp.equals("CLEAR")){
        newKeypad(player, 1, 0.00, station);
        val = 0.0d;
      } else if (temp.equals("ENTER")){
        economy.withdrawPlayer(player, val);
        player.sendMessage(ChatColor.GREEN+"Paid the following amount for the train ticket: "+ChatColor.YELLOW+val);
        ItemStack card = new ItemStack(Material.PAPER, 1);
        ItemMeta cardMeta = card.getItemMeta();
        assert cardMeta != null;
        cardMeta.setDisplayName(ChatColor.AQUA+"Train Ticket");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(station);
        lore.add(Double.toString(val));
        cardMeta.setLore(lore);
        card.setItemMeta(cardMeta);
        player.getInventory().addItem(card);
        player.closeInventory();
        val = 0.0d;
      } else {
        try{
          float value = Float.parseFloat(temp)/100.0f;
          val = Math.round((val*10.0+value)*100.0)/100.0;
          newKeypad(player, 1, val, station);
        } catch (NumberFormatException ignored){
        }
      }
    } // End of new ticket
    
    
    // newKeypad Method: Top up ICIWI
    else if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Top Up ICIWI Card")){
      event.setCancelled(true);
      if (item == null || !item.hasItemMeta()){
        return;
      }
      String temp = "";
      try{
        temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      } catch (Exception e){
        e.printStackTrace();
      }
      if (temp.equals("CLEAR")){
        newKeypad(player, 3, 0.00, ChatColor.BLUE+"ICIWI Card");
        val = 0.0d;
      } else if (temp.equals("ENTER")){
        double cval = Double.parseDouble(Objects.requireNonNull(Objects.requireNonNull(cardToCharge.getItemMeta()).getLore()).get(1));
        cardToCharge.setAmount(0);
        economy.withdrawPlayer(player, val);
        player.sendMessage(ChatColor.GREEN+"Topped up "+ChatColor.YELLOW+"£"+val+".");
        ItemStack card = new ItemStack(Material.NAME_TAG, 1);
        ItemMeta cardMeta = card.getItemMeta();
        assert cardMeta != null;
        cardMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Remaining value:");
        lore.add(Double.toString(cval+val));
        cardMeta.setLore(lore);
        card.setItemMeta(cardMeta);
        player.getInventory().addItem(card);
        player.closeInventory();
        val = 0.0d;
      } else {
        try{
          float value = Float.parseFloat(temp)/100.0f;
          val = Math.round((val*10.0+value)*100.0)/100.0;
          newKeypad(player, 3, val, ChatColor.BLUE+"ICIWI Card");
        } catch (NumberFormatException ignored){
        }
      }
    } // End of Top Up ICIWI
    
  } // End of TMClick
  
  
  public void newKeypad(Player player, int action, double current, String lore1){
    Inventory keypad;
    ItemStack amount;
    ItemMeta newTicketMeta;
    
    // Final amount display
    if (action == 2){
      if (current==0.0) {
    	  keypad = plugin.getServer().createInventory(null, 36, ChatColor.DARK_BLUE+"New ICIWI Card - Enter Value");  
      } else {
    	  keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.DARK_BLUE+"New ICIWI Card - £%.2f", current));
      }
      amount = new ItemStack(Material.NAME_TAG, 1);
      newTicketMeta = amount.getItemMeta();
      assert newTicketMeta != null;
      newTicketMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");
    } else if (action == 3){
      keypad = plugin.getServer().createInventory(null, 36, ChatColor.DARK_BLUE+"Top Up ICIWI Card");
      amount = new ItemStack(Material.NAME_TAG, 1);
      newTicketMeta = amount.getItemMeta();
      assert newTicketMeta != null;
      newTicketMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");
      
      // Put the existing ICIWI card in Slot 9
      keypad.setItem(9, cardToCharge);
    } else {/* action == 1 */
      keypad = plugin.getServer().createInventory(null, 36, ChatColor.DARK_BLUE+"New Single Journey Ticket");
      amount = new ItemStack(Material.PAPER, 1);
      newTicketMeta = amount.getItemMeta();
      assert newTicketMeta != null;
      newTicketMeta.setDisplayName(ChatColor.GREEN+"Train Ticket");
    }
    
    ArrayList<String> lore = new ArrayList<>();
    lore.add(lore1);
    lore.add(String.format("£%.2f", current));
    newTicketMeta.setLore(lore);
    amount.setItemMeta(newTicketMeta);
    keypad.setItem(0, amount);
    
    
    // Keypad buttons
    ItemStack button;
    ItemMeta buttonMeta;
    int j = 1;
    for (int i : new int[]{3, 4, 5, 12, 13, 14, 21, 22, 23}){
      button = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
      buttonMeta = button.getItemMeta();
      assert buttonMeta != null;
      buttonMeta.setDisplayName(String.valueOf(j));
      button.setItemMeta(buttonMeta);
      keypad.setItem(i, button);
      j++;
    }
    button = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
    buttonMeta = button.getItemMeta();
    assert buttonMeta != null;
    buttonMeta.setDisplayName("CLEAR");
    button.setItemMeta(buttonMeta);
    keypad.setItem(30, button);
    
    button = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
    buttonMeta = button.getItemMeta();
    assert buttonMeta != null;
    buttonMeta.setDisplayName("0");
    button.setItemMeta(buttonMeta);
    keypad.setItem(31, button);
    
    button = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
    buttonMeta = button.getItemMeta();
    assert buttonMeta != null;
    buttonMeta.setDisplayName("ENTER");
    button.setItemMeta(buttonMeta);
    keypad.setItem(32, button);
    
    player.openInventory(keypad);
  }
}
