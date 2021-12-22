package mikeshafter.iciwi;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class CustomConfig {
  private File file;
  private final YamlConfiguration config;
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final String name;

  public CustomConfig(String name, Plugin plugin) {
    this.name = name;
    file = new File(plugin.getDataFolder(), name);

    if (!file.exists()) {
      Logger logger = plugin.getLogger();
      logger.log(Level.INFO, file.getParentFile().mkdirs() ? "[Iciwi] New config file created" : "[Iciwi] Config file already exists, initialising files...");
      plugin.saveResource(name, false);
    }

    config = new YamlConfiguration();
    try {
      config.load(file);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void save() {
    try {
      config.save(file);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void saveDefaultConfig() {
    if (!file.exists()) {
      plugin.saveResource(name, false);
    }
  }
  
  public File getFile() {
    return file;
  }
  
  public YamlConfiguration get() {
    if (config == null) reload();
    return config;
  }
  
  public String getString(String path) {
    return this.config.getString(path);
  }
  
  public boolean getBoolean(String path) {
    return this.config.getBoolean(path);
  }
  
  public int getInt(String path) {
    return this.config.getInt(path);
  }
  
  public double getDouble(String path) {
    return this.config.getDouble(path);
  }
  
  public long getLong(String path) {
    return this.config.getLong(path);
  }
  
  public void set(String path, Object value) {
    this.config.set(path, value);
  }
  
  public void reload() {
    file = new File(plugin.getDataFolder(), name);
    try {
      config.load(file);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
