package mikeshafter.iciwi;

import org.bukkit.plugin.Plugin;

import java.io.File;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class Records extends Config {
  private File file;
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final String name;
  
  public Records(Plugin plugin) {
    super("records.yml", plugin);
    this.name = "records.yml";
  }
  
}
