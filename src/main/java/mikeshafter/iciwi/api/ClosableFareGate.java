package mikeshafter.iciwi.api;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public abstract class ClosableFareGate extends FareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private Object[] closeGateArray;

/**
 * Creates a new fare gate at the sign's location.
 *
 * @param path Path to line 0's text in lang.yml
 */
public ClosableFareGate (String path) {
	super(path);
}

/**
 * Creates a new fare gate with an offset from the sign's location.
 *
 * @param path           Path to line 0's text in lang.yml
 * @param locationOffset Default offset sign location
 */
public ClosableFareGate (String path, Vector locationOffset) {
	super(path, locationOffset);
}

/**
 * Sets the closeGateArray
 *
 * @param closeGateArray the closeGateArray, in the format {Location[], Runnable[]}
 */
public void setCloseGateArray (Object[] closeGateArray) {this.closeGateArray = closeGateArray;}

@EventHandler(priority = EventPriority.LOWEST)
public void onPlayerMove (PlayerMoveEvent event) {
	if (this.closeGateArray == null) return;
	Location ploc = event.getPlayer().getLocation();
	for (Location gloc : ((Location[]) closeGateArray[0])) {
		if (gloc.getBlockX() == ploc.getBlockX() && gloc.getBlockY() == ploc.getBlockY() && gloc.getBlockZ() == ploc.getBlockZ()) {
			for (Runnable r : ((Runnable[]) closeGateArray[1]))
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, r, plugin.getConfig().getLong("close-after-pass"));
		}
	}
}
}
