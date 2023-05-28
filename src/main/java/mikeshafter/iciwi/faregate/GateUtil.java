package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.util.Vector;

public class GateUtil {

  private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private static final Lang lang = plugin.lang;

  /**
   * Opens a fare gate
   * @param signText Text on the sign for quick accessing
   * @param sign The sign itself
   */
  protected static void openGate(String[] signText, Sign sign) {
    String signLine0 = signText[0];

    // Get the sign's direction and reference block
    BlockFace signFacing = BlockFace.SOUTH;
    Block referenceBlock = sign.getBlock();
    if (sign.getBlockData() instanceof org.bukkit.block.data.type.WallSign w) {
      signFacing = w.getFacing();
      referenceBlock = sign.getLocation().clone().add(signFacing.getOppositeFace().getDirection()).getBlock();
    }
    else if (sign.getBlockData() instanceof org.bukkit.block.data.type.Sign s) signFacing = s.getRotation();
    signFacing = toCartesian(signFacing);

    // Get the fare gate flags
    Vector gatePos = new Vector(0, 0, 0);  // Default fare gate position
    String args = signLine0.substring(lang.getString("entry").length() + 1, signLine0.length() - 1);

    int flags = 0;
    flags |= args.contains("V") ? 1  : 0;  // Validator
    flags |= args.contains("S") ? 2  : 0;  // Sideways
    flags |= args.contains("L") ? 4  : 0;  // Lefty
    flags |= args.contains("D") ? 8  : 0;  // Double
    flags |= args.contains("R") ? 16 : 0;  // Redstone
    flags |= args.contains("E") ? 32 : 0;  // Eye-level
    flags |= args.contains("F") ? 64 : 0;  // Fare gate

    // Get the direction to build double fare gates in.
    Vector buildDirection = toBuildDirection(signFacing, flags);

    // Get the relative position(s) of the fare gate block(s).


    // Get the absolute position(s) of the fare gate block(s).


    // If openable, open it the Minecraft way


    // If glass pane, create a FareGateBlock object and open


    // Otherwise, set to air


  }

  /**
   * Changes arbitrary sign directions into cartesian (one cardinal direction only) directions.
   * This method has a clockwise bias.
   * @param face the original direction
   * @return the altered direction
   */
  private static BlockFace toCartesian (BlockFace face) {
    if (!face.isCartesian()) {
      return switch (face) {
        case NORTH_WEST, NORTH_NORTH_WEST, NORTH_NORTH_EAST -> BlockFace.NORTH;
        case NORTH_EAST, EAST_NORTH_EAST, EAST_SOUTH_EAST -> BlockFace.EAST;
        case SOUTH_EAST, SOUTH_SOUTH_EAST, SOUTH_SOUTH_WEST -> BlockFace.SOUTH;
        case SOUTH_WEST, WEST_SOUTH_WEST, WEST_NORTH_WEST -> BlockFace.WEST;
        default -> face;
      };
    } else return face;
  }

  /**
   * Gets the direction to build and animate fare gates in.
   * The animation direction should be the opposite direction to the build direction.
   * @param signDirection the direction of the sign
   * @param flags the flags to be applied
   */
  private static Vector toBuildDirection (BlockFace signDirection, int flags) {
    if ((flags & 1 | flags & 16) != 0) return new Vector();  // Validator and Redstone: no animation/double gate allowed
    else if ((flags & 2) != 0) return signDirection.getDirection();  // Sideways

    else if ((flags & 4) != 0) return signDirection.getDirection().getCrossProduct(new Vector(0, 1, 0));  // Lefty
    else return signDirection.getDirection().getCrossProduct(new Vector(0, -1, 0));  // Normal
  }
}
