package mikeshafter.iciwi.api;

import mikeshafter.iciwi.util.IciwiCard;
import org.bukkit.plugin.Plugin;

public interface IciwiPlugin extends Plugin {
  /**
   * Gets the plugin's fare card class
   *
   * @return Class of the fare card
   */
  Class<? extends IcCard> getFareCardClass();
}
