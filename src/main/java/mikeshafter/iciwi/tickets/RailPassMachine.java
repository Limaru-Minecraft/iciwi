package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.IcLogger;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import static mikeshafter.iciwi.util.IciwiUtil.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import mikeshafter.iciwi.gui.GBranch;
import mikeshafter.iciwi.gui.GButton;

@SuppressWarnings("UnstableApiUsage")
public class RailPassMachine implements Machine {

// Attributes
private List<String> operators;
private final Player player;
private IcCard insertedCard;

// Constant helper classes
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Owners owners = plugin.owners;
private final Lang lang = plugin.lang;
private final IcLogger logger = plugin.icLogger;

public RailPassMachine (Player player) { this.player = player; }
public RailPassMachine (Player player, List<String> operators) {
	this.player = player;
	this.operators = operators;
}

public void init (String station) {
	// Set the operators
	this.operators = this.owners.getOwners(station);

	GBranch gui = GBranch.builder()
		.title(this.lang.getComponent("ticket-machine"))
		.button(GButton.builder().content(this.lang.getComponent("menu-new-paper-pass")).tooltip(Component.text("Buy a paper rail pass")).onClick((e) -> paperPass()).build())
		.button(GButton.builder().content(this.lang.getComponent("menu-insert-card")).tooltip(Component.text("Apply a rail pass onto your card")).onClick((e) -> selectCard()).build())
		.build();
	gui.open(this.player);
}

public void selectCard () {
	ItemStack[] playerInv = player.getInventory().getContents();
	GBranch.Builder guiBuilder = GBranch.builder().title(lang.getComponent("ticket-machine"))
		.content(lang.getComponent("select-card"));

	for (ItemStack item : playerInv) {
		Optional<IcCard> card = IcCardFromItem(item);
		if (card.isPresent()) {
			String serial = card.get().getSerial();
			guiBuilder.button(GButton.builder()
				.content(Component.text(serial))
				.onClick(player -> {
					this.insertedCard = card.get();
					this.railPass();
				})
				.build());
		}
	}
	guiBuilder.build().open(player);
}

//public void selectCard () {
//	// Setup listener for bottom inventory selection
//	// Create inventory
//	this.inv = this.plugin.getServer().createInventory(null, 9, this.lang.getComponent("select-card"));
//	// Swap flag
//	this.bottomInv = true;
//	// Set runnable after clicking the card
//	this.clickInvItemRunnable = () -> railPass(this.selectedItem);
//	// Start listening and open inventory
//	this.player.openInventory(this.inv);
//}

public void paperPass () {
	List<String> railPassNames = new ArrayList<>(this.owners.getRailPassNamesFromList(this.operators))
		.stream()
		.filter(name -> !name.startsWith("_") && this.owners.getRailPassPercentage(name) == 0d)
		.toList();
	GBranch.Builder gui = GBranch.builder().title(lang.getComponent("ticket-machine"));
	for (String rpName : railPassNames) {
		gui.button(GButton.builder().content(
			Component.text(rpName)
				.append(Component.text(" - "))
				.append(Component.text(this.owners.getRailPassPrice(rpName)))
		).onClick(e -> buy(rpName)).build());
	}
	gui.build().open(this.player);

	//	int invSize = (railPassNames.size() / 9 + 1) * 9;
	//	this.inv = plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
	//	clickables = new Clickable[invSize];
	//
	//	// create all rail pass buttons
	//	for (int i = 0; i < railPassNames.size(); i++) {
	//		String rpName = railPassNames.get(i);
	//		clickables[i] = Clickable.of(
	//			makeItem(
	//				Material.LIME_STAINED_GLASS_PANE,
	//				0,
	//				Component.text(rpName),
	//				Component.text(this.owners.getRailPassPrice(rpName))
	//			), e -> buy(rpName)
	//		);
	//	}
	//	setItems(clickables, inv);
	//	this.player.openInventory(this.inv);
}

public void railPass () {
	List<String> railPassNames = new ArrayList<>(this.owners.getRailPassNamesFromList(this.operators))
		.stream()
		.filter(name -> !name.startsWith("_"))
		.toList();
	GBranch.Builder gui = GBranch.builder().title(lang.getComponent("ticket-machine"));

	// View rail passes
	Map<String, Long> cardPasses = this.insertedCard.getRailPasses();
	List<ItemDialogBody> passContent = cardPasses.entrySet().stream().map(e -> {
			String name = e.getKey();
			long exp = e.getValue();
			long currentTime = System.currentTimeMillis();
			long duration = this.owners.getRailPassDuration(name);
			long timeToExpire = exp - currentTime;
			//time to expire < 1/8 duration
			if (timeToExpire < duration / 8) {
				ItemStack logo = new ItemStack(Material.RED_CONCRETE);
				ItemMeta itemMeta = logo.getItemMeta();
				assert itemMeta != null;
				itemMeta.displayName(Component.text("Expiring very soon!"));
				logo.setItemMeta(itemMeta);
				Component line = Component.text(name).color(TextColor.color(0xc0c0c0))
					.append(Component.text(" | ").color(TextColor.color(0xffffff)))
					.append(Component.text("Expires in ").color(TextColor.color(0xff0000)))
					.append(Component.text(timeToString(timeToExpire)).color(TextColor.color(0xff0000)));
				return DialogBody.item(logo, DialogBody.plainMessage(line, 400), false, true, 16, 16);
			}
			//time to expire < 1/4 duration
			else if (timeToExpire < duration / 4) {
				ItemStack logo = new ItemStack(Material.YELLOW_CONCRETE);
				ItemMeta itemMeta = logo.getItemMeta();
				assert itemMeta != null;
				itemMeta.displayName(Component.text("Expiring soon!"));
				logo.setItemMeta(itemMeta);
				Component line = Component.text(name).color(TextColor.color(0xc0c0c0))
					.append(Component.text(" | ").color(TextColor.color(0xffffff)))
					.append(Component.text("Expires in ").color(TextColor.color(0xffff00)))
					.append(Component.text(timeToString(timeToExpire)).color(TextColor.color(0xffff00)));
				return DialogBody.item(logo, DialogBody.plainMessage(line, 400), false, true, 16, 16);
			}
			//can be extended at this machine
			else if (railPassNames.contains(name)) {
				ItemStack logo = new ItemStack(Material.CYAN_CONCRETE);
				ItemMeta itemMeta = logo.getItemMeta();
				assert itemMeta != null;
				itemMeta.displayName(Component.text("Extendable here!"));
				logo.setItemMeta(itemMeta);
				Component line = Component.text(name).color(TextColor.color(0xc0c0c0))
					.append(Component.text(" | ").color(TextColor.color(0xFFFFFF)))
					.append(Component.text("Expires in ").color(TextColor.color(0x00ffff)))
					.append(Component.text(timeToString(timeToExpire)).color(TextColor.color(0x00ffff)));
				return DialogBody.item(logo, DialogBody.plainMessage(line, 400), false, true, 16, 16);
			}
			//normal
			else {
				ItemStack logo = new ItemStack(Material.LIME_CONCRETE);
				ItemMeta itemMeta = logo.getItemMeta();
				assert itemMeta != null;
				itemMeta.displayName(Component.text("Not extendable here"));
				logo.setItemMeta(itemMeta);
				Component line = Component.text(name).color(TextColor.color(0xc0c0c0))
					.append(Component.text(" | ").color(TextColor.color(0xffffff)))
					.append(Component.text("Expires in ").color(TextColor.color(0x00ff00)))
					.append(Component.text(timeToString(timeToExpire)).color(TextColor.color(0x00ff00)));
				return DialogBody.item(logo, DialogBody.plainMessage(line, 400), false, true, 16, 16);
			}
		})
		.toList();

	gui.button(GButton.builder()
		.content(Component.text("View Rail Passes"))
		.onClick(player -> {
			Dialog dialog = Dialog.create(builder -> builder.empty()
				.base(DialogBase.builder(Component.text("Purchased Rail Passes"))
					.body(passContent)
					.canCloseWithEscape(true)
					.afterAction(DialogBase.DialogAfterAction.CLOSE)
					.build()
				)
				.type(DialogType.notice(
					ActionButton.builder(Component.text("back"))
						.action(DialogAction.staticAction(ClickEvent.callback(audience -> railPass())))
						.build()
				))
			);
			player.showDialog(dialog);
		})
		.build()
	);

	// Buy passes
	for (String name : railPassNames) {
		gui.button(GButton.builder()
			.content(Component.text(name).color(TextColor.color(0xccff00)))
			.tooltip(Component.text("Buy this rail pass"))
			.onClick(e -> buy(name, this.insertedCard))
			.build()
		);
	}

	gui.build().open(player);

	//	int invSize = (railPassNames.size() / 9 + 1) * 9;
	//	this.inv = this.plugin.getServer().createInventory(null, invSize, lang.getComponent("ticket-machine"));
	//	this.clickables = new Clickable[invSize];
	//
	//	// get serial number
	//	IcCard icCard = IcCardFromItem(item);
	//	if (icCard == null) {
	//		this.player.closeInventory();SignInteractListener.removeMachine(player);
	//		return;
	//	}
	//
	//	// rail pass viewer
	//	this.clickables[0] = Clickable.of(
	//		makeItem(
	//			Material.WHITE_STAINED_GLASS_PANE,
	//			0,
	//			Component.text("View Rail Passes")
	//		), e -> view(e, icCard)
	//	);
	//
	//	// create all rail pass buttons
	//	for (int i = 0; i < railPassNames.size(); i++) {
	//		String rpName = railPassNames.get(i);
	//		this.clickables[i+1] = Clickable.of(
	//			makeItem(
	//				Material.LIME_STAINED_GLASS_PANE,
	//				0,
	//				Component.text(rpName),
	//				Component.text(this.owners.getRailPassPrice(rpName))
	//			),
	//			e -> buy(rpName, icCard)
	//		);
	//	}
	//
	//	// set items and open inventory
	//	setItems(this.clickables, this.inv);
	//	this.player.openInventory(this.inv);
}

private String timeToString (long time) {
	DateFormat df = new SimpleDateFormat("dd:hh:mm:ss");
	return df.format(Date.from(Instant.ofEpochMilli(time)));
}

private void buy (String rpName) {
	double price = this.owners.getRailPassPrice(rpName);

	if (Iciwi.economy.getBalance(this.player) >= price) {
		// take money from player
		Iciwi.economy.withdrawPlayer(this.player, price);

		// generate the rail pass paper ticket item
		Material material = Material.valueOf(this.plugin.getConfig().getString("railpass.material"));
		int customModelData = owners.getCustomModel(operators.getFirst());//this.plugin.getConfig().getInt("railpass.custom-model-data");
		long time = System.currentTimeMillis();

		/*
		Rail Pass
		<name>
		<expiry>
		 */
		ItemStack item = makeItem(material, customModelData, lang.getComponent("paper-rail-pass"), Component.text(rpName), Component.text(String.valueOf(time + this.owners.getRailPassDuration(rpName))));

		// give it to the player
		this.player.getInventory().addItem(item);

		// pay the TOC
		this.owners.deposit(this.owners.getRailPassOperator(rpName), price);

		// log the transaction
		Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "railPassName", rpName, "price", String.valueOf(price));
		logger.info("createPaperPass", lMap);
	}
	else this.player.sendMessage(this.lang.getString("not-enough-money"));

}

