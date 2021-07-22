package mikeshafter.iciwi.tm;

import mikeshafter.iciwi.CardSql;
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
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

import static mikeshafter.iciwi.Iciwi.economy;
import static mikeshafter.iciwi.tm.MakeButton.makeButton;


public class RailPassSelector implements Listener {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final CardSql app = new CardSql();
  
  private String serial;
  private String operator;
  
  private ArrayList<String[]> daysList; // {{days, price}, ...}
  
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
  
  @EventHandler
  public void railPassClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    if (inventory == null) return;
    
    // if there is no item clicked
    if (item == null || !item.hasItemMeta()) {
      return;
    }
    
    if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"New Rail Pass") && event.getRawSlot() < daysList.size()) {
      event.setCancelled(true);
      long days = Long.parseLong(daysList.get(event.getRawSlot())[0]);
      double price = Double.parseDouble(daysList.get(event.getRawSlot())[1]);
      
      player.closeInventory();
      economy.withdrawPlayer(player, price);
      player.sendMessage(String.format("§aPaid §e£%.2f§a for a %s §e%s-day§a rail pass.", price, operator, days));
      app.setDiscount(serial, operator, days*86400+Instant.now().getEpochSecond());
    }
  }
  
}
