package mikeshafter.iciwi.iciwiTM;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Objects;


public class Keypad extends CustomInventory {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
  public void newKeypad(Player player, int action, double current, String lore1) {
    Inventory keypad;
    ItemStack amount;
    ItemMeta newTicketMeta;
    
    // New Iciwi card
    if (action == 2) {
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
        keypad = plugin.getServer().createInventory(null, 36, String.format(ChatColor.DARK_AQUA+"Balance: £%.2f"+ChatColor.BLUE+" Top Up: £0.00", new CardSql().getCardValue(serial_prefix, serial)));
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
    for (int[] i : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      buttonMeta.setDisplayName(String.valueOf(i[1]));
      button.setItemMeta(buttonMeta);
      keypad.setItem(i[0], button);
    }
    button.setType(Material.RED_STAINED_GLASS_PANE);
    buttonMeta.setDisplayName("CLEAR");
    button.setItemMeta(buttonMeta);
    keypad.setItem(30, button);
    
    button.setType(Material.LIME_STAINED_GLASS_PANE);
    buttonMeta.setDisplayName("ENTER");
    button.setItemMeta(buttonMeta);
    keypad.setItem(32, button);
    
    player.openInventory(keypad);
  }
}
