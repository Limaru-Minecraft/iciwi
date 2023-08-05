package mikeshafter.iciwi.api;

import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;

public abstract class ClosableFareGate extends FareGate {

	private Object[] gateCloseMap;

	public void setGateCloseMap (Object[] gateCloseMap) {
		this.gateCloseMap = gateCloseMap;
	}

	/**
	 * Creates a new fare gate at the sign's location.
	 */
	public ClosableFareGate () {
		super();
	}

	/**
	 * Creates a new fare gate with an offset from the sign's location.
	 * @param locationOffset Default offset sign location
	 */
	public ClosableFareGate (Vector locationOffset) {
		super(locationOffset);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (this.gateCloseMap == null) return;
		Location location = event.getPlayer().getLocation();
		Location[] gateLocs = ((Location[]) gateCloseMap[0]);
		for (Location gateLoc : gateLocs) {
			if (gateLoc.getBlockX() == location.getBlockX() && gateLoc.getBlockY() == location.getBlockY() && gateLoc.getBlockZ() == location.getBlockZ()) {
				for (Runnable runnable : ((Runnable[]) gateCloseMap[1])) {
					runnable.run();
				}
				break;
			}
		}
	}
}
