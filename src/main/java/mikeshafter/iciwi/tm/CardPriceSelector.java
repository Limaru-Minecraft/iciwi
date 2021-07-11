package mikeshafter.iciwi.tm;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
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

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import static mikeshafter.iciwi.Iciwi.economy;


public class CardPriceSelector implements Listener {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final CardSql app = new CardSql();
  private final double[] priceArray = {10, 20, 30, 50, 70, 100};
  private String serial;
  
  public void cardPrice(Player player, String serial) {
    // Serial can be null. If serial is null, create new card.
    this.serial = serial;
    
    Inventory cardPrice = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select value...");
    player.sendMessage(player.getName()+" DEBUG 2a");  // TODO: DEBUG
    
    for (double v : priceArray)
      cardPrice.addItem(makeButton(Material.PURPLE_STAINED_GLASS_PANE, String.format(ChatColor.GREEN+"£%.2f", v)));
    
    player.openInventory(cardPrice);
  }
  
  private ItemStack makeButton(final Material material, final String displayName) {
    ItemStack item = new ItemStack(material, 1);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    item.setItemMeta(itemMeta);
    return item;
  }
  
  @EventHandler
  public void cardPriceClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    ItemStack item = event.getCurrentItem();
    Inventory inventory = event.getClickedInventory();
    if (inventory == null) return;
    
    // if there is no item clicked
    if (item == null || !item.hasItemMeta()) {
      return;
    }
    
    if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Select value...") && event.getRawSlot() < priceArray.length) {
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
          player.getInventory().addItem(makeButton(Material.NAME_TAG, ChatColor.GREEN+"ICIWI Card", "Serial number:", "I"+sum+"-"+serial));
          player.closeInventory();
        } else player.sendMessage(ChatColor.RED+"You do not have enough money!");
      }
    }
    player.closeInventory();
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
