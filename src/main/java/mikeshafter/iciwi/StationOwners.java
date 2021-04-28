package mikeshafter.iciwi;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;


public class StationOwners {
  File ownersFile;
  FileConfiguration owners;
  
  public StationOwners() {
    Plugin plugin = Iciwi.getPlugin(Iciwi.class);
    ownersFile = new File(plugin.getDataFolder(), "stationowners.yml");
    owners = YamlConfiguration.loadConfiguration(ownersFile);
    saveConfig();
  }
  
  public void saveConfig() {
    try {
      owners.save(ownersFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public String getStationOwner(String station) {
    return owners.getString(station);
  }
  
  public void setStationOwner(String station, String owner) {
    owners.set(station, owner);
    saveConfig();
  }
}