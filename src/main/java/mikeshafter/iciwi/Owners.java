package mikeshafter.iciwi;

import org.bukkit.plugin.Plugin;

import java.io.File;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class Owners extends Config {
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

  public double getRailPassPrice(String operator, int days) {
    return super.getDouble("RailPassPrices."+operator+"."+days);
  }

}
