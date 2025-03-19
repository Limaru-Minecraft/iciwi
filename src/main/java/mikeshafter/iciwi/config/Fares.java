package mikeshafter.iciwi.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

// New fares: <class>.<start>.<end>
public class Fares extends CustomConfig {

public Fares () {super("fares.yml");}

public Set<String> getAllClasses () {return this.get().getKeys(false);}

/**
 Remove a fare from fares.yml
 @param f Starting station
 @param e Ending station
 @param c Fare class
 */
public void unsetFare (String f, String e, String c) {
	super.set(toPath(c, f, e), null);
	super.save();
}

/**
 Remove all fares from a station given a fare class
 @param c fare class
 @param f starting station
 */
public void deleteStationFromClass (String c, String f) {
	super.set(toPath(c, f), null);
	super.save();
}

/**
 Remove all fares belonging to a fare class.
 @param c fare class
 */
public void deleteClass (String c) {
	super.set(c, null);
	super.save();
}

/**
 Remove all fares between two stations from fares.yml
 @param f Starting station
 @param e Ending station
 */
public void deleteJourney (String f, String e) {
	for (var c : getAllClasses())
		super.set(toPath(c, f, e), null);
	super.save();
}

/**
 Remove all fares starting from a specified station from fares.yml
 @param f Station
 */
public void deleteStation (String f) {
	for (var c : getAllClasses())
		super.set(toPath(c, f), null);
	super.save();
}

/**
 Set a fare to fares.yml
 @param f Starting station
 @param e Ending station
 @param c Fare class
 @param price Price to set
 */
public void setFare (String f, String e, String c, double price) {
	super.set(toPath(c, f, e), price);
	super.save();
}

/**
 Get the corresponding fare if the player pays by card.
 @param f Starting station
 @param e Ending station
 @param cSans_ Fare class, without the starting underscore
 @return Fare. If the fare is not found, this method returns 0.
 */
public double getCardFare (String f, String e, String cSans_) {
	if (cSans_.indexOf("_") == 0) cSans_ = cSans_.substring(1);
	final double fare = this.getDouble(toPath("_" + cSans_, f, e));
	if (fare == 0d) return this.getDouble(toPath(cSans_, f, e));
	else return fare;
}

/**
 Get the corresponding fare if the player pays by cash
 @param f Starting station
 @param e Ending station
 @param c Fare class
 @return Fare. If the fare is not found, this method returns 0.
 */
public double getFare (String f, String e, String c) {return this.getDouble(toPath(c, f, e));}

/**
 Get all fares starting from a certain station, with the specified class
 @param f Starting station
 @param c Fare class
 @return A treemap in the format ENDPOINT, PRICE
 */
public TreeMap<String, Double> getFares (String f, String c) {
	ConfigurationSection section = this.getConfigurationSection(toPath(c, f));
	if (section != null) {
		var fareMap = new TreeMap<String, Double>();

		section.getKeys(false).forEach(e -> fareMap.put(e, section.getDouble(e, 0d)));
		return fareMap;
	}
	else return null;
}

/**
 Get all fares starting from a certain station and ending at another station.
 @param f Starting station
 @param e Ending station
 @return Fare
 */
public TreeMap<String, Double> getFaresFromDestinations (String f, String e) {
	var fareMap = new TreeMap<String, Double>();
	for (var c : getAllClasses()) {
		if (getFare(f, e, c) > 0d)
			fareMap.put(c, getFare(f, e, c));
	}
	return fareMap;
}

/**
 Get all fare classes starting from a certain station and ending at another station.
 @param f Starting station
 @param e   Ending station
 @return Fare classes
 */
public TreeSet<String> getClasses (String f, String e) {
	var set = new TreeSet<String>();
	for (var c : getAllClasses()) {
		if (getFare(f, e, c) > 0d)
			set.add(c);
	}
	return set;
}

/**
 Get all end stations starting from a certain station.
 @param f Starting station
 @return Fare classes
 */
public TreeSet<String> getDestinations (String f) {
	var set = new TreeSet<String>();
	for (var c : getAllClasses()) {
		set.addAll(super.getConfigurationSection(toPath(c, f)).getKeys(false));
	}
	return set;
}

/**
 Get all starting stations starting from a certain station.
 @return All starting stations
 */
public Set<String> getAllStarts () {
	var set = new TreeSet<String>();
	for (var c : getAllClasses()) {
		set.addAll(super.getConfigurationSection(c).getKeys(false));
	}
	return set;
}
}
