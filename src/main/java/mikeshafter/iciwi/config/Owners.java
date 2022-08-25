package mikeshafter.iciwi.config;

import java.util.Objects;
import java.util.Set;

public class Owners extends CustomConfig {
  
  public Owners(org.bukkit.plugin.Plugin plugin) {
    super("owners.yml", plugin);
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
  
  public double getRailPassPrice(String operator, long days) {
    return super.getDouble("RailPassPrices."+operator+"."+days);
  }
  
  public Set<String> getRailPassDays(String operator) {
    return Objects.requireNonNull(super.get().getConfigurationSection("RailPassPrices."+operator)).getKeys(false);
  }
  
  public double getCoffers(String operator) {
    return super.getDouble("Coffers."+operator);
  }
  
  public void setCoffers(String operator, double amt) {
    super.set("Coffers."+operator, amt);
    save();
  }
  
}
