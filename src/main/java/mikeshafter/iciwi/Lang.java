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
  public String ENTRY() {
    return super.getString("entry");
  }
  
  public String EXIT() {
    return super.getString("exit");
  }
  
  public String VALIDATOR() {
    return super.getString("validator");
  }
  
  public String FAREGATE() {
    return super.getString("faregate");
  }
  
  public String MEMBER() {
    return super.getString("member");
  }
  
  //--- Messages
  public String REMAINING() {
    return super.getString("remaining");
  }
  
  public String FARE_EVADE() {
    return super.getString("fare-evade");
  }
  
  
  // Ticket Machine Strings
//--- Global variables
  public String TICKETS() {
    return super.getString("tickets");
  }
  
  public String PLUGIN_NAME() {
    return super.getString("plugin-name");
  }
  
  public String CURRENCY() {
    return super.getString("currency");
  }
  
  public String SERIAL_NUMBER() {
    return super.getString("serial-number");
  }
  
  public String SERIAL_PREFIX() {
    return super.getString("serial-prefix");
  }
  
  
  //--- Messages
  public String NOT_ENOUGH_MONEY() {
    return super.getString("not-enough-money");
  }
  
  public String __TICKET_MACHINE() {
    return super.getString("ticket-machine");
  }
  
  public String NEW_TICKET() {
    return super.getString("menu-new-ticket");
  }
  
  public String ADJUST_FARES() {
    return super.getString("menu-adjust-fares");
  }
  
  public String CARD_OPERATIONS() {
    return super.getString("card-operations");
  }
  
  public String CHECK_FARES() {
    return super.getString("check-fares");
  }
  
  public String __NEW_TICKET() {
    return super.getString("new-ticket");
  }
  
  public String CLEAR() {
    return super.getString("clear");
  }
  
  public String ENTER() {
    return super.getString("enter");
  }
  
  public String TRAIN_TICKET() {
    return super.getString("train-ticket");
  }
  
  public String GENERATE_TICKET() {
    return super.getString("generate-ticket");
  }
  
  public String GENERATE_TICKET_GLOBAL() {
    return super.getString("generate-ticket-global");
  }
  
  public String GLOBAL_TICKET() {
    return super.getString("global-ticket");
  }
  
  public String __SELECT_TICKET() {
    return super.getString("select-ticket");
  }
  
  public String __ADJUST_FARES() {
    return super.getString("adjust-fares");
  }
  
  public String DIRECT_TICKET_INVALID() {
    return super.getString("direct-ticket-invalid");
  }
  
  public String __SELECT_CARD() {
    return super.getString("select-card");
  }
  
  public String __CARD_OPERATION() {
    return super.getString("card-operation");
  }
  
  public String CARD_DETAILS() {
    return super.getString("card-details");
  }
  
  public String NEW_CARD() {
    return super.getString("new-card");
  }
  
  public String TOP_UP_CARD() {
    return super.getString("top-up-card");
  }
  
  public String ADD_RAIL_PASS() {
    return super.getString("menu-add-rail-pass");
  }
  
  public String REFUND_CARD() {
    return super.getString("refund-card");
  }
  
  public String REMAINING_VALUE() {
    return super.getString("remaining-value");
  }
  
  public String CARD_REFUNDED() {
    return super.getString("card-refunded");
  }
  
  public String __SELECT_VALUE() {
    return super.getString("select-value");
  }
  
  public String NEW_CARD_CREATED() {
    return super.getString("new-card-created");
  }
  
  public String __TOP_UP() {
    return super.getString("top-up");
  }
  
  public String CARD_TOPPED_UP() {
    return super.getString("card-topped-up");
  }
  
  public String __ADD_RAIL_PASS() {
    return super.getString("add-rail-pass");
  }
  
  public String DAYS() {
    return super.getString("days");
  }
  
  public String ADDED_RAIL_PASS() {
    return super.getString("added-rail-pass");
  }
  
  public String TAPPED_IN() {
    return super.getString("tapped-in");
  }
  
  public String TRANSFER() {
    return super.getString("transfer");
  }
  
  public String TAPPED_OUT() {
    return super.getString("tapped-out");
  }
  
  public String TICKET_IN() {
    return super.getString("ticket-in");
  }
  
  public String TICKET_OUT() {
    return super.getString("ticket-out");
  }
  
  public String INVALID_TICKET() {
    return super.getString("invalid-ticket");
  }
  
  public String PAYMENT() {
    return super.getString("payment");
  }
  
  public String CASH_DIVERT() {
    return super.getString("cash-divert");
  }
  
  public String PAY_SUCCESS() {
    return super.getString("pay-success");
  }
  
  public String PAY_SUCCESS_CARD() {
    return super.getString("pay-success-card");
  }
  
  public String CREATE_ENTRY_SIGN() {
    return super.getString("create-entry-sign");
  }
  
  public String CREATE_EXIT_SIGN() {
    return super.getString("create-exit-sign");
  }
  
  public String CREATE_FAREGATE_SIGN() {
    return super.getString("create-faregate-sign");
  }
  
  public String CREATE_VALIDATOR_SIGN() {
    return super.getString("create-validator-sign");
  }
  
  public String CREATE_TICKET_MACHINE() {
    return super.getString("create-ticket-machine");
  }
  
  public String DEFAULT_ENTRY_SIGN_LINE_3() {
    return super.getString("default-entry-sign-line-3");
  }
  
  public String DEFAULT_ENTRY_SIGN_LINE_4() {
    return super.getString("default-entry-sign-line-4");
  }
  
  public String DEFAULT_EXIT_SIGN_LINE_3() {
    return super.getString("default-exit-sign-line-3");
  }
  
  public String DEFAULT_EXIT_SIGN_LINE_4() {
    return super.getString("default-exit-sign-line-4");
  }
  
  public String DEFAULT_VALIDATOR_SIGN_LINE_2() {
    return super.getString("default-validator-sign-line-2");
  }
  
  public String DEFAULT_VALIDATOR_SIGN_LINE_3() {
    return super.getString("default-validator-sign-line-3");
  }
  
  public String DEFAULT_VALIDATOR_SIGN_LINE_4() {
    return super.getString("default-validator-sign-line-4");
  }
  
  public String DEFAULT_TM_SIGN_LINE_2() {
    return super.getString("default-tm-sign-line-2");
  }
  
  public String DEFAULT_TM_SIGN_LINE_3() {
    return super.getString("default-tm-sign-line-3");
  }
  
  public String DEFAULT_TM_SIGN_LINE_4() {
    return super.getString("default-tm-sign-line-4");
  }
  
}
