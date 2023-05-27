package mikeshafter.iciwi.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public abstract class ClosableFareGate extends FareGate {

  private Location gateLocation;

  public ClosableFareGate (String signLine0) {
    super(signLine0);
  }

  public ClosableFareGate (String signLine0, Vector locationOffset) {
    super(signLine0, locationOffset);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
	Player player = event.getPlayer();
	Location location = player.getLocation();
    if (location.getBlockX() == this.gateLocation.getBlockX() 
		&& location.getBlockY() == this.gateLocation.getBlockY() 
		&& location.getBlockZ() == this.gateLocation.getBlockZ()) {
      onPlayerInFareGate(player);
    }
  }

  public abstract void onPlayerInFareGate(Player player);

  public void setGateLocation(Location gateLocation) { this.gateLocation = gateLocation; }

  public Location getGateLocation() { return this.gateLocation; }
}
