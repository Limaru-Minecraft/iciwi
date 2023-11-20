package mikeshafter.iciwi.config;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class Fares extends CustomConfig {

  public Fares(org.bukkit.plugin.Plugin plugin) { super("fares.yml", plugin); }

  public Fares () { super("fares.yml"); }

  public Set<String> getAllStations() { return this.get().getKeys(false); }

  /**
   * Remove a fare from fares.yml
   * @param from Starting station
   * @param to Ending station
   * @param fareClass Fare class
   */
  public void unsetFare(String from, String to, String fareClass) {
    super.set(from+"."+to+"."+fareClass, null);
    super.save();
  }

  /**
   * Remove all fares between two stations from fares.yml
   * @param from Starting station
   * @param to Ending station
   */
  public void deleteJourney(String from, String to) {
    super.set(from+"."+to, null);
    super.save();
  }

  /**
   * Remove all fares starting from a specified station from fares.yml
   * @param station Station
   */
  public void deleteStation(String station) {
    super.set(station, null);
    super.save();
  }

  /**
   * Set a fare to fares.yml
   * @param from Starting station
   * @param to Ending station
   * @param fareClass Fare class
   * @param price Price to set
   */
  public void setFare(String from, String to, String fareClass, double price) {
    super.set(from+"."+to+"."+fareClass, price);
    super.save();
  }

  /**
   * Get the corresponding fare if the player pays by card
   * @param from Starting station
   * @param to Ending station
   * @param fareClassNoUnderscore Fare class, without the starting underscore
   * @return Fare
   */
  public double getCardFare(String from, String to, String fareClassNoUnderscore) {
    if (fareClassNoUnderscore.indexOf("_") == 0) fareClassNoUnderscore = fareClassNoUnderscore.substring(1);
    final double fare = this.getDouble(from+"."+to+"._"+fareClassNoUnderscore);
    if (fare == 0d) return this.getDouble(from+"."+to+"."+fareClassNoUnderscore);
    else return fare;
  }

  /**
   * Get the corresponding fare if the player pays by cash
   * @param from Starting station
   * @param to Ending station
   * @param fareClass Fare class
   * @return Fare
   */
  public double getFare(String from, String to, String fareClass) {
    return this.getDouble(from+"."+to+"."+fareClass);
  }

  /**
   * Get the corresponding fare using the default train class
   * @param from Starting station
   * @param to Ending station
   * @return Fare
   */
  @Deprecated
  public double getFare(String from, String to) {
    return getFare(from, to, Iciwi.getPlugin(Iciwi.class).getConfig().getString("default-class"));
  }

  /**
   * Get all fares starting from a certain station
   * @param station Starting station
   * @return Fare
   */
  @Deprecated
  public Map<String, Double> getFares(String station) {
    return getFares(station, Iciwi.getPlugin(Iciwi.class).getConfig().getString("default-class"));
  }

  /**
   * Get all fares starting from a certain station, with the specified class
   * @param from Starting station
   * @param fareClass Fare class
   * @return Fare
   */
  public Map<String, Double> getFares(String from, String fareClass) {
    ConfigurationSection section = this.get().getConfigurationSection(from);
    if (section != null) {
      var fareMap = new HashMap<String, Double>();
      section.getKeys(false).forEach(to -> fareMap.put(to, this.getDouble(from+"."+to+"."+fareClass)));
      return fareMap;
    } else return null;
  }

  /**
   * Get all fares starting from a certain station and ending at another station.
   * @param from Starting station
   * @param to Ending station
   * @return Fare
   */
  public TreeMap<String, Double> getFaresFromDestinations(String from, String to) {
    ConfigurationSection section = this.get().getConfigurationSection(from+"."+to);
    if (section != null) {
      var fareMap = new TreeMap<String, Double>();

      section.getKeys(false).forEach(fareClass -> fareMap.put(fareClass, this.getDouble(from+"."+to+"."+fareClass)));
      return fareMap;
    } else return null;
  }

  /**
   * Get all fare classes starting from a certain station and ending at another station.
   * @param from Starting station
   * @param to Ending station
   * @return Fare classes
   */
  public Set<String> getClasses(String from, String to) {
    ConfigurationSection section = this.get().getConfigurationSection(from+"."+to);
    return section == null ? null : section.getKeys(false);
  }

  /**
   * Get all fare classes starting from a certain station and ending at another station.
   * @param from Starting station
   * @param to Ending station
   * @return Fare classes
   */
  public Set<String> getDestinations(String from) {
    ConfigurationSection section = this.get().getConfigurationSection(from);
    return section == null ? null : section.getKeys(false);
  }
}
