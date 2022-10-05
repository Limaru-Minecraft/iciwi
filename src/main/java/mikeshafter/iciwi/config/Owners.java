package mikeshafter.iciwi.config;

import mikeshafter.iciwi.util.RailPassInfo;

import java.util.HashSet;
import java.util.Set;

public class Owners extends CustomConfig {
  
  public Owners(org.bukkit.plugin.Plugin plugin) {
    super("owners.yml", plugin);
  }

  public Owners() {
    super("owners.yml");
  }
  
  public String getOwner(String station) {
    return super.getString("Operators."+station);
  }
  
  public void setOwner(String station, String operator) {
    super.set("Operators."+station, operator);
  }
  
  public void deposit(String operator, double amt) {
    super.set("Coffers."+operator, super.getDouble("Coffers."+operator)+amt);
    super.save();
  }
  
  public void setRailPassInfo(String name, RailPassInfo info) {
    super.set("RailPassPrices."+name, info);
    super.save();
  }
  
  public long getRailPassDuration(String name) {
    return getRailPassInfo(name).duration;
  }
  
  public RailPassInfo getRailPassInfo(String name) {
    return (RailPassInfo) super.get("RailPassPrices."+name);
  }
  
  public double getRailPassPercentage(String name) {
    return getRailPassInfo(name).percentage;
  }
  
  public double getRailPassPrice(String name) {
    return getRailPassInfo(name).price;
  }
  
  public String getRailPassOperator(String name) {
    return getRailPassInfo(name).operator;
  }
  
  @Deprecated
  public Set<String> getRailPassNames(String operator) {
    Set<String> returnSet = new HashSet<String>();
    // Loop through all names in RailPasses
    // Check if the name has the given operator
    // If it has, add it to returnSet
    return returnSet;
  }
  
  public double getCoffers(String operator) {
    return super.getDouble("Coffers."+operator);
  }
  
  public void setCoffers(String operator, double amt) {
    super.set("Coffers."+operator, amt);
    save();
  }
  
}
