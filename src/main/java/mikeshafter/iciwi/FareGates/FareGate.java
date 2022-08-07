package mikeshafter.iciwi.FareGates;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class FareGate {
  
  private final Player player;
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final Lang lang = new Lang(plugin);
  private Map<Location, BlockFace> locationFaceMap;
  private GateType gateType;
  
  public FareGate(Player player) {
    this.player = player;
  }

  public FareGate(Player player, String signText, @Nullable Location signLoc) {
    this.player = player;
    this.gateType = getGateType(signText);

    if (gateType != null) {
      String args = switch (gateType) {
        case ENTRY -> signText.substring(lang.getString("entry").length()+1, signText.length()-1);
        case EXIT -> signText.substring(lang.getString("exit").length()+1, signText.length()-1);
        case VALIDATOR, SPECIAL -> "";
        case FAREGATE -> signText.substring(lang.getString("faregate").length()+1, signText.length()-1);
        case MEMBER -> signText.substring(lang.getString("member").length()+1, signText.length()-1);
      };
      
      // Parse for validator
      if (this.gateType == GateType.SPECIAL) {
        return;
      }

      // todo: args
      // Convert args into a binary
      int flags = 0;
      if (args.contains("V")) flags += 1;  // Validator
      if (args.contains("S")) flags += 2;  // Sideways sign
      if (args.contains("L")) flags += 4;  // Lefty sign
      if (args.contains("D")) flags += 8;  // Double fare gate
      if (args.contains("R")) flags += 16; // Redstone activator
      if (args.contains("E")) flags += 32; // Eye-level sign.
  
      if ((flags&1) == 1) {
        // validator, location does not matter
        this.gateType = GateType.VALIDATOR;
        return;
      }
  
      flags >>= 1;
  
      // LM-style fare gates
      // location matters
      if ((gateType == GateType.ENTRY || gateType == GateType.EXIT || gateType == GateType.MEMBER) && (signLoc != null && signLoc.getBlock().getState() instanceof Sign && signLoc.getBlock().getState().getBlockData() instanceof WallSign sign)) {
  
        BlockFace signFacing = sign.getFacing();
        byte[][] blockRelLoc = parseArgsLM(flags);
  
        for (byte[] locVector : blockRelLoc) {
          switch (signFacing) {
            case SOUTH -> this.locationFaceMap.put(signLoc.clone().add(locVector[0], locVector[1], locVector[2]), parseOpenDirection(signFacing, locVector[3]));
            case NORTH -> this.locationFaceMap.put(signLoc.clone().add(-locVector[0], locVector[1], -locVector[2]), parseOpenDirection(signFacing, locVector[3]));
            case EAST -> this.locationFaceMap.put(signLoc.clone().add(locVector[2], locVector[1], -locVector[0]), parseOpenDirection(signFacing, locVector[3]));
            case WEST -> this.locationFaceMap.put(signLoc.clone().add(-locVector[2], locVector[1], locVector[0]), parseOpenDirection(signFacing, locVector[3]));
            default -> plugin.getServer().getLogger().info("Fare gate not set up correctly!");
          }
        }
  
      }

      // HL-style fare gates
      else if (gateType == GateType.FAREGATE && !(signLoc == null) && signLoc.getBlock().getState() instanceof Sign && signLoc.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Sign sign) {
  
        BlockFace signFacing = sign.getRotation();
        byte[][] blockRelLoc = parseArgsHL(flags);
  
        for (byte[] locVector : blockRelLoc) {
          switch (signFacing) {
            case SOUTH -> this.locationFaceMap.put(signLoc.clone().add(locVector[0], locVector[1], locVector[2]), null);
            case NORTH -> this.locationFaceMap.put(signLoc.clone().add(-locVector[0], locVector[1], -locVector[2]), null);
            case EAST -> this.locationFaceMap.put(signLoc.clone().add(locVector[2], locVector[1], -locVector[0]), null);
            case WEST -> this.locationFaceMap.put(signLoc.clone().add(-locVector[2], locVector[1], locVector[0]), null);
            default -> plugin.getServer().getLogger().info("Fare gate not set up correctly!");
          }
        }
  
      }
    }
  }

  private GateType getGateType(String text) {
    if (text.contains(lang.getString("entry"))) {
      return GateType.ENTRY;
    } else if (text.contains(lang.getString("exit"))) {
      return GateType.EXIT;
    } else if (text.contains(lang.getString("validator"))) {
      return GateType.SPECIAL;
    } else if (text.contains(lang.getString("faregate"))) {
      return GateType.FAREGATE;
    } else if (text.contains(lang.getString("member"))) {
      return GateType.MEMBER;
    } else return null;
  }
  
  // LM gates
  // 4th value is the opening direction.
  // A 0 for the 4th value means that there is no animation
  private byte[][] parseArgsLM(int args) {
    /*
         -k
      -i [] +i
         +k
    */
    return switch (args) {
      case 0 -> new byte[][] {{-1, 0, -1, 1}};  //
      case 1 -> new byte[][] {{-1, 0, 0, -2}};  // S
      case 2 -> new byte[][] {{1, 0, -1, -1}};  //  L
      case 3 -> new byte[][] {{1, 0, 0, -2}};  // SL
      case 4 -> new byte[][] {{-1, 0, -1, 1}, {-2, 0, -1, -1}};  //   D
      case 5 -> new byte[][] {{-1, 0, 0, -2}, {-1, 0, 1, 2}};  // S D
      case 6 -> new byte[][] {{1, 0, -1, -1}, {2, 0, -1, 1}};  //  LD
      case 7 -> new byte[][] {{1, 0, 0, -2}, {1, 0, 1, 2}};  // SLD
      case 8 -> new byte[][] {{0, -1, -1, 0}};  //    R
      case 9 -> new byte[][] {{0, -1, -1, 0}};  // S  R
      case 10 -> new byte[][] {{0, -1, -1, 0}}; //  L R
      case 11 -> new byte[][] {{0, -1, -1, 0}}; // SL R
      case 12 -> new byte[][] {{0, -1, -1, 0}, {-1, -1, -1, 0}}; //   DR
      case 13 -> new byte[][] {{0, -1, -1, 0}, {-1, -1, -1, 0}}; // S DR
      case 14 -> new byte[][] {{0, -1, -1, 0}, {1, -1, -1, 0}}; //  LDR
      case 15 -> new byte[][] {{0, -1, -1, 0}, {1, -1, -1, 0}}; // SLDR
      case 16 -> new byte[][] {{-1, -1, -1, 1}}; //     E
      case 17 -> new byte[][] {{-1, -1, 0, -2}}; // S   E
      case 18 -> new byte[][] {{1, -1, -1, -1}}; //  L  E
      case 19 -> new byte[][] {{1, -1, 0, -2}}; // SL  E
      case 20 -> new byte[][] {{-1, -1, -1, 1}, {-2, -1, -1, -1}}; //   D E
      case 21 -> new byte[][] {{-1, -1, 0, -2}, {-1, -1, 1, 2}}; // S D E
      case 22 -> new byte[][] {{1, -1, -1, -1}, {2, -1, -1, 1}}; //  LD E
      case 23 -> new byte[][] {{1, -1, 0, -2}, {1, -1, 1, 2}}; // SLD E
      case 24 -> new byte[][] {{0, -2, -1, 0}}; //    RE
      case 25 -> new byte[][] {{0, -2, -1, 0}}; // S  RE
      case 26 -> new byte[][] {{0, -2, -1, 0}}; //  L RE
      case 27 -> new byte[][] {{0, -2, -1, 0}}; // SL RE
      case 28 -> new byte[][] {{0, -2, -1, 0}, {-1, -2, -1, 0}}; //   DRE
      case 29 -> new byte[][] {{0, -2, -1, 0}, {-1, -2, -1, 0}}; // S DRE
      case 30 -> new byte[][] {{0, -2, -1, 0}, {1, -2, -1, 0}}; //  LDRE
      case 31 -> new byte[][] {{0, -2, -1, 0}, {1, -2, -1, 0}}; // SLDRE
      default -> null;
    };
  }
  
  private BlockFace parseOpenDirection(BlockFace signFacing, byte code) {
    switch (code) {
      case 0 -> {
        return null;
      }  // get no open out of the way
      // same directions
      case 2 -> {
        return signFacing;
      }
      case -2 -> {
        return signFacing.getOppositeFace();
      }
      // different directions
      case 1 -> {
        return switch (signFacing) {
          case SOUTH -> BlockFace.EAST;
          case EAST -> BlockFace.NORTH;
          case NORTH -> BlockFace.WEST;
          case WEST -> BlockFace.SOUTH;
          default -> null;
        };
      }
      case -1 -> {
        return switch (signFacing) {
          case SOUTH -> BlockFace.WEST;
          case WEST -> BlockFace.NORTH;
          case NORTH -> BlockFace.EAST;
          case EAST -> BlockFace.SOUTH;
          default -> null;
        };
      }
      default -> {
        return null;
      }
      
    }
    
  }
  
  // HL gates
  // 4th value is the opening direction (always i axis).
  // A 0 for the 4th value means that there is no animation
  private byte[][] parseArgsHL(int args) {
    args &= 14;
    args >>= 1;
    return switch (args) { // LDR
      case 2 -> new byte[][] {{0, 2, 0, 0}, {-1, 2, 0, 0}};  //  D
      case 3 -> new byte[][] {{0, 2, 0, 0}, {1, 2, 0, 0}};  // LD
      case 4 -> new byte[][] {{0, 2, 0, 0}, {0, -2, 0, 0}};  //   R
      case 5 -> new byte[][] {{0, 2, 0, 0}, {0, -2, 0, 0}};  // L R
      case 6 -> new byte[][] {{0, 2, 0, 0}, {-1, 2, 0, 0}, {0, -2, 0, 0}};  //  DR
      case 7 -> new byte[][] {{0, 2, 0, 0}, {1, 2, 0, 0}, {0, -2, 0, 0}};  // LDR
      default -> new byte[][] {{0, 2, 0, 0}};  // no flag, L
    };
  }
  
  public GateType getGateType() {
    return this.gateType;
  }
  
  public Player getPlayer() {
    return this.player;
  }
  
  public void open() {
    List<FareGateBlock> fareGateBlocks;
    fareGateBlocks.forEach(FareGateBlock::onGateOpen);
  }
  
  public void hold() {
    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this::close, plugin.getConfig().getLong("gate-close-delay"));
  }
  
  
  public Set<Location> getGateLocations() {
    return this.locationFaceMap.keySet();
  }
  
  
  public void close() {
    List<FareGateBlock> fareGateBlocks;
    fareGateBlocks.forEach(fareGateBlock -> close());
  }
}
