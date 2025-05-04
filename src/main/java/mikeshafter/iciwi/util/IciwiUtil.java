package mikeshafter.iciwi.util;

import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.api.IciwiPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class IciwiUtil {

/**
 * Replaces named placeholders with their values
 *
 * @param template Initial template string
 * @param values Keys and values to replace with
 * @return A copy of the input string, without any coloring
 */
public static String format (String template, Map<String, String> values) {
	for (Map.Entry<String, String> entry : values.entrySet()) {
		template = template.replace("{" + entry.getKey() + "}", entry.getValue());
	}
	return template;
}

/**
 * Strips the given message of all color codes
 *
 * @param input String to strip of color
 * @return A copy of the input string, without any coloring
 */
public static String stripColor (final String input) {
	if (input == null) return null;
	return input.replaceAll("(?i)§[0-9A-FK-ORX]", "");
}

/**
 * Converts a Component c to a String. For the other way around, use {@link TextComponent#content(String)}.
 *
 * @param c The component to parse.
 * @return the Component in String format
 */
public static String parseComponent (final Component c) {
	if (c instanceof TextComponent) {return ((TextComponent) c).content();}
	else if (c == null) {return "";}
	else {return c.examinableName();}
}

/**
 * Converts a list of Components componentList to a list of Strings. For the other way around, use {@link IciwiUtil#toComponents(List)}.
 *
 * @param cList The component to parse.
 * @return a list of Strings from the list of Components
 */
public static List<String> parseComponents (List<Component> cList) {
	return new ArrayList<>(cList.stream().map(IciwiUtil::parseComponent).toList());
}

/**
 * Converts a list of Strings to a list of Components. For the other way around, use {@link IciwiUtil#parseComponents(List)}.
 *
 * @param sList The component to parse.
 * @return a list of Components from the list of Strings
 */
public static List<Component> toComponents (List<String> sList) {
	return new ArrayList<>(sList.stream().map(c -> (Component) Component.text(c)).toList());
}

  /* UNUSED
   * Checks if any element in Collection c is present in Collection k.
   * @param <E> Parameter to use.
   * @param c First collection
   * @param k Second collection
   * @return whether any element in Collection c is present in Collection k.

  public static <E> boolean any(Collection<E> c, Collection<E> k) {
    for (E e:k) if (c.contains(e)) return true;
    return false;
  }
  */


/**
 * Makes an item
 *
 * @param material        Material to use
 * @param customModelData the custom model data to use
 * @param displayName     Name to display
 * @param lore            Lore of the item
 * @return The new item
 */
public static ItemStack makeItem (final Material material, final int customModelData, final Component displayName, final Component... lore) {
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
 * @return d if n is 0, (n/d) if n is divisible by d, else (n/d)+1.
 */
public static int ceilDiv (final int n, final int d) {return n == 0 ? d : (n + d - 1) / d;}

/**
 * @param n Number
 * @param r Rounding factor
 * @return Rounds up n by r.
 */
public static int roundUp (final int n, final int r) {return ceilDiv(n, r) * r;}

/**
 * Checks if itemStack has lore.
 *
 * @param itemStack the item to check
 * @return true if itemStack has lore, else false.
 */
public static boolean loreCheck (ItemStack itemStack) {return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().lore() != null;}

/**
 * Checks if itemStack has lore.
 *
 * @param itemStack the item to check
 * @return true if itemStack has lore, else false.
 */
public static boolean loreCheck (ItemStack itemStack, int minSize) {return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().lore() != null && Objects.requireNonNull(itemStack.getItemMeta().lore()).size() >= minSize;}

/* UNUSED
 * Checks if itemStack has a displayName.
 * @param itemStack the item to check
 * @return true if itemStack has displayName, else false.
 */
//public static boolean displayNameCheck(ItemStack itemStack) { return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().displayName() != null; }

/**
 * Punches a ticket
 *
 * @param ticket ticket to punch
 * @param line   line of lore to punch (starting from Line 0)
 */
public static void punchTicket (ItemStack ticket, int line) {
	if (!loreCheck(ticket)) {return;}
	var meta = ticket.getItemMeta();
	List<String> lore = parseComponents(Objects.requireNonNull(meta.lore()));
	if (line < lore.size() && !lore.get(line).contains("•")) {
		lore.set(line, lore.get(line) + " •");
		meta.lore(toComponents(lore));
		ticket.setItemMeta(meta);
	}
}

/**
 * Gets an IcCard object from a compatible item. Iciwi-compatible plugins' cards must state the card's identifier in lore[0]
 *
 * @param itemStack the item to convert
 * @return an IcCard if convertible, null if an exception is reached.
 */
public static @Nullable IcCard IcCardFromItem (ItemStack itemStack) {
	if (!loreCheck(itemStack)) return null;
	String n = parseComponent(Objects.requireNonNull(itemStack.getItemMeta().lore()).get(0));
	try {
		Class<?> icCardClass = IciwiPlugin.getCardType(n);
		return (IcCard) icCardClass.getConstructor(ItemStack.class).newInstance(itemStack);
	}
	catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
		return null;
	}


//	String cardPluginName = parseComponent(Objects.requireNonNull(itemStack.getItemMeta().lore()).getFirst());
//	PluginManager pluginManager = Bukkit.getServer().getPluginManager();
//
//	// Get the plugin
//	Plugin providingPlugin = pluginManager.getPlugin(cardPluginName);
//	// check for plugin compatibility
//	try {
//		if (providingPlugin instanceof IciwiPlugin iciwiPlugin && iciwiPlugin.getFareCardClass() != null) {
//			Class<?> icCardClass = iciwiPlugin.getFareCardClass();
//			// Create new card instance using the provided constructor and the item
//			return (IcCard) icCardClass.getConstructor(ItemStack.class).newInstance(itemStack);
//		}
//		return null;
//	} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
//		return null;
//	}
}
}
