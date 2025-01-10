package mikeshafter.iciwi.api;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.util.TicketType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;


public abstract class FareGate implements Listener {

final String header;
SignInfo signInfo;
private Vector locationOffset = new Vector();
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);

/**
 * Creates a new fare gate at the sign's location.
 *
 * @param path Path to line 0's text in lang.yml
 */
public FareGate (String path) { this.header = plugin.lang.getString(path); }

/**
 * Creates a new fare gate with an offset from the sign's location.
 *
 * @param path Path to line 0's text in lang.yml
 * @param locationOffset Default offset sign location
 */
public FareGate (String path, Vector locationOffset) {
	this.header = plugin.lang.getString(path);
	this.locationOffset = locationOffset;
}

@EventHandler(priority = EventPriority.LOWEST)
public void onPlayerInteract (PlayerInteractEvent event) {
	Block block = event.getClickedBlock();
	if (block == null || event.getItem() == null) return;
	Location clickedLocation = block.getLocation();
	clickedLocation.add(this.locationOffset);
	BlockState signState = clickedLocation.getBlock().getState();

	if (!(signState instanceof Sign sign)) {return;}
	SignSide side = sign.getSide(sign.getInteractableSideFor(event.getPlayer()));
	if (!IciwiUtil.stripColor(IciwiUtil.parseComponent(side.line(0))).contains(header)) {return;}

	sign.setWaxed(true);
	sign.update(true);

	// get sign text
	String[] signText = new String[]{
		IciwiUtil.parseComponent(side.line(0)).replaceAll("[\\[\\]]", ""),
		IciwiUtil.parseComponent(side.line(1)).replaceAll("[\\[\\]]", ""),
		IciwiUtil.parseComponent(side.line(2)).replaceAll("[\\[\\]]", ""),
		IciwiUtil.parseComponent(side.line(3)).replaceAll("[\\[\\]]", "")
	};

	var item = event.getItem();
	this.signInfo = new SignInfo(item, signText, sign);
	onInteract(event.getPlayer(), signInfo);
}

public void onInteract (Player player, SignInfo info) {
	var item = info.item();
	if (!IciwiUtil.loreCheck(item)) return;
	var m = item.getType().toString();
	var i = item.getItemMeta().getCustomModelData();
	var c = Iciwi.getPlugin(Iciwi.class).getConfig();

	if (m.equalsIgnoreCase(c.getString("ticket.material")) && i == c.getInt("ticket.custom-model-data")) {
		onTicket(player, info);
	}
	else if (m.equalsIgnoreCase(c.getString("card.material")) && i == c.getInt("card.custom-model-data")) {
		onCard(player, info);
	}
	else if (m.equalsIgnoreCase(c.getString("railpass.material")) && i == c.getInt("railpass.custom-model-data")) {
		onRailPass(player, info);
	}
	else {
		throw new EnumConstantNotPresentException(TicketType.class, m);
	}

}

public abstract void onTicket (Player player, SignInfo info);
public abstract void onCard (Player player, SignInfo info);
public abstract void onRailPass (Player player, SignInfo info);

}
