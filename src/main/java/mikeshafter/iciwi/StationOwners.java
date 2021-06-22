package mikeshafter.iciwi;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;


public class StationOwners {
  
  private static final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private static File file;
  private static FileConfiguration configFile;
  
  public static void setup() {
    file = new File(plugin.getDataFolder(), "owners.yml");
    
    if (!file.exists()) {
      try {
        boolean fileStatus = file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    configFile = YamlConfiguration.loadConfiguration(file);
  }
  
  public static FileConfiguration get() { return configFile; }
  
  public static void save() {
    try {
      configFile.save(file);
    } catch (IOException e) {
      System.out.println("Couldn't save file");
    }
  }
  
  public static void reload() {
    configFile = YamlConfiguration.loadConfiguration(file);
  }
  
  public static String getOwner(String station) {
    return configFile.getString("Operators."+station);
  }
  
  public static void deposit(String operator, double amt) {
    configFile.set("Coffers."+operator, configFile.getDouble("Coffers."+operator)+amt);
    save();
  }
  
  public static double getRailPassPrice(String operator) {
    return configFile.getDouble("RailPassPrices."+operator);
  }
  
}
