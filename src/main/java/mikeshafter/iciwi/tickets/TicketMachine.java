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
import java.util.stream.Collectors;

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
	int card_model = this.owners.getCustomModel(operators.get(0));
    ArrayList<Clickable> clickList = new ArrayList<>();
	boolean flatTix = operators.stream().anyMatch(o -> this.owners.hasOperatorTicket(o));
	boolean customTix = !operators.stream().allMatch(o -> this.owners.hasOperatorTicket(o));

	// Paper ticket
	if (flatTix) {
		clickList.add(Clickable.of(
			makeItem(Material.valueOf(plugin.getConfig().getString("ticket.material")), plugin.getConfig().getInt("ticket.custom-model-data"), lang.getComponent("menu-new-flat-ticket"), Component.text("Tickets are non-refundable")),
			(e) -> selectTicket(operators.stream().filter(o -> this.owners.hasOperatorTicket(o)).toList())
		));
	}
	 if (customTix) {
		clickList.add(Clickable.of(
			makeItem(Material.valueOf(plugin.getConfig().getString("ticket.material")), plugin.getConfig().getInt("ticket.custom-model-data"), lang.getComponent("menu-new-ticket"), Component.text("Tickets are non-refundable")),
			(e) -> SignInteractListener.putMachine(this.player, new CustomMachine(player, station))
		));
	}

	// New card
    clickList.add(
        Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-new-card")), (e) -> {
            SignInteractListener.putMachine(player, new CardMachine(player, station));
            ((CardMachine) SignInteractListener.getMachine(player)).newCard();
        })
    );

	// Select card
    clickList.add(
        Clickable.of(makeItem(Material.valueOf(plugin.getConfig().getString("card.material")), card_model, lang.getComponent("menu-insert-card")), (e) -> {
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

private void selectTicket (List<String> operators) {
	List<Clickable> items = operators.stream().map(o -> {
		int modelId = this.owners.getCustomModel(o);
		return Clickable.of(
			makeItem(
				Material.PAPER,
				modelId,
				Component.text(o),
				Component.text(owners.getOperatorTicket(o))
			), e -> generateOperatorTicket(o)
		);
	}).collect(Collectors.toList());
	Clickable[] clickables = alignLeft(9, new ArrayList<Clickable>(items));
	Inventory inv = plugin.getServer().createInventory(this.player, 9, lang.getComponent("select-ticket"));
	setItems(clickables, inv);
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
	Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "operator", owner, "price", String.valueOf(price));
	logger.info("operatorTicket", lMap);

	player.getInventory().addItem(makeItem(ticketMaterial, customModelData, lang.getComponent("train-ticket"), Component.text("C:" + owner), Component.text("C:" + owner), Component.text(Objects.requireNonNull(plugin.getConfig().getString("default-class")))));
	player.closeInventory();
	SignInteractListener.removeMachine(player);
}


@Override
public void setBottomInv (boolean b) {this.bottomInv = b;}

}
