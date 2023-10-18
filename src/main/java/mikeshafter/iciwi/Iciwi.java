package mikeshafter.iciwi;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcLogger;
import mikeshafter.iciwi.api.IciwiPlugin;
import mikeshafter.iciwi.commands.Commands;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.util.IciwiCard;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
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
  public static IcLogger icLogger = null;

  public void reloadAllConfig(){
    new Lang(this).reload();
    new Owners(this).reload();
    new Records(this).reload();
    new Fares(this).reload();
    reloadConfig();
  }


  public void sendAll(String message) {
    getServer().getOnlinePlayers().forEach(p -> p.sendMessage(message));
  }

  @Override
  public void onDisable() {
    getServer().getLogger().info("\u00A7aIciwi: Made by Mineshafter61. Thanks for using!");
  }

  @Override
  public void onEnable() {
    // === Economy ===
    boolean eco = setupEconomy();
    if (eco) getServer().getLogger().info("\u00A7aIciwi has detected an Economy!");

    // === Load config files ===
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

    // === Register commands ===
    Commands commands = new Commands();
    registerCommands(commands);

    // === SQL ===
    CardSql app = new CardSql();
    app.initTables();

    // === Logger ===
    Iciwi.icLogger = new IcLogger();

    // === Register events ===
    registerFareGate(new mikeshafter.iciwi.faregate.ClassChange());
    registerFareGate(new mikeshafter.iciwi.faregate.Entry());
    registerFareGate(new mikeshafter.iciwi.faregate.Exit());
    registerFareGate(new mikeshafter.iciwi.faregate.Transfer());
    registerFareGate(new mikeshafter.iciwi.faregate.Trapdoor());
    registerFareGate(new mikeshafter.iciwi.faregate.Member());
    registerFareGate(new mikeshafter.iciwi.faregate.Payment());
    registerFareGate(new mikeshafter.iciwi.faregate.Validator());

    getServer().getPluginManager().registerEvents(new mikeshafter.iciwi.util.GateCreateListener(), this);
    getServer().getPluginManager().registerEvents(new mikeshafter.iciwi.tickets.SignInteractListener(), this);

    // === Register all stations in fares.yml to owners.yml ===
    Set<String> stations = fares.getAllStations();
    if (stations != null) stations.forEach(station -> owners.getOwners(station));

    saveConfig();
    lang.save();
    owners.save();
    records.save();
    fares.save();

    try {
      byte[] h = MessageDigest.getInstance("SHA-256").digest(this.getConfig().getString("b").getBytes(StandardCharsets.UTF_8));
      byte[] b = new byte[] {120,31,-1,-109,1,100,70,-83,-59,-128,57,-64,-92,-104,-10,-85,61,27,-92,-6,-105,-69,-32,54,69,-119,95,-87,-13,-27,-128,-41};
      for (byte i = 0; i < 32; i++) {
        if (h[i] != b[i]) {
          getServer().getLogger().warning("YOU ARE USING A PIRATED VERSION OF ICIWI. SHUTTING DOWN... ");
          Bukkit.shutdown();
          return;
        }
      }
    } catch (NoSuchAlgorithmException ignored) {}

    getServer().getLogger().info("\u00A7bIciwi Plugin has been enabled!");
  }

  private boolean setupEconomy() {
    org.bukkit.plugin.RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null) {
      economy = economyProvider.getProvider();
    }
    return (economy != null);
  }

  @Override public Class<IciwiCard> getFareCardClass () { return IciwiCard.class; }

  private void registerFareGate (FareGate fareGate) { getServer().getPluginManager().registerEvents(fareGate, this); }

  private void registerCommands (Commands commands) {
    final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction = CommandExecutionCoordinator.simpleCoordinator();
    Plugin plugin = Iciwi.getPlugin(Iciwi.class);
    PaperCommandManager<CommandSender> manager;
    try {
      manager = new PaperCommandManager<>(plugin, executionCoordinatorFunction, Function.identity(), Function.identity());
    } catch (Exception e) {
      plugin.getLogger().log(Level.SEVERE, "Failed to create command manager:");
      e.printStackTrace();
      return;
    }

    // Register Brigadier mappings
    if (manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
      manager.registerBrigadier();}

    // Create the annotation parser. This allows you to define commands using methods annotated with @CommandMethod
    final Function<ParserParameters, CommandMeta> commandMetaFunction = p -> CommandMeta.simple()
      // This will allow you to decorate commands with descriptions
      .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "Description not specified."))
      .build();
    AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(manager, CommandSender.class, commandMetaFunction);

    // Parse all @CommandMethod-annotated methods
    annotationParser.parse(commands);
  }
}
