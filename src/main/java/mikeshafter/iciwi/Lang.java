package mikeshafter.iciwi;

import org.bukkit.plugin.Plugin;

import java.io.File;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class Lang extends CustomConfig {
  private File file;
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final String name;
  
  public Lang(Plugin plugin) {
    super("lang.yml", plugin);
    this.name = "lang.yml";
  }
  
  
  // Fare gate strings
  // // Signs
  public String ENTRY = super.getString("entry");
  public String EXIT = super.getString("exit");
  public String VALIDATOR = super.getString("validator");
  public String FAREGATE = super.getString("faregate");
  //--- Messages
  public String REMAINING = super.getString("remaining");
  public String FARE_EVADE = super.getString("fareEvade");
  
  
  // Ticket Machine Strings
  //--- Global variables
  public String TICKET_MACHINE = super.getString("ticketMachine");
  public String TICKETS = super.getString("tickets");
  public String PLUGIN_NAME = super.getString("pluginName");
  public String CURRENCY = super.getString("currency");
  public String SERIAL_NUMBER = super.getString("serialNumber");
  public String SERIAL_PREFIX = super.getString("serialPrefix");
  
  
  //--- Messages
  public String NOT_ENOUGH_MONEY = super.getString("notEnoughMoney");
  
  
}
