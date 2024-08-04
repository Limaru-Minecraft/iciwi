package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.Clickable;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.lang.Runnable;
import java.security.SecureRandom;
import java.util.*;

import static mikeshafter.iciwi.util.IciwiUtil.*;

public class CardMachine implements Machine {

// Attributes
private Inventory inv;
private Clickable[] clickables;
private ItemStack selectedItem;
private List<String> operators;
private final Player player;
private boolean bottomInv;
private Runnable clickInvItemRunnable;

// Constant helper classes
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final CardSql cardSql = new CardSql();
private final Owners owners = plugin.owners;
private final Lang lang = plugin.lang;

// Constructor and Menu Display
public CardMachine (Player player) {this.player = player;}
public CardMachine (Player player, String station) {
	this.player = player;
	this.operators = this.owners.getOwners(station);
}

// getters
public Clickable[] getClickables () {return clickables;}

public ItemStack getSelectedItem () {return selectedItem;}

public boolean useBottomInv () {return bottomInv;}

@Override public Runnable getClickInvItemRunnable () {return clickInvItemRunnable;}

// setters
@Override public void setSelectedItem (ItemStack selectedItem) {this.selectedItem = selectedItem;}

// initial menu
public void init (String station) {
	// setup inventory
	inv = plugin.getServer().createInventory(this.player, 9, lang.getComponent("ticket-machine"));
	this.clickables = new Clickable[9];

	// Create buttons
	this.clickables[2] = Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-new-card")), (event) -> newCard());
	this.clickables[6] = Clickable.of(makeItem(Material.NAME_TAG, 0, lang.getComponent("menu-insert-card")), (event) -> selectCard());

	// Get operators
	this.operators = this.owners.getOwners(station);
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
	// Set runnable after clicking the card
	clickInvItemRunnable = this::cardMenu;
	// Start listening and open inventory
	player.openInventory(inv);
}

// main menu after inserting iciwi card
public void cardMenu () {
	// get card details
	IcCard icCard = IcCardFromItem(this.selectedItem);
	assert icCard != null;
	Material cardMaterial = Material.valueOf(plugin.getConfig().getString("card.material"));
	int cardModelData = plugin.getConfig().getInt("card.custom-model-data");

	// setup inventory
	inv = plugin.getServer().createInventory(null, 9, lang.getComponent("ticket-machine"));
	this.clickables = new Clickable[9];

	// Card details
	this.clickables[0] = Clickable.of(makeItem(cardMaterial, cardModelData, lang.getComponent("menu-card-details"), Component.text("Plugin: §b").append(Objects.requireNonNull(this.selectedItem.getItemMeta().lore()).get(0)), Component.text("Serial: §a" + icCard.getSerial()), Component.text("Value: §6" + icCard.getValue())), (e) -> {});

	// Create buttons
	this.clickables[2] = Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-new-card")), (event) -> newCard());
	this.clickables[3] = Clickable.of(makeItem(Material.LIGHT_BLUE_WOOL, 0, lang.getComponent("menu-top-up-card")), (event) -> topUpCard(this.selectedItem));
	this.clickables[4] = Clickable.of(makeItem(Material.LIME_WOOL, 0, lang.getComponent("menu-rail-pass")), (event) -> {
		SignInteractListener.machineHashMap.put(player, new RailPassMachine(player, this.operators));
		((RailPassMachine) SignInteractListener.machineHashMap.get(player)).railPass(this.selectedItem);
	});
	this.clickables[5] = Clickable.of(makeItem(Material.ORANGE_WOOL, 0, lang.getComponent("menu-refund-card")), (event) -> refundCard(this.selectedItem));
	this.clickables[6] = Clickable.of(makeItem(Material.PURPLE_WOOL, 0, lang.getComponent("menu-select-other-card")), (event) -> selectCard());

	// Set items
	setItems(this.clickables, inv);
	// Start listening and open inventory
	player.openInventory(inv);
}

// new iciwi card menu
public void newCard () {
	// Setup listener
	// setup inventory
	List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
	int invSize = roundUp(priceArray.size(), 9);
	inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
	this.clickables = new Clickable[priceArray.size()];

	for (int i = 0; i < priceArray.size(); i++) {
		this.clickables[i] = Clickable.of(makeItem(Material.PURPLE_STAINED_GLASS_PANE, 0, Component.text(String.format(lang.getString("currency") + "%.2f", priceArray.get(i)))), (event) -> {
			double value = Double.parseDouble(parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName()).replaceAll("[^\\d.]", ""));
			double deposit = plugin.getConfig().getDouble("deposit");

			event.setCancelled(true);

			if (Iciwi.economy.getBalance(player) >= deposit + value) {
				// Take money from player and send message
				Iciwi.economy.withdrawPlayer(player, deposit + value);

				// Prepare card
				int s = new SecureRandom().nextInt(100000);
				char sum = new char[]{'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[((s % 10) * 2 + (s / 10 % 10) * 3 + (s / 100 % 10) * 5 + (s / 1000 % 10) * 7 + (s / 10000) * 9) % 19];
				String serial = lang.getString("serial-prefix") + sum + "-" + s;

				// Get card generator
				Material cardMaterial = Material.valueOf(plugin.getConfig().getString("card.material"));
				int customModelData = plugin.getConfig().getInt("card.custom-model-data");
				// Generate card
				cardSql.newCard(serial, value);
				player.getInventory().addItem(makeItem(cardMaterial, customModelData, lang.getComponent("plugin-name"), Component.text(plugin.getName()), Component.text(serial)));

				// Log card

				cardSql.logMaster(player.getUniqueId().toString());
				cardSql.logCardCreate(serial, value);

				// Send confirmation message
				player.sendMessage(String.format(lang.getString("new-card-created"), deposit, value));
				player.closeInventory();
			}
			else {
				player.closeInventory();
				player.sendMessage(lang.getString("not-enough-money"));
			}
		});
	}

	// Set items
	setItems(this.clickables, inv);
	// Start listening and open inventory
	player.openInventory(inv);
}

// top up menu
public void topUpCard (ItemStack item) {
	// Setup listener
	// setup inventory
	List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
	int invSize = roundUp(priceArray.size(), 9);
	inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
	clickables = new Clickable[invSize];

	// get serial number
	String serial = parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1));

	for (int i = 0; i < priceArray.size(); i++) {
		clickables[i] = Clickable.of(makeItem(Material.LIME_STAINED_GLASS_PANE, 0, Component.text(String.format(lang.getString("currency") + "%.2f", priceArray.get(i)))), (event) -> {
			double value = Double.parseDouble(parseComponent(Objects.requireNonNull(event.getCurrentItem()).getItemMeta().displayName()).replaceAll("[^\\d.]", ""));

			if (Iciwi.economy.getBalance(player) >= value) {
				// Get old value for later
				double old = cardSql.getCardValue(serial);

				// Update value in SQL
				cardSql.addValueToCard(serial, value);
				player.closeInventory();

				// Log card

				cardSql.logMaster(player.getUniqueId().toString());
				cardSql.logCardTopup(serial, old, value, old + value);

				// Take money from player and send message
				Iciwi.economy.withdrawPlayer(player, value);
				player.sendMessage(String.format(lang.getString("card-topped-up"), value));

			}
			else {
				player.closeInventory();
				player.sendMessage(lang.getString("not-enough-money"));
			}
		});
	}

	// Set items
	setItems(this.clickables, inv);
	// Start listening and open inventory
	player.openInventory(inv);
}

// refunds the card
public void refundCard (ItemStack item) {
	// get serial number
	String serial = parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1));
	for (ItemStack itemStack : player.getInventory().getContents()) {
		// check if the lore matches
		if (loreCheck(itemStack, 2) && Objects.requireNonNull(itemStack.getItemMeta().lore()).get(1).equals(Component.text(serial))) {

			// get remaining value
			double remainingValue = this.cardSql.getCardValue(serial);

			// get deposit
			double deposit = this.plugin.getConfig().getDouble("deposit");

			// return remaining value and deposit to the player
			Iciwi.economy.depositPlayer(player, deposit + remainingValue);

			// remove card from the inventory and from the database
			player.getInventory().remove(itemStack);
			this.cardSql.deleteCard(serial);

			// send message and break out of loop
			player.sendMessage(String.format(lang.getString("card-refunded"), serial, remainingValue + deposit));

			// log refund

			cardSql.logMaster(player.getUniqueId().toString());
			cardSql.logCardRefund(serial, remainingValue);

			// close inventory
			player.closeInventory();
			break;
		}
	}
}

@Override public void setBottomInv (boolean b) {this.bottomInv = b;}

}
