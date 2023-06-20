package mikeshafter.iciwi.util;

import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.api.IciwiPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;


public class IciwiUtil {

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
   * @return the Component in String format
   */
  public static String parseComponent(Component c) {
    if (c instanceof TextComponent) return ((TextComponent) c).content();
    else return c.examinableName();
  }

  /**
   * Converts a list of Components componentList to a list of Strings. For the other way around, use {@link IciwiUtil#toComponents(List)}.
   * @param cList The component to parse.
   * @return a list of Strings from the list of Components
   */
  public static List<String> parseComponents(List<Component> cList) {
    List<String> r = new ArrayList<>();
    cList.forEach(c -> r.add(parseComponent(c)));
    return r;
  }

  /**
   * Converts a list of Strings to a list of Components. For the other way around, use {@link IciwiUtil#parseComponents(List)}.
   * @param sList The component to parse.
   * @return a list of Components from the list of Strings
   */
  public static List<Component> toComponents(List<String> sList) {
    List<Component> r = new ArrayList<>();
    sList.forEach(c -> r.add(Component.text(c)));
    return r;
  }

  /**
   * Checks if any element in Collection c is present in Collection k.
   * @param <E> Parameter to use.
   * @param c First collection
   * @param k Second collection
   * @return whether any element in Collection c is present in Collection k.
   */
  public static <E> boolean any(Collection<E> c, Collection<E> k) {
    for (E e:k) if (c.contains(e)) return true;
    return false;
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
   * @param itemStack the item to check
   * @return true if itemStack has lore, else false.
   */
  public static boolean loreCheck(ItemStack itemStack) { return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().lore() != null; }

  /**
   * Checks if itemStack has a displayName.
   * @param itemStack the item to check
   * @return true if itemStack has displayName, else false.
   */
  public static boolean displayNameCheck(ItemStack itemStack) { return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().displayName() != null; }

  /**
   * Punches a ticket
   * @param ticket ticket to punch
   * @param line line of lore to punch (starting from Line 0)
   */
  public static void punchTicket(ItemStack ticket, int line) {
    if (loreCheck(ticket)) {
      var meta = ticket.getItemMeta();
      List<String> lore = parseComponents(Objects.requireNonNull(meta.lore()));
      if (line < lore.size() && !lore.get(line).contains("•")) {
        lore.set(line, lore.get(line) + " •");
        meta.lore(toComponents(lore));
        ticket.setItemMeta(meta);
      }
    }
  }

  /**
   * Gets an IcCard object from a compatible item.
   * @param itemStack the item to convert
   * @return an IcCard if convertible, null if an exception is reached.
   */
  public static @Nullable IcCard IcCardFromItem(ItemStack itemStack) {
    // Iciwi-compatible plugins' cards must state their plugin name in lore[0]
    if (!loreCheck(itemStack)) return null;
    String cardPluginName = parseComponent(Objects.requireNonNull(itemStack.getItemMeta().lore()).get(0));
    PluginManager pluginManager = Bukkit.getServer().getPluginManager();

    // Get the plugin
    Plugin providingPlugin = pluginManager.getPlugin(cardPluginName);
    // check for plugin compatibility
    try {
      if (providingPlugin instanceof IciwiPlugin iciwiPlugin && iciwiPlugin.getFareCardClass() != null) {
        Class<?> icCardClass = iciwiPlugin.getFareCardClass();
        // Create new card instance using the provided constructor and the item
        return (IcCard) icCardClass.getConstructor(ItemStack.class).newInstance(itemStack);

      } return null;
    }
    catch (java.lang.NoSuchMethodException | java.lang.InstantiationException | java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
      return null;
    }
  }
}
