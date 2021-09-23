package mikeshafter.iciwi.FareGates;

import org.bukkit.Material;
import org.bukkit.block.BlockState;


public class GlassFareGate extends FareGate {
  
  public GlassFareGate(BlockState fareGate) {
    super(fareGate);
  }
  
  public void open() {
    super.getBlock().setType(Material.AIR);
  }
  
  public void close() {
    super.getBlock().setType(super.getMaterial());
    super.getBlock().setBlockData(super.getBlockData());
  }
}
