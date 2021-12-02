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
  
  public String __TICKET_MACHINE = super.getString("TicketMachine");
  public String NEW_TICKET = super.getString("menuNewTicket");
  public String ADJUST_FARES = super.getString("menuAdjustFares");
  public String CARD_OPERATIONS = super.getString("CardOperations");
  public String CHECK_FARES = super.getString("CheckFares");
  
  public String __NEW_TICKET = super.getString("NewTicket");
  public String CLEAR = super.getString("Clear");
  public String ENTER = super.getString("Enter");
  public String TRAIN_TICKET = super.getString("TrainTicket");
  public String GENERATE_TICKET = super.getString("generateTicket");
  
  public String __SELECT_TICKET = super.getString("SelectTicket");
  
  public String __ADJUST_FARES = super.getString("AdjustFares");
  public String DIRECT_TICKET_INVALID = super.getString("directTicketInvalid");

  public String __SELECT_CARD = super.getString("SelectCard");

  public String __CARD_OPERATION = super.getString("CardOperation");
  public String CARD_DETAILS = super.getString("CardDetails");
  public String NEW_CARD = super.getString("NewCard");
  public String TOP_UP_CARD = super.getString("TopUpCard");
  public String ADD_RAIL_PASS = super.getString("AddRailPass");
  public String REFUND_CARD = super.getString("RefundCard");
  public String REMAINING_VALUE = super.getString("RemainingValue");

  public String __SELECT_VALUE = super.getString("SelectValue");
  public String NEW_CARD_CREATED = super.getString("NewCardCreated");

  public String __TOP_UP = super.getString("TopUp");
  public String CARD_TOPPED_UP = super.getString("CardToppedUp");

  public String __ADD_RAIL_PASS = super.getString("AddRailPass");
  public String DAYS = super.getString("Days");
  public String ADDED_RAIL_PASS = super.getString("AddedRailPass");

  public String TAPPED_IN = super.getString("TappedIn");
  public String TRANSFER = super.getString("Transfer");
  public String TAPPED_OUT = super.getString("TappedOut");
  public String TICKET_IN = super.getString("TicketIn");
  public String TICKET_OUT = super.getString("TicketOut");
  
  
}
