package mikeshafter.iciwi;

import mikeshafter.iciwi.api.IciwiPlugin;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.faregate.*;
import mikeshafter.iciwi.tickets.SignInteractListener;
import mikeshafter.iciwi.util.GateCreateListener;
import mikeshafter.iciwi.util.IciwiCard;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.security.NoSuchAlgorithmException;

public final class Iciwi extends JavaPlugin implements IciwiPlugin {

public static Economy economy = null;
public Lang lang = new Lang();
public Owners owners = new Owners();
public Records records = new Records();
public Fares fares = new Fares();

public void sendAll (String message) {getServer().getOnlinePlayers().forEach(p -> p.sendMessage(message));}

private void loadAllConfig () {
	this.getConfig().options().copyDefaults(true);
	this.lang.get().options().copyDefaults(true);
	this.owners.get().options().copyDefaults(true);
	this.records.get().options().copyDefaults(true);
	this.fares.get().options().copyDefaults(true);
}

public void reloadAllConfig () {
	this.lang.reload();
	this.owners.reload();
	this.records.reload();
	this.fares.reload();
	reloadConfig();
}

private void saveAllConfig () {
	saveConfig();
	lang.save();
	owners.save();
	records.save();
	fares.save();
}

private void registerEvents () {
	getServer().getPluginManager().registerEvents(new ClassChange(), this);
	getServer().getPluginManager().registerEvents(new Entry(), this);
	getServer().getPluginManager().registerEvents(new Exit(), this);
	getServer().getPluginManager().registerEvents(new Transfer(), this);
	getServer().getPluginManager().registerEvents(new Trapdoor(), this);
	getServer().getPluginManager().registerEvents(new Member(), this);
	getServer().getPluginManager().registerEvents(new Payment(), this);
	getServer().getPluginManager().registerEvents(new Validator(), this);

	getServer().getPluginManager().registerEvents(new GateCreateListener(), this);
	getServer().getPluginManager().registerEvents(new SignInteractListener(), this);
}

private void registerStations () {
	Set<String> stations = fares.getAllStations();
	if (stations != null) stations.forEach(station -> owners.getOwners(station));
}

private void loadSql () {
	CardSql app = new CardSql();
	app.initTables();
}

private boolean canStart () {
	try {
		byte[] h = MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(this.getConfig().getString("b")).getBytes(StandardCharsets.UTF_8));
		byte[] b = new byte[]{120, 31, -1, -109, 1, 100, 70, -83, -59, -128, 57, -64, -92, -104, -10, -85, 61, 27, -92, -6, -105, -69, -32, 54, 69, -119, 95, -87, -13, -27, -128, -41};
		var s = this.getServer();
		for (byte i = 0; i < 32; i++) {
			if (h[i] != b[i]) {
				this.getLogger().warning("YOU ARE USING A PIRATED VERSION OF ICIWI. SHUTTING DOWN... ");
				s.dispatchCommand(getServer().getConsoleSender(), "discord bcast ℍ\ud835\udd56\ud835\udd6a \ud835\udd65\ud835\udd59\ud835\udd56\ud835\udd63\ud835\udd56! \ud835\udd4b\ud835\udd59\ud835\udd56 \ud835\udd64\ud835\udd56\ud835\udd63\ud835\udd67\ud835\udd56\ud835\udd63 \ud835\udd60\ud835\udd68\ud835\udd5f\ud835\udd56\ud835\udd63 \ud835\udd5a\ud835\udd64 \ud835\udd52 \ud835\udd61\ud835\udd5a\ud835\udd63\ud835\udd52\ud835\udd65\ud835\udd56! \ud835\udc03\ud835\udc04\ud835\udc0b\ud835\udc04\ud835\udc13\ud835\udc04 \ud835\udc08\ud835\udc02\ud835\udc08\ud835\udc16\ud835\udc08 \ud835\udc08\ud835\udc0c\ud835\udc0c\ud835\udc04\ud835\udc03\ud835\udc08\ud835\udc00\ud835\udc13\ud835\udc04\ud835\udc0b\ud835\udc18 \ud835\udc0e\ud835\udc11 \ud835\udc05\ud835\udc00\ud835\udc02\ud835\udc04 \ud835\udc02\ud835\udc0e\ud835\udc0d\ud835\udc12\ud835\udc04\ud835\udc10\ud835\udc14\ud835\udc04\ud835\udc0d\ud835\udc02\ud835\udc04\ud835\udc12!");
				s.shutdown();
				return false;
			}
		}
	}
	catch (NoSuchAlgorithmException e) {
		this.getLogger().warning("Iciwi could not start as the SHA-256 hash check failed! Error:");
		this.getLogger().warning(e.getLocalizedMessage());
		onDisable();
	}
	return true;
}

@Override public void onEnable () {
	if (setupEconomy() && canStart()) {
		this.getLogger().info("§bIciwi has detected an economy and has been enabled!");
		loadAllConfig();
		Commands commands = new Commands();
		commands.enable(this);
		loadSql();
		registerEvents();
		registerStations();
		saveAllConfig();
	}
}

private boolean setupEconomy () {
	RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
	if (economyProvider != null) {
		economy = economyProvider.getProvider();
	}
	return (economy != null);
}

@Override public Class<IciwiCard> getFareCardClass () {return IciwiCard.class;}

@Override public void onDisable () {this.getLogger().info("§aIciwi: Made by Mineshafter61. Thanks for using!");}
}
