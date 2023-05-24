package mikeshafter.iciwi.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;


public class MachineUtil {

  /**
   * Checks if a string can be parsed as a double.
   * @param s The string to check.
   * @return Whether the string is parseable as a double.
   */
  public static boolean isDouble(String s) {
    return Pattern.matches(("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((((\\d+)(\\.)?((\\d+)?)([eE][+-]?(\\d+))?)|"+"(\\.(\\d+)([eE][+-]?(\\d+))?)|"+"(("+"(0[xX](\\p{XDigit}+)(\\.)?)|"+"(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+))"+")[pP][+-]?(\\d+)))"+"[fFdD]?))"+"[\\x00-\\x20]*"), s);
  }
  
  /**
   * Converts a Component c to a String. For the other way around, use {@link TextComponent#content(String)}.
   * @param c The component to parse.
   * @return
   */
  public static String parseComponent(Component c) {
    if (c instanceof TextComponent) return ((TextComponent) c).content();
    else return c.examinableName();
  }

  public static List<String> parseComponents(List<Component> componentList) {
    List<String> r = new ArrayList<>();
    componentList.forEach(c -> r.add(parseComponent(c)));
    return r;
  }
  
  /**
   * Checks if any element in Collection c is present in Collection k.
   * @param <E> Parameter to use.
   * @param c First collection
   * @param k Second collection
   * @return
   */
  public static <E> boolean any(Collection<E> c, Collection<E> k) {
    for (E e:k) if (c.contains(e)) return true;
    return false;
  }

  /**
   * Makes an item
   * 
   * @deprecated Use {@link MachineUtil#makeItem(Material, int, Component, Component...)} instead
   * @param material    Material to use
   * @param displayName Name to display
   * @param lore        Lore of the item
   * @return The new item
   */
@Deprecated public static ItemStack makeItem(final Material material, final Component displayName, final Component... lore) {
  ItemStack item = new ItemStack(material);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.displayName(displayName);
    itemMeta.lore(Arrays.asList(lore));
    item.setItemMeta(itemMeta);
    return item;
  }
  
  /**
   * Makes an item
   * @param material        Material to use
   * @param customModelData the custom model data to use
   * @param displayName     Name to display
   * @param lore            Lore of the item
   * @return The new item
   */
  public static ItemStack makeItem(final Material material, final int customModelData, final Component displayName, final Component... lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.displayName(displayName);
    itemMeta.setCustomModelData(customModelData);
    itemMeta.lore(Arrays.asList(lore));
    item.setItemMeta(itemMeta);
    return item;
  }
  
  /**
   * @param n Numerator
   * @param d Denominator
   * @return (n/d) if n is divisible by d, else (n/d)+1. 
   */
  public static int ceilDiv(final int n, final int d) { return (n + d - 1) / d; }
  
  /**
   * @param n Number
   * @param r Rounding factor
   * @return Rounds up n by r.
   */
  public static int roundUp(final int n, final int r) { return ceilDiv(n, r) * r; }
  
  /**
   * Checks if itemStack has lore.
   * @param itemStack
   * @return true if itemStack has lore, else false.
   */
  public static boolean loreCheck(ItemStack itemStack) { return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().lore() != null; }

  /**
   * Checks if itemStack has a displayName.
   * @param itemStack
   * @return true if itemStack has displayName, else false.
   */
  public static boolean displayNameCheck(ItemStack itemStack) { return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().displayName() != null; }
}
