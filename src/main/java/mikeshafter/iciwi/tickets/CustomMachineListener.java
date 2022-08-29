package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
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

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class CustomMachineListener implements Listener {
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final Lang lang = new Lang(plugin);
  private CustomMachine machine;
  
  @EventHandler
  public void CTMSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign sign) {
      String signLine0 = ChatColor.stripColor(sign.line(0).toString());
      Player player = event.getPlayer();
      
      // === Custom ticket machine ===
      
      if (signLine0.equalsIgnoreCase("["+lang.getString("custom-tickets")+"]")) {
        String station = ChatColor.stripColor(sign.line(1).toString()).replaceAll("\\s+", "");
        machine = new CustomMachine(player, station);
      }
    }
  }
  
  @EventHandler
  public void CustomMachineSelectStation(InventoryClickEvent event) {
    
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    if (inventory == null) return;
    
    if (item != null && item.hasItemMeta() && item.getItemMeta() != null) {
      Component itemName = item.getItemMeta().displayName();
      Component inventoryName = event.getView().title();
      
      if (inventoryName.equals(Component.text(lang.getString("select-station")))) {
        // Select and set ending station
        machine.setTerminal(itemName);
        machine.selectClass();
      } else if (inventoryName.equals(Component.text(lang.getString("select-class")))) {
        machine.generateTicket(item);
      }
    }
  }
}
