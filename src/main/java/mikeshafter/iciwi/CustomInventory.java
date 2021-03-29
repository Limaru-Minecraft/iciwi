package mikeshafter.iciwi;

import org.bukkit.Bukkit;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

import static mikeshafter.iciwi.Iciwi.economy;


public class CustomInventory implements Listener{
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);


  String station;
  double val;
  ItemStack cardToCharge;
  
  
  public void newTM(Player player, String sta) {
    station = sta;
    Inventory tm = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Ticket Machine");
    
    // Single Journey Ticket
    ItemStack item = new ItemStack(Material.PAPER, 1);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(ChatColor.GREEN+"Buy a single journey ticket");
    ArrayList<String> lore = new ArrayList<>();
    lore.add(station);
    itemMeta.setLore(lore);
    item.setItemMeta(itemMeta);
    tm.setItem(0, item);
    
    // New ICIWI Card
    item.setType(Material.NAME_TAG);
    itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE+"Buy New ICIWI Card");
    itemMeta.setLore(null);
    item.setItemMeta(itemMeta);
    tm.setItem(2, item);
    
    // Top Up Card
    itemMeta.setDisplayName(ChatColor.YELLOW+"Check Value and Top Up");
    item.setItemMeta(itemMeta);
    tm.setItem(4, item);
    
    // Check Fares
    item.setType(Material.PAPER);
    itemMeta.setDisplayName(ChatColor.AQUA+"Check fares");
    itemMeta.setLore(lore);
    item.setItemMeta(itemMeta);
    tm.setItem(6, item);
    
    // Refund Card
    item.setType(Material.NAME_TAG);
    itemMeta.setDisplayName(ChatColor.GOLD+"Refund ICIWI Card");
    itemMeta.setLore(null);
    item.setItemMeta(itemMeta);
    tm.setItem(8, item);
    
    player.openInventory(tm);
  }


  @EventHandler
  public void TMClick(InventoryClickEvent event){
    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    if (inventory == null) return;
    CardSql app = new CardSql();

    // === newTM method ===
    if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Ticket Machine")){
      val = 0;
      if (item == null || !item.hasItemMeta()){
        return;
      }
      String temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName();

      // Buy a single journey ticket
      if (temp.equals(ChatColor.GREEN+"Buy a single journey ticket")){
        player.closeInventory();
        station = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getLore()).get(0);
        newKeypad(player, 1, 0.00, station);
      }

      // Buy New ICIWI Card
      else if (temp.equals(ChatColor.LIGHT_PURPLE+"Buy New ICIWI Card")) {
        player.closeInventory();
        newKeypad(player, 2, 0.00, ChatColor.BLUE+"ICIWI Card");
      }

      // Check Value and Top Up
      else if (temp.equals(ChatColor.YELLOW+"Check Value and Top Up")) {
        player.closeInventory();
        Inventory selectCard = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select ICIWI Card...");
        player.openInventory(selectCard);
      }

      // Check fares
      else if (temp.equals(ChatColor.AQUA+"Check fares")) {
        player.closeInventory();
        station = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getLore()).get(0);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+player.getName()+" {\"text\":\">> Fare chart <<\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://mineshafter61.github.io/LimaruSite/farecharts/"+station+".png\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Click to view the fare chart.\"}}");
      }

      // Refund
      else if (temp.equals(ChatColor.GOLD+"Refund ICIWI Card")) {
        player.closeInventory();
        Inventory selectCard = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select ICIWI Card(s) to refund...");
        player.openInventory(selectCard);
      }
    }


    // === selectCard (top up) ===
    else if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"Select ICIWI Card...")) {
      event.setCancelled(true);
  
      if (item == null || !item.hasItemMeta()) return;
      // Check if it's really an ICIWI card
      String temp = "";
      try {
        temp = Objects.requireNonNull(Objects.requireNonNull(item.getItemMeta()).getLore()).get(0);
      } catch (Exception ignored) {
      }
      if (temp.equals("Serial number:")) {
        player.closeInventory();
        cardToCharge = item;
        newKeypad(player, 3, 0.0, ChatColor.BLUE+"Amount to top up:");
      }
    }


    // === refund card ===
    else if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Select ICIWI Card(s) to refund...")){
      event.setCancelled(true);

      if (item == null || !item.hasItemMeta()){
        return;
      }
      String temp = "";
      // Check if it's really an ICIWI card
      try{
        temp = Objects.requireNonNull(Objects.requireNonNull(item.getItemMeta()).getLore()).get(0);
      } catch (Exception ignored){
      }
      // if yes, delete the card
      if (temp.equals("Serial number:")){
        String serial_prefix = Objects.requireNonNull(Objects.requireNonNull(item.getItemMeta()).getLore()).get(1).substring(0,2);
        int serial = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(item.getItemMeta()).getLore()).get(1).substring(3));
        economy.depositPlayer(player, 5d+app.getCardValue(serial_prefix, serial));
        app.delCard(serial_prefix, serial);
        // ==
        player.getInventory().remove(item);
      }
    } // end of select card


    // === newKeypad/New ICIWI Card ===
    else if (event.getView().getTitle().contains(ChatColor.BLUE+"New ICIWI Card")) {
      event.setCancelled(true);
      if (item == null || !item.hasItemMeta()) {
        return;
      }
      String temp = "";
      try {
        temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      } catch (Exception ignored) {
      }
  
      // Reset value
      if (temp.equals("CLEAR")) {
        newKeypad(player, 2, 0.00, ChatColor.BLUE+"ICIWI Card");
        val = 0.0d;
      }
      // Done with keying in values
      else if (temp.equals("ENTER")){
        economy.withdrawPlayer(player, 5.0+val);
        player.sendMessage(ChatColor.GREEN+"Fare of card: "+ChatColor.YELLOW+"£5.00"+ChatColor.GREEN+". Current card value: "+ChatColor.YELLOW+"£"+val);
        ItemStack card = new ItemStack(Material.NAME_TAG, 1);
        ItemMeta cardMeta = card.getItemMeta();
        assert cardMeta != null;
        cardMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");
        int serial = (int) (Math.floor(Instant.now().getEpochSecond())%100000 + (int) (Math.random())*110000 );
        char sum = new char[] {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[(serial%10 + serial/10%10 + serial/100%10 + serial/1000%10 + serial/10000) % 19];
        app.newCard("I"+sum, serial, val);
    
    
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Serial number:");
        lore.add("I"+sum+"-"+serial);
        cardMeta.setLore(lore);
        card.setItemMeta(cardMeta);
        player.getInventory().addItem(card);
        player.closeInventory();
        val = 0.0d;
      }
      // Pressed a number
      else {
        try{
          float value = Float.parseFloat(temp)/100.0f;
          val = Math.round((val*10.0+value)*100.0)/100.0;
          newKeypad(player, 2, val, ChatColor.BLUE+"ICIWI Card - £"+val);
        } catch (NumberFormatException ignored) {
        }
      }
    }


    // === newKeypad/New paper ticket ===
    else if (event.getView().getTitle().contains(ChatColor.BLUE+"New Single Journey Ticket")) {
      event.setCancelled(true);
      if (item == null || !item.hasItemMeta()) {
        return;
      }
      String temp = "";
      try {
        temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      } catch (Exception ignored) {
      }
  
      // Reset value
      if (temp.equals("CLEAR")) {
        newKeypad(player, 1, 0.00, station);
        val = 0.0d;
      }
      // Done with keying in values
      else if (temp.equals("ENTER")){
        if (economy.getBalance(player) >= val){
          economy.withdrawPlayer(player, val);
          player.sendMessage(ChatColor.GREEN+"Paid the following amount for the train ticket: "+ChatColor.YELLOW+val);
          ItemStack card = new ItemStack(Material.PAPER, 1);
          ItemMeta cardMeta = card.getItemMeta();
          assert cardMeta != null;
          cardMeta.setDisplayName(ChatColor.AQUA+"Train Ticket");
          ArrayList<String> lore = new ArrayList<>();
          lore.add(station);
          lore.add(String.format("%.2f", val));
          cardMeta.setLore(lore);
          card.setItemMeta(cardMeta);
          player.getInventory().addItem(card);
          player.closeInventory();
          val = 0.0d;
        } else player.sendMessage(ChatColor.RED+"You do not have enough money in your bank account!");
      }
      // Pressed a number
      else {
        try{
          float value = Float.parseFloat(temp)/100.0f;
          val = Math.round((val*10.0+value)*100.0)/100.0;
          newKeypad(player, 1, val, station);
        } catch (NumberFormatException ignored) {
        }
      }
    }


    // === newKeypad/Top up ICIWI ===
    else if (event.getView().getTitle().contains(ChatColor.DARK_AQUA+"Balance: ")) {
      event.setCancelled(true);
      if (item == null || !item.hasItemMeta()) {
        return;
      }
      String temp = "";
      try {
        temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      } catch (Exception ignored) {
      }
  
      // Reset value
      if (temp.equals("CLEAR")) {
        newKeypad(player, 3, 0.00, ChatColor.BLUE+"ICIWI Card");
        val = 0.0d;
      }
      // Done with keying in values
      else if (temp.equals("ENTER")){
        economy.withdrawPlayer(player, val);
        player.sendMessage(ChatColor.GREEN+"Topped up "+ChatColor.YELLOW+"£"+val+".");
        // Get card number
        String serial_prefix = Objects.requireNonNull(Objects.requireNonNull(cardToCharge.getItemMeta()).getLore()).get(1).substring(0,2);
        int serial = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(cardToCharge.getItemMeta()).getLore()).get(1).substring(3));
        double cval = app.getCardValue(serial_prefix, serial);
        app.updateCard(serial_prefix,serial,cval+val);
        player.closeInventory();
        val = 0.0d;
      }
      // Pressed a number
      else {
        try{
          float value = Float.parseFloat(temp)/100.0f;
          val = Math.round((val*10.0+value)*100.0)/100.0;
          newKeypad(player, 3, val, ChatColor.BLUE+"ICIWI Card");
        } catch (NumberFormatException ignored){
        }
      }
    }

  }


  public void newKeypad(Player player, int action, double current, String lore1){
    Inventory keypad;
    ItemStack amount;
    ItemMeta newTicketMeta;

    // New Iciwi card
    if (action == 2){
      if (current == 0.0)
        keypad = plugin.getServer().createInventory(null, 36, ChatColor.BLUE+"New ICIWI Card - Enter Value");
      else
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.BLUE+"New ICIWI Card - £%.2f", current));

      amount = new ItemStack(Material.NAME_TAG, 1);
      newTicketMeta = amount.getItemMeta();
      assert newTicketMeta != null;
      newTicketMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");
    }

    // Top up iciwi card
    else if (action == 3) {
      String serial_prefix = Objects.requireNonNull(Objects.requireNonNull(cardToCharge.getItemMeta()).getLore()).get(1).substring(0, 2);
      int serial = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(cardToCharge.getItemMeta()).getLore()).get(1).substring(3));
      if (current == 0.0)
        keypad = plugin.getServer().createInventory(null, 36, ChatColor.DARK_AQUA+"Balance: "+new CardSql().getCardValue(serial_prefix, serial)+ChatColor.BLUE+" Top Up: £0.00");
      else
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.BLUE+"Top Up: £%.2f", current));
  
      amount = new ItemStack(Material.NAME_TAG, 1);
      newTicketMeta = amount.getItemMeta();
      assert newTicketMeta != null;
      newTicketMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");
      // Put the existing ICIWI card in Slot 9
      keypad.setItem(9, cardToCharge);

    }

    // New paper ticket
    else {
      if (current == 0.0)
        keypad = plugin.getServer().createInventory(null, 36, ChatColor.BLUE+"New Ticket - Enter Value");
      else
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.BLUE+"New Ticket - £%.2f", current));

      amount = new ItemStack(Material.PAPER, 1);
      newTicketMeta = amount.getItemMeta();
      assert newTicketMeta != null;
      newTicketMeta.setDisplayName(ChatColor.GREEN+"Train Ticket");
    }

    // === Items ===

    // Top left dummy item
    ArrayList<String> lore = new ArrayList<>();
    lore.add(lore1);
    lore.add(String.format("£%.2f", current));
    newTicketMeta.setLore(lore);
    amount.setItemMeta(newTicketMeta);
    keypad.setItem(0, amount);

    // Keypad buttons
    ItemStack button = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
    ItemMeta buttonMeta = button.getItemMeta();
    assert buttonMeta != null;
    for (int[] i : new int[][]{{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}}){
      buttonMeta.setDisplayName(String.valueOf(i[1]));
      button.setItemMeta(buttonMeta);
      keypad.setItem(i[0], button);
    }
    button.setType(Material.RED_STAINED_GLASS_PANE);
    buttonMeta.setDisplayName("CLEAR");
    button.setItemMeta(buttonMeta);
    keypad.setItem(30, button);

    button.setType(Material.GRAY_STAINED_GLASS_PANE);
    buttonMeta.setDisplayName("0");
    button.setItemMeta(buttonMeta);
    keypad.setItem(31, button);

    button.setType(Material.LIME_STAINED_GLASS_PANE);
    buttonMeta.setDisplayName("ENTER");
    button.setItemMeta(buttonMeta);
    keypad.setItem(32, button);

    player.openInventory(keypad);
  }
}
