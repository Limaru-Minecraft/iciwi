package mikeshafter.iciwi;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;


public class TicketRecords {
  private final Iciwi plugin;
  private File file = null;
  private FileConfiguration configFile = null;
  
  
  public TicketRecords(Iciwi plugin) {
    this.plugin = plugin;
    saveDefaults();
  }
  
  public void reload() {
    if (this.file == null)
      this.file = new File(this.plugin.getDataFolder(), "records.yml");
    
    this.configFile = YamlConfiguration.loadConfiguration(this.file);
    
    InputStream defaultStream = this.plugin.getResource("records.yml");
    if (defaultStream != null) {
      YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
      this.configFile.setDefaults(defaultConfig);
    }
  }
  
  public FileConfiguration get() {
    if (this.configFile == null) reload();
    return this.configFile;
  }
  
  public void save() {
    if (this.configFile == null || this.file == null) return;
    try {
      this.get().save(this.file);
    } catch (IOException e) {
      plugin.getLogger().log(Level.SEVERE, "Could not save records file: ", e);
    }
  }
  
  public void saveDefaults() {
    if (this.file == null)
      this.file = new File(this.plugin.getDataFolder(), "records.yml");
    
    if (this.file.exists()) {
      this.plugin.saveResource("records.yml", false);
    }
  }
  
  public String getString(String path) {
    return this.configFile.getString(path);
  }
  
  public boolean getBoolean(String path) {
    return this.configFile.getBoolean(path);
  }
  
  public long getLong(String path) {
    return this.configFile.getLong(path);
  }
  
  public void set(String path, Object value) {
    this.configFile.set(path, value);
  }
  
}
