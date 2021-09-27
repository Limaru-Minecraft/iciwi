package mikeshafter.iciwi;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;


public class TicketRecords {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private File file;
  private FileConfiguration config;
  
  public void save() {
    if (config == null || file == null) {
      return;
    }
    try {
      get().save(file);
    } catch (IOException ex) {
      plugin.getLogger().log(Level.SEVERE, "Could not save config to "+file, ex);
    }
  }
  
  public FileConfiguration get() {
    return config;
  }
  
  public String getString(String path) {
    return config.getString(path);
  }
  
  public boolean getBoolean(String path) {
    return config.getBoolean(path);
  }
  
  public long getLong(String path) {
    return config.getLong(path);
  }
  
  public void set(String path, Object value) {
    config.set(path, value);
  }
  
  public void reload() {
    if (file == null) {
      file = new File(plugin.getDataFolder(), "lang.yml");
    }
    config = YamlConfiguration.loadConfiguration(file);
    
    // Look for defaults in the jar
    Reader defaultLang = new InputStreamReader(Objects.requireNonNull(plugin.getResource("lang.yml")), StandardCharsets.UTF_8);
    YamlConfiguration def = YamlConfiguration.loadConfiguration(defaultLang);
    config.setDefaults(def);
  }
}
