package mikeshafter.iciwi.api;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import mikeshafter.iciwi.util.IciwiUtil;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;


public abstract class FareGate implements Listener {

	private String signLine0;
	private Vector locationOffset = new Vector();

	/**
	 * Creates a new fare gate at the sign's location.
	 */
	public FareGate () {}

	/**
	 * Creates a new fare gate with an offset from the sign's location.
	 * @param locationOffset Default offset sign location
	 */
	public FareGate(Vector locationOffset) { this.locationOffset = locationOffset; }

	/**
	 * Sets the first line to be used in the sign
	 * @param signLine0 first line in sign
	 */
	public void setSignLine0 (String signLine0) { this.signLine0 = signLine0; }

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block == null || event.getItem() == null) return;
		Location clickedLocation = block.getLocation();
		clickedLocation.add(this.locationOffset);
		BlockState signState = clickedLocation.getBlock().getState();
		if (signState instanceof Sign sign) {
			SignSide side = sign.getSide(sign.getInteractableSideFor(event.getPlayer()));

//			event.getPlayer().sendMessage("CONST sideLines >");  //TODO: debug
//			event.getPlayer().sendMessage(side.getLine(0));  //TODO: debug
//			event.getPlayer().sendMessage(side.getLine(1));  //TODO: debug
//			event.getPlayer().sendMessage(side.getLine(2));  //TODO: debug
//			event.getPlayer().sendMessage(side.getLine(3));  //TODO: debug

			if (IciwiUtil.stripColor(side.getLine(0)).contains(signLine0)) {
				// get sign text
				String[] signText = new String[4];
				for (int i = 0; i < 4; i++) {
					signText[i] = side.getLine(i).replace("[", "").replace("]", "");
				}
				// call onInteract
				onInteract(event.getPlayer(), event.getItem(), signText, sign);
			}
		}
	}

	/**
	 * Method called when player interacts with the fare gate
	 * @param player Player who interacted
	 * @param item Item used to interact
	 * @param signText Text in the sign
	 */
	public abstract void onInteract(Player player, ItemStack item, String[] signText, Sign sign);
}
