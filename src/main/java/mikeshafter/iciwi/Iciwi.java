package mikeshafter.iciwi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class Iciwi extends JavaPlugin implements Listener, CommandExecutor{
  
  @Override
  public void onEnable(){
    getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "ICIWI Plugin has been invoked!");
    loadConfig();
    getConfig().set("Stations.ESE.ESE", "0");
    getServer().getPluginManager().registerEvents(new events(), this);
  }
  
  @Override
  public void onDisable(){
    getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "ICIWI Plugin has been disabled!");
  }
  
  public void loadConfig() {
    getConfig().options().copyDefaults(true);
    saveConfig();
  }
  
  private static boolean isDouble(final String str) {
    if (str == null || str.length() == 0) {return false;}  // String not present
    for (char c : str.toCharArray()) {  // Check every char
      if (!Character.isDigit(c) | c != '.') {return false;}  // Check if the char is not a digit or decimal
    }
    return true;  //
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equalsIgnoreCase("checkfare")){
      if (args.length != 2){
        sender.sendMessage("Correct usage: /checkfare <from> <to>"); return false;
      } else {
        if (isDouble(args[0]) && isDouble(args[1])) {
          Double price = Double.parseDouble(getConfig().getString("Stations." + args[0] + "." + args[1]));
          sender.sendMessage(ChatColor.AQUA + "Fare from " + args[0] + " to " + args[1] + ": " + price);
          return true;
        } else {return false;}
      }
      
    } else if (cmd.getName().equalsIgnoreCase("ticket")){
      if (!(sender instanceof Player)){
        sender.sendMessage("This command can only be run by a player."); return false;
      } else if (args.length != 2){
        sender.sendMessage("Correct usage: /ticket <from> <to>"); return false;
      } else {
        Player player = (Player) sender;
        // Item format:
        //   Name: Train Ticket
        //   Lore1: From » To
        ItemStack ticket = new ItemStack(Material.PAPER);
        ItemMeta ticketMeta = ticket.getItemMeta();
        ticketMeta.setDisplayName(ChatColor.AQUA + "Train Ticket");
        ticketMeta.setLore(Arrays.asList(ChatColor.GOLD + args[0] + "»" + args[1], ChatColor.GREEN + "Valid for one (1) journey only."));
        player.getInventory().addItem(ticket);
      }
    }
    return false;
  }
}
