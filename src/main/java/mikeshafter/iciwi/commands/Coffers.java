package mikeshafter.iciwi.commands;
import org.bukkit.command.TabExecutor;
import mikeshafter.iciwi.Iciwi;
import java.util.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import mikeshafter.iciwi.config.*;
import org.bukkit.plugin.Plugin;

public class Coffers implements TabExecutor {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  Owners owners = new Owners();

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("coffers") && sender.hasPermission("iciwi.coffers")) {
      if (args.length == 2 && args[0].equals("empty") && sender instanceof Player player) {
        // Check if the player owns the company
        String ownerName = owners.get().getString("Aliases."+args[1]);
        if (player.getName().equalsIgnoreCase(ownerName)) {
          // Empty coffers and deposit in player's wallet
          plugin.economy.depositPlayer(player, owners.get().getDouble("Coffers."+args[1]));
          owners.get().set("Coffers."+args[1], 0.0);
          return true;
        }
      } 
      else if (args.length == 1 && args[0].equals("empty") && sender instanceof Player player) {
        // Check if the player owns the company
        for (String company : Objects.requireNonNull(owners.get().plugin.getConfigurationSection("Aliases")).getKeys(false)) {
          if (Objects.requireNonNull(owners.get().getString("Aliases."+company)).equalsIgnoreCase(player.getName())) {
            double coffer = owners.get().getDouble("Coffers."+company);
            sender.sendMessage(String.format("Received £%.2f from %s", coffer, company));
            plugin.economy.depositPlayer(player, coffer);
            owners.get().set("Coffers."+company, 0.0);
          }
        }
        return true;
      } 
      else if (args.length == 1 && args[0].equals("view")) {
        if (sender.hasPermission("iciwi.coffers.viewall")) {
          sender.sendMessage("=== COFFERS OF EVERY COMPANY ===");
          for (String company : Objects.requireNonNull(owners.get().plugin.getConfigurationSection("Coffers")).getKeys(false)) {
            sender.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+owners.get().getDouble("Coffers."+company));
          }
        } else {
          Player player = (Player) sender;
          sender.sendMessage("=== COFFERS OF YOUR COMPANIES ===");
          for (String company : Objects.requireNonNull(owners.get().plugin.getConfigurationSection("Aliases")).getKeys(false)) {
            if (Objects.requireNonNull(owners.get().getString("Aliases."+company)).equalsIgnoreCase(player.getName())) {
              sender.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+owners.get().getDouble("Coffers."+company));
            }
          }
        }
        return true;
      }
    }
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) 
    
  } 
}