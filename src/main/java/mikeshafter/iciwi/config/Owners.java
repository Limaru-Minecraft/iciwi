package mikeshafter.iciwi.config;

import org.bukkit.configuration.ConfigurationSection;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class Owners extends CustomConfig {
  
  public Owners(org.bukkit.plugin.Plugin plugin) {
    super("owners.yml", plugin);
  }
  
  public Owners() {
    super("owners.yml");
  }
  
  /**
   * @param station Station to get the operator of
   * @return The TOC
   */
  public String getOwner(String station) {
    return super.getString("Operators."+station);
  }
  
  /**
   * @param station  Station to set a TOC to
   * @param operator The TOC
   */
  public void setOwner(String station, String operator) {
    super.set("Operators."+station, operator);
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
   */
  public void setRailPassInfo(String name, String operator, long duration, double price, double percentage) {
    super.set("RailPassPrices."+name+"operator", operator);
    super.set("RailPassPrices."+name+"duration", duration);
    super.set("RailPassPrices."+name+"price", price);
    super.set("RailPassPrices."+name+"percentage", percentage);
    super.save();
  }
  
  /**
   * @param name       Name of the rail pass
   * @param operator   The operator who sells the rail pass
   * @param duration   How long the rail pass lasts (in milliseconds)
   * @param price      Price of the rail pass
   * @param percentage Percentage payable by the commuter
   */
  public void setRailPassInfo(String name, String operator, String duration, double price, double percentage) {
    super.set("RailPassPrices."+name+"operator", operator);
    super.set("RailPassPrices."+name+"duration", duration);
    super.set("RailPassPrices."+name+"price", price);
    super.set("RailPassPrices."+name+"percentage", percentage);
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
   * This method should only be used to transfer data to the SQL. It should not be used to query the information of rail passes directly.
   */
  public double getRailPassPrice(String name) {
    return super.getDouble("RailPassPrices."+name+"price");
  }
  
  /**
   * @param name Name of the rail pass
   * @return The operator who sells the rail pass
   * This method should only be used to transfer data to the SQL. It should not be used to query the information of rail passes directly.
   */
  public String getRailPassOperator(String name) {
    return super.getString("RailPassPrices."+name+"operator");
  }
  
  /**
   * @param operator TOC to search up
   * @return Names of rail passes sold by the operator
   * @deprecated This method is deprecated as future queries into rail passes should be made into the SQL database instead.
   */
  @Deprecated
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
    return operatorMap.entrySet().stream().filter(entry -> player.equals((String) entry.getValue())).map(Map.Entry::getKey).toList();
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
