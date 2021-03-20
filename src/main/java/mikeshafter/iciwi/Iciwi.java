package mikeshafter.iciwi;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public final class Iciwi extends JavaPlugin implements CommandExecutor{
  
  public static Economy economy = null;
  public boolean destroy = true;
  
  @Override
  public void onDisable(){
    saveConfig();
    getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"ICIWI: Made by Mineshafter61. Thanks for using!");
  }

  @Override
  public void onEnable(){ // Use config to store station names and fares
    
    // === Economy ===
    boolean eco = setupEconomy();
    
    // === Config ===
    getConfig().options().copyDefaults(true);
    saveConfig();
    
    // === Register events ===
    getServer().getPluginManager().registerEvents(new EventSigns(), this);
    getServer().getPluginManager().registerEvents(new CustomInventory(), this);
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    
    // === Destroy minecarts ===
    Bukkit.dispatchCommand(console, "train destroyall");
    Bukkit.dispatchCommand(console, "ekillall minecarts world");
  
    // === Periodic train destroyer ===
    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
      public void run(){
        Bukkit.broadcastMessage(ChatColor.GREEN+"§b[§aICIWI§b] §fTrains will be destroyed in 1 minute!");
      }
    }, 72000, 216000);
    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
      public void run(){
        if (destroy){
          ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
          Bukkit.broadcastMessage(ChatColor.GREEN+"§b[§aICIWI§b] §fTrains have been destroyed!");
          Bukkit.dispatchCommand(console, "train destroyall");
          Bukkit.dispatchCommand(console, "ekillall minecarts world");
        } else {
          destroy = true;
          Bukkit.broadcastMessage(ChatColor.GREEN+"§b[§aICIWI§b] §fTrainDestroy rescheduled, trains will be destroyed in the next cycle.");
        }}}, 73200, 216000);

    // === SQL ===
    CardSql app = new CardSql();
    app.initTables(new String[] {"Entetsu", "Lipan"});

    getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"ICIWI Plugin has been invoked!");
  }
  
  
  private boolean setupEconomy(){
    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null){
      economy = economyProvider.getProvider();
    }
    return (economy != null);
  }
  
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
    if (command.getName().equalsIgnoreCase("checkfare")){
      try{
        String from = args[0];
        String to = args[1];
        double fare = JsonManager.getJson(to, from);
        sender.sendMessage("Train fare from "+from+" to "+to+": "+fare);
        return true;
      } catch (Exception e){
        sender.sendMessage("Error while checking fare.");
      }
    } else if (command.getName().equalsIgnoreCase("ticketmachine")){
      if (sender instanceof Player && !args[0].isEmpty()){
        Player player = (Player) sender;
        String station = args[0];
        CustomInventory inventory = new CustomInventory();
        inventory.newTM(player, station);
        return true;
      } else {
        sender.sendMessage("Usage: /ticketmachine <station>");
        return false;
      }
    } else if (command.getName().equalsIgnoreCase("traindestroydelay")){
      destroy = false;
      sender.sendMessage("§b[§aICIWI§b] §fTrainDestroy rescheduled.");
      return true;
    }
    return false;
  }
}
