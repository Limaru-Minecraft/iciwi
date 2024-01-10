package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.Clickable;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import static mikeshafter.iciwi.util.IciwiUtil.*;

public class TicketMachine implements Machine {

// Attributes
private Inventory inv;
private Clickable[] clickables;
private ItemStack selectedItem;
private List<String> operators;
private final Player player;
private boolean bottomInv;

// Constant helper classes
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final CardSql cardSql = new CardSql();
private final Owners owners = plugin.owners;
private final Lang lang = plugin.lang;

// Constructor and Menu Display
public TicketMachine (Player player) {this.player = player;}

// getters
public Clickable[] getClickables () {return clickables;}
public ItemStack getSelectedItem () {return selectedItem;}
public boolean useBottomInv () {return bottomInv;}

// setters
@Override public void setSelectedItem (ItemStack selectedItem) {this.selectedItem = selectedItem;}

// initial menu
public void init (String station) {
	// setup inventory
	inv = plugin.getServer().createInventory(this.player, 9, lang.getComponent("ticket-machine"));
	this.clickables = new Clickable[9];

	// Create buttons
	this.clickables[2] = Clickable.of(makeItem(Material.PAPER, 0, lang.getComponent("menu-new-ticket"), Component.text("Tickets are non-refundable")), (event) -> SignInteractListener.machineHashMap.put(this.player, new CustomMachine(player, station)));
	this.clickables[4] = Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-new-card")), (event) -> {
		SignInteractListener.machineHashMap.put(player, new CardMachine(player));
		((CardMachine) SignInteractListener.machineHashMap.get(player)).newCard();
	});
	this.clickables[6] = Clickable.of(makeItem(Material.NAME_TAG, 0, lang.getComponent("menu-insert-card")), (event) -> selectCard());

	// Get operators
	operators = this.owners.getOwners(station);
	// Set items
	setItems(clickables, inv);
	// Start listening and open inventory
	player.openInventory(inv);
}

// card selection menu. player clicks in their own inventory to select a card
public void selectCard () {
	// Setup listener for bottom inventory selection
	// Create inventory
	inv = this.plugin.getServer().createInventory(null, 9, lang.getComponent("select-card"));
	// Swap flag
	bottomInv = true;
	// Start listening and open inventory
	player.openInventory(inv);
}

/**
 Puts the items of a clickable[] into an inventory.

 @param clickables The clickable[] stated above.
 @param inventory  The inventory stated above. */
private void setItems (Clickable[] clickables, Inventory inventory) {
	ItemStack[] items = new ItemStack[clickables.length];
	for (int i = 0; i < clickables.length; i++)
		if (clickables[i] != null) items[i] = clickables[i].getItem();
	inventory.setStorageContents(items);
}

@Override public void setBottomInv (boolean b) {this.bottomInv = b;}

}
