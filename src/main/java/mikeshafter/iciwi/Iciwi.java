package mikeshafter.iciwi;

// import net.milkbowl.vault.chat.Chat;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public final class Iciwi extends JavaPlugin implements Listener, CommandExecutor{

//  private static boolean isDouble(final String str){
//    if (str == null || str.length() == 0){
//      return false;
//    }  // String not present
//    for (char c : str.toCharArray()){  // Check every char
//      if (!Character.isDigit(c) | c != '.'){
//        return false;
//      }  // Check if the char is not a digit or decimal
//    }
//    return true;
//  }
  
  // Vault setup code starts
// ==================================
//  public static Permission permission = null;
  public static Economy economy = null;
  
  @Override
  public void onDisable(){
    saveConfig();
    getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"ICIWI Plugin has been disabled!");
  }
  
  @Override
  public void onEnable(){ // Use config to store station names and fares
    boolean eco = setupEconomy();
    getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"ICIWI Plugin has been invoked! Economy status: "+eco);
  
    getConfig().options().copyDefaults(true);
    saveConfig();
    getServer().getPluginManager().registerEvents(new events(), this);
  }
//  public static Chat chat = null;

//  private boolean setupPermissions()
//  {
//    RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
//    if (permissionProvider != null) {
//      permission = permissionProvider.getProvider();
//    }
//    return (permission != null);
//  }

//  private boolean setupChat()
//  {
//    RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
//    if (chatProvider != null) {
//      chat = chatProvider.getProvider();
//    }
//
//    return (chat != null);
//  }
  
  private boolean setupEconomy(){
    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null){
      economy = economyProvider.getProvider();
    }
    
    return (economy != null);
  }
// ==================================
// Vault setup code ends
  
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
    if (command.getName().equalsIgnoreCase("checkfare")){
      try{
        String from = args[0];
        String to = args[1];
        double fare = JSONmanager.getjson(to, from);
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
        inventory.newTicket(player, station);
        return true;
      
      } else {
        sender.sendMessage("Usage: /ticketmachine <station>");
        return false;
      }
    } else {
      return true;
    }
    return false;
  }
}
