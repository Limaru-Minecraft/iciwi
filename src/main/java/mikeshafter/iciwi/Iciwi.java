package mikeshafter.iciwi;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.ArrayList;


public final class Iciwi extends JavaPlugin implements CommandExecutor, TabCompleter {
  
  public static Economy economy = null;
  public boolean destroy = true;
  
  @Override
  public void onDisable() {
    saveConfig();
    getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"ICIWI: Made by Mineshafter61. Thanks for using!");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("checkfare") && sender.hasPermission("iciwi.checkfare")) {
      try {
        String from = args[0];
        String to = args[1];
        double fare = JsonManager.getFare(from, to);
        sender.sendMessage("Train fare from "+from+" to "+to+": "+fare);
        return true;
      } catch (Exception e) {
        sender.sendMessage("Error while checking fare.");
      }
      
    }
    
    else if (command.getName().equalsIgnoreCase("ticketmachine") && sender.hasPermission("iciwi.ticketmachine")) {
      if (sender instanceof Player && !args[0].isEmpty()) {
        Player player = (Player) sender;
        String station = args[0];
        TicketMachine inventory = new TicketMachine();
        inventory.newTM(player, station);
        return true;
      } else {
        sender.sendMessage("Usage: /ticketmachine <station>");
        return false;
      }
      
    }
    
    else if (command.getName().equalsIgnoreCase("newdiscount") && sender.hasPermission("iciwi.newdiscount")) {
      // newdiscount <serial> <operator> <days before expiry>
      if (args.length == 3) {
        long expiry = Long.parseLong(args[2])*86400+Instant.now().getEpochSecond();
        new CardSql().setDiscount(args[0], args[1], expiry);
        return true;
      }
      
    }
    
    else if (command.getName().equalsIgnoreCase("redeemcard") && sender.hasPermission("iciwi.redeemcard")) {
      if (sender instanceof Player && !args[0].isEmpty()) {
        Player player = (Player) sender;
        int serial = Integer.parseInt(args[0].substring(3));
        // Check the checksum
        char sum = new char[] {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[
                       ((serial%10)*2+(serial/10%10)*3+(serial/100%10)*5+(serial/1000%10)*7+(serial/10000)*9)%19
                       ];
        if (args[0].charAt(1) == sum) {
          // Generate and place card into player's inventory
          ItemStack card = new ItemStack(Material.NAME_TAG, 1);
          ItemMeta cardMeta = card.getItemMeta();
          assert cardMeta != null;
          cardMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");
          new CardSql().newCard("I"+sum+serial, 0.0);
          ArrayList<String> lore = new ArrayList<>();
          lore.add("Serial number:");
          lore.add(args[0]);
          cardMeta.setLore(lore);
          card.setItemMeta(cardMeta);
          player.getInventory().addItem(card);
          player.closeInventory();
          player.sendMessage(ChatColor.GREEN+"Card redeemed.");
          return true;
        } else {
          player.sendMessage(ChatColor.RED+"Wrong checksum!");
          return true;
        }
      }
    } else if (command.getName().equalsIgnoreCase("reloadconfig") && sender.hasPermission("iciwi.reload")) {
      StationOwners.reload();
      return true;
    } else if (command.getName().equalsIgnoreCase("coffers") && sender.hasPermission("iciwi.coffers")) {
      if (args.length == 2 && args[0].equals("empty") && sender instanceof Player) {
        Player player = (Player) sender;
        // Check if the player owns the company
        String ownerName = StationOwners.get().getString("Aliases."+args[1]);
        if (player.getName().equalsIgnoreCase(ownerName)) {
          // Empty coffers and deposit in player's wallet
          economy.depositPlayer(player, StationOwners.get().getDouble("Coffers."+args[1]));
          StationOwners.get().set("Coffers."+args[1], 0.0);
        }
      } else if (args.length == 1 && args[0].equals("empty") && sender instanceof Player) {
        Player player = (Player) sender;
        // Check if the player owns the company
        for (String company : StationOwners.get().getConfigurationSection("Aliases").getKeys(false)) {
          if (StationOwners.get().getString("Aliases."+company).equalsIgnoreCase(player.getName())) {
            economy.depositPlayer(player, StationOwners.get().getDouble("Coffers."+company));
            StationOwners.get().set("Coffers."+company, 0.0);
          }
        }
      } else if (args.length == 1 && args[0].equals("view")) {
        if (sender.hasPermission("iciwi.coffers.viewall")) {
          sender.sendMessage("=== COFFERS OF EVERY COMPANY ===");
          for (String company : StationOwners.get().getConfigurationSection("Coffers").getKeys(false)) {
            sender.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+StationOwners.get().getDouble("Coffers."+company));
          }
        } else {
          Player player = (Player) sender;
          sender.sendMessage("=== COFFERS OF YOUR COMPANIES ===");
          for (String company : StationOwners.get().getConfigurationSection("Aliases").getKeys(false)) {
            if (StationOwners.get().getString("Aliases."+company).equalsIgnoreCase(player.getName())) {
              sender.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+StationOwners.get().getDouble("Coffers."+company));
            }
          }
        }
      }
    }
    
    return false;
  }
  
  // === Config ===
  
  @Override
  public void onEnable() { // Use config to store station names and fares
    
    // === Economy ===
    boolean eco = setupEconomy();
    
    // === Register events ===
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    getServer().getPluginManager().registerEvents(new TicketMachine(), this);
    getServer().getPluginManager().registerEvents(new TBarrier(), this);
  
    // === SQL ===
    CardSql app = new CardSql();
    app.initTables();
  
    // === Load station operator list ===
    StationOwners.setup();
    getConfig().options().copyDefaults(true);
    StationOwners.get().options().copyDefaults(true);
  
    // owners.yml teacher
    StationOwners.get().addDefault("Aliases.ExampleOperator", "ExampleUsername");
    StationOwners.get().addDefault("Operators.ExampleStation", "ExampleOperator");
    StationOwners.get().addDefault("Coffers.ExampleOperator", 0.0);
  
    saveConfig();
    StationOwners.save();
  
    getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"ICIWI Plugin has been invoked!");
  }
  
  private boolean setupEconomy() {
    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null) {
      economy = economyProvider.getProvider();
    }
    return (economy != null);
  }
}
