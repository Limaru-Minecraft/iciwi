package mikeshafter.iciwi.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;


public class IciwiUtil {
  public static boolean isDouble(String s) {
    return Pattern.matches(("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((((\\d+)(\\.)?((\\d+)?)([eE][+-]?(\\d+))?)|"+"(\\.(\\d+)([eE][+-]?(\\d+))?)|"+"(("+"(0[xX](\\p{XDigit}+)(\\.)?)|"+"(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+))"+")[pP][+-]?(\\d+)))"+"[fFdD]?))"+"[\\x00-\\x20]*"), s);
  }
  
  public static String parseComponent(Component c) {
    if (c instanceof TextComponent) return ((TextComponent) c).content();
    else return c.examinableName();
  }

  public static List<String> parseComponents(List<Component> componentList) {
    List<String> r = new ArrayList<>();
    componentList.forEach(c -> r.add(parseComponent(c)));
    return r;
  }
  
  public static <E> boolean any(Collection<E> c, Collection<E> k) {
    for (E e:k) if (c.contains(e)) return true;
    return false;
  }
  
  public static ItemStack makeItem(final Material material, final Component displayName, final Component... lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.displayName(displayName);
    itemMeta.lore(Arrays.asList(lore));
    item.setItemMeta(itemMeta);
    return item;
  }
  
  public static int ceilDiv(final int n, final int d) {
    return (n + d - 1) / d;
  }
  
  public static int roundUp(final int n, final int r) {
    return ceilDiv(n, r) * r;
  }
  
  public static boolean loreCheck(ItemStack itemStack) { return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().lore() != null; }

  public static boolean displayNameCheck(ItemStack itemStack) { return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().displayName() != null; }

  public static CheckedEvent qualifyEvent (PlayerInteractEvent event) {
    return new CheckedEvent(event);
  }

  public static class CheckedEvent {
    @Nullable private final ItemStack item;
    private final Player player;
    private final boolean cancel;
    @Nullable private final BlockState state;
    @Nullable private final BlockData data;
    @Nullable private final Location location;

    public CheckedEvent (PlayerInteractEvent event) {
      this.item = event.getItem();
      this.player = event.getPlayer();

      @Nullable Block block = event.getClickedBlock();
      this.cancel = block == null || this.item == null || event.getAction() != Action.RIGHT_CLICK_BLOCK;
      if (!this.cancel) {
        this.state = block.getState();
        this.data = block.getBlockData();
        this.location = block.getLocation();
      } else {
        this.state = null;
        this.data = null;
        this.location = null;
      }
    }

    public Player getPlayer () {
      return this.player;
    }

    public ItemStack getItem () {
      if (cancel) throw new NoSuchElementException();
      return item;
    }

    public boolean isCancel () {
      return cancel;
    }

    public BlockState getState () {
      if (cancel) throw new NoSuchElementException();
      return state;
    }

    public BlockData getData () {
      if (cancel) throw new NoSuchElementException();
      return data;
    }

    public Location getLocation () {
      if (cancel) throw new NoSuchElementException();
      return location;
    }

  }
}
