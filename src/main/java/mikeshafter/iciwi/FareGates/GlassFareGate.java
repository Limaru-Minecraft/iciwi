package mikeshafter.iciwi.FareGates;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public class GlassFareGate extends FareGate {
  
  public GlassFareGate(Player player) {
    super(player);
  }
  
  public GlassFareGate(Player player, String signText, Location signLoc) {
    super(player, signText, signLoc);
  }
}
