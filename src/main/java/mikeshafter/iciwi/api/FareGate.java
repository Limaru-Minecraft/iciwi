package mikeshafter.iciwi.api;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import static mikeshafter.iciwi.util.IciwiUtil.*;


public abstract class FareGate implements Listener {

  private String signLine0;
  private Vector locationOffset = new Vector();
  private BlockState signState;

  /**
   * Constructor.
   * @param signLine0 First line (preferably without "[]")
   */
  public FareGate (String signLine0) {
    this.signLine0 = signLine0;
  }

  /**
   * Constructor.
   * @param signLine0 First line (preferably without "[]")
   * @param locationOffset Default offset sign location
   */
  public FareGate(String signLine0, Vector locationOffset) {
    this.signLine0 = signLine0;
    this.locationOffset = locationOffset;
  }

  /**
   * Gets the first line to be used in the sign
   * @return first line in sign
   */
  public String getSignLine0 () {
    return this.signLine0;
  }

  /**
   * Sets the first line to be used in the sign
   * @param signLine0 first line in sign
   */
  public void setSignLine0 (String signLine0) {
    this.signLine0 = signLine0;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Block block = event.getClickedBlock();
    if (block == null) return;
    Location clickedLocation = block.getLocation();
    clickedLocation.add(this.locationOffset);
    BlockState signState = clickedLocation.getBlock().getState();
    if (signState instanceof Sign sign && parseComponent(sign.line(0)).contains(signLine0)) {
      String[] signText = new String[4];
      signText[0] = parseComponent(sign.line(0));
      signText[1] = parseComponent(sign.line(1));
      signText[2] = parseComponent(sign.line(2));
      signText[3] = parseComponent(sign.line(3));
      onInteract(event.getPlayer(), event.getItem(), signText);
    }
  }

  /**
   * Method called when player interacts with the fare gate
   * @param player Player who interacted
   * @param item Item used to interact
   * @param signText Text in the sign
   */
  public abstract void onInteract(Player player, ItemStack item, String[] signText);
}
