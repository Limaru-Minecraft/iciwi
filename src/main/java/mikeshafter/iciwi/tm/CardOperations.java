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
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Objects;

import static mikeshafter.iciwi.Iciwi.economy;
import static mikeshafter.iciwi.StationOwners.getOwner;
import static mikeshafter.iciwi.tm.MakeButton.makeButton;


public class CardOperations implements Listener {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final CardSql app = new CardSql();
  private String serial, station;
  
  
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
  public void cardOpsClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    ItemStack item = event.getCurrentItem();
    Inventory inventory = event.getClickedInventory();
    if (inventory == null) return;
    
    // if there is no item clicked
    if (item == null || !item.hasItemMeta()) {
      return;
    }
    
    if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"Remaining value: ")) {
      String name = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      
      if (name.equals(ChatColor.LIGHT_PURPLE+"New ICIWI Card")) {
        player.closeInventory();
        CardPriceSelector cardPriceSelector = new CardPriceSelector();
        cardPriceSelector.cardPrice(player, null);
        
      } else if (name.equals(ChatColor.AQUA+"Top Up ICIWI Card")) {
        // Use private variables this.serial and this.station
        player.closeInventory();
        CardPriceSelector cardPriceSelector = new CardPriceSelector();
        cardPriceSelector.cardPrice(player, this.serial);
        
      } else if (name.equals(ChatColor.GREEN+"New Rail Pass")) {
        // Use private variables this.serial and this.station
        player.closeInventory();
        RailPassSelector railPassSelector = new RailPassSelector();
        railPassSelector.railPass(player, this.serial, this.station);
        
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
  }
  
}
