package mikeshafter.iciwi.util;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.api.IcCard;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;


public class IciwiCard implements IcCard {
  private final CardSql cardSql = new CardSql();
  private final String serial;
  
  public IciwiCard (ItemStack item) { this.serial = IciwiUtil.parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1)); }

  /**
   * Gets the serial number of the card
   * NOTE: Iciwi-compatible plugins' cards must state their plugin name in lore[0]
   * @return Serial number
   */
  @Override
  public String getSerial() { return this.serial; }

  /**
   * Withdraws a certain amount;
   * @param amount The amount to withdraw from the card
   * @return Whether the withdrawal is successful
   */
  @Override
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
  @Override
  public boolean deposit(double amount) {
    cardSql.addValueToCard(serial, amount);
    return true;
  }

  /**
   * Gets the amount in the card
   */
  @Override
  public double getValue() { return cardSql.getCardValue(serial); }

}