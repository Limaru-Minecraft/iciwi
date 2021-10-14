package mikeshafter.iciwi;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class Lang extends Config {
  private File file;
  private final YamlConfiguration config;
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final String name;

  public Lang(Plugin plugin) {
    super("lang.yml", plugin)
    this.name = "lang.yml";
  }


  // Fare gate strings
  // // Signs
  public String ENTRY = this.config.getString("entry");
  public String EXIT = this.config.getString("exit");
  public String VALIDATOR = this.config.getString("validator");
  public String FAREGATE = this.config.getString("faregate");
  //--- Messages
  public String REMAINING = this.config.getString("remaining");
  public String FARE_EVADE = this.config.getString("fareEvade");


  // Ticket Machine Strings
  //--- Global variables
  public String TICKET_MACHINE = this.config.getString("ticketMachine");
  public String TICKETS = this.config.getString("tickets");
  public String PLUGIN_NAME = this.config.getString("pluginName");
  public String CURRENCY = this.config.getString("currency");
  public String SERIAL_NUMBER = this.config.getString("serialNumber");
  public String SERIAL_PREFIX = this.config.getString("serialPrefix");


  //--- Messages
  public String NOT_ENOUGH_MONEY = this.config.getString("notEnoughMoney");


}
