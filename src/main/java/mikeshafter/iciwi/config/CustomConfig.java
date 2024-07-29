package mikeshafter.iciwi.config;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomConfig {
private File file;
private final YamlConfiguration config;
protected final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final String name;

public CustomConfig (String name) {
	this.name = name;
	file = new File(plugin.getDataFolder(), name);
	Logger logger = plugin.getLogger();
	if (!file.exists()) {
		logger.log(Level.INFO, file.getParentFile().mkdirs() ? "[Iciwi] New config file created" : "[Iciwi] Config file already exists, initialising files...");
		plugin.saveResource(name, false);
	}

	config = new YamlConfiguration();
	try { config.load(file); }
	catch (Exception e) { logger.warning(e.getLocalizedMessage()); }
}

public void save () {
	try { config.save(file); }
	catch (Exception e) { plugin.getLogger().warning(e.getLocalizedMessage()); }
}

protected Plugin getConfigPlugin () {return this.plugin;}

public void saveDefaultConfig () { if (!file.exists()) { plugin.saveResource(name, false); } }

public File getFile () {return file;}

public YamlConfiguration get () {
	if (config == null) reload();
	return config;
}

/**
 * Gets the requested Object by path.
 * <p>
 * If the Object does not exist but a default value has been specified,
 * this will return the default value. If the Object does not exist and no
 * default value was specified, this will return null.
 *
 * @param path Path of the Object to get.
 * @return Requested Object.
 */
public Object get (String path) {return this.config.get(path);}

/**
 * Gets the requested String by path.
 * <p>
 * If the String does not exist but a default value has been specified,
 * this will return the default value. If the String does not exist and no
 * default value was specified, this will return null.
 *
 * @param path Path of the String to get.
 * @return Requested String.
 */
public String getString (@NotNull String path) {
	var s = this.config.getString(path);
	return s == null ? "" : s;
}

/**
 * Gets the requested boolean by path.
 * <p>
 * If the boolean does not exist but a default value has been specified,
 * this will return the default value. If the boolean does not exist and
 * no default value was specified, this will return false.
 *
 * @param path Path of the boolean to get.
 * @return Requested boolean.
 */
public boolean getBoolean (@NotNull String path) { return this.config.getBoolean(path); }

/**
 * Gets the requested int by path.
 * <p>
 * If the int does not exist but a default value has been specified, this
 * will return the default value. If the int does not exist and no default
 * value was specified, this will return 0.
 *
 * @param path Path of the int to get.
 * @return Requested int.
 */
public int getInt (@NotNull String path) { return this.config.getInt(path); }

/**
 * Gets the requested double by path.
 * <p>
 * If the double does not exist but a default value has been specified,
 * this will return the default value. If the double does not exist and no
 * default value was specified, this will return 0.
 *
 * @param path Path of the double to get.
 * @return Requested double.
 */
public double getDouble (@NotNull String path) { return this.config.getDouble(path); }

/**
 * Gets the requested long by path.
 * <p>
 * If the long does not exist but a default value has been specified, this
 * will return the default value. If the long does not exist and no
 * default value was specified, this will return 0.
 *
 * @param path Path of the long to get.
 * @return Requested long.
 */
public long getLong (@NotNull String path) { return this.config.getLong(path); }

/**
 * Gets the requested List by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return null.
 *
 * @param path Path of the List to get.
 * @return Requested List.
 */
public List<?> getList(@NotNull String path) { return this.config.getList(path); };

/**
 * Gets the requested List of String by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return an empty List.
 * <p>
 * This method will attempt to cast any values into a String if possible,
 * but may miss any values out if they are not compatible.
 *
 * @param path Path of the List to get.
 * @return Requested List of String.
 */
@NotNull
public List<String> getStringList(@NotNull String path){return this.config.getStringList(path);}

/**
 * Gets the requested List of Integer by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return an empty List.
 * <p>
 * This method will attempt to cast any values into a Integer if possible,
 * but may miss any values out if they are not compatible.
 *
 * @param path Path of the List to get.
 * @return Requested List of Integer.
 */
@NotNull
public List<Integer> getIntegerList(@NotNull String path){return this.config.getIntegerList(path);}

/**
 * Gets the requested List of Boolean by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return an empty List.
 * <p>
 * This method will attempt to cast any values into a Boolean if possible,
 * but may miss any values out if they are not compatible.
 *
 * @param path Path of the List to get.
 * @return Requested List of Boolean.
 */
@NotNull
public List<Boolean> getBooleanList(@NotNull String path){return this.config.getBooleanList(path);}

/**
 * Gets the requested List of Double by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return an empty List.
 * <p>
 * This method will attempt to cast any values into a Double if possible,
 * but may miss any values out if they are not compatible.
 *
 * @param path Path of the List to get.
 * @return Requested List of Double.
 */
@NotNull
public List<Double> getDoubleList(@NotNull String path){return this.config.getDoubleList(path);};

/**
 * Gets the requested List of Float by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return an empty List.
 * <p>
 * This method will attempt to cast any values into a Float if possible,
 * but may miss any values out if they are not compatible.
 *
 * @param path Path of the List to get.
 * @return Requested List of Float.
 */
@NotNull
public List<Float> getFloatList(@NotNull String path){return this.config.getFloatList(path);}

/**
 * Gets the requested List of Long by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return an empty List.
 * <p>
 * This method will attempt to cast any values into a Long if possible,
 * but may miss any values out if they are not compatible.
 *
 * @param path Path of the List to get.
 * @return Requested List of Long.
 */
@NotNull
public List<Long> getLongList(@NotNull String path){return this.config.getLongList(path);}

/**
 * Gets the requested List of Byte by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return an empty List.
 * <p>
 * This method will attempt to cast any values into a Byte if possible,
 * but may miss any values out if they are not compatible.
 *
 * @param path Path of the List to get.
 * @return Requested List of Byte.
 */
@NotNull
public List<Byte> getByteList(@NotNull String path){return this.config.getByteList(path);}

/**
 * Gets the requested List of Character by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return an empty List.
 * <p>
 * This method will attempt to cast any values into a Character if
 * possible, but may miss any values out if they are not compatible.
 *
 * @param path Path of the List to get.
 * @return Requested List of Character.
 */
@NotNull
public List<Character> getCharacterList(@NotNull String path) {return this.config.getCharacterList(path);}

/**
 * Gets the requested List of Short by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return an empty List.
 * <p>
 * This method will attempt to cast any values into a Short if possible,
 * but may miss any values out if they are not compatible.
 *
 * @param path Path of the List to get.
 * @return Requested List of Short.
 */
@NotNull
public List<Short> getShortList(@NotNull String path) {return this.config.getShortList(path);}

/**
 * Gets the requested List of Maps by path.
 * <p>
 * If the List does not exist but a default value has been specified, this
 * will return the default value. If the List does not exist and no
 * default value was specified, this will return an empty List.
 * <p>
 * This method will attempt to cast any values into a Map if possible, but
 * may miss any values out if they are not compatible.
 *
 * @param path Path of the List to get.
 * @return Requested List of Maps.
 */
@NotNull
public List<Map<?, ?>> getMapList(@NotNull String path) {return this.config.getMapList(path);}

/**
 * Gets the requested ConfigurationSection by path.
 * <p>
 * If the ConfigurationSection does not exist but a default value has been
 * specified, this will return the default value. If the
 * ConfigurationSection does not exist and no default value was specified,
 * this will return null.
 *
 * @param path Path of the ConfigurationSection to get.
 * @return Requested ConfigurationSection.
 */
public ConfigurationSection getConfigurationSection (@NotNull String path) { return this.config.getConfigurationSection(path); }

/**
 * Sets the specified path to the given value.
 * <p>
 * If value is null, the entry will be removed. Any existing entry will be
 * replaced, regardless of what the new value is.
 * </p>
 *
 * @param path Path of the object to set.
 * @param value New value to set the path to.
 */
public void set (String path, Object value) { this.config.set(path, value); }

/**
 * Converts an array of path nodes to a path string.
 * <p>
 * The final return value will be in the format <code>"element 0"."element 1"."element 2"...</code> and so on.
 * </p>
 *
 * @param pathNodes Names of the nodes of the path
 * @return The path string for other methods.
 */
protected String toPath (String @NotNull ... pathNodes) {
	// optimisation
	if (pathNodes.length == 1) return pathNodes[0];

	StringBuilder s = new StringBuilder(pathNodes[0]);
	for (int i = 1; i < pathNodes.length; i++) {
		s.append(".").append(pathNodes[i]);
	}
	return s.toString();
}

public void reload () {
	file = new File(plugin.getDataFolder(), name);
	try {
		config.load(file);
	}
	catch (Exception e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}
}
