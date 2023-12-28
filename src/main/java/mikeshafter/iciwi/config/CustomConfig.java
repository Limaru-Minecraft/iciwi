package mikeshafter.iciwi.config;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomConfig {
private File file;
private final YamlConfiguration config;
protected final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
private final String name;

public CustomConfig (String name) { this(name, Iciwi.getPlugin(Iciwi.class)); }

public CustomConfig (String name, Plugin plugin) {
	this.name = name;
	file = new File(plugin.getDataFolder(), name);

	if (!file.exists()) {
		Logger logger = plugin.getLogger();
		logger.log(Level.INFO, file.getParentFile().mkdirs() ? "[Iciwi] New config file created" : "[Iciwi] Config file already exists, initialising files...");
		plugin.saveResource(name, false);
	}

	config = new YamlConfiguration();
	try { config.load(file); }
	catch (Exception e) { e.printStackTrace(); }
}

public void save () {
	try { config.save(file); }
	catch (Exception e) { e.printStackTrace(); }
}

protected Plugin getConfigPlugin () {return this.plugin;}

public void saveDefaultConfig () {
	if (!file.exists()) { plugin.saveResource(name, false); }
}

public File getFile () {return file;}

public YamlConfiguration get () {
	if (config == null) reload();
	return config;
}

public Object get (String path) {return this.config.get(path);}

public String getString (String path) {
	var s = this.config.getString(path);
	return s == null ? "" : s;
}

public boolean getBoolean (String path) { return this.config.getBoolean(path); }

public int getInt (String path) { return this.config.getInt(path); }

public double getDouble (String path) { return this.config.getDouble(path); }

public long getLong (String path) { return this.config.getLong(path); }

public ConfigurationSection getConfigurationSection (String path) { return this.config.getConfigurationSection(path); }

public void set (String path, Object value) { this.config.set(path, value); }

public void reload () {
	file = new File(plugin.getDataFolder(), name);
	try {
		config.load(file);
	}
	catch (Exception e) {
		e.printStackTrace();
	}
}
}
