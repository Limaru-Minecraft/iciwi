package mikeshafter.iciwi.util;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Owners;
import org.bukkit.inventory.ItemStack;
import java.util.Map;
import java.util.Objects;


public class IciwiCard implements IcCard {
private final CardSql cardSql = new CardSql();
private final String serial;

public IciwiCard (ItemStack item) {this.serial = IciwiUtil.parseComponent(Objects.requireNonNull(item.getItemMeta().lore()).get(1));}

/**
 Gets the serial number of the card
 NOTE: Iciwi-compatible plugins' cards must state their plugin name in lore[0]

 @return Serial number */
@Override public String getSerial () {return this.serial;}

/**
 Withdraws a certain amount;

 @param amount The amount to withdraw from the card
 @return Whether the withdrawal is successful */
@Override public boolean withdraw (double amount) {
	if (getValue() < amount) return false;
	cardSql.subtractValueFromCard(serial, amount);
	return true;
}

/**
 Deposits a certain amount;

 @param amount The amount to deposit into the card
 @return Whether the deposit is successful */
@Override public boolean deposit (double amount) {
	cardSql.addValueToCard(serial, amount);
	return true;
}

/**
 Gets the amount in the card
 */
@Override public double getValue () { return cardSql.getCardValue(serial); }

/**
 * Gets the railpasses on the card
 *
 * @return A map in the format of [name, start time]
 */
@Override
public Map<String, Long> getRailPasses () { return cardSql.getAllDiscounts(serial); }

/**
 * Sets a rail pass for a certain card and operator
 *
 * @param name  Name of the rail pass
 * @param start Start time of the rail pass, as a long
 */
@Override
public void setRailPass (String name, long start) { cardSql.setDiscount(serial, name, start); }

/**
 * Gets the expiry time of a certain railpass belonging to a card
 *
 * @param name Name of the rail pass
 * @return The expiry time
 */
@Override
public long getExpiry (String name) { return cardSql.getStart(serial, name) + new Owners().getRailPassDuration(name); }

@Override
public String toString () {	return this.serial; }
}