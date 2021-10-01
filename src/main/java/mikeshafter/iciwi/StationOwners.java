package mikeshafter.iciwi;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;


public class StationOwners {
  
  private final Iciwi plugin;
  private File file = null;
  private FileConfiguration configFile = null;
  
  
  public StationOwners(Iciwi plugin) {
    this.plugin = plugin;
    saveDefaults();
  }
  
  public void reload() {
    if (this.file == null)
      this.file = new File(this.plugin.getDataFolder(), "owners.yml");
    
    this.configFile = YamlConfiguration.loadConfiguration(this.file);
    
    InputStream defaultStream = this.plugin.getResource("owners.yml");
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
      plugin.getLogger().log(Level.SEVERE, "Could not save owners file: ", e);
    }
  }
  
  public void saveDefaults() {
    if (this.file == null)
      this.file = new File(this.plugin.getDataFolder(), "owners.yml");
    
    if (this.file.exists()) {
      this.plugin.saveResource("owners.yml", false);
    }
  }
  
  public String getOwner(String station) {
    return get().getString("Operators."+station);
  }
  
  public void deposit(String operator, double amt) {
    get().set("Coffers."+operator, configFile.getDouble("Coffers."+operator)+amt);
    save();
  }
  
  public double getRailPassPrice(String operator, int days) {
    return get().getDouble("RailPassPrices."+operator+"."+days);
  }
  
}
