package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.ClickItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

import static mikeshafter.iciwi.util.MachineUtil.*;


public class TicketMachine implements Listener {
  
  // Attributes
  private Inventory i;
  private HashMap<UUID, Inventory> inventoryMap;
  
  // Constant helper classes
  private final CardSql cardSql = new CardSql();
  private final Owners owners = new Owners();
  private final Lang lang = new Lang();
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  
  // Constructor and Menu Display
  public TicketMachine()
  {
    inventoryMap = new HashMap<>();
  }
  
  public void init (Player player, String station)
  {
    // setup inventory
    i = plugin.getServer().createInventory(null, 9, lang.getComponent("ticket-machine"));
    i.setItem(2, makeItem(Material.PAPER, Component.text("Generate New Ticket"), Component.text("Tickets are non-refundable")));
    i.setItem(6, makeItem(Material.NAME_TAG, Component.text("Insert Card")));
    ClickItem newTicket = ClickItem.of(makeItem(Material.PAPER, Component.text("Generate New Ticket"), Component.text("Tickets are non-refundable")), (event) -> {
      new CustomMachine(player, station);
    });
    ClickItem cardOps = ClickItem.of(makeItem(Material.NAME_TAG, Component.text("Insert Card")), (event) -> {
      // card operations
    });
    
    // open inventory
    inventoryMap.put(player.getUniqueId(), i);
    player.openInventory(i);
  }

  // Listener
  @EventHandler
  public void TicketMachineListener (InventoryClickEvent event)
  {
    // Cancel unwanted clicks
    // Restrict putting items from the bottom inventory into the top inventory
    Inventory clickedInventory = event.getClickedInventory();
    Player player = (Player) event.getWhoClicked();
  
    if (!inventoryMap.containsKey(player.getUniqueId()))
      return;
    
    if (clickedInventory == player.getOpenInventory().getBottomInventory()) {
      if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
        event.setCancelled(true);
        return;
      }
    
      if (event.getAction() == InventoryAction.NOTHING && event.getClick() != ClickType.MIDDLE) {
        event.setCancelled(true);
        return;
      }
    }
    
    if (clickedInventory == player.getOpenInventory().getTopInventory()) {
      event.setCancelled(true);
      ItemStack[] contents = clickedInventory.getContents();
      
        /*
        TODO
        Get clicked slot
        Get ClickItem (item+listener class) in clicked slot
        If the item in ClickItem is equal (in data) to the clicked slot's item, run the listener using <item>.run(event);
         */
      
    }
    
  }
  
}
