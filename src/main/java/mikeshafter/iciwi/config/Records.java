package mikeshafter.iciwi.config;

public class Records extends CustomConfig {

public Records () { super("records.yml"); }

/**
 * Get the station at which the card entered the transit system.
 * This is used on both entry and onExit.
 *
 * @param serial Serial number of card
 * @return The station at which the card entered the transit system.
 */
public String getStation (String serial) {return super.getString(serial + ".station");}

/**
 * Set the station at which the card entered the transit system.
 * This is used on both entry and onExit.
 * This should only be set to a non-null value when the card is in the transit system. Otherwise, set it to null.
 *
 * @param serial  Serial number of card
 * @param station Station to set
 */
public void setStation (String serial, String station) {
	super.set(serial + ".station", station);
	super.save();
}

/**
 * Get the fare class of the journey taken by the card
 * This is used on onExit
 *
 * @param serial Serial number of card
 * @return the fare class of the journey taken by the card
 */
public String getClass (String serial) {
	String c = super.getString(serial + ".fareclass");
	if (c.isEmpty()) {return plugin.getConfig().getString("default-class");}
	else {return c;}
}

/**
 * Set the fare class of the journey taken by the card
 * This is used on entry
 *
 * @param serial    Serial number of card
 * @param fareClass the fare class of the journey taken by the card
 */
public void setClass (String serial, String fareClass) {
	super.set(serial + ".fareclass", fareClass);
	super.save();
}

/**
 * Get the entry station of the previous journey
 * This is used on onExit
 *
 * @param serial Serial number of card
 * @return the station at which the card entered the transit system on the previous journey
 */
public String getPreviousStation (String serial) {
	return super.getString(serial + ".p-station");
}

/**
 * Set the entry station of the previous journey
 * This is used on onExit
 *
 * @param serial  Serial number of card
 * @param station the station at which the card entered the transit system on the previous journey
 */
public void setPreviousStation (String serial, String station) {
	super.set(serial + ".p-station", station);
	super.save();
}

/**
 * Gets whether the card is eligible for a transfer discount on onExit
 * This is used on entry
 *
 * @param serial Serial number of card
 * @return true if the card is eligible for a transfer discount at the end of the journey
 */
public boolean getTransfer (String serial) {return super.getBoolean(serial + ".has-transfer");}

/**
 * Sets whether the card is eligible for a transfer discount on onExit.
 * This is used on entry
 *
 * @param serial      Serial number of card
 * @param hasTransfer true if the card is eligible for a transfer discount at the end of the journey
 */
public void setTransfer (String serial, boolean hasTransfer) {
	super.set(serial + ".has-transfer", hasTransfer);
	super.save();
}

/**
 * Gets the onExit timestamp of the previous journey.
 * This is used on entry
 *
 * @param serial Serial number of card
 * @return The timestamp of the previous time the card was used to onExit the transit system.
 */
public long getTimestamp (String serial) {return super.getLong(serial + ".timestamp");}

/**
 * Writes the last onExit time to the records file.
 * This is used on onExit
 *
 * @param serial    Serial number of card
 * @param timestamp The timestamp at which the card was used to onExit the transit system.
 */
public void setTimestamp (String serial, long timestamp) {
	super.set(serial + ".timestamp", timestamp);
	super.save();
}

/**
 * Gets the price of the previous journey.
 * This is used on onExit
 *
 * @param serial Serial number of card
 * @return the final price of the previous journey
 */
public double getPreviousFare (String serial) {
	return super.getDouble(serial + ".p-fare");
}

/**
 * Writes the price of the journey to the records file
 * This is used on onExit
 *
 * @param serial Serial number of card
 * @param fare   The final fare of the journey
 */
public void setPreviousFare (String serial, double fare) {
	super.set(serial + ".p-fare", fare);
	super.save();
}

/**
 * Gets the expiry time of the current fare cap.
 *
 * @param serial Serial number of card
 * @param operator Fare cap's TOC
 * @return Fare cap expiry time
 */
public long getCapExpiry (String serial, String operator) {
	return super.getLong(toPath(serial, operator, "expiry"));
}

/**
 * Gets the remaining amount of the current fare cap.
 *
 * @param serial Serial number of card
 * @param operator Fare cap's TOC
 * @return Fare cap remaining amount
 */
public double getCapRemAmt (String serial, String operator) {
	return super.getDouble(toPath(serial, operator, "rem-amt"));
}

/**
 * Sets the expiry time of the current fare cap.
 *
 * @param serial Serial number of card
 * @param operator Fare cap's TOC
 * @param exp New expiry time
 */
public void setCapExpiry (String serial, String operator, long exp) {
	super.set(toPath(serial, operator, "expiry"), exp);
	super.save();
}

/**
 * Sets the remaining amount of the current fare cap.
 *
 * @param serial Serial number of card
 * @param operator Fare cap's TOC
 * @param amt New remaining amount
 */
public void setCapRemAmt (String serial, String operator, double amt) {
	super.set(toPath(serial, operator, "rem-amt"), amt);
	super.save();
}
}
