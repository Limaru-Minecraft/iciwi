package mikeshafter.iciwi.api;

import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import mikeshafter.iciwi.Iciwi;
import java.util.Arrays;

public abstract class ClosableFareGate extends FareGate {
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private Object[] closeGateArray;

  /**
   * Sets the closeGateArray
   * @param closeGateArray the closeGateArray, in the format {Location[], Runnable[]}
   */
  public void setCloseGateArray (Object[] closeGateArray) { this.closeGateArray = closeGateArray; }

  /**
   * Creates a new fare gate at the sign's location.
   */
  public ClosableFareGate () { super(); }

  /**
   * Creates a new fare gate with an offset from the sign's location.
   * @param locationOffset Default offset sign location
   */
  public ClosableFareGate (Vector locationOffset) { super(locationOffset); }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (this.closeGateArray == null) return;
    Location location = event.getPlayer().getLocation();
    Location[] gateLocs = ((Location[]) closeGateArray[0]);
    for (Location gateLoc : gateLocs) {
      if (gateLoc.getBlockX() == location.getBlockX() && gateLoc.getBlockY() == location.getBlockY() && gateLoc.getBlockZ() == location.getBlockZ()) {
        for (Runnable runnable : ((Runnable[]) closeGateArray[1]))
          plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, plugin.getConfig().getLong("close-after-pass"));
        break;
      }
    }
  }
}
