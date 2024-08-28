package mikeshafter.iciwi.api;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.util.FareGateBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import java.util.Objects;

public abstract class ClosableFareGate extends FareGate {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private Object[] closeGateArray;

/**
 * Creates a new fare gate at the sign's location.
 *
 * @param path Path to line 0's text in lang.yml
 */
public ClosableFareGate (String path) {
	super(path);
}

/**
 * Creates a new fare gate with an offset from the sign's location.
 *
 * @param path           Path to line 0's text in lang.yml
 * @param locationOffset Default offset sign location
 */
public ClosableFareGate (String path, Vector locationOffset) {
	super(path, locationOffset);
}

/**
 * Sets the closeGateArray
 *
 * @param closeGateArray the closeGateArray, in the format {Location[], Runnable[]}
 */
public void setCloseGateArray (Object[] closeGateArray) {this.closeGateArray = closeGateArray;}

@EventHandler(priority = EventPriority.LOWEST)
public void onPlayerMove (PlayerMoveEvent event) {
	if (this.closeGateArray == null) return;
	Location ploc = event.getPlayer().getLocation();
	for (Location gloc : ((Location[]) closeGateArray[0])) {
		if (gloc.getBlockX() == ploc.getBlockX() && gloc.getBlockY() == ploc.getBlockY() && gloc.getBlockZ() == ploc.getBlockZ()) {
			for (Runnable r : ((Runnable[]) closeGateArray[1]))
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, r, plugin.getConfig().getLong("close-after-pass"));
		}
	}
}

/**
 Opens a fare gate
 @return the Location and Runnable to close the fare gate
 */
protected Object[] openGate () {
	var sign = this.signInfo.sign();
	// Get the sign's direction and reference block
	BlockFace signFacing = BlockFace.SOUTH;
	Block referenceBlock = sign.getBlock();
	if (sign.getBlockData() instanceof WallSign w) {
		signFacing = w.getFacing();
		referenceBlock = sign.getLocation().clone().add(signFacing.getOppositeFace().getDirection()).getBlock();
	}
	else if (sign.getBlockData() instanceof org.bukkit.block.data.type.Sign s) signFacing = s.getRotation();
	signFacing = toCartesian(signFacing);

	// Get the fare gate flags.
	final short flags = getFlags(this.signInfo.signText()[0]);

	// Get the relative position(s) of the fare gate block(s).
	Vector[] relativePositions = toPos(signFacing, flags);

	// Gate close functions
	Object[] closeGate = { new Location[relativePositions.length], new Runnable[relativePositions.length] };

	// Get the absolute position(s) of the fare gate block(s) (reference block location + relative block vector).
	for (int i = 0; i < 2 && i < relativePositions.length; i++) {
		((Location[]) closeGate[0])[i] = referenceBlock.getLocation().clone().add(relativePositions[i]);
		Block currentBlock = ((Location[]) closeGate[0])[i].getBlock();

		// If openable, open it!
		if (currentBlock.getBlockData() instanceof Openable openable) {
			openable.setOpen(true);
			currentBlock.setBlockData(openable);

			((Runnable[]) closeGate[1])[i] = () -> {
				openable.setOpen(false);
				currentBlock.setBlockData(openable);
			};
		}

		// If powerable, power it!
		else if (currentBlock.getBlockData() instanceof Powerable powerable) {
			powerable.setPowered(true);
			currentBlock.setBlockData(powerable);

			((Runnable[]) closeGate[1])[i] = () -> {
				powerable.setPowered(false);
				currentBlock.setBlockData(powerable);
			};
		}

		// If glass pane, create a FareGateBlock object and open
		else if (currentBlock.getBlockData() instanceof Fence || currentBlock.getBlockData() instanceof GlassPane) {
			BlockFace direction = i == 0 ? toFace(toBuildDirection(signFacing, flags)).getOppositeFace() : toFace(toBuildDirection(signFacing, flags));
			FareGateBlock fgBlock = new FareGateBlock(currentBlock, direction, 100);
			fgBlock.openGate();

			((Runnable[]) closeGate[1])[i] = fgBlock::closeGate;
		}

		// Otherwise, set to air
		else {
			BlockData data = currentBlock.getBlockData();
			currentBlock.setType(Material.AIR);

			((Runnable[]) closeGate[1])[i] = () -> currentBlock.setBlockData(data);
		}

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ((Runnable[]) closeGate[1])[i], plugin.getConfig().getLong("gate-close-delay"));
	}
	return closeGate;
}

