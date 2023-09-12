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

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerMove(PlayerMoveEvent event) {
    if (this.closeGateArray == null) return;
    Location ploc = event.getPlayer().getLocation();
    Location[] glocs = ((Location[]) closeGateArray[0]);
    label0:
    for (Location gloc : glocs) {
      if (gloc.getBlockX() == ploc.getBlockX() && gloc.getBlockY() == ploc.getBlockY() && gloc.getBlockZ() == ploc.getBlockZ()) {
        for (Runnable r : ((Runnable[]) closeGateArray[1]))
          plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, r, plugin.getConfig().getLong("close-after-pass"));
        break label0;
      }
    }
  }
}
