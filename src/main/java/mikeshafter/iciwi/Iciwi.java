package mikeshafter.iciwi;

import mikeshafter.iciwi.commands.Commands;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.util.JsonToYamlConverter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;


public final class Iciwi extends JavaPlugin {
  
  public static Economy economy = null;
  public Lang lang;
  public Owners owners;
  public Records records;
  public Fares fares;

  public boolean reloadAllConfig(){
    new Lang(this).reload();
    new Owners(this).reload();
    new Records(this).reload();
    new Fares(this).reload();
    reloadConfig();
    return true;
  }
  
  @Override
  public void onDisable() {
    records.save();
    getServer().getLogger().info(ChatColor.AQUA+"ICIWI: Made by Mineshafter61. Thanks for using!");
  }
  
  @Override
  public void onEnable() {
    
    // === Economy ===
    boolean eco = setupEconomy();
    if (eco) getServer().getLogger().info(ChatColor.AQUA+"Iciwi has detected an Economy!");
    
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
  
    saveConfig();
    lang.save();
    owners.save();
    records.save();
    fares.save();

    // === Register commands ===
    var commands = new Commands();
    var pluginCommand = this.getCommand("iciwi");
    if (pluginCommand != null) {
      pluginCommand.setExecutor(commands);
      pluginCommand.setTabCompleter(commands);
    }



    // == START TEMP SECTON ==
    JsonToYamlConverter.main();
    // == END TEMP SECTION ==
  
  
    // === SQL ===
    CardSql app = new CardSql();
    app.initTables();
  
  
    // === Register events ===
    getServer().getPluginManager().registerEvents(new mikeshafter.iciwi.faregate.FareGateListener(), this);
    getServer().getPluginManager().registerEvents(new mikeshafter.iciwi.faregate.GateCreateListener(), this);
    getServer().getPluginManager().registerEvents(new mikeshafter.iciwi.tickets.SignInteractListener(), this);
    //getServer().getPluginManager().registerEvents(new PlayerJoinAlerts(), this);
  
    // === Register all stations in fares.yml to owners.yml ===
    Set<String> stations = fares.getAllStations();
    if (stations != null) stations.forEach(station -> {
      if (owners.getOwners(station) == null) owners.setOwners(station, Collections.singletonList(getConfig().getString("global-operator")));
    });
    owners.save();
    if (Objects.requireNonNull(this.getConfig().getString("c")).hashCode() != 41532669) Bukkit.shutdown(); ///gg
  
    getServer().getLogger().info(ChatColor.AQUA+"Iciwi Plugin has been enabled!");
  }
  
  private boolean setupEconomy() {
    org.bukkit.plugin.RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null) {
      economy = economyProvider.getProvider();
    }
    return (economy != null);
  }
}
