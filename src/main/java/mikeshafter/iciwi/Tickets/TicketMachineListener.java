package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import mikeshafter.iciwi.Owners;
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
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class TicketMachineListener implements Listener {
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final CardSql app = new CardSql();
  private final Owners owners = new Owners(plugin);
  private final Lang lang = new Lang(plugin);
  private String operator;
  private TicketMachine machine;


  @EventHandler
  public void TMSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign sign) {
      String signLine0 = ChatColor.stripColor(sign.getLine(0));
      String station = ChatColor.stripColor(sign.getLine(1)).replaceAll("\\s+", "");
      this.operator = owners.getOwner(station);
      Player player = event.getPlayer();
      player.sendMessage("debug 0");  // TODO: DEBUG

      if (signLine0.equalsIgnoreCase("["+lang.TICKETS+"]") && !sign.getLine(1).equals(ChatColor.BOLD+"Buy/Top Up")) {
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
          player.sendMessage("debug 1");  // TODO: DEBUG
        }
      }
    }
  }

  @EventHandler
  public void TMClick(InventoryClickEvent event) {
    // Lang Constants
    String CURRENCY_SYMBOL = "£";
    String ICIWI_CARD = ChatColor.GREEN+"ICIWI Card";
    String SERIAL_PREFIX = "A";
    String NOT_ENOUGH_MONEY = ChatColor.RED+"You do not have enough money!";

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

    String __SELECT_CARD = ChatColor.DARK_BLUE+"Select Card...";
    String SERIAL_NUMBER = "Serial number:";
    String __CARD_OPERATION = ChatColor.DARK_BLUE+"Select Option - ";

    String __SELECT_VALUE = ChatColor.DARK_BLUE+"Select value...";

    String __TOP_UP = ChatColor.DARK_BLUE+"Top Up - ";

    String __ADD_RAIL_PASS = ChatColor.DARK_BLUE+"Add rail pass - ";


    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    if (inventory == null) return;
    player.sendMessage("Debug 0"); // TODO: Debug

    if (item != null && item.hasItemMeta() && item.getItemMeta() != null) {
      String itemName = item.getItemMeta().getDisplayName();
      String inventoryName = event.getView().getTitle();

      // == Page 0 ==
      double value;
      if (inventoryName.equals(__TICKET_MACHINE)) {
        player.sendMessage("Debug 0a");  // TODO: DEBUG

        if (itemName.equals(NEW_TICKET)) machine.newTicket_1(0.0);
        else if (itemName.equals(ADJUST_FARES)) machine.adjustFares_1();
        else if (itemName.equals(CARD_OPERATIONS)) machine.cardOperations_1();
        else if (itemName.equals(CHECK_FARES)) machine.checkFares_1();

      }

      // == New Ticket : Page 1 ==
      else if (inventoryName.contains(__NEW_TICKET)) {
        value = Double.parseDouble(inventoryName.substring(__NEW_TICKET.length()+lang.CURRENCY.length()));

        player.closeInventory();

        if (itemName.equals(CLEAR)) machine.newTicket_1(0.0);

        else if (itemName.equals(ENTER)) machine.generateTicket(value);

        else {
          player.sendMessage("Debug 1 New Ticket");  // TODO: DEBUG
          double numberPressed = Double.parseDouble(itemName);
          player.sendMessage(String.valueOf(numberPressed));
          player.sendMessage(String.valueOf(value));
          value = (Math.round(value*1000)+numberPressed)/100;
          player.sendMessage(String.valueOf(value));
          machine.newTicket_1(value);
        }

      }

      // == Adjust Fares : Page 1 ==
      else if (inventoryName.equals(__SELECT_TICKET)) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null) {
          player.sendMessage("Debug 1 Adjust Fares");  // TODO: DEBUG
          player.closeInventory();
          // String station = item.getItemMeta().getLore().get(0);
          String ticketPrice = item.getItemMeta().getLore().get(1).substring(1);
          if (isDouble(ticketPrice)) machine.adjustFares_2(0.0, item);
          else player.sendMessage(ChatColor.RED+"Invalid ticket! Direct tickets cannot be adjusted!");
        }
      }

      // == Adjust Fares : Page 2 ==
      else if (inventoryName.contains(__ADJUST_FARES)) {
        value = Double.parseDouble(inventoryName.substring(__ADJUST_FARES.length()+lang.CURRENCY.length()));
        ItemStack item0 = inventory.getItem(0);
        player.closeInventory();

        if (itemName.equals(CLEAR)) machine.adjustFares_2(0.0, item0);

        else if (itemName.equals(ENTER) && item0 != null) {
          machine.generateTicket(item0, value);
        } else {
          player.sendMessage("Debug 2 Adjust Fares");  // TODO: DEBUG
          double numberPressed = Integer.parseInt(itemName);
          value = (Math.round(value*1000)+numberPressed)/100;
          machine.adjustFares_2(value, item0);
        }

      }

      // == Card Operations : Page 1 ==
      else if (inventoryName.equals(__SELECT_CARD)) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null && item.getItemMeta().getLore().get(0).equals(SERIAL_NUMBER)) {
          player.sendMessage("Debug 1 Card Operations");  // TODO: DEBUG
          player.closeInventory();
          String serial = item.getItemMeta().getLore().get(1);
          machine.cardOperations_2(serial);
        }
      }

      // == Card Operations : Page 2 ==
      else if (inventoryName.contains(__CARD_OPERATION)) {
        String serial = inventoryName.substring(__CARD_OPERATION.length());
        if (!itemName.equals(ChatColor.YELLOW+"Card Details")) {
          player.closeInventory();
          player.sendMessage("Debug 2 Card Operations");  // TODO: DEBUG
          if (itemName.equals(ChatColor.LIGHT_PURPLE+"New ICIWI Card")) {
            machine.newCard_3();
          } else if (itemName.equals(ChatColor.AQUA+"Top Up ICIWI Card")) {
            machine.topUp_3(serial);
          } else if (itemName.equals(ChatColor.GREEN+"New Rail Pass")) {
            machine.railPass_3(serial, this.operator);
          } else if (itemName.equals(ChatColor.GOLD+"Refund Card")) {
            // search for player's card
            for (ItemStack itemStack : player.getInventory().getContents()) {
              // get loreStack
              if (itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().getLore() != null && Objects.equals(itemStack.getItemMeta().getLore().get(0), SERIAL_NUMBER)) {
                // get serialNumber
                String serialNumber = itemStack.getItemMeta().getLore().get(1);
                double remainingValue = app.getCardValue(serialNumber);
                Iciwi.economy.depositPlayer(player, remainingValue);
                player.getInventory().remove(itemStack);
                app.delCard(serialNumber);
                player.sendMessage("Debug 2a refund card");  // TODO: DEBUG
                break;
              }
            }

          }
        }
      }

      // == New Card : Page 3 ==
      else if (inventoryName.equals(__SELECT_VALUE)) {
        if (itemName.contains(CURRENCY_SYMBOL)) {
          double val = Double.parseDouble(itemName.replaceAll("[^\\d.]", ""));
          double deposit = plugin.getConfig().getDouble("deposit");

          player.closeInventory();

          if (Iciwi.economy.getBalance(player) >= deposit+val) {
            player.sendMessage("Debug 3 new card");  // TODO: DEBUG
            // Take money from player and send message
            Iciwi.economy.withdrawPlayer(player, deposit+val);
            // TODO: send message
            // Prepare card
            int serial = new SecureRandom().nextInt(100000);
            char sum = new char[] {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[
                           ((serial%10)*2+(serial/10%10)*3+(serial/100%10)*5+(serial/1000%10)*7+(serial/10000)*9)%19
                           ];
            app.newCard(SERIAL_PREFIX+sum+"-"+serial, val);
            player.getInventory().addItem(makeButton(ICIWI_CARD, SERIAL_NUMBER, SERIAL_PREFIX+sum+"-"+serial));
          } else player.sendMessage(NOT_ENOUGH_MONEY);
        }
      }

      // == Top Up : Page 3 ==
      else if (inventoryName.contains(__TOP_UP)) {
        String serial = inventoryName.substring(__TOP_UP.length());
        if (isDouble(itemName.replaceAll("[^\\d.]", ""))) {
          double val = Double.parseDouble(itemName.replaceAll("[^\\d.]", ""));
  
          player.closeInventory();
          player.sendMessage("Debug 3 top up card");  // TODO: DEBUG
  
          // Top up existing card
          if (Iciwi.economy.getBalance(player) >= val) {
            Iciwi.economy.withdrawPlayer(player, val);
            // TODO: send message
            app.addValueToCard(serial, val);
  
          } else player.sendMessage(NOT_ENOUGH_MONEY);
        }
      }

      // == Rail Pass : Page 3 ==
      else if (inventoryName.contains(__ADD_RAIL_PASS)) {
        String serial = inventoryName.substring(__ADD_RAIL_PASS.length());
        long days = Long.parseLong(itemName.replaceAll("[^\\d.]", ""));
        double price = owners.getRailPassPrice(this.operator, days);

        player.closeInventory();
        player.sendMessage("Debug 3 rail pass");  // TODO: DEBUG

        Iciwi.economy.withdrawPlayer(player, price);
        // TODO: send message
        app.setDiscount(serial, operator, days*86400+Instant.now().getEpochSecond());
      }
    }
  }

  private boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }

  protected ItemStack makeButton(final String displayName, final String... lore) {
    ItemStack item = new ItemStack(Material.NAME_TAG, 1);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    itemMeta.setLore(Arrays.asList(lore));
    item.setItemMeta(itemMeta);
    return item;
  }
}
