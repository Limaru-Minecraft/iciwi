package mikeshafter.iciwi.util;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;


public class FareGateBlock {
private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
private final Block block;
private final BlockData blockData;
private final Location blockLoc;
private final BlockFace openDirection;
private final long openTime;
private BlockDisplay blockDisplay;
private BukkitTask task = null;
private boolean gateClosing = false;
private int remainCount = 0;

public FareGateBlock (Block block, BlockFace openDirection, long openTime) {
	this.openTime = openTime;
	this.block = block;
	this.blockLoc = block.getLocation();
	this.openDirection = openDirection;
	this.blockData = block.getBlockData();
}

private BlockFace getOpenDirection () {return this.openDirection;}

private long getOpenTime () {return this.openTime;}

private BlockDisplay getBlockDisplay () {return this.blockDisplay;}

public Block getBlock () {return this.block;}

private void spawnBlockDisplay () {
	// Spawn BlockDisplay at the block's center location (centered on block)
	Location displayLoc = this.blockLoc.clone().add(0.5d, 0.5d, 0.5d);
	this.blockDisplay = (BlockDisplay) this.block.getWorld().spawnEntity(displayLoc, EntityType.BLOCK_DISPLAY);
	
	// Configure the BlockDisplay
	this.blockDisplay.setBlock(this.blockData);
	this.blockDisplay.setGravity(false);
	this.blockDisplay.setInvulnerable(true);
	this.blockDisplay.setPersistent(false);
	
	// Center the display properly on the block using transformation
	org.bukkit.util.Transformation initialTransform = new org.bukkit.util.Transformation(
		new Vector3f(-0.5f, -0.5f, -0.5f),
		new org.joml.Quaternionf(),
		new Vector3f(1f, 1f, 1f),
		new org.joml.Quaternionf()
	);
	this.blockDisplay.setTransformation(initialTransform);
	
	// Optional: Set brightness to match the original block
	// this.blockDisplay.setBrightness(new Display.Brightness(15, 15));
}

private void killBlockDisplay () {
	if (this.blockDisplay != null && !this.blockDisplay.isDead()) {
		this.blockDisplay.remove();
	}
	// Clean up any lingering BlockDisplay entities near the location
	this.block.getWorld().getNearbyEntities(this.blockLoc, 1.5, 1.5, 1.5, 
		(entity) -> entity.getType() == EntityType.BLOCK_DISPLAY)
		.forEach(Entity::remove);
}

private void onGateClose () {
	int ticksToClose = plugin.getConfig().getInt("ticks-to-close");
	// Use smaller steps for smoother movement (move every tick instead of large jumps)
	Vector moveDirection = this.getOpenDirection().getOppositeFace().getDirection().multiply(1d / ticksToClose);
	this.moveBlockDisplaySmooth(this.getBlockDisplay(), moveDirection, ticksToClose - this.remainCount, false);
	
	Bukkit.getScheduler().runTaskLater(plugin, () -> {
		this.killBlockDisplay();
		this.getBlock().setBlockData(this.blockData);
	}, (ticksToClose + 5 - this.remainCount));
}

private void moveBlockDisplaySmooth (BlockDisplay display, Vector direction, int count, boolean canCancel) {
	if (canCancel && this.task.isCancelled()) {
		this.remainCount = count;
	}
	else if (count > 0 && display != null && !display.isDead()) {
		// Use transformation for smoother movement instead of teleporting
		org.bukkit.util.Transformation currentTransform = display.getTransformation();
		final Transformation newTransform = getTransformation(direction, currentTransform);

		display.setTransformation(newTransform);
		display.setInterpolationDelay(0);
		display.setInterpolationDuration(1);

		Bukkit.getScheduler().runTaskLater(plugin, () ->
			this.moveBlockDisplaySmooth(display, direction, count - 1, canCancel), 1L);
	}
}

private static @NotNull Transformation getTransformation (Vector direction, Transformation currentTransform) {
	Vector3f currentTranslation = currentTransform.getTranslation();

	Vector3f newTranslation = new Vector3f(
		currentTranslation.x + (float) direction.getX(),
		currentTranslation.y + (float) direction.getY(),
		currentTranslation.z + (float) direction.getZ()
	);

	Transformation newTransform = new Transformation(
		newTranslation,
		currentTransform.getLeftRotation(),
		currentTransform.getScale(),
		currentTransform.getRightRotation()
	);
	return newTransform;
}

public void openGate () {
	int ticksToOpen = plugin.getConfig().getInt("ticks-to-open");
	this.spawnBlockDisplay();
	
	// Remove the physical block after a short delay
	Bukkit.getScheduler().runTaskLater(plugin, () -> 
		this.getBlock().setType(Material.AIR), 5L);
	
	// Schedule gate closing
	this.task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
		this.gateClosing = true;
		this.onGateClose();
	}, (this.getOpenTime() + ticksToOpen + 5));
	
	// Move the BlockDisplay in the opening direction with smooth movement
	Vector moveDirection = this.getOpenDirection().getDirection().multiply(1d / ticksToOpen);
	this.moveBlockDisplaySmooth(this.blockDisplay, moveDirection, ticksToOpen, true);
}

public void closeGate () {closeGate(false);}

public void closeGate (boolean force) {
	if (!force) {
		if (this.task != null && !this.gateClosing && !this.task.isCancelled()) {
			this.task.cancel();
			this.onGateClose();
		}
	}
	else {
		this.killBlockDisplay();
		this.getBlock().setBlockData(this.blockData);
	}
}
}