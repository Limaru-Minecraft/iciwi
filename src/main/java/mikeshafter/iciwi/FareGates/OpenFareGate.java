package mikeshafter.iciwi.FareGates;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;


public class OpenFareGate extends FareGate {
  
  private final Location blockLocation;
  private BlockState state;
  
  public OpenFareGate(Player player, Location blockLocation) {
    super(player);
    this.blockLocation = blockLocation;
  }
  
  @Override
  public boolean open() {
    state = blockLocation.getBlock().getState();
    // HL trapdoors
    if (this.blockLocation.getBlock().getBlockData() instanceof Openable openable) {
      openable.setOpen(true);
      this.blockLocation.getBlock().setBlockData(openable);
    }
    
    // Lever
    else if (this.blockLocation.getBlock().getBlockData() instanceof Powerable powerable) {
      powerable.setPowered(true);
      this.blockLocation.getBlock().setBlockData(powerable);
    }
    
    // LM glass
    else {
      blockLocation.getBlock().setType(Material.AIR);
    }
    return true;
  }
  
  @Override
  public boolean close() {
    state = blockLocation.getBlock().getState();
    // HL trapdoors
    if (this.blockLocation.getBlock().getBlockData() instanceof Openable openable) {
      openable.setOpen(false);
      this.blockLocation.getBlock().setBlockData(openable);
    }
    
    // Lever
    else if (this.blockLocation.getBlock().getBlockData() instanceof Powerable powerable) {
      powerable.setPowered(false);
      this.blockLocation.getBlock().setBlockData(powerable);
    }
    
    // LM glass
    else {
      blockLocation.getBlock().setType(state.getType());
      blockLocation.getBlock().setBlockData(state.getBlockData());
    }
    return true;
  }
}
