package mikeshafter.iciwi.commands;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Records;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class Commands implements TabExecutor {
  
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  public Lang lang;
  public Records records;
  public Fares fares;
  private final HashMap<Player, Queue<Integer>> statMap = new HashMap<>();
  
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    return inCommand(sender, command, label, args, true).execute();
  }
  
  private CommandReturnValues inCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, boolean execute) {
    // args.length drivers
    int length = args.length-1;
    
    // initial method
    if (length == 0) {
      return new CommandReturnValues(false, Arrays.asList("checkfare", "farechart", "getticket", "railpass", "redeemcard", "reload", "coffers"));
    } else {

      switch (args[1]) {
    // Check Fare
        case "checkfare":
          switch (length) {
            case 1 -> return new CommandReturnValues(false, fares.getAllStations().toList());
            case 2 -> return new CommandReturnValues(false, fares.getAllStations().toList());
            case 3:
              if (fares.getFare(args[2], args[3] > 0)){sender.sendMessage(fares.getFare(args[1], args[2]));
              return new CommandReturnValues(true, fares.getClasses(args[1], args[2]));
              break;}
              else {return new CommandReturnValues(false, fares.getClasses(args[1], args[2]).toList();break;}
            case 4:
              if (execute) sender.sendMessage(fares.getFare(args[1], args[2], args[3]));
              return new CommandReturnValues(true, null);
              break;
          }
          break;
    
    // Fare chart
        case "farechart":
          switch (length) {
            case 1 -> return new CommandReturnValues(false, fares.getAllStations().toList());
            //TODO: Copy from Github branch
          }
          break;
    
    // Get ticket
        case "getticket":
          break;
    
    // Rail Pass
    case "railpass":
          break;
    
    // Redeem Card
    case "redeemcard":
          break;
    
    // Reload Config
        case "reload":
          plugin.getConfig.reload();
          fares.reload();
          lang.reload();
          owners.reload();
    
    // Coffers
        case "coffers":
          if (length == 1) {
            return sender instanceof Player ? new CommandReturnValues(Arrays.asList("empty", "view")) : new CommandReturnValues(Collections.singletonList("view"));
          }
            
          else if (length == 2 && args[2].equals("empty") && sender instanceof Player player) {
            // Get the companies the player owns
            List<String> companies = owners.getOwnedCompanies(player.getName());
            if (execute) {
              for (String company : companies) {
                double coffer = owners.getCoffers(company);
                sender.sendMessage(String.format("Received $%.2f from %s", coffer, company));
                plugin.economy.depositPlayer(player, coffer);
                owners.setCoffers(company, 0d);
              }
            }
            return new CommandReturnValues(true, companies);
          }

          else if (length == 3 && args[2].equals("empty") && sender instanceof Player player) {
            if (execute && owners.getOwnership(player.getName(), args[3])) {
              plugin.economy.depositPlayer(player, owners.getCoffers(company);
              owners.setCoffers(company, 0d);
              return new CommandReturnValues(true);
            }
          }

          else if (length == 2 && args[2].equals("view")) {
            //TODO
          }
          break;
          
      }
    }
    return new CommandReturnValues();
  }
  
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    return inCommand(sender, command, label, args, false).tabComplete();
  }
  
}


record CommandReturnValues(boolean execute, List<String> tabComplete) {
  public CommandReturnValues() {
    this(false, null);
  }

  public CommandReturnValues(boolean execute) {
    this(execute, null);
  }

  public CommandReturnValues(List<String> tabComplete) {
    this(false, tabComplete);
  }
}