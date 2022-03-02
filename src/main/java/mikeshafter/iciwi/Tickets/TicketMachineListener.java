package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import mikeshafter.iciwi.Owners;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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
import java.util.*;
import java.util.regex.Pattern;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class TicketMachineListener implements Listener {
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final CardSql cardSql = new CardSql();
  private final Owners owners = new Owners(plugin);
  private final Lang lang = new Lang(plugin);
  private String operator;
  private TicketMachine machine;


  @EventHandler
  public void TMSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign sign) {
      String signLine0 = ChatColor.stripColor(sign.getLine(0));
      Player player = event.getPlayer();

      if (signLine0.equalsIgnoreCase("["+lang.TICKETS()+"]")) {
        // Figure out which ticket machine is to be used
        String machineType = plugin.getConfig().getString("ticket-machine-type");
  
        String station;
        if (Objects.equals(machineType, "GLOBAL")) {
          station = lang.GLOBAL_TICKET();
          machine = new GlobalTicketMachine(player);
          this.operator = plugin.getConfig().getString("global-operator");
        } else {
          station = ChatColor.stripColor(sign.getLine(1)).replaceAll("\\s+", "");
          this.operator = owners.getOwner(station);
          machine = new TicketMachine(player, station);
        }
        machine.newTM_0();
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
      if (inventoryName.equals(lang.__TICKET_MACHINE())) {
        event.setCancelled(true);
    
        if (itemName.equals(lang.NEW_TICKET())) {
          if (Objects.equals(plugin.getConfig().getString("ticket-machine-type"), "GLOBAL")) {
            machine.generateTicket(plugin.getConfig().getDouble("global-ticket-price"));
            player.closeInventory();
          } else machine.newTicket_1(0.0);
        } else if (itemName.equals(lang.ADJUST_FARES())) machine.adjustFares_1();
        else if (itemName.equals(lang.CARD_OPERATIONS())) machine.cardOperations_1();
        else if (itemName.equals(lang.CHECK_FARES())) {
          machine.checkFares_1(1);
          player.closeInventory();
        }
    
      }
  
      // == New Ticket : Page 1 ==
      else if (inventoryName.contains(lang.__NEW_TICKET())) {
        value = Double.parseDouble(inventoryName.substring(lang.__NEW_TICKET().length()+lang.CURRENCY().length()));
    
        event.setCancelled(true);
    
        if (itemName.equals(lang.CLEAR())) machine.newTicket_1(0.0);
    
        else if (itemName.equals(lang.ENTER())) {
          player.closeInventory();
          machine.generateTicket(value);
        } else {
          double numberPressed = Double.parseDouble(itemName);
          value = (Math.round(value*1000)+numberPressed)/100;
          machine.newTicket_1(value);
        }
    
      }
  
      // == Adjust Fares : Page 1 ==
      else if (inventoryName.equals(lang.__SELECT_TICKET())) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null) {
          player.closeInventory();
          // String station = item.getItemMeta().getLore().get(0);
          String ticketPrice = item.getItemMeta().getLore().get(1).substring(1);
          if (isDouble(ticketPrice)) machine.adjustFares_2(0.0, item);
          else player.sendMessage(lang.DIRECT_TICKET_INVALID());
        }
      }
  
      // == Adjust Fares : Page 2 ==
      else if (inventoryName.contains(lang.__ADJUST_FARES())) {
        value = Double.parseDouble(inventoryName.substring(lang.__ADJUST_FARES().length()+lang.CURRENCY().length()));
        ItemStack item0 = inventory.getItem(0);
    
        event.setCancelled(true);
    
        if (itemName.equals(lang.CLEAR())) machine.adjustFares_2(0.0, item0);
    
        else if (itemName.equals(lang.ENTER()) && item0 != null) {
          player.closeInventory();
          machine.generateTicket(item0, value);
        } else {
          double numberPressed = Integer.parseInt(itemName);
          value = (Math.round(value*1000)+numberPressed)/100;
          machine.adjustFares_2(value, item0);
        }
    
      }
  
      // == Card Operations : Page 1 ==
      else if (inventoryName.equals(lang.__SELECT_CARD())) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null && item.getItemMeta().getLore().get(0).equals(lang.SERIAL_NUMBER())) {
          player.closeInventory();
          String serial = item.getItemMeta().getLore().get(1);
          machine.cardOperations_2(serial);
        }
      }
  
      // == Card Operations : Page 2 ==
      else if (inventoryName.contains(lang.__CARD_OPERATION())) {
        String serial = inventoryName.substring(lang.__CARD_OPERATION().length());
        if (!itemName.equals(lang.CARD_DETAILS())) {
          player.closeInventory();
          if (itemName.equals(lang.NEW_CARD())) {
            machine.newCard_3();
          } else if (itemName.equals(lang.TOP_UP_CARD())) {
            machine.topUp_3(serial);
          } else if (itemName.equals(lang.RAIL_PASS())) {
            machine.railPass_3(serial, this.operator);
          } else if (itemName.equals(lang.REFUND_CARD())) {
            // search for player's card
            for (ItemStack itemStack : player.getInventory().getContents()) {
              // get loreStack
              if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().getLore() != null && Objects.equals(itemStack.getItemMeta().getLore().get(0), lang.SERIAL_NUMBER())) {
                // get serialNumber
                if (itemStack.getItemMeta().getLore().get(1).equals(serial)) {
                  double remainingValue = cardSql.getCardValue(serial);
                  Iciwi.economy.depositPlayer(player, remainingValue);
                  player.getInventory().remove(itemStack);
                  cardSql.delCard(serial);
                  player.sendMessage(String.format(lang.CARD_REFUNDED(), serial, remainingValue));
                  break;
                }
              }
            }
        
          }
        }
      }
  
      // == New Card : Page 3 ==
      else if (inventoryName.equals(lang.__SELECT_VALUE())) {
        if (itemName.contains(lang.CURRENCY())) {
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
            cardSql.newCard(lang.SERIAL_PREFIX()+sum+"-"+serial, val);
            player.sendMessage(String.format(lang.NEW_CARD_CREATED(), deposit, val));
            player.getInventory().addItem(makeButton(lang.PLUGIN_NAME(), lang.SERIAL_NUMBER(), lang.SERIAL_PREFIX()+sum+"-"+serial));
          } else player.sendMessage(lang.NOT_ENOUGH_MONEY());
        }
      }
  
      // == Top Up : Page 3 ==
      else if (inventoryName.contains(lang.__TOP_UP())) {
        String serial = inventoryName.substring(lang.__TOP_UP().length());
        if (isDouble(itemName.replaceAll("[^\\d.]", ""))) {
          double val = Double.parseDouble(itemName.replaceAll("[^\\d.]", ""));
      
          player.closeInventory();
      
          // Top up existing card
          if (Iciwi.economy.getBalance(player) >= val) {
            Iciwi.economy.withdrawPlayer(player, val);
            player.sendMessage(String.format(lang.CARD_TOPPED_UP(), val));
            cardSql.addValueToCard(serial, val);
        
          } else player.sendMessage(lang.NOT_ENOUGH_MONEY());
        }
      }
  
      // == Rail Pass : Page 3 ==
      else if (inventoryName.contains(lang.__ADD_RAIL_PASS())) {
        String serial = inventoryName.substring(lang.__ADD_RAIL_PASS().length());
  
        // Check discounts
        if (itemName.equals(lang.CARD_DISCOUNTS())) {
          event.setCancelled(true);
          // Return a menu
          List<TextComponent> discountList = cardSql.getDiscountedOperators(serial).entrySet().stream()
              .sorted(Map.Entry.comparingByValue())
              .map(entry -> Component.text().content(
                      "\u00A76- \u00A7a"+entry.getKey()+"\u00a76 | Exp. "+String.format("\u00a7b%s\n", new Date(entry.getValue()*1000)))
                  .append(Component.text().content("\u00a76 | Extend \u00a7a"))
                  .append(owners.getRailPassDays(entry.getKey()).stream().map(days -> Component.text().content("["+days+"d: \u00a7a"+owners.getRailPassPrice(entry.getKey(), Long.parseLong(days))+"\u00a76]").clickEvent(ClickEvent.runCommand("/newdiscount "+serial+" "+entry.getKey()+" "+days))).toList())
                  .build()).toList();
    
          TextComponent menu = Component.text().content("==== Rail Passes You Own ====\n").color(NamedTextColor.GOLD).build();
  
          for (TextComponent displayEntry : discountList) menu = menu.append(displayEntry);
    
          Audience audience = (Audience) player;
          audience.sendMessage(menu);
        }
  
        // Buy new rail pass
        else {
          long days = Long.parseLong(itemName.replaceAll("[^\\d.]", ""));
          double price = owners.getRailPassPrice(this.operator, days);
    
          if (Iciwi.economy.getBalance(player) >= price) {
      
            player.closeInventory();
      
            Iciwi.economy.withdrawPlayer(player, price);
            long expiry = days*86400+Instant.now().getEpochSecond();
            player.sendMessage(String.format(lang.ADDED_RAIL_PASS(), this.operator, days, price));
            if (cardSql.getDiscountedOperators(serial).containsKey(operator))
              cardSql.renewDiscount(serial, operator, days*86400);
            else cardSql.setDiscount(serial, operator, expiry);
            owners.deposit(operator, price);
      
          } else player.sendMessage(lang.NOT_ENOUGH_MONEY());
        }
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
