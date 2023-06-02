package mikeshafter.iciwi.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public abstract class ClosableFareGate extends FareGate {

	private Location gateLocation;

	/**
	 * Creates a new fare gate at the sign's location.
	 * @param signLine0 First line (preferably without "[]")
	 */
	public ClosableFareGate (String signLine0) {
		super(signLine0);
	}

	/**
	 * Creates a new fare gate with an offset from the sign's location.
	 * @param signLine0 First line (preferably without "[]")
	 * @param locationOffset Default offset sign location
	 */
	public ClosableFareGate (String signLine0, Vector locationOffset) {
		super(signLine0, locationOffset);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location location = player.getLocation();
		int x = this.gateLocation.getBlockX();
		int y = this.gateLocation.getBlockY();
		int z = this.gateLocation.getBlockZ();
		if (location.getBlockX() == x
		&&	location.getBlockY() == y
		&&	location.getBlockZ() == z) {
			onPlayerInFareGate(x, y, z);
		}
	}

	/**
	 * Called when the player moves into the defined gateLocation.
	 * @param x gateLocation's x coordinate
	 * @param y gateLocation's y coordinate
	 * @param z gateLocation's z coordinate
	 */
	public abstract void onPlayerInFareGate(int x, int y, int z);

	/**
	 * Set the gateLocation used in {@link ClosableFareGate#onPlayerInFareGate(int, int, int)}
	 * @param gateLocation new gate location.
	 */
	public void setGateLocation(Location gateLocation) { this.gateLocation = gateLocation; }

	/**
	 * Get the gateLocation used in {@link ClosableFareGate#onPlayerInFareGate(Player)}
	 * @return new gate location.
	 */
	public Location getGateLocation() { return this.gateLocation; }
}
