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

/**
 * @see YamlConfiguration#save(File)
 */
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
 * @see YamlConfiguration#get(String)
 */
public Object get (String path) {return this.config.get(path);}

/**
 * @see YamlConfiguration#getString(String)
 */
public String getString (@NotNull String path) {
	var s = this.config.getString(path);
	return s == null ? "" : s;
}

/**
 * @see YamlConfiguration#getBoolean(String)
 */
public boolean getBoolean (@NotNull String path) { return this.config.getBoolean(path); }

/**
 * @see YamlConfiguration#getInt(String)
 */
public int getInt (@NotNull String path) { return this.config.getInt(path); }

/**
 * @see YamlConfiguration#getDouble(String)
 */
public double getDouble (@NotNull String path) { return this.config.getDouble(path); }

/**
 * @see YamlConfiguration#getLong(String)
 */
public long getLong (@NotNull String path) { return this.config.getLong(path); }

/**
 * @see YamlConfiguration#getList(String)
 */
public List<?> getList(@NotNull String path) { return this.config.getList(path); };

/**
 * @see YamlConfiguration#getStringList(String)
 */
@NotNull
public List<String> getStringList(@NotNull String path){return this.config.getStringList(path);}

/**
 * @see YamlConfiguration#getIntegerList(String)
 */
@NotNull
public List<Integer> getIntegerList(@NotNull String path){return this.config.getIntegerList(path);}

/**
 * @see YamlConfiguration#getBooleanList(String)
 */
@NotNull
public List<Boolean> getBooleanList(@NotNull String path){return this.config.getBooleanList(path);}

/**
 * @see YamlConfiguration#getDoubleList(String)
 */
@NotNull
public List<Double> getDoubleList(@NotNull String path){return this.config.getDoubleList(path);};

/**
 * @see YamlConfiguration#getFloatList(String)
 */
@NotNull
public List<Float> getFloatList(@NotNull String path){return this.config.getFloatList(path);}

/**
 * @see YamlConfiguration#getLongList(String)
 */
@NotNull
public List<Long> getLongList(@NotNull String path){return this.config.getLongList(path);}

/**
 * @see YamlConfiguration#getByteList(String)
 */
@NotNull
public List<Byte> getByteList(@NotNull String path){return this.config.getByteList(path);}

/**
 * @see YamlConfiguration#getCharacterList(String)
 */
@NotNull
public List<Character> getCharacterList(@NotNull String path) {return this.config.getCharacterList(path);}

/**
 * @see YamlConfiguration#getShortList(String)
 */
@NotNull
public List<Short> getShortList(@NotNull String path) {return this.config.getShortList(path);}

/**
 * @see YamlConfiguration#getMapList(String)
 */
@NotNull
public List<Map<?, ?>> getMapList(@NotNull String path) {return this.config.getMapList(path);}

/**
 * @see YamlConfiguration#getConfigurationSection(String)
 */
public ConfigurationSection getConfigurationSection (@NotNull String path) { return this.config.getConfigurationSection(path); }

/**
 * @see YamlConfiguration#set(String, Object)
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
protected String toPath (@NotNull String... pathNodes) {
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
