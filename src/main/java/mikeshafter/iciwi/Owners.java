package mikeshafter.iciwi;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Objects;
import java.util.Set;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class Owners extends CustomConfig {
  private File file;
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final String name;
  
  public Owners(Plugin plugin) {
    super("owners.yml", plugin);
    this.name = "owners.yml";
  }
  
  public String getOwner(String station) {
    return super.getString("Operators."+station);
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
