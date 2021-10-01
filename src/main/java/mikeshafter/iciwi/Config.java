package mikeshafter.iciwi;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;


public class Config {
  private final File file;
  private final YamlConfiguration config;
  
  public Config(String name, Plugin plugin) {
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
    return config;
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
