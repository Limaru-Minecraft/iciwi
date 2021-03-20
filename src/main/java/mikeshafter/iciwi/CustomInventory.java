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

  //TODO: Make SQL database to store cards
  //TODO: Make cards use a serial number

  public void newTM(Player player, String sta){
    station = sta;
    Inventory tm = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Ticket Machine");

    // Single Journey Ticket
    ItemStack newTicket = new ItemStack(Material.PAPER, 1);
    ItemMeta newTicketMeta = newTicket.getItemMeta();
    assert newTicketMeta != null;
    newTicketMeta.setDisplayName(ChatColor.GREEN+"Buy a single journey ticket");
    ArrayList<String> lore = new ArrayList<>();
    lore.add(station);
    newTicketMeta.setLore(lore);
    newTicket.setItemMeta(newTicketMeta);
    tm.setItem(0, newTicket);

    // New ICIWI Card
    ItemStack newICIWI = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta newICIWIMeta = newICIWI.getItemMeta();
    assert newICIWIMeta != null;
    newICIWIMeta.setDisplayName(ChatColor.LIGHT_PURPLE+"Buy New ICIWI Card");
    newICIWI.setItemMeta(newICIWIMeta);
    tm.setItem(2, newICIWI);

    // Top Up Card
    ItemStack topUp = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta topUpMeta = topUp.getItemMeta();
    assert topUpMeta != null;
    topUpMeta.setDisplayName(ChatColor.YELLOW+"Top Up ICIWI Card");
    topUp.setItemMeta(topUpMeta);
    tm.setItem(4, topUp);

    // Check Fares
    ItemStack checkFare = new ItemStack(Material.PAPER, 1);
    ItemMeta checkFareMeta = topUp.getItemMeta();
    assert checkFareMeta != null;
    checkFareMeta.setDisplayName(ChatColor.AQUA+"Check fares");
    checkFareMeta.setLore(lore);
    checkFare.setItemMeta(checkFareMeta);
    tm.setItem(6, checkFare);

    // Refund Card
    ItemStack refund = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta refundMeta = topUp.getItemMeta();
    assert refundMeta != null;
    refundMeta.setDisplayName(ChatColor.GOLD+"Refund ICIWI Card");
    refund.setItemMeta(refundMeta);
    tm.setItem(8, refund);

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
      else if (temp.equals(ChatColor.LIGHT_PURPLE+"Buy New ICIWI Card")){
        player.closeInventory();
        newKeypad(player, 2, 0.00, ChatColor.BLUE+"ICIWI Card");
      }

      // Top Up ICIWI Card
      else if (temp.equals(ChatColor.YELLOW+"Top Up ICIWI Card")){
        player.closeInventory();
        Inventory selectCard = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select ICIWI Card to top up...");
        player.openInventory(selectCard);
      }

      // Check fares
      else if (temp.equals(ChatColor.AQUA+"Check fares")){
        player.closeInventory();
        station = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getLore()).get(0);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+player.getName()+" {\"text\":\">> Fare chart <<\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://mineshafter61.github.io/LimaruSite/farecharts/"+station+".png\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Click to view the fare chart.\"}}");
      }

      // Refund
      else if (temp.equals(ChatColor.GOLD+"Refund ICIWI Card")){
        player.closeInventory();
        Inventory selectCard = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select ICIWI Card(s) to refund...");
        player.openInventory(selectCard);
      }
    }


    // === selectCard (top up) ===
    else if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"Select ICIWI Card to top up...")){
      event.setCancelled(true);

      if (item == null || !item.hasItemMeta()) return;
      // Check if it's really an ICIWI card
      String temp = "";
      try{
        temp = Objects.requireNonNull(Objects.requireNonNull(item.getItemMeta()).getLore()).get(0);
      } catch (Exception ignored){
      }
      if (temp.equals("Serial number:")){
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
        //TODO: Change to SQL
        //economy.depositPlayer(player, 5d+Double.parseDouble(Objects.requireNonNull(Objects.requireNonNull(item.getItemMeta()).getLore()).get(1)));
        //new code
        String serial_prefix = Objects.requireNonNull(Objects.requireNonNull(item.getItemMeta()).getLore()).get(1).substring(0,2);
        int serial = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(item.getItemMeta()).getLore()).get(1).substring(3));
        economy.depositPlayer(player, 5d + app.getCardValue(serial_prefix, serial));
        app.delCard(serial_prefix, serial);
        // ==
        player.getInventory().remove(item);
      }
    } // end of select card


    // === newKeypad/New ICIWI Card ===
    else if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"New ICIWI Card")){
      event.setCancelled(true);
      if (item == null || !item.hasItemMeta()){
        return;
      }
      String temp = "";
      try{ temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName(); } catch (Exception ignored){}

      // Reset value
      if (temp.equals("CLEAR")){
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

        //TODO: Change to SQL
//        ArrayList<String> lore = new ArrayList<>();
//        lore.add("Remaining value:");
//        lore.add(String.format("%.2f", val));
//        cardMeta.setLore(lore);

        //New code here
        int serial = (int) (Math.floor(Instant.now().getEpochSecond())%100000 + (int) (Math.random())*110000 );
        char sum = new char[] {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[String.valueOf(serial).chars().map(Character::getNumericValue).sum()%19];
        app.newCard("I"+sum, serial, val);
        //
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
        } catch (NumberFormatException ignored){
        }
      }
    }


    // === newKeypad/New paper ticket ===
    else if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"New Single Journey Ticket")){
      event.setCancelled(true);
      if (item == null || !item.hasItemMeta()){
        return;
      }
      String temp = "";
      try{ temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName(); } catch (Exception ignored){}

      // Reset value
      if (temp.equals("CLEAR")){
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
        } catch (NumberFormatException ignored){
        }
      }
    }


    // === newKeypad/Top up ICIWI ===
    else if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"Top Up ICIWI Card")){
      event.setCancelled(true);
      if (item == null || !item.hasItemMeta()){
        return;
      }
      String temp = "";
      try{ temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName(); } catch (Exception ignored){}

      // Reset value
      if (temp.equals("CLEAR")){
        newKeypad(player, 3, 0.00, ChatColor.BLUE+"ICIWI Card");
        val = 0.0d;
      }
      // Done with keying in values
      else if (temp.equals("ENTER")){
//        cardToCharge.setAmount(0);
        economy.withdrawPlayer(player, val);
        player.sendMessage(ChatColor.GREEN+"Topped up "+ChatColor.YELLOW+"£"+val+".");
        // ItemStack card = new ItemStack(Material.NAME_TAG, 1);
        // ItemMeta cardMeta = card.getItemMeta();
        // assert cardMeta != null;
        // cardMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");

        //TODO: Change to SQL
//        double cval = Double.parseDouble(Objects.requireNonNull(Objects.requireNonNull(cardToCharge.getItemMeta()).getLore()).get(1));
//        ArrayList<String> lore = new ArrayList<>();
//        lore.add("Remaining value:");
//        lore.add(String.format("%.2f", cval+val));
//        cardMeta.setLore(lore);
        //New code
        String serial_prefix = Objects.requireNonNull(Objects.requireNonNull(cardToCharge.getItemMeta()).getLore()).get(1).substring(0,2);
        int serial = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(cardToCharge.getItemMeta()).getLore()).get(1).substring(3));
        double cval = app.getCardValue(serial_prefix, serial);
        app.updateCard(serial_prefix,serial,cval+val);
        // ==

        // card.setItemMeta(cardMeta);
        // player.getInventory().addItem(card);
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
        keypad = plugin.getServer().createInventory(null, 36, ChatColor.DARK_BLUE+"New ICIWI Card - Enter Value");
      else
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.DARK_BLUE+"New ICIWI Card - £%.2f", current));

      amount = new ItemStack(Material.NAME_TAG, 1);
      newTicketMeta = amount.getItemMeta();
      assert newTicketMeta != null;
      newTicketMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");
    }

    // Top up iciwi card
    else if (action == 3){
      if (current == 0.0)
        keypad = plugin.getServer().createInventory(null, 36, ChatColor.DARK_BLUE+"Top Up ICIWI Card - Enter Value");
      else
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.DARK_BLUE+"Top Up ICIWI Card - £%.2f", current));

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
        keypad = plugin.getServer().createInventory(null, 36, ChatColor.DARK_BLUE+"New Single Journey Ticket - Enter Value");
      else
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.DARK_BLUE+"New Single Journey Ticket - £%.2f", current));

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
