package mikeshafter.iciwi.faregate;

import org.junit.Test;
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
   * Gets the relative positions of the fare gate blocks, with direction accounted for
   * The length of the returned Vector[] can be of length 0, 1, or 2.
   * @param signDirection the sign's facing direction
   * @param flags the flags to be applied
   * @param buildDirection the direction to build fare gates in
   * @return The positions of the fare gate blocks.
   */
  public static Vector[] toPos (BlockFace signDirection, int flags, Vector buildDirection) {
    // length 0 if validator
    if ((flags & 1) != 0) return new Vector[0];

    // initialise vector array and default position vector
    Vector[] v = (flags & 8) == 0 ? new Vector[1] : new Vector[2];
    v[0] = signDirection.getDirection().getCrossProduct(new Vector(0, -1, 0));

    // parse default, S, E, R, F flags
    if ((flags &  4) != 0) v[0].add(signDirection.getDirection());
    if ((flags & 32) != 0) v[0].subtract(new Vector(0, 1, 0));
    if ((flags & 16) != 0) v[0] = new Vector(0, -2, 0);
    if ((flags & 64) != 0) v[0] = new Vector(0, 2, 0);
    // parse L flag
    if ((flags & 2) != 0 && (signDirection == BlockFace.SOUTH || signDirection == BlockFace.NORTH)) v[0].multiply(new Vector(-1, 1, 1));
    if ((flags & 2) != 0 && (signDirection == BlockFace.EAST  || signDirection == BlockFace.WEST )) v[0].multiply(new Vector(1, 1, -1));

    // parse D flag
    if ((flags & 8) != 0) v[1] = v[0].add(buildDirection);

    // return
    return v;
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
    else if ((flags & 2) != 0) return signDirection.getDirection().getCrossProduct(new Vector(0, 1, 0));	// Lefty
    else if ((flags & 4) != 0) return signDirection.getDirection();	// Sideways
    else return signDirection.getDirection().getCrossProduct(new Vector(0, -1, 0));	// Normal
  }

// TEST METHOD END


  @Test
  public void toCartesianTest () {

    // Test values
    var testValues = new BlockFace[][] {
      new BlockFace[] {BlockFace.EAST, BlockFace.EAST},
      new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH},
      new BlockFace[] {BlockFace.WEST, BlockFace.WEST},
      new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH},

      new BlockFace[] {BlockFace.EAST, BlockFace.NORTH_EAST},
      new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH_EAST},
      new BlockFace[] {BlockFace.WEST, BlockFace.SOUTH_WEST},
      new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH_WEST},

      new BlockFace[] {BlockFace.EAST, BlockFace.EAST_NORTH_EAST},
      new BlockFace[] {BlockFace.EAST, BlockFace.EAST_SOUTH_EAST},
      new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH_SOUTH_WEST},
      new BlockFace[] {BlockFace.SOUTH, BlockFace.SOUTH_SOUTH_EAST},
      new BlockFace[] {BlockFace.WEST, BlockFace.WEST_NORTH_WEST},
      new BlockFace[] {BlockFace.WEST, BlockFace.WEST_SOUTH_WEST},
      new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH_NORTH_EAST},
      new BlockFace[] {BlockFace.NORTH, BlockFace.NORTH_NORTH_WEST},
    };

    // Assert every value
    for (var value : testValues) {
      var expected = value[0];
      var face = value[1];
      assertEquals(expected, toCartesian(face));
    }
  }


  @Test
  public void toPosTest () {

    // Test values
    var testValues = new Object[][] {
      new Object[] {asVectorArray(1, 1, 1), BlockFace.SOUTH, 3, new Vector(1,1,1)}
    };

    // Assert every value
    for (var value : testValues) {
      var expected = value[0];
      var signDirection = (BlockFace) value[1];
      var flags = (Integer) value[2];
      var buildDirection = (Vector) value[3];
      assertEquals(expected, toPos(signDirection, flags, buildDirection));
    }

  }

  // Helper method
  private Vector[] asVectorArray(int... coordinates) {
    if (coordinates.length % 3 != 0) {
      return null;
    }
    Vector[] returnArray = new Vector[coordinates.length/3];
    for (int i = 0; i < coordinates.length; i += 3) {
      returnArray[i/3] = new Vector(coordinates[i], coordinates[i+1], coordinates[i+2]);
    }
    return returnArray;
  }


  @Test
  public void toBuildDirectionTest () {

    // Test values
    var testValues = new Object[][] {
      new Object[] {new Vector(1,1,1), BlockFace.SOUTH, 3}
    };

    // Assert every value
    for (var value : testValues) {
      var expected = value[0];
      var signDirection = (BlockFace) value[1];
      var flags = (Integer) value[2];
      assertEquals(expected, toBuildDirection(signDirection, flags));
    }
  }
}
