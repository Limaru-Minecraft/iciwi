package mikeshafter.iciwi.tm;


import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.StationOwners;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static mikeshafter.iciwi.Iciwi.economy;
import static mikeshafter.iciwi.StationOwners.getOwner;
import static mikeshafter.iciwi.tm.MakeButton.makeButton;


public class TicketMachine implements Listener {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
  private final CardSql app = new CardSql();
  private final double[] priceArray = {10, 20, 30, 50, 70, 100};
  double val;
  private String serial, station;
  private String operator;
  private ArrayList<String[]> daysList; // {{days, price}, ...}
  
  
  @EventHandler
  public void TMSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign) {
      Sign sign = (Sign) event.getClickedBlock().getState();
      String signLine0 = ChatColor.stripColor(sign.getLine(0));
      String station = ChatColor.stripColor(sign.getLine(1)).replaceAll("\\s+", "");
      
      if ((signLine0.equalsIgnoreCase("[Tickets]") || signLine0.equalsIgnoreCase("-Tickets-") || signLine0.equalsIgnoreCase("[Ticket Machine]")) && !sign.getLine(1).equals(ChatColor.BOLD+"Buy/Top Up")) {
        this.newTM(event.getPlayer(), station);
      }
    }
  }
  
  
  public void newTM(Player player, String station) {
    Inventory tm = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Ticket Machine");
    
    // Single Journey Ticket
    tm.setItem(1, makeButton(Material.PAPER, ChatColor.GREEN+"New Single Journey Ticket", station));
    tm.setItem(3, makeButton(Material.MAP, ChatColor.YELLOW+"Adjust Fares"));
    tm.setItem(5, makeButton(Material.NAME_TAG, ChatColor.LIGHT_PURPLE+"ICIWI Card Operations", station));
    tm.setItem(7, makeButton(Material.BOOK, ChatColor.AQUA+"Check Fares", station));
    
    player.openInventory(tm);
  }
  
  public void cardOps(Player player, String serial, String station) {
    this.serial = serial;
    this.station = station;
    
    double value = app.getCardValue(serial);
    
    Inventory cardOps = plugin.getServer().createInventory(null, 9, String.format(ChatColor.DARK_BLUE+"Remaining value: £%.2f", value));
    
    // Buttons
    ItemStack[] buttons = {
        makeButton(Material.MAGENTA_WOOL, ChatColor.LIGHT_PURPLE+"New ICIWI Card"),
        makeButton(Material.CYAN_WOOL, ChatColor.AQUA+"Top Up ICIWI Card"),
        makeButton(Material.LIME_WOOL, ChatColor.GREEN+"New Rail Pass", getOwner(station)),
        makeButton(Material.ORANGE_WOOL, ChatColor.GOLD+"Refund"),
    };
    for (int i = 0; i < buttons.length; i++) {
      cardOps.setItem(i, buttons[i]);
    }
    player.openInventory(cardOps);
  }
  
  @EventHandler
  public void TMClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    if (inventory == null) return;
    
    // if there is no item clicked
    if (item == null || !item.hasItemMeta()) {
      return;
    }
    
    // === newTM method ===
    if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Ticket Machine")) {
      event.setCancelled(true);
      val = 0;
      String name = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      
      // Buy a single journey ticket
      if (name.equals(ChatColor.GREEN+"New Single Journey Ticket")) {
        player.closeInventory();
        String station = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getLore()).get(0);
        keypad(player, station, 0d, 0d);
      }
      
      // Adjust fares
      else if (name.equals(ChatColor.YELLOW+"Adjust Fares")) {
        player.closeInventory();
        Inventory selectTicket = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select Ticket...");
        player.sendMessage(player.getName()+" DEBUG 2c");  // TODO: DEBUG
        player.openInventory(selectTicket);
      }
      
      // Check Value and Top Up
      else if (name.equals(ChatColor.LIGHT_PURPLE+"ICIWI Card Operations")) {
        player.closeInventory();
        
        boolean skipSelectCard = true;
        // Check if player holds an ICIWI card
        for (ItemStack i : player.getInventory().getContents()) {
          if (i != null && i.hasItemMeta() && i.getItemMeta() != null && i.getItemMeta().hasLore() && i.getItemMeta().getLore() != null && i.getItemMeta().getLore().get(0).equals("Serial number:")) {
            // If yes, make the player select the card
            Inventory selectCard = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select ICIWI Card...");
            // Keep station name
            String station = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getLore()).get(0);
            selectCard.setItem(4, makeButton(Material.LIGHT_GRAY_STAINED_GLASS_PANE, station));
            // Open inventory
            player.openInventory(selectCard);
            skipSelectCard = false;
            break;
          }
        }
        if (skipSelectCard) {
          player.closeInventory();
          cardPrice(player, null);
        }
        // Else, proceed straight to buying a new card
      }

      // Check fares
      else if (name.equals(ChatColor.AQUA+"Check Fares")) {
        player.closeInventory();
        String station = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getLore()).get(0);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+player.getName()+" {\"text\":\">> Fare chart <<\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://mineshafter61.github.io/LimaruSite/farecharts/"+station+".png\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Click to view the fare chart.\"}}");
      }
    }


    // === SELECT METHODS === - Ticket
    else if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"Select Ticket...")) {
      event.setCancelled(true);
      // Get station ticket is from
      if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null) {
        player.closeInventory();
        String station = item.getItemMeta().getLore().get(0);
        String ticketPrice = item.getItemMeta().getLore().get(1);
        if (MakeButton.isDouble(ticketPrice)) keypad(player, station, 0, Double.parseDouble(ticketPrice));
        else player.sendMessage(ChatColor.RED+"Invalid ticket! Direct tickets cannot be adjusted!");
      }
    }


    // === SELECT METHODS === - Card
    else if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Select ICIWI Card...")) {
      event.setCancelled(true);
      if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null && item.getItemMeta().getLore().get(0).equals("Serial number:")) {
        player.closeInventory();
        String serial = item.getItemMeta().getLore().get(1);
        String station = Objects.requireNonNull(Objects.requireNonNull(inventory.getItem(4)).getItemMeta()).getDisplayName();
        cardOps(player, serial, station);
      }
    }


    // === CARD OPS ===
    else if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"Remaining value: ")) {
      String name = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
  
      if (name.equals(ChatColor.LIGHT_PURPLE+"New ICIWI Card")) {
        player.closeInventory();
        cardPrice(player, null);
    
      } else if (name.equals(ChatColor.AQUA+"Top Up ICIWI Card")) {
        // Use private variables this.serial and this.station
        player.closeInventory();
        cardPrice(player, this.serial);
    
      } else if (name.equals(ChatColor.GREEN+"New Rail Pass")) {
        // Use private variables this.serial and this.station
        player.closeInventory();
        railPass(player, this.serial, this.station);
    
      } else if (name.equals(ChatColor.GOLD+"Refund")) {
        // Use private variables this.serial and this.station
    
        economy.depositPlayer(player, 5d+app.getCardValue(this.serial));
        player.sendMessage(String.format(ChatColor.GREEN+"Refunded card "+ChatColor.YELLOW+serial+ChatColor.GREEN+". Received "+ChatColor.YELLOW+"£%.2f"+ChatColor.GREEN+".", 5d+app.getCardValue(serial)));
    
        app.delCard(this.serial);
        for (ItemStack card : player.getInventory().getContents())
          if (card.hasItemMeta() && card.getItemMeta() != null && card.getItemMeta().hasLore() && card.getItemMeta().getLore() != null && card.getItemMeta().getLore() == Arrays.asList("Serial number:", this.serial))
            player.getInventory().remove(card);
        player.closeInventory();
      }
    }


    // === CARD PRICE SELECTOR ===
    else if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Select value...") && event.getRawSlot() < priceArray.length) {
      event.setCancelled(true);
      int i = event.getRawSlot();
      double val = priceArray[i];
  
      if (serial != null) {
        // Top up existing card
        if (economy.getBalance(player) >= val) {
          economy.withdrawPlayer(player, val);
          player.sendMessage(ChatColor.GREEN+"Topped up "+ChatColor.YELLOW+"£"+val+".");
          app.addValueToCard(serial, val);
        } else player.sendMessage(ChatColor.RED+"You do not have enough money!");
      } else {
        // generate a new card
        if (economy.getBalance(player) >= 5.0+val) {
          // Take money from player and send message
          economy.withdrawPlayer(player, 5.0+val);
          player.sendMessage(ChatColor.GREEN+"Deposit: "+ChatColor.YELLOW+"£5.00"+ChatColor.GREEN+". Current card value: "+ChatColor.YELLOW+"£"+val);
          // Prepare card
          int serial = new SecureRandom().nextInt(100000);
          char sum = new char[] {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[
                         ((serial%10)*2+(serial/10%10)*3+(serial/100%10)*5+(serial/1000%10)*7+(serial/10000)*9)%19
                         ];
          app.newCard("I"+sum+"-"+serial, val);
          player.getInventory().addItem(MakeButton.makeButton(Material.NAME_TAG, ChatColor.GREEN+"ICIWI Card", "Serial number:", "I"+sum+"-"+serial));
          player.closeInventory();
        } else player.sendMessage(ChatColor.RED+"You do not have enough money!");
      }
      player.closeInventory();
    }
  
  
    // === RAIL PASS SELECTOR ===
    if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"New Rail Pass") && event.getRawSlot() < daysList.size()) {
      event.setCancelled(true);
      long days = Long.parseLong(daysList.get(event.getRawSlot())[0]);
      double price = Double.parseDouble(daysList.get(event.getRawSlot())[1]);
    
      player.closeInventory();
      economy.withdrawPlayer(player, price);
      player.sendMessage(String.format("§aPaid §e£%.2f§a for a %s §e%s-day§a rail pass.", price, operator, days));
      app.setDiscount(serial, operator, days*86400+Instant.now().getEpochSecond());
    }
  
  
    // === KEYPAD ===
    if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"New Ticket")) {
    
      ItemStack dummyItem = inventory.getItem(0);
      assert dummyItem != null;
      String station = Objects.requireNonNull(Objects.requireNonNull(dummyItem.getItemMeta()).getLore()).get(0);
      double former = Double.parseDouble(Objects.requireNonNull(dummyItem.getItemMeta().getLore()).get(1));
    
      double current = Double.parseDouble(event.getView().getTitle().split("£")[1]);
    
      // get the name of the item
      String name = "";
      try {
        name = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      } catch (Exception ignored) {
      }
    
      // Reset value
      if (name.equals("CLEAR")) {
        keypad(player, station, 0, former);
      }

      // Done with keying in values
      else if (name.equals("ENTER")) {
        if (economy.getBalance(player) >= (current-former) && current >= former) {
          // Take money from player
          economy.withdrawPlayer(player, (current-former));
          // Place the money inside the coffers
          String stationOwner = StationOwners.getOwner(station);
          StationOwners.deposit(stationOwner, (current-former));
          // Send message
          player.sendMessage(ChatColor.GREEN+"Paid the following amount for the train ticket: "+ChatColor.YELLOW+"£"+(current-former));
          // Prepare card
          ItemStack card = new ItemStack(Material.PAPER, 1);
          ItemMeta cardMeta = card.getItemMeta();
          assert cardMeta != null;
          cardMeta.setDisplayName(ChatColor.AQUA+"Train Ticket");
          cardMeta.setLore(Arrays.asList(station, String.valueOf(current)));
          card.setItemMeta(cardMeta);
          player.getInventory().addItem(card);
          player.closeInventory();
        } else
          player.sendMessage(ChatColor.RED+"You do not have enough money, or the value you have entered is less than the previous value!");
      }

      // Pressed a number
      else {
        try {
          float value = Float.parseFloat(name)/100.0f;
          current = Math.round((current*10.0+value)*100.0)/100.0;
          keypad(player, station, current, former);
          event.setCancelled(true);
        } catch (NumberFormatException ignored) {
        }
      }
    }
  }
  
  // === KEYPAD ===
  public void keypad(Player player, String station, double current, double former) {
    Inventory keypad;
    player.sendMessage(player.getName()+" DEBUG 2b");  // TODO: DEBUG
    
    // New paper ticket
    if (current == former) {
      if (current == 0.0)
        keypad = plugin.getServer().createInventory(null, 36, ChatColor.DARK_BLUE+"New Ticket - Enter Value");
      else
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.DARK_BLUE+"New Ticket - £%.2f", current));
    } else {
      if (current == 0.0)
        keypad = plugin.getServer().createInventory(null, 36, ChatColor.DARK_BLUE+"Adjust Ticket - Enter New Value");
      else
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.DARK_BLUE+"Adjust Ticket - £%.2f", current));
    }
    
    // === Items ===
    keypad.setItem(0, makeButton(Material.PAPER, ChatColor.GREEN+"Train Ticket", station, String.format("£%.2f", former)));
    for (int[] i : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      keypad.setItem(i[0], makeButton(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(i[1])));
    }
    keypad.setItem(30, makeButton(Material.RED_STAINED_GLASS_PANE, "CLEAR"));
    keypad.setItem(32, makeButton(Material.LIME_STAINED_GLASS_PANE, "ENTER"));
    
    player.openInventory(keypad);
  }
  
  public void cardPrice(Player player, String serial) {
    // Serial can be null. If serial is null, create new card.
    this.serial = serial;
    
    Inventory cardPrice = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select value...");
    player.sendMessage(player.getName()+" DEBUG 2a");  // TODO: DEBUG
    
    for (double v : priceArray)
      cardPrice.addItem(makeButton(Material.PURPLE_STAINED_GLASS_PANE, String.format(ChatColor.GREEN+"£%.2f", v)));
    
    player.openInventory(cardPrice);
  }
  
  public void railPass(Player player, String serial, String station) {
    this.serial = serial;
    this.operator = StationOwners.getOwner(station);
    this.daysList = new ArrayList<>();
    
    Inventory railPass = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"New Rail Pass");
    
    for (String days : Objects.requireNonNull(StationOwners.get().getConfigurationSection("RailPassPrices."+operator)).getKeys(false)) {
      double price = StationOwners.getRailPassPrice(operator, Integer.parseInt(days));
      this.daysList.add(new String[] {days, String.valueOf(price)});
      railPass.addItem(makeButton(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN+days+" Day(s)", String.valueOf(price)));
    }
    player.openInventory(railPass);
  }
  
}