private void buy (String rpName, IcCard icCard) {
	double price = this.owners.getRailPassPrice(rpName);

	if (Iciwi.economy.getBalance(this.player) >= price) {
		// take money from player
		Iciwi.economy.withdrawPlayer(this.player, price);

		// check if the card already has the rail pass
		if (icCard.getRailPasses().containsKey(rpName)) {
			// Extend existing rail pass
			icCard.setRailPass(rpName, icCard.getExpiry(rpName));
			this.player.sendMessage(String.format(this.lang.getString("extended-rail-pass"), rpName, this.owners.getRailPassPrice(rpName)));
		}
		else {
			// New rail pass
			icCard.setRailPass(rpName, System.currentTimeMillis());
			this.player.sendMessage(String.format(this.lang.getString("added-rail-pass"), rpName, this.owners.getRailPassPrice(rpName)));
		}

		// pay the TOC
		this.owners.deposit(this.owners.getRailPassOperator(rpName), price);

		// log to icLogger
		Map<String, String> lMap = Map.of("player", player.getUniqueId().toString(), "card", icCard.getSerial(), "railPassName", rpName, "start", String.valueOf(System.currentTimeMillis()));
		logger.info("createRailPass", lMap);
	}
	else this.player.sendMessage(this.lang.getString("not-enough-money"));

}

}
