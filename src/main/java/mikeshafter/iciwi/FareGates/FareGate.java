package mikeshafter.iciwi.FareGates;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;


public class FareGate {
  private BlockState fareGate;
  
  public FareGate(BlockState fareGate) {
    this.fareGate = fareGate;
  }
  
  public BlockState getFareGate() {
    return fareGate;
  }
  
  public void setFareGate(BlockState fareGate) {
    this.fareGate = fareGate;
  }
  
  public Location getLocation() {
    return this.fareGate.getLocation();
  }
  
  public Material getMaterial() {
    return this.fareGate.getType();
  }
  
  public Block getBlock() {
    return this.fareGate.getBlock();
  }
  
  public BlockData getBlockData() {
    return this.fareGate.getBlockData();
  }
}

