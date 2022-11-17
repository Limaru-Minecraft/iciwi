package mikeshafter.iciwi.util;

import mikeshafter.iciwi.CardSql;
import org.bukkit.inventory.ItemStack;

public class IciwiCard extends IcCard {
  private final CardSql cardSql = new CardSql();
  private String serial;
  
  public IciwiCard (ItemStack item) {
    serial = MachineUtil.componentToString(Objects.requireNonNull(item.getItemMeta().lore()).get(1));
  }
  /**
   * Withdraws a certain amount;
   * @param amount The amount to withdraw from the card
   * @return Whether the withdrawal is successful
   */
  public boolean withdraw(double amount) {
    if (getValue() < amount) return false;
    cardSql.subtractValueFromCard(serial, amount);
    return true;
  }

  /**
   * Deposits a certain amount;
   * @param amount The amount to deposit into the card
   * @return Whether the deposit is successful
   */
  public boolean deposit(double amount) {
    cardSql.addValueToCard(serial, amount);
    return true;
  }

  /**
   * Gets the amount in the card
   * THIS SHOULD RETURN 0d IF THE CARD IS A DEBIT/CREDIT CARD
   */
  public double getValue() {
    return cardSql.getCardValue(serial);
  }
}