package mikeshafter.iciwi;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class StationOwners extends Config {
  private File file;
  private final YamlConfiguration config;
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final String name;

  public StationOwners(Plugin plugin) {
    super("owners.yml", plugin);
  }

  public String getOwner(String station) {
    return super.getString("Operators."+station);
  }

  public void deposit(String operator, double amt) {
    super.set("Coffers."+operator, configFile.getDouble("Coffers."+operator)+amt);
    super.save();
  }

  public double getRailPassPrice(String operator, int days) {
    return super.getDouble("RailPassPrices."+operator+"."+days);
  }

}
