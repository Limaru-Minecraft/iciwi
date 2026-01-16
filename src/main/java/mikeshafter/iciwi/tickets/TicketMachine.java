package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.IcLogger;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.gui.GBranch;
import mikeshafter.iciwi.gui.GButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static mikeshafter.iciwi.util.IciwiUtil.*;

public class TicketMachine implements Machine {

private final Player player;
// Constant helper classes
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Owners owners = plugin.owners;
private final Lang lang = plugin.lang;
private final IcLogger logger = plugin.icLogger;

// Constructor and Menu Display
public TicketMachine (Player player) {this.player = player;}

public void init (String station) {
    List<String> operators = this.owners.getOwners(station);
	boolean flatTix = operators.stream().anyMatch(this.owners::hasOperatorTicket);
	boolean customTix = !operators.stream().allMatch(this.owners::hasOperatorTicket);

	GBranch.Builder guiBuilder = GBranch.builder()
		.title(this.lang.getComponent("ticket-machine"));
	if (flatTix) {
		guiBuilder.button(GButton.builder()
			.content(lang.getComponent("menu-new-flat-ticket"))
			.tooltip(Component.text("Tickets are non-refundable"))
			.onClick(p -> selectTicket(operators.stream().filter(this.owners::hasOperatorTicket).toList()))
			.build()
		);
	}
	if (customTix) {
		guiBuilder.button(GButton.builder()
			.content(lang.getComponent("menu-new-ticket"))
			.tooltip(Component.text("Tickets are non-refundable"))
			.onClick(p -> new CustomMachine(p, station))
			.build()
		);
	}

	guiBuilder.button(GButton.builder()
		.content(lang.getComponent("menu-new-card"))
		.onClick(p -> new CardMachine(p, station).newCard())
		.build()
	);

	guiBuilder.button(GButton.builder()
		.content(lang.getComponent("menu-insert-card"))
		.onClick(p -> new CardMachine(p, station).selectCard())
		.build()
	);

	guiBuilder.build().open(player);

//	// Paper ticket
//	if (flatTix) {
//		clickList.add(Clickable.of(
//			makeItem(Material.valueOf(plugin.getConfig().getString("ticket.material")), plugin.getConfig().getInt("ticket.custom-model-data"), lang.getComponent("menu-new-flat-ticket"), Component.text("Tickets are non-refundable")),
//			(e) -> selectTicket(operators.stream().filter(o -> this.owners.hasOperatorTicket(o)).toList())
//		));
//	}
//	 if (customTix) {
//		clickList.add(Clickable.of(
//			makeItem(Material.valueOf(plugin.getConfig().getString("ticket.material")), plugin.getConfig().getInt("ticket.custom-model-data"), lang.getComponent("menu-new-ticket"), Component.text("Tickets are non-refundable")),
//			(e) -> SignInteractListener.putMachine(this.player, new CustomMachine(player, station))
//		));
//	}
//
//	// New card
//    clickList.add(
//        Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-new-card")), (e) -> {
//            SignInteractListener.putMachine(player, new CardMachine(player, station));
//            ((CardMachine) SignInteractListener.getMachine(player)).newCard();
//        })
//    );
//
//	// Select card
//    clickList.add(
//        Clickable.of(makeItem(Material.valueOf(plugin.getConfig().getString("card.material")), card_model, lang.getComponent("menu-insert-card")), (e) -> {
//            SignInteractListener.putMachine(player, new CardMachine(player, station));
//            ((CardMachine) SignInteractListener.getMachine(player)).selectCard();
//        })
//    );
//
//	this.clickables = justify(9, clickList);
//
//	// Attributes
//	Inventory inv = plugin.getServer().createInventory(this.player, 9, lang.getComponent("ticket-machine"));
//	setItems(clickables, inv);
//	// Start listening and open inventory
//	player.openInventory(inv);
}

private void selectTicket (List<String> operators) {
	List<GButton> buttons = operators.stream().map(o -> GButton.builder().content(
		Component.text(o).append(Component.text(" - "), Component.text(owners.getOperatorTicket(o))))
		.tooltip(Component.text("Click to buy this ticket"))
		.onClick(p -> generateOperatorTicket(o))
		.build())
	.toList();

	GBranch gui = GBranch.builder()
		.buttons(buttons)
		.title(this.lang.getComponent("ticket-machine"))
		.content(lang.getComponent("select-ticket"))
		.build();

	gui.open(player);

//	List<Clickable> items = operators.stream().map(o -> {
//		int modelId = this.owners.getCustomModel(o);
//		return Clickable.of(
//			makeItem(
//				Material.PAPER,
//				modelId,
//				Component.text(o),
//				Component.text(owners.getOperatorTicket(o))
//			), e -> generateOperatorTicket(o)
//		);
//	}).collect(Collectors.toList());
//	Clickable[] clickables = alignLeft(9, new ArrayList<Clickable>(items));
//	Inventory inv = plugin.getServer().createInventory(this.player, 9, lang.getComponent("select-ticket"));
//	setItems(clickables, inv);
//	player.openInventory(inv);
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
}
}
