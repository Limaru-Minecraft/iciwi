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

  // /**
  //  * Converts this card's details into an Iciwi-loggable map
  //  * @return This card in a loggable state
  //  */
  // @Override
  // public Map<String, Object> toMap() {

  //   // Get rail pass details
  //   HashMap<String, Object> railPassMap = new HashMap<>();
  //   // Unpack rail pass map
  //   for (String key : getRailPasses().keySet()) {
  //     railPassMap.put("card_railpass_" + key + "_price", owners.getRailPassPrice(key));
  //     railPassMap.put("card_railpass_" + key + "_percentage", owners.getRailPassPercentage(key));
  //     railPassMap.put("card_railpass_" + key + "_start", railPassMap.get(key));
  //     railPassMap.put("card_railpass_" + key + "_duration", owners.getRailPassDuration(key));
  //     railPassMap.put("card_railpass_" + key + "_operator", owners.getRailPassOperator(key));
  //   }

  //   // Get card details
  //   Map<String, Object> map = new HashMap<>(Map.ofEntries(
  //     Map.entry("card_serial", this.serial),
  //     Map.entry("card_value", cardSql.getCardValue(serial))
  //   ));
  //   map.putAll(railPassMap);

  //   return map;
  // }
}