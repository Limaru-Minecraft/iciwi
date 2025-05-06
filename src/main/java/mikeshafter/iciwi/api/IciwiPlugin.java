package mikeshafter.iciwi.api;

import java.util.HashMap;

public class IciwiPlugin {
private static final HashMap<String, Class<? extends IcCard>> cards = new HashMap<>();

/**
 * Register a plugin's fare card class
 *
 * @param identifier Card identifier (can be any string)
 * @param card Card class
 */
public static void registerCard (String identifier, Class<? extends IcCard> card) {
	cards.put(identifier, card);
}

/**
 * Gets the plugin's fare card class
 * Iciwi-compatible plugins' cards must state their identifier (IciwiPlugin#registerCard) in lore[0]
 *
 * @return Class of the fare card
 */
public static Class<? extends IcCard> getCardType(String identifier) {
	return cards.get(identifier);
}
}
