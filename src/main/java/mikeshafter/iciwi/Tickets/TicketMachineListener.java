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
        }
      }
    }
  }

  @EventHandler
  public void TMClick(InventoryClickEvent event) {

    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    if (inventory == null) return;

    if (item != null && item.hasItemMeta() && item.getItemMeta() != null) {
      String itemName = item.getItemMeta().getDisplayName();
      String inventoryName = event.getView().getTitle();

      // == Page 0 ==
      double value;
      if (inventoryName.equals(lang.__TICKET_MACHINE)) {

        if (itemName.equals(lang.NEW_TICKET)) machine.newTicket_1(0.0);
        else if (itemName.equals(lang.ADJUST_FARES)) machine.adjustFares_1();
        else if (itemName.equals(lang.CARD_OPERATIONS)) machine.cardOperations_1();
        else if (itemName.equals(lang.CHECK_FARES)) machine.checkFares_1();

      }

      // == New Ticket : Page 1 ==
      else if (inventoryName.contains(lang.__NEW_TICKET)) {
        value = Double.parseDouble(inventoryName.substring(lang.__NEW_TICKET.length()+lang.CURRENCY.length()));

        player.closeInventory();

        if (itemName.equals(lang.CLEAR)) machine.newTicket_1(0.0);

        else if (itemName.equals(lang.ENTER)) machine.generateTicket(value);

        else {
          double numberPressed = Double.parseDouble(itemName);
          value = (Math.round(value*1000)+numberPressed)/100;
          machine.newTicket_1(value);
        }

      }

      // == Adjust Fares : Page 1 ==
      else if (inventoryName.equals(lang.__SELECT_TICKET)) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null) {
          player.closeInventory();
          // String station = item.getItemMeta().getLore().get(0);
          String ticketPrice = item.getItemMeta().getLore().get(1).substring(1);
          if (isDouble(ticketPrice)) machine.adjustFares_2(0.0, item);
          else player.sendMessage(lang.DIRECT_TICKET_INVALID);
        }
      }

      // == Adjust Fares : Page 2 ==
      else if (inventoryName.contains(lang.__ADJUST_FARES)) {
        value = Double.parseDouble(inventoryName.substring(lang.__ADJUST_FARES.length()+lang.CURRENCY.length()));
        ItemStack item0 = inventory.getItem(0);
        player.closeInventory();

        if (itemName.equals(lang.CLEAR)) machine.adjustFares_2(0.0, item0);

        else if (itemName.equals(lang.ENTER) && item0 != null) {
          machine.generateTicket(item0, value);
        } else {
          double numberPressed = Integer.parseInt(itemName);
          value = (Math.round(value*1000)+numberPressed)/100;
          machine.adjustFares_2(value, item0);
        }

      }

      // == Card Operations : Page 1 ==
      else if (inventoryName.equals(lang.__SELECT_CARD)) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null && item.getItemMeta().getLore().get(0).equals(lang.SERIAL_NUMBER)) {
          player.closeInventory();
          String serial = item.getItemMeta().getLore().get(1);
          machine.cardOperations_2(serial);
        }
      }

      // == Card Operations : Page 2 ==
      else if (inventoryName.contains(lang.__CARD_OPERATION)) {
        String serial = inventoryName.substring(lang.__CARD_OPERATION.length());
        if (!itemName.equals(lang.CARD_DETAILS)) {
          player.closeInventory();
          if (itemName.equals(lang.NEW_CARD)) {
            machine.newCard_3();
          } else if (itemName.equals(lang.TOP_UP_CARD)) {
            machine.topUp_3(serial);
          } else if (itemName.equals(lang.ADD_RAIL_PASS)) {
            machine.railPass_3(serial, this.operator);
          } else if (itemName.equals(lang.REFUND_CARD)) {
            // search for player's card
            for (ItemStack itemStack : player.getInventory().getContents()) {
              // get loreStack
              if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().getLore() != null && Objects.equals(itemStack.getItemMeta().getLore().get(0), lang.SERIAL_NUMBER)) {
                // get serialNumber
                String serialNumber = itemStack.getItemMeta().getLore().get(1);
                double remainingValue = app.getCardValue(serialNumber);
                Iciwi.economy.depositPlayer(player, remainingValue);
                player.getInventory().remove(itemStack);
                app.delCard(serialNumber);
                break;
              }
            }

          }
        }
      }

      // == New Card : Page 3 ==
      else if (inventoryName.equals(lang.__SELECT_VALUE)) {
        if (itemName.contains(lang.CURRENCY)) {
          double val = Double.parseDouble(itemName.replaceAll("[^\\d.]", ""));
          double deposit = plugin.getConfig().getDouble("deposit");

          player.closeInventory();

          if (Iciwi.economy.getBalance(player) >= deposit+val) {
            // Take money from player and send message
            Iciwi.economy.withdrawPlayer(player, deposit+val);
            // Prepare card
            int serial = new SecureRandom().nextInt(100000);
            char sum = new char[] {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[
                           ((serial%10)*2+(serial/10%10)*3+(serial/100%10)*5+(serial/1000%10)*7+(serial/10000)*9)%19
                           ];
            app.newCard(lang.SERIAL_PREFIX+sum+"-"+serial, val);
            player.sendMessage(String.format(lang.NEW_CARD_CREATED, deposit, val));
            player.getInventory().addItem(makeButton(lang.PLUGIN_NAME, lang.SERIAL_NUMBER, lang.SERIAL_PREFIX+sum+"-"+serial));
          } else player.sendMessage(lang.NOT_ENOUGH_MONEY);
        }
      }

      // == Top Up : Page 3 ==
      else if (inventoryName.contains(lang.__TOP_UP)) {
        String serial = inventoryName.substring(lang.__TOP_UP.length());
        if (isDouble(itemName.replaceAll("[^\\d.]", ""))) {
          double val = Double.parseDouble(itemName.replaceAll("[^\\d.]", ""));

          player.closeInventory();

          // Top up existing card
          if (Iciwi.economy.getBalance(player) >= val) {
            Iciwi.economy.withdrawPlayer(player, val);
            player.sendMessage(String.format(lang.CARD_TOPPED_UP, val));
            app.addValueToCard(serial, val);

          } else player.sendMessage(lang.NOT_ENOUGH_MONEY);
        }
      }

      // == Rail Pass : Page 3 ==
      else if (inventoryName.contains(lang.__ADD_RAIL_PASS)) {
        String serial = inventoryName.substring(lang.__ADD_RAIL_PASS.length());
        long days = Long.parseLong(itemName.replaceAll("[^\\d.]", ""));
        double price = owners.getRailPassPrice(this.operator, days);

        if (Iciwi.economy.getBalance(player) >= price) {
          app.setDiscount(serial, this.operator, System.currentTimeMillis()+days*86400000);

          player.closeInventory();

          Iciwi.economy.withdrawPlayer(player, price);
          player.sendMessage(String.format(lang.ADDED_RAIL_PASS, this.operator, days, price));
          app.setDiscount(serial, operator, days*86400+Instant.now().getEpochSecond());

        } else player.sendMessage(lang.NOT_ENOUGH_MONEY);
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
