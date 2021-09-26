package mikeshafter.iciwi.FareGates;

import mikeshafter.iciwi.Lang;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;


public class FareGate {
  
  private final Player player;
  Lang lang = new Lang();
  public FareGate[] fareGate;
  
  public FareGate(Player player) {
    this.player = player;
  }
  
  public FareGate(Player player, String signText, @Nullable Location signLoc) {
    this.player = player;
    GateType gateType = getGateType(signText);
    this.fareGate = null;
    
    if (gateType != null) {
      String args = switch (gateType) {
        case ENTRY -> signText.substring(lang.ENTRY.length()+1, signText.length()-1);
        case EXIT -> signText.substring(lang.EXIT.length()+1, signText.length()-1);
        case VALIDATOR -> signText.substring(lang.VALIDATOR.length()+1, signText.length()-1);
        case FAREGATE -> signText.substring(lang.FAREGATE.length()+1, signText.length()-1);
      };
      
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
        this.fareGate = null;
        return;
      }
      
      flags >>= 1;
      
      // LM-style fare gates
      // location matters
      if ((gateType == GateType.ENTRY || gateType == GateType.EXIT) && (signLoc != null && signLoc.getBlock().getState() instanceof Sign && signLoc.getBlock().getState().getBlockData() instanceof WallSign sign)) {
        
        BlockFace direction = sign.getFacing();
        
        if (direction == BlockFace.SOUTH) {
          this.fareGate = new OpenFareGate[parseArgs(flags).length];
          for (int i = 0; i < parseArgs(flags).length; i++) {
            byte[] locVector = parseArgs(flags)[i];
            this.fareGate[i] = new OpenFareGate(player, signLoc.clone().add(locVector[0], locVector[1], -locVector[2]));
          }
        } else if (direction == BlockFace.EAST) {
          this.fareGate = new OpenFareGate[parseArgs(flags).length];
          for (int i = 0; i < parseArgs(flags).length; i++) {
            byte[] locVector = parseArgs(flags)[i];
            this.fareGate[i] = new OpenFareGate(player, signLoc.clone().add(-locVector[0], locVector[1], -locVector[2]));
          }
        } else if (direction == BlockFace.NORTH) {
          this.fareGate = new OpenFareGate[parseArgs(flags).length];
          for (int i = 0; i < parseArgs(flags).length; i++) {
            byte[] locVector = parseArgs(flags)[i];
            this.fareGate[i] = new OpenFareGate(player, signLoc.clone().add(-locVector[0], locVector[1], locVector[2]));
          }
        } else if (direction == BlockFace.WEST) {
          this.fareGate = new OpenFareGate[parseArgs(flags).length];
          for (int i = 0; i < parseArgs(flags).length; i++) {
            byte[] locVector = parseArgs(flags)[i];
            this.fareGate[i] = new OpenFareGate(player, signLoc.clone().add(locVector[0], locVector[1], locVector[2]));
          }
        }
        
      }
      
      // HL-style fare gates
      else if (gateType == GateType.FAREGATE && !(signLoc == null) && signLoc.getBlock().getState() instanceof Sign sign) {
        flags &= 14;
        flags >>= 1;
        byte[][] openLoc = switch (flags) { // LDR
          case 2 -> new byte[][] {{0, 2, 0}, {1, 2, 0}};
          case 3 -> new byte[][] {{0, 2, 0}, {-1, 2, 0}};
          case 4 -> new byte[][] {{0, 2, 0}, {0, -2, 0}};
          case 5 -> new byte[][] {{0, 2, 0}, {0, -2, 0}};
          case 6 -> new byte[][] {{0, 2, 0}, {1, 2, 0}, {0, -2, 0}};
          case 7 -> new byte[][] {{0, 2, 0}, {-1, 2, 0}, {0, -2, 0}};
          default -> new byte[][] {{0, 2, 0}};
        };
        this.fareGate = new OpenFareGate[openLoc.length];
        for (int i = 0; i < openLoc.length; i++) {
          byte[] locVector = openLoc[i];
          this.fareGate[i] = new OpenFareGate(player, signLoc.clone().add(locVector[0], locVector[1], locVector[2]));
        }
        
      }
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
  
  
  private byte[][] parseArgs(int args) {
    /*
         +k
      +i [] -i
         -k
    */
    return switch (args) {
      case 0 -> new byte[][] {{1, 0, 1}};  //
      case 1 -> new byte[][] {{1, 0, 0}};  // S
      case 2 -> new byte[][] {{-1, 0, 1}};  //  L
      case 3 -> new byte[][] {{-1, 0, 0}};  // SL
      case 4 -> new byte[][] {{1, 0, 1}, {2, 0, 1}};  //   D
      case 5 -> new byte[][] {{1, 0, 0}, {1, 0, -1}};  // S D
      case 6 -> new byte[][] {{-1, 0, 1}, {-2, 0, 1}};  //  LD
      case 7 -> new byte[][] {{-1, 0, 0}, {-1, 0, -1}};  // SLD
      case 8 -> new byte[][] {{0, -1, 1}};  //    R
      case 9 -> new byte[][] {{0, -1, 1}};  // S  R
      case 10 -> new byte[][] {{0, -1, 1}}; //  L R
      case 11 -> new byte[][] {{0, -1, 1}}; // SL R
      case 12 -> new byte[][] {{0, -1, 1}, {1, -1, 1}}; //   DR
      case 13 -> new byte[][] {{0, -1, 1}, {1, -1, 1}}; // S DR
      case 14 -> new byte[][] {{0, -1, 1}, {-1, -1, 1}}; //  LDR
      case 15 -> new byte[][] {{0, -1, 1}, {-1, -1, 1}}; // SLDR
      case 16 -> new byte[][] {{1, -1, 1}}; //     E
      case 17 -> new byte[][] {{1, -1, 0}}; // S   E
      case 18 -> new byte[][] {{-1, -1, 1}}; //  L  E
      case 19 -> new byte[][] {{-1, -1, 0}}; // SL  E
      case 20 -> new byte[][] {{1, -1, 1}, {2, -1, 1}}; //   D E
      case 21 -> new byte[][] {{1, -1, 0}, {1, -1, -1}}; // S D E
      case 22 -> new byte[][] {{-1, -1, 1}, {-2, -1, 1}}; //  LD E
      case 23 -> new byte[][] {{-1, -1, 0}, {-1, -1, -1}}; // SLD E
      case 24 -> new byte[][] {{0, -2, 1}}; //    RE
      case 25 -> new byte[][] {{0, -2, 1}}; // S  RE
      case 26 -> new byte[][] {{0, -2, 1}}; //  L RE
      case 27 -> new byte[][] {{0, -2, 1}}; // SL RE
      case 28 -> new byte[][] {{0, -2, 1}, {1, -2, 1}}; //   DRE
      case 29 -> new byte[][] {{0, -2, 1}, {1, -2, 1}}; // S DRE
      case 30 -> new byte[][] {{0, -2, 1}, {-1, -2, 1}}; //  LDRE
      case 31 -> new byte[][] {{0, -2, 1}, {-1, -2, 1}}; // SLDRE
      default -> null;
    };
  }
  
  
  public boolean open() {
    if (this.fareGate == null) return true;
    else {
      boolean a = true;
      for (FareGate gate : fareGate) a &= gate.open();
      return a;
    }
  }
  
  
  public boolean close() {
    if (this.fareGate == null) return true;
    else {
      boolean a = true;
      for (FareGate gate : fareGate) a &= gate.close();
      return a;
    }
  }
}
