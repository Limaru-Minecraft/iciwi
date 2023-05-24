package mikeshafter.iciwi.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ActionBlock implements Listener {

  private @NotNull Player player;
  private @Nullable ItemStack item;
  private @Nullable Block block;
  private @NotNull Location location;
  private @NotNull BlockState state;
  private @NotNull BlockData data;

  public abstract boolean checkHeldItem();

  public abstract boolean doAction();

  public abstract void open();


  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    this.player = event.getPlayer();
    this.item = event.getItem();
    this.block = event.getClickedBlock();

    // Helper fields
    if (this.block != null){
      this.state = this.block.getState();
      this.data = this.block.getBlockData();
      this.location = this.block.getLocation();
    }

    // Standard procedure
    if (checkHeldItem() && doAction()) {
      open();
    }
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {}

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public ItemStack getItem() {
    return item;
  }

  public void setItem(ItemStack item) {
    this.item = item;
  }

  public Block getBlock() {
    return block;
  }

  public void setBlock(Block block) {
    this.block = block;
  }

  public BlockState getState() {
    return state;
  }

  public void setState(BlockState state) {
    this.state = state;
  }

  public BlockData getData() {
    return data;
  }

  public void setData(BlockData data) {
    this.data = data;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }
}
