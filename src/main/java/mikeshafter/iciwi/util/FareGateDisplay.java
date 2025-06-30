package mikeshafter.iciwi.util;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;


public class FareGateDisplay {
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

public FareGateDisplay (Block block, BlockFace openDirection, long openTime) {
	this.openTime = openTime;
	this.block = block;
	this.blockLoc = block.getLocation();
	this.openDirection = openDirection;
	this.blockData = block.getBlockData();
}

private BlockFace getOpenDirection () {return this.openDirection;}

private long getOpenTime () {return this.openTime;}

private Entity getBlockDisplay () {return this.blockDisplay;}

public Block getBlock () {return this.block;}

private void spawnBlockDisplay () {
	this.blockDisplay = this.block.getWorld().spawn(this.blockLoc.add(0.5d, -1.9805d, 0.5d), BlockDisplay.class);
	this.blockDisplay.setBlock(this.blockData);

	this.resetCountdown();
}

private void killBlockDisplay () {
	this.blockDisplay.remove();
}

private void onGateClose () {
	int ticksToClose = plugin.getConfig().getInt("ticks-to-close");
	this.moveBlockDisplay(this.getBlockDisplay(), this.getOpenDirection().getOppositeFace().getDirection().multiply(1d / ticksToClose), ticksToClose - this.remainCount, false);
	Bukkit.getScheduler().runTaskLater(plugin, () -> {
		this.killBlockDisplay();
		this.getBlock().setBlockData(this.blockData);
	}, (ticksToClose + 5 - this.remainCount));
}

private void moveBlockDisplay (Entity entity, Vector direction, int count, boolean canCancel) {
	if (canCancel && this.task.isCancelled()) this.remainCount = count;
	else {
		if (count > 0 && !entity.isDead()) {
			Location newLoc = entity.getLocation().add(direction);
			Entity passenger = !entity.getPassengers().isEmpty() ? entity.getPassengers().get(0) : null;
			if (passenger == null) return;

			entity.removePassenger(passenger);
			entity.teleport(newLoc);
			entity.addPassenger(passenger);
			Bukkit.getScheduler().runTaskLater(plugin, () -> this.moveBlockDisplay(entity, direction, count - 1, canCancel), 1L);
		}

	}
}

public void openGate () {
	int ticksToOpen = plugin.getConfig().getInt("ticks-to-open");
	this.spawnBlockDisplay();
	Bukkit.getScheduler().runTaskLater(plugin, () -> this.getBlock().setType(Material.AIR), 5L);
	this.task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
		this.gateClosing = true;
		this.onGateClose();
	}, (this.getOpenTime() + ticksToOpen + 5));
	this.moveBlockDisplay(this.blockDisplay, this.getOpenDirection().getDirection().multiply(1d / ticksToOpen), ticksToOpen, true);
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

private void resetCountdown () {
	if (!this.blockDisplay.isDead()) this.blockDisplay.setTicksLived(1);
	Bukkit.getScheduler().runTaskLater(plugin, this::resetCountdown, 20L);
}
}
