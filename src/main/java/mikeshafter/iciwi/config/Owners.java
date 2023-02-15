package mikeshafter.iciwi.config;

import org.bukkit.configuration.ConfigurationSection;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;


public class Owners extends CustomConfig {
  
  public Owners(org.bukkit.plugin.Plugin plugin) {
    super("owners.yml", plugin);
  }
  
  public Owners() {
    super("owners.yml");
  }
  
  public List<String> getOwners(String station) {
    return super.get().getStringList("Operators."+station);
  }

  /**
   * Gets all registered TOCs
   * @return Set of all TOCs
   */
  public Set<String> getAllCompanies() {
    var aliases = this.get().getConfigurationSection("Aliases");
    return aliases == null ? new HashSet<>() : aliases.getKeys(false);
  }

  /**
   * @param station  Station to set a TOC to
   * @param operators The TOCs
   */
  public void setOwners(String station, List<String> operators) { super.set("Operators."+station, operators); }
  
  /**
   * @param station  Station to add a TOC to
   * @param operator The TOC
   */
  public void addOwner(String station, String operator) {
    List<String> operators = getOwners(station);
    operators.add(operator);
    setOwners(station, operators);
  }
  
  /**
   * @param station  Station to remove a TOC from
   * @param operator The TOC
   */
  public void removeOwner(String station, String operator) {
    List<String> operators = getOwners(station);
    operators.remove(operator);
    setOwners(station, operators);
  }
  
  /**
   * @param operator TOC to search up
   * @param amt      Amount of money to give to the operator
   */
  public void deposit(String operator, double amt) {
    super.set("Coffers."+operator, super.getDouble("Coffers."+operator)+amt);
    super.save();
  }
  
  /**
   * @param operator TOC to search up
   * @param amt      Amount of money to remove from the operator
   *                 This method should be used in conjunction with Owners#deposit for quick transactions between TOCs.
   */
  public void withdraw(String operator, double amt) {
    super.set("Coffers."+operator, super.getDouble("Coffers."+operator)-amt);
    super.save();
  }
  
  /**
   * @param name       Name of the rail pass
   * @param operator   The operator who sells the rail pass
   * @param duration   How long the rail pass lasts (in d:hh:mm:ss)
   * @param price      Price of the rail pass
   * @param percentage Percentage payable by the commuter when taking a train owned by said operator
   *                   Creates a rail pass using a long Unix time as its duration.
   */
  public void setRailPassInfo(String name, String operator, long duration, double price, double percentage) {
    super.set("RailPasses."+name+"operator", operator);
    super.set("RailPasses."+name+"duration", timeToString(duration));
    super.set("RailPasses."+name+"price", price);
    super.set("RailPasses."+name+"percentage", percentage);
    super.save();
  }
  
  /**
   * @param name       Name of the rail pass
   * @param operator   The operator who sells the rail pass
   * @param duration   How long the rail pass lasts (in milliseconds)
   * @param price      Price of the rail pass
   * @param percentage Percentage payable by the commuter
   *                   Creates a rail pass using a timestring as its duration.
   */
  public void setRailPassInfo(String name, String operator, String duration, double price, double percentage) {
    super.set("RailPasses."+name+"operator", operator);
    super.set("RailPasses."+name+"duration", duration);
    super.set("RailPasses."+name+"price", price);
    super.set("RailPasses."+name+"percentage", percentage);
    super.save();
  }
  
  private String timeToString(long time) {
    DateFormat df = new SimpleDateFormat("dd:hh:mm:ss");
    return df.format(Date.from(Instant.ofEpochMilli(time)));
  }
  
  /**
   * @param name Name of the rail pass
   * @return How long the rail pass lasts
   * This method should only be used to transfer data to the SQL. It should not be used to query the information of rail passes directly.
   */
  public long getRailPassDuration(String name) {
    return timetoLong(super.getString("RailPassPrices."+name+"duration"));
  }
  
  private long timetoLong(String time) {
    try {
      return new SimpleDateFormat("dd:hh:mm:ss").parse(time).getTime();
    } catch (ParseException e) {
      return 0L;
    }
  }
  
  /**
   * @param name Name of the rail pass
   * @return Percentage payable by the commuter
   * This method should only be used to transfer data to the SQL. It should not be used to query the information of rail passes directly.
   */
  public double getRailPassPercentage(String name) {
    return super.getDouble("RailPassPrices."+name+"percentage");
  }
  
  /**
   * @param name Name of the rail pass
   * @return Price of the rail pass
   *             This method should only be used to transfer data to the SQL. It should not be used to query the information of rail passes directly.
   */
  public double getRailPassPrice(String name) {
    return super.getDouble("RailPassPrices."+name+"price");
  }
  
  /**
   * @param name Name of the rail pass
   * @return The operator who sells the rail pass
   *             This method should only be used to transfer data to the SQL. It should not be used to query the information of rail passes directly.
   */
  public String getRailPassOperator(String name) {
    return super.getString("RailPassPrices."+name+"operator");
  }
  
  /**
   * @param operator TOC to search up
   * @return Names of rail passes sold by the operator
   */
  public Set<String> getRailPassNames(String operator) {
    // Loop through all names in RailPasses
    ConfigurationSection railPassPrices = super.get().getConfigurationSection("RailPassPrices");
    // Check if the name has the given operator
    // If it has, add it to returnSet
    HashSet<String> h = new HashSet<>();
    assert railPassPrices != null;
    for (String pass : railPassPrices.getKeys(false))
      if (Objects.equals(railPassPrices.getString(pass+".operator"), operator))
        h.add(pass);
    return h;
  }

  /**
   * Gets all the rail passes regardless of operator.
   * @return Set of all rail passes
   */
  public Set<String> getAllRailPasses() {
    var railPasses = this.get().getConfigurationSection("RailPasses");
    return railPasses == null ? new HashSet<>() : railPasses.getKeys(false);
  }
  
  /**
   * @param operator TOC to search up
   * @return Total amount of money earned by the operator since its last /coffers empty
   */
  public double getCoffers(String operator) {
    return super.getDouble("Coffers."+operator);
  }
  
  /**
   * @param operator TOC to search up
   * @param amt      Amount of money to set the operator's coffers to
   */
  public void setCoffers(String operator, double amt) {
    super.set("Coffers."+operator, amt);
    save();
  }

  /**
   * @param player Name of the player
   * @return List of companies the player owns
  */
  public List<String> getOwnedCompanies(String player) {
    Map<String, Object> operatorMap = super.getConfigurationSection("Aliases").getValues(false);
    return operatorMap.entrySet().stream().filter(entry -> player.equals(entry.getValue())).map(Map.Entry::getKey).toList();
  }

  /**
  * @param player Name of the player
  * @param operator TOC to search up
  * @return whether the player owns the TOC
  */
  public boolean getOwnership(String player, String operator) {
    return player.equalsIgnoreCase(super.getString("Aliases."+operator));
  }
  
}