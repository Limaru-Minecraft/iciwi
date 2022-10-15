package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.MachineUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
      String signLine0 = MachineUtil.componentToString(sign.line(0));
      Player player = event.getPlayer();
  
      // === Normal ticket machine ===
  
      if (signLine0.equalsIgnoreCase("["+lang.getString("tickets")+"]")) {
        // Figure out which ticket machine is to be used
        String machineType = plugin.getConfig().getString("ticket-machine-type");
    
        String station;
  
        if (Objects.equals(machineType, "GLOBAL")) {
          machine = new GlobalTicketMachine(player);
          this.operator = plugin.getConfig().getString("global-operator");
        } else {
          station = ((TextComponent) sign.line(1)).content().replaceAll("\\s+", "");
          this.operator = owners.getOwner(station);
          machine = new TicketMachine(player, station);
        }
        machine.newTM_0();
      }

      // === Rail pass machine ===

      else if (signLine0.equalsIgnoreCase("["+lang.getString("passes")+"]")) {
        String company = MachineUtil.componentToString(sign.line(1)).replaceAll("\\s+", "");
        machine = new RailPassMachine(player, company);
        machine.newTM_0();
      }

      // === Custom machine ===

      else if (signLine0.equalsIgnoreCase("["+lang.getString("custom-tickets")+"]")) {
        String station = MachineUtil.componentToString(sign.line(1)).replaceAll("\\s+", "");
        new CustomMachine(player, station);
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
      String itemName = MachineUtil.componentToString(item.getItemMeta().displayName());
      String inventoryName = MachineUtil.componentToString(event.getView().title());
  
      // == Page 0 ==
      double value;
      if (inventoryName.equals(lang.getString("ticket-machine"))) {
        event.setCancelled(true);
    
        if (itemName.equals(lang.getString("menu-new-ticket"))) {
          if (Objects.equals(plugin.getConfig().getString("ticket-machine-type"), "GLOBAL")) {
            machine.generateTicket(plugin.getConfig().getDouble("global-ticket-price"));
            player.closeInventory();
          } else machine.newTicket_1(0.0);
        } else if (itemName.equals(lang.getString("menu-adjust-fares"))) machine.adjustFares_1();
        else if (itemName.equals(lang.getString("card-operations"))) machine.cardOperations_1();
        else if (itemName.equals(lang.getString("check-fares"))) {
          machine.checkFares_1(1);
          player.closeInventory();
        }
    
      }
  
      // == New Ticket : Page 1 ==
      else if (inventoryName.contains(lang.getString("menu-new-ticket"))) {
        value = Double.parseDouble(inventoryName.substring(lang.getString("menu-new-ticket").length()+lang.getString("currency").length()));
    
        event.setCancelled(true);
    
        if (itemName.equals(lang.getString("clear"))) machine.newTicket_1(0.0);
    
        else if (itemName.equals(lang.getString("enter"))) {
          player.closeInventory();
          machine.generateTicket(value);
        } else {
          double numberPressed = Double.parseDouble(itemName);
          value = (Math.round(value*1000)+numberPressed)/100;
          machine.newTicket_1(value);
        }
    
      }
  
      // == Adjust Fares : Page 1 ==
      else if (inventoryName.equals(lang.getString("select-ticket"))) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().lore() != null) {
          player.closeInventory();
          // String station = item.getItemMeta().getLore().get(0);
          String ticketPrice = MachineUtil.componentToString(Objects.requireNonNull(item.getItemMeta().lore()).get(1)).substring(1);
          if (MachineUtil.isDouble(ticketPrice)) machine.adjustFares_2(0.0, item);
          else player.sendMessage(lang.getString("direct-ticket-invalid"));
        }
      }
  
      // == Adjust Fares : Page 2 ==
      else if (inventoryName.contains(lang.getString("menu-adjust-fares"))) {
        value = Double.parseDouble(inventoryName.substring(lang.getString("menu-adjust-fares").length()+lang.getString("currency").length()));
        ItemStack item0 = inventory.getItem(0);
    
        event.setCancelled(true);
    
        if (itemName.equals(lang.getString("clear"))) machine.adjustFares_2(0.0, item0);
    
        else if (itemName.equals(lang.getString("enter")) && item0 != null) {
          player.closeInventory();
          machine.generateTicket(item0, value);
        } else {
          double numberPressed = Integer.parseInt(itemName);
          value = (Math.round(value*1000)+numberPressed)/100;
          machine.adjustFares_2(value, item0);
        }
    
      }
  
      // == Card Operations : Page 1 ==
      else if (inventoryName.equals(lang.getString("select-card"))) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().lore() != null && Objects.requireNonNull(item.getItemMeta().lore()).get(0).equals(lang.getComponent("serial-number"))) {
          player.closeInventory();
          String serial = MachineUtil.componentToString(Objects.requireNonNull(item.getItemMeta().lore()).get(1));
          machine.cardOperations_2(serial);
        }
      }

      // == Rail Pass Ticket Menu : Page 0 ==
      else if (inventoryName.equals(lang.getString("select-card-rail-pass")) && machine instanceof RailPassMachine rpm) {
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().lore() != null && Objects.requireNonNull(item.getItemMeta().lore()).get(0).equals(lang.getComponent("serial-number"))) {
          player.closeInventory();
          String serial = MachineUtil.componentToString(Objects.requireNonNull(item.getItemMeta().lore()).get(1));
          rpm.railPass_3(serial);
        }
      }

      // == Card Operations : Page 2 ==
      else if (inventoryName.contains(lang.getString("card-operation"))) {
        String serial = inventoryName.substring(lang.getString("card-operation").length());
        if (!itemName.equals(lang.getString("card-details"))) {
          player.closeInventory();
          if (itemName.equals(lang.getString("new-card"))) {
            machine.newCard_3();
          } else if (itemName.equals(lang.getString("top-up-card"))) {
            machine.topUp_3(serial);
          } else if (itemName.equals(lang.getString("menu-rail-pass"))) {
            machine.railPass_3(serial, this.operator);
          } else if (itemName.equals(lang.getString("refund-card"))) {
            // search for player's card
            for (ItemStack itemStack : player.getInventory().getContents()) {
              // get loreStack
              if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().lore() != null
                      && Objects.requireNonNull(itemStack.getItemMeta().lore()).get(0).equals(lang.getComponent("serial-number"))) {
                // get serialNumber
                if (Objects.requireNonNull(item.getItemMeta().lore()).get(1).equals(Component.text(serial))) {
                  // return remaining value to the player
                  double remainingValue = cardSql.getCardValue(serial);
                  Iciwi.economy.depositPlayer(player, remainingValue);
                  // return the deposit to the player
                  double deposit = plugin.getConfig().getDouble("deposit");
                  Iciwi.economy.depositPlayer(player, deposit);
                  // remove card from the inventory and from the database
                  player.getInventory().remove(itemStack);
                  cardSql.deleteCard(serial);
                  // send message and break out of loop
                  player.sendMessage(String.format(lang.getString("card-refunded"), serial, remainingValue+deposit));
                  break;
                }
              }
            }
      
          }
        }
      }

      // == New Card : Page 3 ==
      else if (inventoryName.equals(lang.getString("select-value"))) {
        if (itemName.contains(lang.getString("currency"))) {
          double val = Double.parseDouble(itemName.replaceAll("[^\\d.]", ""));
          double deposit = plugin.getConfig().getDouble("deposit");
    
          player.closeInventory();
    
          if (Iciwi.economy.getBalance(player) >= deposit+val) {
            // Take money from player and send message
            Iciwi.economy.withdrawPlayer(player, deposit+val);
            // Prepare card
            int s = new SecureRandom().nextInt(100000);
            char sum = new char[] {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[((s%10)*2+(s/10%10)*3+(s/100%10)*5+(s/1000%10)*7+(s/10000)*9)%19];
            // Generate card
            machine.generateCard(lang.getString("serial-prefix")+sum+"-"+s, val);
            // Send confirmation message
            player.sendMessage(String.format(lang.getString("new-card-created"), deposit, val));
          } else player.sendMessage(lang.getString("not-enough-money"));
        }
      }

      // == Top Up : Page 3 ==
      else if (inventoryName.contains(lang.getString("top-up"))) {
        String serial = inventoryName.substring(lang.getString("top-up").length());
        if (MachineUtil.isDouble(itemName.replaceAll("[^\\d.]", ""))) {
          double val = Double.parseDouble(itemName.replaceAll("[^\\d.]", ""));
      
          player.closeInventory();
      
          // Top up existing card
          if (Iciwi.economy.getBalance(player) >= val) {
            Iciwi.economy.withdrawPlayer(player, val);
            player.sendMessage(String.format(lang.getString("card-topped-up"), val));
            cardSql.addValueToCard(serial, val);
      
          } else player.sendMessage(lang.getString("not-enough-money"));
        }
      }

      // == Rail Pass : Page 3 ==
      else if (inventoryName.contains(lang.getString("menu-rail-pass"))) {
        String serial = inventoryName.substring(lang.getString("menu-rail-pass").length());
  
        // Check discounts
        if (itemName.equals(lang.getString("card-discounts"))) {
          event.setCancelled(true);
          // Return a menu
          List<TextComponent> discountList = cardSql.getAllDiscounts(serial).entrySet().stream()
              .sorted(Map.Entry.comparingByValue())
              .map(entry -> Component.text().content(
                      // Show expiry date
                      "\u00A76- \u00A7a"+entry.getKey()+"\u00a76 | Exp. "+String.format("\u00a7b%s\n", new Date(entry.getValue()*1000)))
                  // Option to extend
                  .append(Component.text().content("\u00a76 | Extend \u00a7a")).clickEvent(ClickEvent.runCommand("/newdiscount "+serial+" "+entry.getKey()))
                  .build()).toList();
  
          TextComponent menu = Component.text().content("==== Rail Passes You Own ====\n").color(NamedTextColor.GOLD).build();
  
          for (TextComponent displayEntry : discountList) menu = menu.append(displayEntry);
          menu = menu.append(Component.text("\n"));
  
          player.sendMessage(menu);
        }
  
        // Buy new rail pass
        else {
          String name = itemName.replaceAll("[^\\d.]", "");
          double price = owners.getRailPassPrice(name);
  
          if (Iciwi.economy.getBalance(player) >= price) {
  
            player.closeInventory();
  
            Iciwi.economy.withdrawPlayer(player, price);
            long duration = owners.getRailPassDuration(name);
  
            if (cardSql.getAllDiscounts(serial).containsKey(name))
              cardSql.setDiscount(serial, name, cardSql.getStart(serial, name)+duration);
            else
              cardSql.setDiscount(serial, name, Instant.now().getEpochSecond());
  
            owners.deposit(operator, price);
  
          } else player.sendMessage(lang.getString("not-enough-money"));
        }
      }
    }
  }
}
