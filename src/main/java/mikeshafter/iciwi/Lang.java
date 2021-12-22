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
  public final String ENTRY = super.getString("entry");
  public final String EXIT = super.getString("exit");
  public final String VALIDATOR = super.getString("validator");
  public final String FAREGATE = super.getString("faregate");
  //--- Messages
  public final String REMAINING = super.getString("remaining");
  public final String FARE_EVADE = super.getString("fare-evade");
  
  
  // Ticket Machine Strings
//--- Global variables
  public final String TICKETS = super.getString("tickets");
  public final String PLUGIN_NAME = super.getString("plugin-name");
  public final String CURRENCY = super.getString("currency");
  public final String SERIAL_NUMBER = super.getString("serial-number");
  public final String SERIAL_PREFIX = super.getString("serial-prefix");
  
  
  //--- Messages
  public final String NOT_ENOUGH_MONEY = super.getString("not-enough-money");
  
  public final String __TICKET_MACHINE = super.getString("ticket-machine");
  public final String NEW_TICKET = super.getString("menu-new-ticket");
  public final String ADJUST_FARES = super.getString("menu-adjust-fares");
  public final String CARD_OPERATIONS = super.getString("card-operations");
  public final String CHECK_FARES = super.getString("check-fares");
  
  public final String __NEW_TICKET = super.getString("new-ticket");
  public final String CLEAR = super.getString("clear");
  public final String ENTER = super.getString("enter");
  public final String TRAIN_TICKET = super.getString("train-ticket");
  public final String GENERATE_TICKET = super.getString("generate-ticket");
  public final String GENERATE_TICKET_GLOBAL = super.getString("generate-ticket-global");
  public final String GLOBAL_TICKET = super.getString("global-ticket");
  
  public final String __SELECT_TICKET = super.getString("select-ticket");
  
  public final String __ADJUST_FARES = super.getString("adjust-fares");
  public final String DIRECT_TICKET_INVALID = super.getString("direct-ticket-invalid");
  
  public final String __SELECT_CARD = super.getString("select-card");
  
  public final String __CARD_OPERATION = super.getString("card-operation");
  public final String CARD_DETAILS = super.getString("card-details");
  public final String NEW_CARD = super.getString("new-card");
  public final String TOP_UP_CARD = super.getString("top-up-card");
  public final String ADD_RAIL_PASS = super.getString("menu-add-rail-pass");
  public final String REFUND_CARD = super.getString("refund-card");
  public final String REMAINING_VALUE = super.getString("remaining-value");
  public final String CARD_REFUNDED = super.getString("card-refunded");
  
  public final String __SELECT_VALUE = super.getString("select-value");
  public final String NEW_CARD_CREATED = super.getString("new-card-created");
  
  public final String __TOP_UP = super.getString("top-up");
  public final String CARD_TOPPED_UP = super.getString("card-topped-up");
  
  public final String __ADD_RAIL_PASS = super.getString("add-rail-pass");
  public final String DAYS = super.getString("days");
  public final String ADDED_RAIL_PASS = super.getString("added-rail-pass");
  
  public final String TAPPED_IN = super.getString("tapped-in");
  public final String TRANSFER = super.getString("transfer");
  public final String TAPPED_OUT = super.getString("tapped-out");
  public final String TICKET_IN = super.getString("ticket-in");
  public final String TICKET_OUT = super.getString("ticket-out");
  
  public final String PAYMENT = super.getString("payment");
  public final String CASH_DIVERT = super.getString("cash-divert");
  public final String PAY_SUCCESS = super.getString("pay-success");
  public final String PAY_SUCCESS_CARD = super.getString("pay-success-card");
  
  public final String CREATE_ENTRY_SIGN = super.getString("create-entry-sign");
  public final String CREATE_EXIT_SIGN = super.getString("create-exit-sign");
  public final String CREATE_FAREGATE_SIGN = super.getString("create-faregate-sign");
  public final String CREATE_VALIDATOR_SIGN = super.getString("create-validator-sign");
  public final String CREATE_TICKET_MACHINE = super.getString("create-ticket-machine");
  
  public final String DEFAULT_ENTRY_SIGN_LINE_3 = super.getString("default-entry-sign-line-3");
  public final String DEFAULT_ENTRY_SIGN_LINE_4 = super.getString("default-entry-sign-line-4");
  
  public final String DEFAULT_EXIT_SIGN_LINE_3 = super.getString("default-exit-sign-line-3");
  public final String DEFAULT_EXIT_SIGN_LINE_4 = super.getString("default-exit-sign-line-4");
  
  public final String DEFAULT_VALIDATOR_SIGN_LINE_2 = super.getString("default-validator-sign-line-2");
  public final String DEFAULT_VALIDATOR_SIGN_LINE_3 = super.getString("default-validator-sign-line-3");
  public final String DEFAULT_VALIDATOR_SIGN_LINE_4 = super.getString("default-validator-sign-line-4");
  
  public final String DEFAULT_TM_SIGN_LINE_2 = super.getString("default-tm-sign-line-2");
  public final String DEFAULT_TM_SIGN_LINE_3 = super.getString("default-tm-sign-line-3");
  public final String DEFAULT_TM_SIGN_LINE_4 = super.getString("default-tm-sign-line-4");
  
}
