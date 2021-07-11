package mikeshafter.iciwi.tm;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.StationOwners;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static mikeshafter.iciwi.Iciwi.economy;


public class Keypad implements Listener {
  
  static Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
  @EventHandler
  public void KeypadClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    ItemStack item = event.getCurrentItem();
    Inventory inventory = event.getClickedInventory();
    if (inventory == null) return;
    
    if (event.getView().getTitle().contains(ChatColor.BLUE+"New Ticket")) {
      event.setCancelled(true);
      
      ItemStack dummyItem = inventory.getItem(0);
      assert dummyItem != null;
      String station = Objects.requireNonNull(Objects.requireNonNull(dummyItem.getItemMeta()).getLore()).get(0);
      double former = Double.parseDouble(Objects.requireNonNull(dummyItem.getItemMeta().getLore()).get(1));
      
      double current = Double.parseDouble(event.getView().getTitle().split("£")[1]);
      
      // if there is no item clicked
      if (item == null || !item.hasItemMeta()) {
        return;
      }
      
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
  
  public void keypad(Player player, String station, double current, double former) {
    Inventory keypad;
    player.sendMessage(player.getName()+" DEBUG 2b");  // TODO: DEBUG
    
    
    // New paper ticket
    
    if (current == former) {
      if (current == 0.0)
        keypad = plugin.getServer().createInventory(null, 36, ChatColor.BLUE+"New Ticket - Enter Value");
      else
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.BLUE+"New Ticket - £%.2f", current));
    } else {
      if (current == 0.0)
        keypad = plugin.getServer().createInventory(null, 36, ChatColor.BLUE+"Adjust Ticket - Enter New Value");
      else
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.BLUE+"Adjust Ticket - £%.2f", current));
    }
    
    
    // === Items ===
    
    // Top left dummy item
    keypad.setItem(0, makeButton(Material.PAPER, ChatColor.GREEN+"Train Ticket", station, String.format("£%.2f", former)));
    
    // Keypad buttons
    for (int[] i : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}})
      keypad.setItem(i[0], makeButton(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(i[1])));
    
    keypad.setItem(30, makeButton(Material.RED_STAINED_GLASS_PANE, "CLEAR"));
    keypad.setItem(32, makeButton(Material.LIME_STAINED_GLASS_PANE, "ENTER"));
    
    
    player.openInventory(keypad);
  }
  
  private ItemStack makeButton(final Material material, final String displayName, final String... lore) {
    ItemStack item = new ItemStack(material, 1);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    itemMeta.setLore(Arrays.asList(lore));
    item.setItemMeta(itemMeta);
    return item;
  }
  
  private ItemStack makeButton(final Material material, final String displayName) {
    ItemStack item = new ItemStack(material, 1);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    item.setItemMeta(itemMeta);
    return item;
  }
  
  private ItemStack makeButton(final Material material, final String displayName, final List<String> lore) {
    ItemStack item = new ItemStack(material, 1);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    itemMeta.setLore(lore);
    item.setItemMeta(itemMeta);
    return item;
  }
}
