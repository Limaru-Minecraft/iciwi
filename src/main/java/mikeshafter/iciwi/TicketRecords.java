package mikeshafter.iciwi;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class TicketRecords extends Config {
  private File file;
  private final YamlConfiguration config;
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final String name;

  public TicketRecords(Plugin plugin) {
    super("records.yml", plugin)
  }

}
