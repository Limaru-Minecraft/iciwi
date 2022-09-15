package mikeshafter.iciwi.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.regex.Pattern;


public class MachineUtil {
  public static boolean isDouble(String s) {
    return Pattern.matches(("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|"+"(\\.(\\p{Digit}+)([eE][+-]?(\\p{Digit}+))?)|"+"(("+"(0[xX](\\p{XDigit}+)(\\.)?)|"+"(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+))"+")[pP][+-]?(\\p{Digit}+)))"+"[fFdD]?))"+"[\\x00-\\x20]*"), s);
  }
  
  public static String componentToString(Component c) {
    if (c instanceof TextComponent) return ((TextComponent) c).content();
    else return c.examinableName();
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
}
