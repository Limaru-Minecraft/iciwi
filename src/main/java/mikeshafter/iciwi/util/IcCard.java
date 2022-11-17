package mikeshafter.iciwi.util;
import mikeshafter.iciwi.CardSql;

public abstract class IcCard {
  private final String serial;
  private final CardSql cardSql = new CardSql();
  
  /**
   * Withdraws a certain amount
   * @param amount The amount to withdraw from the card
   * @return Whether the withdrawal is successful
   */
  public boolean withdraw(double amount);

  /**
   * Gets the serial number of the card
   * @return Serial number
   */
  public String getSerial() {return serial;}

  /**
   * Deposits a certain amount
   * @param amount The amount to deposit into the card
   * @return Whether the withdrawal is successful
   */
  public boolean deposit(double amount);

  /**
   * Gets the amount in the card
   * THIS SHOULD RETURN 0d IF THE CARD IS A DEBIT/CREDIT CARD
   */
  public double getValue(); 

  /**
   * Gets the railpasses on the card
   * @return A map in the format of <name, start time>
   */
  public Map<String, Long> getRailPasses() {
    return cardSql.getAllDiscounts(serial);
  }

  /**
   * Sets a rail pass for a certain card and operator
   *
   * @param name   Name of the rail pass
   * @param start  Start time of the rail pass, as a long
   */
  public void setRailPass(String name, long start) {
    cardSql.setDiscount(serial, name, start);
  }

  /**
   * Gets the expiry time of a certain railpass belonging to a card
   *
   * @param name   Name of the discount (include operator)
   */
  public long getExpiry(String name) {
    cardSql.getExpiry(serial, name);
  }
}