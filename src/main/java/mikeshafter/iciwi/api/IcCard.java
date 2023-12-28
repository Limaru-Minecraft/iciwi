package mikeshafter.iciwi.api;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.config.Owners;

import java.util.Map;


public interface IcCard {
String serial = "";
CardSql cardSql = new CardSql();

/**
 Withdraws a certain amount

 @param amount The amount to withdraw from the card
 @return Whether the withdrawal is successful */
boolean withdraw (double amount);

/**
 Gets the serial number of the card (REQUIRED)
 NOTE: Iciwi-compatible plugins' cards must state their plugin name in lore[0]

 @return Serial number */
String getSerial ();

/**
 Deposits a certain amount

 @param amount The amount to deposit into the card
 @return Whether the withdrawal is successful */
boolean deposit (double amount);

/**
 Gets the amount in the card
 THIS SHOULD RETURN Double.MAX_VALUE IF THE CARD IS A DEBIT/CREDIT CARD

 @return the amount in the card */
default double getValue () {return Double.MAX_VALUE;}

/**
 Gets the railpasses on the card

 @return A map in the format of [name, start time] */
default Map<String, Long> getRailPasses () { return cardSql.getAllDiscounts(serial); }

/**
 Sets a rail pass for a certain card and operator

 @param name  Name of the rail pass
 @param start Start time of the rail pass, as a long */
default void setRailPass (String name, long start) { cardSql.setDiscount(serial, name, start); }

/**
 Gets the expiry time of a certain railpass belonging to a card

 @param name Name of the rail pass
 @return The expiry time */
default long getExpiry (String name) { return cardSql.getStart(serial, name) + new Owners().getRailPassDuration(name); }
}
