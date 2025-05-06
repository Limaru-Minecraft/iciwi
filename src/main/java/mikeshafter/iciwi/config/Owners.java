package mikeshafter.iciwi.config;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;


public class Owners extends CustomConfig {

public Owners () { super("owners.yml"); }

/**
 Gets the owners of a station.
 @param station the station to query
 @return the owners of the station */
public @NotNull List<String> getOwners (String station) {
	List<String> ownersList = super.getStringList("Operators." + station);
	if (ownersList.isEmpty()) {
		String s = super.getConfigPlugin().getConfig().getString("default-operator");
		var o = s == null ? List.of("null") : List.of(s);
		setOwners(station, o);
		return o;
	}
	else return ownersList;
}

/**
 Gets all registered TOCs
 @return List of all TOCs */
public List<String> getAllCompanies () {
	var aliases = super.getConfigurationSection("Aliases");
	return aliases == null ? new ArrayList<>() : aliases.getKeys(false).stream().toList();
}

/**
 @param station   Station to set a TOC to
 @param operators The TOCs */
public void setOwners (String station, List<String> operators) {super.set("Operators." + station, operators);}

/**
 @param station  Station to add a TOC to
 @param operator The TOC */
public void addOwner (String station, String operator) {
	List<String> operators = getOwners(station);
	if (!operators.contains(operator)) operators.add(operator);
	setOwners(station, operators);
}

/**
 @param station  Station to remove a TOC from
 @param operator The TOC */
public void removeOwner (String station, String operator) {
	List<String> operators = getOwners(station);
	operators.remove(operator);
	setOwners(station, operators);
}

/**
 @param operator TOC to search up
 @param amt      Amount of money to give to the operator */
public void deposit (String operator, double amt) {
	String TOCOwnerName = super.getString("Aliases." + operator);
	if (!TOCOwnerName.isEmpty())
		Iciwi.economy.depositPlayer(plugin.getServer().getOfflinePlayer(TOCOwnerName), amt);
}

/**
 @param operator TOC to search up
 @param amt      Amount of money to remove from the operator
 This method should be used in conjunction with Owners#deposit for quick transactions between TOCs. */
public void withdraw (String operator, double amt) {
	String TOCOwnerName = super.getString("Aliases." + operator);
	if (!TOCOwnerName.isEmpty())
		Iciwi.economy.withdrawPlayer(plugin.getServer().getOfflinePlayer(TOCOwnerName), amt);
}

/**
 *  Creates a rail pass using a long Unix time as its duration.
 @param name       Name of the rail pass
 @param operator   The operator who sells the rail pass
 @param duration   How long the rail pass lasts (in d:hh:mm:ss)
 @param price      Price of the rail pass
 @param percentage Percentage payable by the commuter when taking a train owned by said operator
*/
public void setRailPassInfo (String name, String operator, long duration, double price, double percentage) {
	super.set(toPath("RailPasses", name, "operator"), operator);
	super.set(toPath("RailPasses", name, "duration"), timeToString(duration));
	super.set(toPath("RailPasses", name, "price"), price);
	super.set(toPath("RailPasses", name, "percentage"), percentage);
	super.save();
}

/**
 * Creates a rail pass using a timestring as its duration.
 @param name       Name of the rail pass
 @param operator   The operator who sells the rail pass
 @param duration   How long the rail pass lasts (in milliseconds)
 @param price      Price of the rail pass
 @param percentage Percentage payable by the commuter
 */
public void setRailPassInfo (String name, String operator, String duration, double price, double percentage) {
	super.set(toPath("RailPasses", name, "operator"), operator);
	super.set(toPath("RailPasses", name, "duration"), duration);
	super.set(toPath("RailPasses", name, "price"), price);
	super.set(toPath("RailPasses", name, "percentage"), percentage);
	super.save();
}

private String timeToString (long time) {
	DateFormat df = new SimpleDateFormat("dd:hh:mm:ss");
	return df.format(Date.from(Instant.ofEpochMilli(time)));
}

/**
 @param name Name of the rail pass
 @return How long the rail pass lasts */
public long getRailPassDuration (String name) {
	return timeToLong(super.getString(toPath("RailPasses", name, "duration")));
}

private long timeToLong (String time) {
	try {
		return new SimpleDateFormat("dd:hh:mm:ss").parse(time).getTime();
	}
	catch (ParseException e) {
		return 0L;
	}
}

/**
 Get the percentage payable of the rail pass

 @param name Name of the rail pass
 @return Percentage payable by the commuter */
public double getRailPassPercentage (String name) { return super.getDouble(toPath("RailPasses", name, "percentage")) ;}

/**
 Get the price of the rail pass

 @param name Name of the rail pass
 @return Price of the rail pass */
public double getRailPassPrice (String name) { return super.getDouble(toPath("RailPasses", name, "price")) ;}

/**
 Get the operator who sells the rail pass

 @param name Name of the rail pass
 @return The operator who sells the rail pass */
public String getRailPassOperator (String name) { return super.getString(toPath("RailPasses", name, "operator")); }

/**
 Get the name of the rail pass that is sold by the operator

 @param operator TOC to search up
 @return Names of rail passes sold by the operator */
public List<String> getRailPassNames (String operator) {
	// Loop through all names in RailPasses
	ConfigurationSection railPassPrices = super.getConfigurationSection("RailPasses");
	// Check if the name has the given operator
	// If it has, add it to h
	ArrayList<String> h = new ArrayList<>();
	// if there are no rail passes, return an empty set
	if (railPassPrices == null) {
		return new ArrayList<>();
	}
	for (String pass : railPassPrices.getKeys(false))
		if (Objects.equals(railPassPrices.getString(pass + ".operator"), operator)) h.add(pass);
	return h;
}

/**
 Gets all the rail passes from a list of operators

 @param operators TOCs to search up
 @return List of the rail passes */
public List<String> getRailPassNamesFromList (List<String> operators) {
    ArrayList<String> set = new ArrayList<>();
    for (String operator : operators) {
        set.addAll(getRailPassNames(operator));
    }
    return set;
}

/**
 Gets all the rail passes from all operators.

 @return List of all rail passes */
public List<String> getAllRailPasses () {
	var railPasses = super.getConfigurationSection("RailPasses");
	return railPasses == null ? new ArrayList<>() : railPasses.getKeys(false).stream().toList();
}

/**
 @param player Name of the player
 @return List of companies the player owns */
public List<String> getOwnedCompanies (String player) {
	Map<String, Object> operatorMap = super.getConfigurationSection("Aliases").getValues(false);
	return operatorMap.entrySet().stream().filter(entry -> player.equals(entry.getValue())).map(Map.Entry::getKey).toList();
}

/**
 @param player   Name of the player
 @param operator TOC to search up
 @return whether the player owns the TOC */
public boolean getOwnership (String player, String operator) { return player.equalsIgnoreCase(super.getString("Aliases." + operator)); }

/**
 Gets the price of a single company-wide ticket
 @param operator Name of the company
 @return the price of a single journey ticket */
public double getOperatorTicket (String operator) { return super.getDouble(toPath("TicketTypeUtil", operator)); }

/**
 Gets whether a company ticket exists
 @param operator Name of the company
 @return true if a company ticket exists */
public boolean hasOperatorTicket (String operator) { return super.getDouble(toPath("TicketTypeUtil", operator)) > 0; }

/**
 Sets the price of a single company-wide ticket
 @param operator Name of the company
 @param price The price of a company-wide ticket
 */
public void setOperatorTicket (String operator, double price) { super.set(toPath("TicketTypeUtil", operator), price); }

/**
 * Gets the fare cap of a company
 *
 * @param operator Name of the company
 * @return the maximum amount in which a card can pay for a given duration
 */
public double getFareCapAmt (String operator) {return super.getDouble(toPath("Caps" ,  operator ,  "amount"));}

/**
 * Gets the duration of the fare cap of a company
 *
 * @param operator Name of the company
 * @return the duration in which the fare cap is active
 */
public long getFareCapDuration (String operator) {return Long.parseLong(super.getString(toPath("Caps" ,  operator ,  "duration")));}

/**
 * Gets the duration of the fare cap of a company as a string
 *
 * @param operator Name of the company
 * @return the duration in which the fare cap is active
 */
public String getFareCapDurationString (String operator) {return super.getString(toPath("Caps" ,  operator ,  "duration"));}

/**
 * Gets whether a company has a fare cap
 *
 * @param operator Name of the company
 * @return true if a fare cap exists
 */
public boolean hasFareCap (String operator) {return super.getDouble(toPath("Caps" ,  operator ,  "amount")) > 0;}

/**
 * Sets the amount capped for a company
 *
 * @param operator Name of the company
 * @param amt      the maximum amount in which a card can pay for a given duration
 */
public void setFareCapAmt (String operator, double amt) {super.set(toPath("Caps" ,  operator ,  "amount"), amt);}

/**
 * Sets the duration of the fare cap of a company
 *
 * @param operator Name of the company
 * @param duration the duration in which the fare cap is active
 */
public void setFareCapDuration (String operator, String duration) {super.set(toPath("Caps" ,  operator ,  "duration"), duration);}
}
