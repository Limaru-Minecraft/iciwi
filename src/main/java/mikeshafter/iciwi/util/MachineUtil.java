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
  public static boolean isDouble(String s) {
    return Pattern.matches(("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|"+"(\\.(\\p{Digit}+)([eE][+-]?(\\p{Digit}+))?)|"+"(("+"(0[xX](\\p{XDigit}+)(\\.)?)|"+"(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+))"+")[pP][+-]?(\\p{Digit}+)))"+"[fFdD]?))"+"[\\x00-\\x20]*"), s);
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
}
