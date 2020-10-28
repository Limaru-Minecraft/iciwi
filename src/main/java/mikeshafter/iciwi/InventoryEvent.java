package mikeshafter.iciwi;

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

import java.util.Objects;

import static mikeshafter.iciwi.Iciwi.economy;

public class InventoryEvent implements Listener{
  
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
  @EventHandler
  public void InvenClick(InventoryClickEvent event){
    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    
    if (inventory == null){
      return;
    }
    if (event.getView().getTitle().equals(ChatColor.GOLD+"Ticket Machine")){
      
      event.setCancelled(true);
      if (item == null || !item.hasItemMeta()){
        return;
      }
      
      String temp = "";
      try{
        temp = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      } catch (Exception e){
        e.printStackTrace();
      }
      switch (temp){
        case "Buy New ICIWI Card":
          if (economy.getBalance(player) >= 5.0){
            player.sendMessage(ChatColor.GREEN+"Bought new ICIWI card");
            economy.withdrawPlayer(player, 5.0);
            // ICIWI card
            ItemStack newICIWI = new ItemStack(Material.NAME_TAG, 1);
            ItemMeta newICIWIMeta = newICIWI.getItemMeta();
            assert newICIWIMeta != null;
            newICIWIMeta.setDisplayName("Buy New ICIWI Card");
            newICIWI.setItemMeta(newICIWIMeta);
            player.getInventory().addItem(item); // make a real iciwi card
          } else {
            player.sendMessage(ChatColor.RED+"You do not have enough funds!");
          }
          player.closeInventory();
          break;
        case "Top Up ICIWI Card":
          //TODO: later
          
          break;
        case "Train Ticket":
          double ticketPrice = Double.parseDouble(Objects.requireNonNull(item.getItemMeta().getLore()).get(1));
          
          // Station that the player is at
          String station = Objects.requireNonNull(item.getItemMeta().getLore()).get(0);
          if (economy.getBalance(player) >= ticketPrice){
            player.sendMessage(ChatColor.GREEN+"Bought ticket from "+station+ChatColor.GOLD+" Fare: "+ticketPrice);
            economy.withdrawPlayer(player, ticketPrice);
            player.getInventory().addItem(item);
          }
          player.closeInventory();
          break;
      }
    }
  }
}