private short getFlags (String s) {
	String args;
	if (this.header.length() == s.length()) args = "";
	else args = s.substring(this.header.length());

	short flags = 0;
	flags |= (short) (args.contains("V") ? 1 : 0);    // Validator
	flags |= (short) (args.contains("L") ? 2 : 0);    // Lefty
	flags |= (short) (args.contains("S") ? 4 : 0);    // Sideways
	flags |= (short) (args.contains("D") ? 8 : 0);    // Double
	flags |= (short) (args.contains("R") ? 16 : 0);    // Redstone
	flags |= (short) (args.contains("E") ? 32 : 0);    // Eye-level
	flags |= (short) (args.contains("F") ? 64 : 0);    // Fare gate
	return flags;
}

/**
 Changes vectors into BlockFace directions.
 This method does not work with non-cartesian directions.

 @param vector vector to change
 @return corresponding BlockFace */
private BlockFace toFace (Vector vector) {
	if (Objects.equals(vector, BlockFace.EAST.getDirection())) return BlockFace.EAST;
	if (Objects.equals(vector, BlockFace.SOUTH.getDirection())) return BlockFace.SOUTH;
	if (Objects.equals(vector, BlockFace.WEST.getDirection())) return BlockFace.WEST;
	if (Objects.equals(vector, BlockFace.NORTH.getDirection())) return BlockFace.NORTH;
	else return BlockFace.SELF;
}

/**
 Changes arbitrary sign directions into cartesian (one cardinal direction only) directions.
 This method has a clockwise bias.

 @param face the original direction
 @return the altered direction */
private BlockFace toCartesian (BlockFace face) {
	if (!face.isCartesian()) {
		return switch (face) {
			case NORTH_WEST, NORTH_NORTH_WEST, NORTH_NORTH_EAST -> BlockFace.NORTH;
			case NORTH_EAST, EAST_NORTH_EAST, EAST_SOUTH_EAST -> BlockFace.EAST;
			case SOUTH_EAST, SOUTH_SOUTH_EAST, SOUTH_SOUTH_WEST -> BlockFace.SOUTH;
			case SOUTH_WEST, WEST_SOUTH_WEST, WEST_NORTH_WEST -> BlockFace.WEST;
			default -> face;
		};
	}
	else return face;
}

/**
 Gets the direction to build and animate fare gates in.
 The animation direction should be the opposite direction to the build direction.

 @param signDirection the direction of the sign
 @param flags         the flags to be applied
 @return the direction to build fare gates in. */
private Vector toBuildDirection (BlockFace signDirection, int flags) {
	if ((flags & 1 | flags & 16) != 0)
		return new Vector();     // Validator and Redstone: no animation/double gate allowed
	else if ((flags & 4) != 0) return signDirection.getDirection();     // Sideways
	else if ((flags & 2) != 0) return signDirection.getDirection().getCrossProduct(new Vector(0, -1, 0));    // Lefty
	else return signDirection.getDirection().getCrossProduct(new Vector(0, 1, 0));     // Normal
}

/**
 Gets the relative positions of the fare gate blocks, with direction accounted for
 The length of the returned Vector[] can be of length 0, 1, or 2.

 @param signDirection the sign's facing direction
 @param flags         the flags to be applied
 @return The positions of the fare gate blocks. */
private Vector[] toPos (BlockFace signDirection, int flags) {
	// length 0 if validator
	if ((flags & 1) != 0) return new Vector[0];

	// initialise vector array and default position vector
	Vector[] v = (flags & 8) == 0 ? new Vector[1] : new Vector[2];
	v[0] = signDirection.getDirection().getCrossProduct(new Vector(0, 1, 0));

	// parse default, S, E, R, F flags
	if ((flags & 4) != 0) v[0].add(signDirection.getDirection());
	if ((flags & 32) != 0) v[0].subtract(new Vector(0, 1, 0));
	if ((flags & 16) != 0) v[0] = new Vector(0, -2, 0);
	if ((flags & 64) != 0) v[0] = new Vector(0, 2, 0);
	// parse L flag
	if ((flags & 2) != 0 && (signDirection == BlockFace.SOUTH || signDirection == BlockFace.NORTH)) v[0].multiply(new Vector(-1, 1, 1));
	if ((flags & 2) != 0 && (signDirection == BlockFace.EAST || signDirection == BlockFace.WEST)) v[0].multiply(new Vector(1, 1, -1));

	// parse D flag
	if ((flags & 8) != 0) v[1] = v[0].clone().add(toBuildDirection(signDirection, flags));

	return v;
}

}
