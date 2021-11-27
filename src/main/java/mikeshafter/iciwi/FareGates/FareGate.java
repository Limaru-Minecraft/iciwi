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

import java.util.ArrayList;
import java.util.List;


public class FareGate {

  private final Player player;
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final Lang lang = new Lang(plugin);
  private FareGate[] fareGates;
  private GateType gateType;

  public FareGate(Player player) {
    this.player = player;
  }

  public FareGate(Player player, String signText, @Nullable Location signLoc) {
    this.player = player;
    this.gateType = getGateType(signText);
    this.fareGates = null;

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

      if ((flags&1) == 1 || this.gateType == GateType.VALIDATOR) {
        // validator, location does not matter
        this.gateType = GateType.VALIDATOR;
        return;
      }

      flags >>= 1;

      // LM-style fare gates
      // location matters
      if ((gateType == GateType.ENTRY || gateType == GateType.EXIT) && (signLoc != null && signLoc.getBlock().getState() instanceof Sign && signLoc.getBlock().getState().getBlockData() instanceof WallSign sign)) {

        BlockFace direction = sign.getFacing();
        byte[][] locations = parseArgsLM(flags);
        this.fareGates = new OpenFareGate[locations.length];

        for (int i = 0; i < locations.length; i++) {
          byte[] locVector = locations[i];
          switch (direction) {
            case SOUTH -> this.fareGates[i] = new OpenFareGate(player, signLoc.clone().add(locVector[0], locVector[1], locVector[2]));
            case NORTH -> this.fareGates[i] = new OpenFareGate(player, signLoc.clone().add(-locVector[0], locVector[1], -locVector[2]));
            case EAST -> this.fareGates[i] = new OpenFareGate(player, signLoc.clone().add(locVector[2], locVector[1], -locVector[0]));
            case WEST -> this.fareGates[i] = new OpenFareGate(player, signLoc.clone().add(-locVector[2], locVector[1], locVector[0]));
            default -> plugin.getServer().getLogger().info("Fare gate not set up correctly!");
          }
        }

      }

      // HL-style fare gates
      else if (gateType == GateType.FAREGATE && !(signLoc == null) && signLoc.getBlock().getState() instanceof Sign && signLoc.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Sign sign) {

        BlockFace direction = sign.getRotation();
        byte[][] locations = parseArgsHL(flags);
        this.fareGates = new OpenFareGate[locations.length];

        for (int i = 0; i < locations.length; i++) {
          byte[] locVector = locations[i];
          switch (direction) {
            case SOUTH -> this.fareGates[i] = new OpenFareGate(player, signLoc.clone().add(locVector[0], locVector[1], locVector[2]));
            case NORTH -> this.fareGates[i] = new OpenFareGate(player, signLoc.clone().add(-locVector[0], locVector[1], -locVector[2]));
            case EAST -> this.fareGates[i] = new OpenFareGate(player, signLoc.clone().add(locVector[2], locVector[1], -locVector[0]));
            case WEST -> this.fareGates[i] = new OpenFareGate(player, signLoc.clone().add(-locVector[2], locVector[1], locVector[0]));
            default -> plugin.getServer().getLogger().info("Fare gate not set up correctly!");
          }
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

  // LM gates
  private byte[][] parseArgsLM(int args) {
    /*
         -k
      -i [] +i
         +k
    */
    return switch (args) {
      case 0 -> new byte[][] {{-1, 0, -1}};  //
      case 1 -> new byte[][] {{-1, 0, 0}};  // S
      case 2 -> new byte[][] {{1, 0, -1}};  //  L
      case 3 -> new byte[][] {{1, 0, 0}};  // SL
      case 4  -> new byte[][] {{-1, 0, -1}, {-2, 0, -1}};  //   D
      case 5  -> new byte[][] {{-1, 0, 0}, {-1, 0, 1}};  // S D
      case 6  -> new byte[][] {{1, 0, -1}, {2, 0, -1}};  //  LD
      case 7  -> new byte[][] {{1, 0, 0}, {1, 0, -1}};  // SLD
      case 8  -> new byte[][] {{0, -1, -1}};  //    R
      case 9  -> new byte[][] {{0, -1, -1}};  // S  R
      case 10 -> new byte[][] {{0, -1, -1}}; //  L R
      case 11 -> new byte[][] {{0, -1, -1}}; // SL R
      case 12 -> new byte[][] {{0, -1, -1}, {-1, -1, -1}}; //   DR
      case 13 -> new byte[][] {{0, -1, -1}, {-1, -1, -1}}; // S DR
      case 14 -> new byte[][] {{0, -1, -1}, {1, -1, -1}}; //  LDR
      case 15 -> new byte[][] {{0, -1, -1}, {1, -1, -1}}; // SLDR
      case 16 -> new byte[][] {{-1, -1, -1}}; //     E
      case 17 -> new byte[][] {{-1, -1, 0}}; // S   E
      case 18 -> new byte[][] {{1, -1, -1}}; //  L  E
      case 19 -> new byte[][] {{1, -1, 0}}; // SL  E
      case 20 -> new byte[][] {{-1, -1, -1}, {-2, -1, -1}}; //   D E
      case 21 -> new byte[][] {{-1, -1, 0}, {-1, -1, 1}}; // S D E
      case 22 -> new byte[][] {{1, -1, -1}, {2, -1, -1}}; //  LD E
      case 23 -> new byte[][] {{1, -1, 0}, {1, -1, -1}}; // SLD E
      case 24 -> new byte[][] {{0, -2, -1}}; //    RE
      case 25 -> new byte[][] {{0, -2, -1}}; // S  RE
      case 26 -> new byte[][] {{0, -2, -1}}; //  L RE
      case 27 -> new byte[][] {{0, -2, -1}}; // SL RE
      case 28 -> new byte[][] {{0, -2, -1}, {-1, -2, -1}}; //   DRE
      case 29 -> new byte[][] {{0, -2, -1}, {-1, -2, -1}}; // S DRE
      case 30 -> new byte[][] {{0, -2, -1}, {1, -2, -1}}; //  LDRE
      case 31 -> new byte[][] {{0, -2, -1}, {1, -2, -1}}; // SLDRE
      default -> null;
    };
  }

  // HL gates
  private byte[][] parseArgsHL(int args) {
    args &= 14;
    args >>= 1;
    return switch (args) { // LDR
      case 2 -> new byte[][] {{0, 2, 0}, {-1, 2, 0}};  //  D
      case 3 -> new byte[][] {{0, 2, 0}, {1, 2, 0}};  // LD
      case 4 -> new byte[][] {{0, 2, 0}, {0, -2, 0}};  //   R
      case 5 -> new byte[][] {{0, 2, 0}, {0, -2, 0}};  // L R
      case 6 -> new byte[][] {{0, 2, 0}, {-1, 2, 0}, {0, -2, 0}};  //  DR
      case 7 -> new byte[][] {{0, 2, 0}, {1, 2, 0}, {0, -2, 0}};  // LDR
      default -> new byte[][] {{0, 2, 0}};  // no flag, L
    };
  }

  public GateType getGateType() {
    return this.gateType;
  }

  public Player getPlayer() {
    return player;
  }

  public boolean open() {
    if (this.fareGates == null) return true;
    else {
      boolean a = true;
      for (FareGate gate : fareGates) a &= gate.open();
      return a;
    }
  }

  public void hold() {
    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this::close, plugin.getConfig().getLong("gate-close-delay"));
  }


  public List<int[]> getGateLocations() {
    ArrayList<int[]> a = new ArrayList<>();
    for (FareGate gate : this.fareGates) a.addAll(gate.getGateLocations());
    return a;
  }


  public boolean close() {
    if (this.fareGates == null) return true;
    else {
      boolean closed = true;
      for (FareGate gate : this.fareGates) closed &= gate.close();
      return closed;
    }
  }
}
