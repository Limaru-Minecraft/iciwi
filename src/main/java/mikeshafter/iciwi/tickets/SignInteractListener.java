package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.Clickable;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import java.util.HashMap;

public class SignInteractListener implements Listener {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final Lang lang = new Lang(plugin);
  protected static final HashMap<Player, Machine> machineHashMap = new HashMap<>();


  @EventHandler(priority = EventPriority.LOWEST)
  public void TicketMachineListener(final InventoryClickEvent event) {
    final Player player = (Player) event.getWhoClicked();

    if (machineHashMap.containsKey(player)) {
      final Inventory clickedInventory = event.getClickedInventory();
      Machine machine = machineHashMap.get(player);

      if (clickedInventory == player.getOpenInventory().getBottomInventory()) {
        //event.setCancelled(true);
        // close the previous inventory
        // player.closeInventory();
        // player inventory item selection code
        if (machine.useBottomInv()) {
          machine.setSelectedItem(event.getCurrentItem());
          // there can only be 1 action here, which is to open the card menu
          machine.onCardSelection();
          machine.setBottomInv(false);
        }
        return;
      }

      if (clickedInventory == player.getOpenInventory().getTopInventory()) {
        //event.setCancelled(true);
        // get contents of actual inventory
        final ItemStack[] contents = clickedInventory.getContents();
        // get slot
        final int clickedSlot = event.getRawSlot();
        // get clicked item
        final Clickable clickedItem = machine.getClickables()[clickedSlot];
        // compare items and run
        if (clickedItem != null && clickedItem.getItem().equals(contents[clickedSlot]))
          clickedItem.run(event);
        // don't need to test for more
        return;
      }
      if (clickedInventory != null) {
        clickedInventory.close();
      }
    }

  }


  @EventHandler(priority = EventPriority.LOWEST)
  public void onSignClick(final PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign sign) {
      Player player = event.getPlayer();
      SignSide side = IciwiUtil.getClickedSide(sign, player);
      String signLine0 = IciwiUtil.stripColor(side.getLine(0));
      final String station = IciwiUtil.stripColor(side.getLine(1)).replaceAll("\\s+", "");

      // === Normal ticket machine ===
      if (signLine0.equalsIgnoreCase("["+lang.getString("tickets")+"]")) {
        final TicketMachine machine = new TicketMachine(player);
        machine.init(station);
        machineHashMap.put(player, machine);
      }

      // === Card vending machine ===
      if (signLine0.equalsIgnoreCase("["+lang.getString("cards")+"]")) {
        final CardMachine machine = new CardMachine(player);
        machine.init(station);
        machineHashMap.put(player, machine);
      }

      // === Rail pass machine ===
      else if (signLine0.equalsIgnoreCase("["+lang.getString("passes")+"]")) {
        final RailPassMachine machine = new RailPassMachine(player);
        machine.init(station);
        machineHashMap.put(player, machine);
      }

      // === Custom machine ===
      else if (signLine0.equalsIgnoreCase("["+lang.getString("custom-tickets")+"]")) {
        CustomMachine machine = new CustomMachine(player, station);
        machineHashMap.put(player, machine);
      }
    }
  }
}
