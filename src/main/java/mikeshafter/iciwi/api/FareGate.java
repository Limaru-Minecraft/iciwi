package mikeshafter.iciwi.api;

import mikeshafter.iciwi.Iciwi;
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
import java.util.ArrayList;


public abstract class FareGate implements Listener {

private final String header;
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

	// get sign text
	String[] signText = new String[]{
		IciwiUtil.parseComponent(side.line(0)).replaceAll("[\\[\\]]", ""),
		IciwiUtil.parseComponent(side.line(1)).replaceAll("[\\[\\]]", ""),
		IciwiUtil.parseComponent(side.line(2)).replaceAll("[\\[\\]]", ""),
		IciwiUtil.parseComponent(side.line(3)).replaceAll("[\\[\\]]", "")
	};

	var c = event.getItem().lore();
	var lore = (c == null) ? new ArrayList<String>() : IciwiUtil.parseComponents(c);
	// call onInteract
	onInteract(event.getPlayer(), new SignInfo(event.getItem(), lore, signText, sign));
}

public void onInteract (Player player, SignInfo info) {
	var item = info.item();
	var sign = info.sign();
	if (!IciwiUtil.loreCheck(item)) return;

    // Wax sign
	sign.setWaxed(true);
	sign.update(true);

	switch (IciwiUtil.getTicketType(item)) {
		case TICKET -> onTicket(player, info);
		case CARD -> onCard(player, info);
		case RAIL_PASS -> onRailPass(player, info);
	}
}

public abstract void onTicket (Player player, SignInfo info);
public abstract void onCard (Player player, SignInfo info);
public abstract void onRailPass (Player player, SignInfo info);

}
