package mikeshafter.iciwi.FareGates;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;


public class OpenFareGate extends FareGate {
  
  private final Location blockLocation;
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);  // TODO: DEBUG
  private BlockState state;
  
  public OpenFareGate(Player player, Location blockLocation) {
    super(player);
    this.blockLocation = blockLocation;
  }

  @Override
  public boolean open() {
    state = blockLocation.getBlock().getState();
    // HL trapdoors
    if (state.getBlockData() instanceof Openable openable) {
      openable.setOpen(true);
      state.setBlockData(openable);
      state.update();
      plugin.getServer().getLogger().info("OpenFareGate Open Debug 1 HL");  // TODO: DEBUG
    }
  
    // Lever
    else if (state.getBlockData() instanceof Powerable powerable) {
      powerable.setPowered(true);
      state.setBlockData(powerable);
      state.update();
      plugin.getServer().getLogger().info("OpenFareGate Open Debug 1 Lever");  // TODO: DEBUG
    }
  
    // LM glass
    else {
      blockLocation.getBlock().setType(Material.AIR);
      plugin.getServer().getLogger().info("OpenFareGate Open Debug 1 LM");  // TODO: DEBUG
    }
    return true;
  }
  
  @Override
  public List<int[]> getGateLocations() {
    int x = this.blockLocation.getBlockX();
    int y = this.blockLocation.getBlockY();
    int z = this.blockLocation.getBlockZ();
    return Collections.singletonList(new int[] {x, y, z});
  }
  
  @Override
  public boolean close() {
    plugin.getServer().getLogger().info("OpenFareGate Close Debug 0");  // TODO: DEBUG
    plugin.getServer().getLogger().info("OpenFareGate"+this.blockLocation.getBlockX()+" "+this.blockLocation.getBlockY()+" "+this.blockLocation.getBlockZ());
    // HL trapdoors
    if (state.getBlockData() instanceof Openable openable) {
      openable.setOpen(false);
      this.state.setBlockData(openable);
      this.state.update();
      plugin.getServer().getLogger().info("OpenFareGate Close Debug 1 HL");  // TODO: DEBUG
    }
    
    // Lever
    else if (state.getBlockData() instanceof Powerable powerable) {
      powerable.setPowered(false);
      this.state.setBlockData(powerable);
      this.state.update();
      plugin.getServer().getLogger().info("OpenFareGate Close Debug 1 Lever");  // TODO: DEBUG
    }
    
    // LM glass
    else {
      this.state.update();
      this.blockLocation.getBlock().setType(this.state.getType());
      this.blockLocation.getBlock().setBlockData(this.state.getBlockData());
      plugin.getServer().getLogger().info("OpenFareGate Close Debug 1 LM");  // TODO: DEBUG
    }
    return true;
  }
}
