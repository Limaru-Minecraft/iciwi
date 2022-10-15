package mikeshafter.iciwi.config;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class Fares extends CustomConfig {
  
  public Fares(org.bukkit.plugin.Plugin plugin) {
    super("fares.yml", plugin);
  }

  public Fares () {
    super("fares.yml");
  }
  
  public Set<String> getAllStations() {
    return this.get().getKeys(false);
  }
  
  public double getFare(String from, String to) {
    return getFare(from, to, Iciwi.getPlugin(Iciwi.class).getConfig().getString("default-class"));
  }
  
  public double getFare(String from, String to, String fareClass) {
    return this.getDouble(from+"."+to+"."+fareClass);
  }
  
  public Map<String, Double> getFares(String station) {
    return getFares(station, Iciwi.getPlugin(Iciwi.class).getConfig().getString("default-class"));
  }
  
  public Map<String, Double> getFares(String from, String fareClass) {
    ConfigurationSection section = this.get().getConfigurationSection(from);
    if (section != null) {
      var fareMap = new HashMap<String, Double>();
      section.getKeys(false).forEach(to -> fareMap.put(to, this.getDouble(from+"."+to+"."+fareClass)));
      return fareMap;
    } else return null;
  }
  
  public TreeMap<String, Double> getFaresFromDestinations(String from, String to) {
    ConfigurationSection section = this.get().getConfigurationSection(from+"."+to);
    if (section != null) {
      var fareMap = new TreeMap<String, Double>();
      section.getKeys(false).forEach(fareClass -> fareMap.put(fareClass, this.getDouble(from+"."+to+"."+fareClass)));
      return fareMap;
    } else return null;
  }

  public Set<String> getClasses(String from, String to) {
        ConfigurationSection section = this.get().getConfigurationSection(from+"."+to);
return section.getKeys(false);
  }
}
