package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.IcLogger;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.Clickable;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static mikeshafter.iciwi.util.IciwiUtil.*;

public class TicketMachine implements Machine {

private Clickable[] clickables;
private ItemStack selectedItem;
private final Player player;
private boolean bottomInv;

// Constant helper classes
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Owners owners = plugin.owners;
private final Lang lang = plugin.lang;
private final IcLogger logger = plugin.icLogger;

// Constructor and Menu Display
public TicketMachine (Player player) {this.player = player;}

// getters
public Clickable[] getClickables () {return clickables;}
public ItemStack getSelectedItem () {return selectedItem;}
public boolean useBottomInv () {return bottomInv;}

// setters
@Override
public void setSelectedItem (ItemStack selectedItem) {this.selectedItem = selectedItem;}

public void init (String station) {
    List<String> operators = this.owners.getOwners(station);
    ArrayList<Clickable> clickList = new ArrayList<>();
    boolean addCustomTickets = true;
    for (String operator : operators) {
        if (this.owners.hasOperatorTicket(operator)) {
			clickList.add(Clickable.of(
				makeItem(Material.PAPER, 0, lang.getComponent("menu-new-flat-ticket"), Component.text(operator)),
				(_) -> generateOperatorTicket(operator)
			));
        }
        else if (addCustomTickets) {
            clickList.add(Clickable.of(
				makeItem(Material.PAPER, 0, lang.getComponent("menu-new-ticket"), Component.text("Tickets are non-refundable")),
				(_) -> SignInteractListener.putMachine(this.player, new CustomMachine(player, station))
			));
            addCustomTickets = false;
        }
    }

	// New card
    clickList.add(
        Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-new-card")), (_) -> {
            SignInteractListener.putMachine(player, new CardMachine(player, station));
            ((CardMachine) SignInteractListener.getMachine(player)).newCard();
        })
    );

	// Select card
    clickList.add(
        Clickable.of(makeItem(Material.NAME_TAG, 0, lang.getComponent("menu-insert-card")), (_) -> {
            SignInteractListener.putMachine(player, new CardMachine(player, station));
            ((CardMachine) SignInteractListener.getMachine(player)).selectCard();
        })
    );

	this.clickables = justify(9, clickList);

	// Attributes
	Inventory inv = plugin.getServer().createInventory(this.player, 9, lang.getComponent("ticket-machine"));
	setItems(clickables, inv);
	// Start listening and open inventory
	player.openInventory(inv);
}

/**
 * Generates a new flat fare ticket
 *
 * @param owner TOC handling the ticket
 */
protected void generateOperatorTicket (String owner) {
	// Find the price
	double price = this.owners.getOperatorTicket(owner);

	// Check if the price is invalid or if the player has no money
	if (price == 0d || Iciwi.economy.getBalance(this.player) < price) {
		player.sendMessage(lang.getString("not-enough-money"));
		return;
	}

	// Let the player pay for the ticket
	Iciwi.economy.withdrawPlayer(this.player, price);

	owners.deposit(owner, price);

	// Get ticket materials
	Material ticketMaterial = Material.valueOf(plugin.getConfig().getString("ticket.material"));
	int customModelData = plugin.getConfig().getInt("ticket.custom-model-data");

	// log into icLogger
	Map<String, Object> lMap = Map.of("player", player.getUniqueId().toString(), "operator", owner, "price", price);
	logger.info("operatorTicket", lMap);

	player.getInventory().addItem(makeItem(ticketMaterial, customModelData, lang.getComponent("train-ticket"), Component.text("C:" + owner), Component.text("C:" + owner), Component.text(Objects.requireNonNull(plugin.getConfig().getString("default-class")))));
	player.closeInventory();
	SignInteractListener.removeMachine(player);
}

/**
 * Spread items evenly across n slots in an array
 *
 * @param n     Number of slots
 * @param items Items to spread
 * @return Final spreaded array
 */
private Clickable[] justify (int n, ArrayList<Clickable> items) {
	// optimisation
	int l = items.size();
	if (n == l) return items.toArray(new Clickable[l]);
	// Create an array with n elements
	Clickable[] arr = new Clickable[n];
	for (int i = 1; i <= l; i++) arr[i * n / (l + 1)] = items.get(i - 1);
	return arr;
}


@Override
public void setBottomInv (boolean b) {this.bottomInv = b;}

}
