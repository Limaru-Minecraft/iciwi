package mikeshafter.iciwi.faregate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import static org.junit.jupiter.api.Assertions.*;

public class CardUtilTest {

// TEST METHOD BEGIN

  /**
   * Changes arbitrary sign directions into cartesian (one cardinal direction only) directions.
   * This method has a clockwise bias.
   * @param face the original direction
   * @return the altered direction
   */
  public static BlockFace toCartesian (BlockFace face) {
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
   * @return the direction to build fare gates in.
   */
  public static Vector toBuildDirection (BlockFace signDirection, int flags) {
    if ((flags & 1 | flags & 16) != 0) return new Vector();	// Validator and Redstone: no animation/double gate allowed
    else if ((flags & 4) != 0) return signDirection.getDirection();	// Sideways
    else if ((flags & 2) != 0) return signDirection.getDirection().getCrossProduct(new Vector(0, -1, 0));	// Lefty
    else return signDirection.getDirection().getCrossProduct(new Vector(0, 1, 0));	// Normal
  }

  /**
   * Gets the relative positions of the fare gate blocks, with direction accounted for
   * The length of the returned Vector[] can be of length 0, 1, or 2.
   * @param signDirection the sign's facing direction
   * @param flags the flags to be applied
   * @return The positions of the fare gate blocks.
   */
  public static Vector[] toPos (BlockFace signDirection, int flags) {
    // length 0 if validator
    if ((flags & 1) != 0) return new Vector[0];

    // initialise vector array and default position vector
    Vector[] v = (flags & 8) == 0 ? new Vector[1] : new Vector[2];
    v[0] = signDirection.getDirection().getCrossProduct(new Vector(0, 1, 0));

    // parse default, S, E, R, F flags
    if ((flags &  4) != 0) v[0].add(signDirection.getDirection());
    if ((flags & 32) != 0) v[0].subtract(new Vector(0, 1, 0));
    if ((flags & 16) != 0) v[0] = new Vector(0, -2, 0);
    if ((flags & 64) != 0) v[0] = new Vector(0, 2, 0);
    // parse L flag
    if ((flags & 2) != 0 && (signDirection == BlockFace.SOUTH || signDirection == BlockFace.NORTH)) v[0].multiply(new Vector(-1, 1, 1));
    if ((flags & 2) != 0 && (signDirection == BlockFace.EAST  || signDirection == BlockFace.WEST )) v[0].multiply(new Vector(1, 1, -1));

    // parse D flag
    if ((flags & 8) != 0) v[1] = v[0].clone().add(toBuildDirection(signDirection, flags));

    // return
    return v;
  }

// TEST METHOD END

  
  @ParameterizedTest
  @CsvFileSource(resources = "/cartesian.csv")
  public void toCartesianTest (String expected, String face) {
    assertEquals(BlockFace.valueOf(expected.toUpperCase().strip()), toCartesian(BlockFace.valueOf(face.toUpperCase().strip())));  
  }


  @ParameterizedTest
  @CsvFileSource(resources = "/build_direction.csv")
  public void toBuildDirectionTest (int i, int j, int k, String signDirection, int flags) {
    //assertEquals(new Vector(i, j, k), toBuildDirection(BlockFace.valueOf(signDirection.toUpperCase().strip()), flags));
    Vector result = toBuildDirection(BlockFace.valueOf(signDirection.toUpperCase().strip()), flags);
    if (result.getBlockX() ==  1) System.out.println("EAST");
    if (result.getBlockZ() ==  1) System.out.println("SOUTH");
    if (result.getBlockX() == -1) System.out.println("WEST");
    if (result.getBlockZ() == -1) System.out.println("NORTH");
  }

  
  @ParameterizedTest
  @CsvFileSource(resources = "/pos.csv")
  public void toPosTest (String expected, String signDirection, int flags) {
    //assertEquals(asVectorArray(expected), toPos(BlockFace.valueOf(signDirection.toUpperCase().strip()), flags));
    Vector[] result = toPos(BlockFace.valueOf(signDirection.toUpperCase().strip()), flags);
    int[] r;
    if (result.length == 1) {
      r = new int[3];
      r[0] = result[0].getBlockX();
      r[1] = result[0].getBlockY();
      r[2] = result[0].getBlockZ();
    } else if (result.length == 2) {
      r = new int[6];
      r[0] = result[0].getBlockX();
      r[1] = result[0].getBlockY();
      r[2] = result[0].getBlockZ();
      r[3] = result[1].getBlockX();
      r[4] = result[1].getBlockY();
      r[5] = result[1].getBlockZ();
    } else {
      r = new int[0];
    }
    for (int k = -2; k <= 2; k++) {
      for (int i = -2; i <= 2; i++) {
        if (r.length == 3) {
          if (r[0] == i && r[2] == k) System.out.print("◼︎");
          else System.out.print("◻︎");
        }
        else if (r.length == 6) {
          if ((r[0] == i && r[2] == k) || (r[3] == i && r[5] == k)) System.out.print("◼︎");
          else System.out.print("◻︎");
        } else System.out.print("◻︎");
      }
      System.out.println();
    }
    System.out.print(r[1]);
    if (r.length == 6) System.out.print(" "); System.out.println(r[4]);
  }

  
  // Helper method
  private Vector[] asVectorArray(String c) {
    // Split coordinate string with delimiter
    String[] coords = c.split(" ");
    if (coords.length % 3 != 0) {
      return null;
    }
    Vector[] returnArray = new Vector[coords.length/3];
    for (int i = 0; i < coords.length; i += 3) {
      returnArray[i/3] = new Vector(Integer.parseInt(coords[i]), Integer.parseInt(coords[i+1]), Integer.parseInt(coords[i+2]));
    }
    return returnArray;
  }

}

