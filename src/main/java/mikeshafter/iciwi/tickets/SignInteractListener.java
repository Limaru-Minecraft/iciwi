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
import java.util.HashMap;

public class SignInteractListener implements Listener {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Lang lang = plugin.lang;
private static final Player[] playerArr = new Player[plugin.getServer().getMaxPlayers()];
private static final Machine[] machineArr = new Machine[plugin.getServer().getMaxPlayers()];

protected static Machine getMachine (Player player) {
	for (int i = 0; i < playerArr.length; i++) {
		if (playerArr[i] == player) {
			return machineArr[i];
		}
	}
}

protected static void putMachine (Player player, Machine machine) {
	for (int i = 0; i < playerArr.length; i++) {
		if (playerArr[i] == null) {
			playerArr[i] = player;
			machineArr[i] = machine;
			return;
		}
	}
}

protected static void removeMachine (Player player) {
	for (int i = 0; i < playerArr.length; i++) {
		if (playerArr[i] == player) {
			playerArr[i] = null;
			machineArr[i] = null;
			return;
		}
	}
}

	protected static boolean hasMachine (Player player) {
		for (int i = 0; i < playerArr.length; i++) {
			if (playerArr[i] == player) {
				return true;
			}
		}
		return false;
	}

@EventHandler (priority = EventPriority.LOWEST) public void TicketMachineListener (final InventoryClickEvent event) {
	final Player player = (Player) event.getWhoClicked();

	if (hasMachine(player)) {
		final Inventory clickedInventory = event.getClickedInventory();
		final Machine machine = getMachine(player);

		if (clickedInventory == player.getOpenInventory().getBottomInventory()) {
			// player inventory item selection code
			if (machine.useBottomInv()) {
				machine.setSelectedItem(event.getCurrentItem());
				// there can only be 1 action here, which is to open the card menu
				//machine.onCardSelection();  // todo: generalise this
				machine.getClickInvItemRunnable().run();
				machine.setBottomInv(false);
			}
			return;
		}

		if (clickedInventory == player.getOpenInventory().getTopInventory()) {
			// get contents of actual inventory
			final ItemStack[] contents = clickedInventory.getContents();
			// get slot
			final int clickedSlot = event.getRawSlot();
			// get clicked item
			final Clickable clickedItem = machine.getClickables()[clickedSlot];
			// compare items and run
			if (clickedItem != null && clickedItem.getItem().equals(contents[clickedSlot])) clickedItem.run(event);
			// don't need to test for more
			return;
		}
		if (clickedInventory != null) {
			clickedInventory.close();
		}
	}

}


@EventHandler (priority = EventPriority.LOWEST) @SuppressWarnings ("Deprecation") public void onSignClick (final PlayerInteractEvent event) {
	// SignSide#getLine required for some older signs to work!
	if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign sign) {
		Player player = event.getPlayer();
		SignSide side = sign.getSide(sign.getInteractableSideFor(player));
		final String signLine0 = IciwiUtil.stripColor(IciwiUtil.parseComponent(side.line(0)));
		final String station = IciwiUtil.stripColor(IciwiUtil.parseComponent(side.line(1))).replaceAll("\\s+", "");

		// === Normal ticket machine ===
		if (signLine0.equalsIgnoreCase("[" + lang.getString("tickets") + "]")) {
			sign.setWaxed(true);
			sign.update(true);
			final TicketMachine machine = new TicketMachine(player);
			machine.init(station);
			putMachine(player, machine);
		}

		// === Card vending machine ===
		if (signLine0.equalsIgnoreCase("[" + lang.getString("cards") + "]")) {
			sign.setWaxed(true);
			sign.update(true);
			final CardMachine machine = new CardMachine(player);
			machine.init(station);
			putMachine(player, machine);
		}

		// === Rail pass machine ===
		else if (signLine0.equalsIgnoreCase("[" + lang.getString("passes") + "]")) {
			sign.setWaxed(true);
			sign.update(true);
			final RailPassMachine machine = new RailPassMachine(player);
			machine.init(station);
			putMachine(player, machine);
		}

		// === Custom machine ===
		else if (signLine0.equalsIgnoreCase("[" + lang.getString("custom-tickets") + "]")) {
			sign.setWaxed(true);
			sign.update(true);
			CustomMachine machine = new CustomMachine(player, station);
			putMachine(player, machine);
		}
	}
}
}
