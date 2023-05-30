package mikeshafter.iciwi.api;

import mikeshafter.iciwi.util.IciwiCard;
import org.bukkit.plugin.Plugin;

public interface IciwiPlugin extends Plugin {
	/**
	 * Gets the plugin's fare card class
	 * Iciwi-compatible plugins' cards must state their plugin name in lore[0]
	 * @return Class of the fare card
	 */
	Class<? extends IcCard> getFareCardClass();
}
