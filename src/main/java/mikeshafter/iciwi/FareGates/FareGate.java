package mikeshafter.iciwi.FareGates;

import mikeshafter.iciwi.Lang;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;


public class FareGate {
  
  private final Player player;
  Lang lang = new Lang();
  @Nullable
  private Block block = null;
  @Nullable
  private BlockState state = null;
  @Nullable
  private Location location = null;
  
  public FareGate(Player player) {
    this.player = player;
  }
  
  public FareGate(Player player, Block block) {
    this.player = player;
    this.block = block;
    this.location = block.getLocation();
  }
  
  public FareGate(Player player, @Nullable Location location) {
    this.location = location;
    this.player = player;
  }
  
  public FareGate(Player player, String signText, Location signLoc) {
    this.player = player;
    GateType gateType = getGateType(signText);
    
    if (gateType != null) {
      char[] args = switch (gateType) {
        case ENTRY -> signText.substring(lang.ENTRY.length()+1, signText.length()-1).toCharArray();
        case EXIT -> signText.substring(lang.EXIT.length()+1, signText.length()-1).toCharArray();
        case VALIDATOR -> signText.substring(lang.VALIDATOR.length()+1, signText.length()-1).toCharArray();
        case FAREGATE -> signText.substring(lang.FAREGATE.length()+1, signText.length()-1).toCharArray();
      };
      
      // todo: args
    }
  }
  
  
  private GateType getGateType(String text) {
    if (text.contains(lang.ENTRY)) {
      return GateType.ENTRY;
    } else if (text.contains(lang.EXIT)) {
      return GateType.EXIT;
    } else if (text.contains(lang.VALIDATOR)) {
      return GateType.VALIDATOR;
    } else if (text.contains(lang.FAREGATE)) {
      return GateType.FAREGATE;
    } else return null;
  }
  
  
  public boolean open() {
    if (location == null || block == null) {
      return false;
    }
    state = block.getState();
    if (block.getBlockData() instanceof Openable openable) {
      openable.setOpen(true);
    } else {
      block.setType(Material.AIR);  // Default Limaru fare gate
    }
    return true;
  }
  
  
  public boolean close() {
    if (location == null || state == null) {
      return false;
    }
    if (state.getBlockData() instanceof Openable openable) {
      openable.setOpen(false);
    } else {
      location.getBlock().setType(state.getType());
      location.getBlock().setBlockData(state.getBlockData());
    }
    return true;
  }
}
