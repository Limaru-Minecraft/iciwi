package mikeshafter.iciwi;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class Config {
  private File file;
  private final YamlConfiguration config;
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final String name;

  public Config(String name, Plugin plugin) {
    this.name = name;
    file = new File(plugin.getDataFolder(), name);

    if (!file.exists()) {
      file.getParentFile().mkdir();
      plugin.saveResource(name, true);
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

  public File getFile() {
    return file;
  }

  public YamlConfiguration get() {
    if (config == null) reload();
    return config;
  }

  public String getString(String path) {
    this.config.getString(path);
  }

  public String getInt(String path) {
    this.config.getInt(path);
  }

  public String getDouble(String path) {
    this.config.getDouble(path);
  }

  public String getLong(String path) {
    this.config.getLong(path);
  }

  public void set(String path, Object value) {
    this.config.set(path, value)
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
