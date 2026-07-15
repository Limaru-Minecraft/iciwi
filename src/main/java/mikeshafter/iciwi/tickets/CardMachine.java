package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.IcLogger;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.gui.FInput;
import mikeshafter.iciwi.gui.GBranch;
import mikeshafter.iciwi.gui.GButton;
import mikeshafter.iciwi.gui.GForm;
import mikeshafter.iciwi.util.IciwiUtil;
import static mikeshafter.iciwi.util.IciwiUtil.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CardMachine implements Machine {

// Attributes
private IcCard insertedCard;
private List<String> operators;
private final Player player;

// Constant helper classes
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final CardSql cardSql = new CardSql();
private final Owners owners = plugin.owners;
private final Lang lang = plugin.lang;
private final IcLogger logger = plugin.icLogger;

// Constructor and Menu Display
public CardMachine (Player player) {this.player = player;}
public CardMachine (Player player, String station) {
	this.player = player;
	this.operators = this.owners.getOwners(station);
}

public void init (String station) {
	this.operators = owners.getOwners(station);

	GBranch gui = GBranch.builder()
		.title(lang.getComponent("ticket-machine"))
		.content(Component.empty())
		.button(GButton.builder()
			.content(lang.getComponent("menu-new-card"))
			.onClick(p -> newCard())
			.build()
		)
		.button(GButton.builder()
			.content(lang.getComponent("menu-insert-card"))
			.onClick(p -> selectCard())
			.build()
		)
		.build();

	gui.open(this.player);

//	// == old ==
//	// setup inventory
//	inv = plugin.getServer().createInventory(this.player, 9, lang.getComponent("ticket-machine"));
//	this.clickables = new Clickable[9];
//	// Get operators
//	this.operators = this.owners.getOwners(station);
//
//	// Create buttons
//	this.clickables[2] = Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-new-card")), (e) -> newCard());
//	this.clickables[6] = Clickable.of(makeItem(Material.NAME_TAG, 0, lang.getComponent("menu-insert-card")), (e) -> selectCard());
//
//	// Set items
//	setItems(clickables, inv);
//	// Start listening and open inventory
//	player.openInventory(inv);
//	// == end old ==
}

// card selection menu. player clicks in their own inventory to select a card
public void selectCard () {
	ItemStack[] playerInv = player.getInventory().getContents();
	GBranch.Builder guiBuilder = GBranch.builder().title(lang.getComponent("ticket-machine"))
		.content(lang.getComponent("select-card"));

	boolean quickReturn = true;
	for (ItemStack item : playerInv) {
		Optional<IcCard> card = IcCardFromItem(item);
		if (card.isPresent()) {
			String serial = card.get().getSerial();
			guiBuilder.button(GButton.builder()
				.content(Component.text(serial))
				.onClick(player -> {
					this.insertedCard = card.get();
					this.cardMenu();
				})
				.build());
			quickReturn = false;
		}
	}
	if (quickReturn) return;
	guiBuilder.build().open(player);
}

// main menu after inserting iciwi card
public void cardMenu () {
	if (this.insertedCard == null) return;
	GBranch branch = GBranch.builder().title(lang.getComponent("ticket-machine"))
		.content(
			Component.text("Serial: ").color(TextColor.color(0x55aaaa)).append(
			Component.text(this.insertedCard.getSerial()).color(TextColor.color(0x55aaaa)),
			Component.text(" | ").color(TextColor.color(0xFFffff)),
			Component.text("Value: ").color(TextColor.color(0xFFAA00)),
			Component.text(this.insertedCard.getValueStr()).color(TextColor.color(0xFFAA00)))
		)
		.button(GButton.builder().content(lang.getComponent("menu-new-card")).onClick(player -> newCard()).build())
		.button(GButton.builder().content(lang.getComponent("menu-top-up-card")).onClick(player -> topUpCard()).build())
		.button(GButton.builder().content(lang.getComponent("menu-rail-pass")).onClick(player -> new RailPassMachine(player, this.insertedCard, operators).railPass()).build())
		.button(GButton.builder().content(lang.getComponent("menu-refund-card")).onClick(player -> refundCard()).build())
		.button(GButton.builder().content(lang.getComponent("menu-select-other-card")).onClick(player -> selectCard()).build())
		.build();
	branch.open(this.player);
}

// old
	//// get card details
	//IcCard icCard = IcCardFromItem(this.selectedItem);
	//if (icCard == null || this.selectedItem.getItemMeta().lore() == null) return;
//
	//Material cardMaterial = Material.valueOf(plugin.getConfig().getString("card.material"));
	//int cardModelData = owners.getCustomModel(operators.get(0));//plugin.getConfig().getInt("card.custom-model-data");
//
	//// setup inventory
	//inv = plugin.getServer().createInventory(null, 9, lang.getComponent("ticket-machine"));
	//this.clickables = new Clickable[9];
//
	//// Card details
	//this.clickables[0] = Clickable.of(makeItem(cardMaterial, cardModelData, lang.getComponent("menu-card-details"), Component.text("Plugin: §b").append(Objects.requireNonNull(this.selectedItem.getItemMeta().lore()).get(0)), Component.text("Serial: §a" + icCard.getSerial()), Component.text("Value: §6" + icCard.getValueStr() )), (e) -> e.setCancelled(true));
//
	//// Create buttons
	//this.clickables[2] = Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-new-card")), (e) -> newCard());
	//this.clickables[3] = Clickable.of(makeItem(Material.LIGHT_BLUE_WOOL, 0, lang.getComponent("menu-top-up-card")), (e) -> topUpCard(icCard));
	//this.clickables[4] = Clickable.of(makeItem(Material.LIME_WOOL, 0, lang.getComponent("menu-rail-pass")), (e) -> {
		//SignInteractListener.putMachine(player, new RailPassMachine(player, this.operators));
		//((RailPassMachine) SignInteractListener.getMachine(player)).railPass(this.selectedItem);
	//});
	//this.clickables[5] = Clickable.of(makeItem(Material.ORANGE_WOOL, 0, lang.getComponent("menu-refund-card")), (e) -> refundCard(icCard));
	//this.clickables[6] = Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-select-other-card")), (e) -> selectCard());
//
	//// Set items
	//setItems(this.clickables, inv);
	//// Start listening and open inventory
	//player.openInventory(inv);

// new iciwi card menu
public void newCard () {
	GForm form = GForm.builder().title(lang.getComponent("ticket-machine"))
		.item(new FInput("value", "Initial Value", ""))
		.action(ctx -> {
			double value = Double.parseDouble(ctx.getString("value"));
			double deposit = plugin.getConfig().getDouble("deposit");
			if (Iciwi.economy.getBalance(player) >= deposit + value) {
				// Take money from player and send message
				Iciwi.economy.withdrawPlayer(player, deposit + value);

				// Prepare card
				String serial;
				int exp = 15;
				SecureRandom sr = new SecureRandom();
				do {
					int s = sr.nextInt(2 << exp++);
					char sum = new char[]{'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}
						[((s % 10) * 2 + (s / 10 % 10) * 3 + (s / 100 % 10) * 5 + (s / 1000 % 10) * 7 + (s / 10000) * 9) % 19];
					serial = lang.getString("serial-prefix") + sum + "-" + s;
				} while (cardSql.cardExists(serial) && exp < 31);

				// Get card generator
				Material cardMaterial = Material.valueOf(plugin.getConfig().getString("card.material"));
				int customModelData = owners.getCustomModel(operators.getFirst());//plugin.getConfig().getInt("card.custom-model-data");
				String customCardName = owners.getCustomCardName(operators.getFirst());

				// Generate card
				cardSql.newCard(serial, value);
				player.getInventory().addItem(makeItem(cardMaterial, customModelData, Component.text(customCardName), Component.text(plugin.getName()), Component.text(serial)));

				// log to icLogger
				Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "serial", serial, "value", String.valueOf(value));
				logger.info("new-card", lMap);

				// Send confirmation message
				player.sendMessage(String.format(lang.getString("new-card-created"), deposit, value));
				// Receipt
				player.getInventory().addItem(IciwiUtil.makeItem(Material.FILLED_MAP, 0, Component.text("§7Receipt"), Component.text("New card serial: " + serial), Component.text("Deposit: " + deposit), Component.text("Pre top-up: " + value), Component.text("Total: " + ( deposit + value )) ));
				player.closeInventory();
			}
		}).build();
	form.open(player);
}
// == old ==
//	// Setup listener
//	// setup inventory
//	List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
//	int invSize = roundUp(priceArray.size(), 9);
//	inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
//	this.clickables = new Clickable[priceArray.size()];
//
//	for (int i = 0; i < priceArray.size(); i++) {
//		this.clickables[i] = Clickable.of(makeItem(Material.PURPLE_STAINED_GLASS_PANE, 0, Component.text(String.format(lang.getString("currency") + "%.2f", priceArray.get(i)))), (event) -> {
//			double value = Double.parseDouble(parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName()).replaceAll("[^\\d.]", ""));
//			double deposit = plugin.getConfig().getDouble("deposit");
//
//			event.setCancelled(true);
//
//			if (Iciwi.economy.getBalance(player) >= deposit + value) {
//				// Take money from player and send message
//				Iciwi.economy.withdrawPlayer(player, deposit + value);
//
//				// Prepare card
//				int s = new SecureRandom().nextInt(100000);
//				char sum = new char[]{'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[((s % 10) * 2 + (s / 10 % 10) * 3 + (s / 100 % 10) * 5 + (s / 1000 % 10) * 7 + (s / 10000) * 9) % 19];
//				String serial = lang.getString("serial-prefix") + sum + "-" + s;
//
//				// Get card generator
//				Material cardMaterial = Material.valueOf(plugin.getConfig().getString("card.material"));
//				int customModelData = owners.getCustomModel(operators.get(0));//plugin.getConfig().getInt("card.custom-model-data");
//				// Generate card
//				cardSql.newCard(serial, value);
//				player.getInventory().addItem(makeItem(cardMaterial, customModelData, lang.getComponent("plugin-name"), Component.text(plugin.getName()), Component.text(serial)));
//
//				// log to icLogger
//				Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "serial", serial, "value", String.valueOf(value));
//				logger.info("new-card", lMap);
//
//				// Send confirmation message
//				player.sendMessage(String.format(lang.getString("new-card-created"), deposit, value));
//				// Receipt
//				player.getInventory().addItem(IciwiUtil.makeItem(Material.BOOK, 0, Component.text("Receipt"), Component.text("Total: "+ (deposit + value)) ));
//				player.closeInventory();
//				SignInteractListener.removeMachine(player);
//			}
//			else {
//				player.closeInventory();
//				SignInteractListener.removeMachine(player);
//				player.sendMessage(lang.getString("not-enough-money"));
//			}
//		});
//	}
//
//	// Set items
//	setItems(this.clickables, inv);
//	// Start listening and open inventory
//	player.openInventory(inv);

// top up menu
public void topUpCard () {
	GForm form = GForm.builder().title(lang.getComponent("ticket-machine"))
		.item(new FInput("value", "Value", "0"))
		.action(ctx -> {
			double value = Double.parseDouble(ctx.getString("value"));

			if (Iciwi.economy.getBalance(player) >= value) {
				Iciwi.economy.withdrawPlayer(player, value);
				// Get old value for message
				double old = this.insertedCard.getValue();

				this.insertedCard.deposit(value);
				player.closeInventory();
				//SignInteractListener.removeMachine(player);

				// log to icLogger
				Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "card", this.insertedCard.getSerial(), "old", String.valueOf(old), "change", String.valueOf(value));
				logger.info("top-up-card", lMap);

				// Take money from player and send message
				Iciwi.economy.withdrawPlayer(player, value);
				// Receipt
				player.getInventory().addItem(IciwiUtil.makeItem(Material.FILLED_MAP, 0, Component.text("Receipt"), Component.text("Top-up card: " + this.insertedCard.getSerial()), Component.text("Top-up value: " + value) ));
				player.sendMessage(String.format(lang.getString("card-topped-up"), value));
			}
			else {
				player.closeInventory();
				//SignInteractListener.removeMachine(player);
				player.sendMessage(lang.getString("not-enough-money"));
			}
		}).build();
	form.open(player);
}


// refunds the card
public void refundCard () {
	// get serial number
	String serial = this.insertedCard.getSerial();
	player.closeInventory();  // close first to prevent removing the item
	for (ItemStack itemStack : player.getInventory().getContents()) {
		// check if the lore matches
		if (loreCheck(itemStack, 2) && Objects.requireNonNull(itemStack.getItemMeta().lore()).get(1).equals(Component.text(serial))) {

			// get remaining value
			double remainingValue = this.insertedCard.getValue();

			// get deposit
			double deposit = this.plugin.getConfig().getDouble("deposit");

			// return remaining value and deposit to the player
			Iciwi.economy.depositPlayer(player, deposit + remainingValue);

			// remove card from the inventory and from the database
			player.getInventory().remove(itemStack);
			this.cardSql.deleteCard(serial);

			// send message and break out of loop
			player.sendMessage(String.format(lang.getString("card-refunded"), serial, remainingValue + deposit));

			// log to icLogger
			Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "card", this.insertedCard.getSerial(), "value", String.valueOf(remainingValue));
			logger.info("refund-card", lMap);

			// close inventory

			//SignInteractListener.removeMachine(player);
			break;
		}
	}
}

}
