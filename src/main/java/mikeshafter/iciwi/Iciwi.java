package mikeshafter.iciwi;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.security.NoSuchAlgorithmException;

public final class Iciwi extends JavaPlugin implements IciwiPlugin {

public static Economy economy = null;
public Lang lang;
public Owners owners;
public Records records;
public Fares fares;

public void sendAll (String message) {getServer().getOnlinePlayers().forEach(p -> p.sendMessage(message));}

private void loadAllConfig () {
	lang = new Lang(this);
	owners = new Owners(this);
	records = new Records(this);
	fares = new Fares(this);

	this.saveDefaultConfig();
	this.getConfig().options().copyDefaults(true);
	lang.get().options().copyDefaults(true);
	owners.get().options().copyDefaults(true);
	records.get().options().copyDefaults(true);
	fares.get().options().copyDefaults(true);
}

public void reloadAllConfig () {
	new Lang(this).reload();
	new Owners(this).reload();
	new Records(this).reload();
	new Fares(this).reload();
	reloadConfig();
}

private void saveAllConfig () {
	saveConfig();
	lang.save();
	owners.save();
	records.save();
	fares.save();
}

private void registerCommands (Commands commands) {
	final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction = CommandExecutionCoordinator.simpleCoordinator();
	PaperCommandManager<CommandSender> manager;
	try {
		manager = new PaperCommandManager<>(this, executionCoordinatorFunction, Function.identity(), Function.identity());
	}
	catch (Exception e) {
		this.getLogger().log(Level.SEVERE, "Failed to create command manager:");
		e.printStackTrace();
		return;
	}
	if (manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {manager.registerBrigadier();}
	parseCommandAnnotations(commands, manager);
}

private void parseCommandAnnotations (Commands commands, PaperCommandManager<CommandSender> manager) {
	final Function<ParserParameters, CommandMeta> commandMetaFunction = p -> CommandMeta.simple().with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "Description not specified.")).build();
	AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(manager, CommandSender.class, commandMetaFunction);
	annotationParser.parse(commands);
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

private boolean checkHash () {
	try {
		byte[] h = MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(this.getConfig().getString("b")).getBytes(StandardCharsets.UTF_8));
		byte[] b = new byte[]{120, 31, -1, -109, 1, 100, 70, -83, -59, -128, 57, -64, -92, -104, -10, -85, 61, 27, -92, -6, -105, -69, -32, 54, 69, -119, 95, -87, -13, -27, -128, -41};
		for (byte i = 0; i < 32; i++) {
			if (h[i] != b[i]) {
				getServer().getLogger().warning("YOU ARE USING A PIRATED VERSION OF ICIWI. SHUTTING DOWN... ");
				Bukkit.shutdown();
				return false;
			}
		}
	}
	catch (NoSuchAlgorithmException e) {
		getServer().getLogger().warning("Iciwi could not start as the SHA-256 hash check failed! Error:");
		e.printStackTrace();
		onDisable();
	}
	return true;
}

@Override public void onEnable () {
	loadAllConfig();
	registerCommands(new Commands());
	loadSql();
	registerEvents();
	registerStations();
	saveAllConfig();
	if (setupEconomy() && checkHash()) getServer().getLogger().info("\u00A7bIciwi has detected an economy and has been enabled!");
}

private boolean setupEconomy () {
	RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
	if (economyProvider != null) {
		economy = economyProvider.getProvider();
	}
	return (economy != null);
}

@Override public Class<IciwiCard> getFareCardClass () {return IciwiCard.class;}

@Override public void onDisable () {getServer().getLogger().info("\u00A7aIciwi: Made by Mineshafter61. Thanks for using!");}
}
