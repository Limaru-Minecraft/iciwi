package mikeshafter.iciwi;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;


public class Lang {
  private final Iciwi plugin;
  private File file = null;
  private FileConfiguration configFile = null;
  
  
  public Lang(Iciwi plugin) {
    this.plugin = plugin;
    saveDefaults();
  }
  
  public void reload() {
    if (this.file == null)
      this.file = new File(this.plugin.getDataFolder(), "lang.yml");
    
    this.configFile = YamlConfiguration.loadConfiguration(this.file);
    
    InputStream defaultStream = this.plugin.getResource("lang.yml");
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
      plugin.getLogger().log(Level.SEVERE, "Could not save lang file: ", e);
    }
  }
  
  public void saveDefaults() {
    if (this.file == null)
      this.file = new File(this.plugin.getDataFolder(), "lang.yml");
    
    if (this.file.exists()) {
      this.plugin.saveResource("lang.yml", false);
    }
  }
  
  
  // Fare gate strings
  // // Signs
  public String ENTRY = this.configFile.getString("entry");
  public String EXIT = this.configFile.getString("exit");
  public String VALIDATOR = this.configFile.getString("validator");
  public String FAREGATE = this.configFile.getString("faregate");
  //--- Messages
  public String REMAINING = this.configFile.getString("remaining");
  public String FARE_EVADE = this.configFile.getString("fareEvade");
  
  
  // Ticket Machine Strings
  //--- Global variables
  public String TICKET_MACHINE = this.configFile.getString("ticketMachine");
  public String TICKETS = this.configFile.getString("tickets");
  public String PLUGIN_NAME = this.configFile.getString("pluginName");
  public String CURRENCY = this.configFile.getString("currency");
  public String SERIAL_NUMBER = this.configFile.getString("serialNumber");
  public String SERIAL_PREFIX = this.configFile.getString("serialPrefix");
  
  
  //--- Messages
  public String NOT_ENOUGH_MONEY = this.configFile.getString("notEnoughMoney");
  
  
}
