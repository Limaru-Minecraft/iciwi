package mikeshafter.iciwi.commands;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.config.Records;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Commands implements TabExecutor {
  
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Lang lang = new Lang();
  private final Records records = new Records();
  private final Fares fares = new Fares();
  private final Owners owners = new Owners();
  
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
  
      switch (args[0]) {
        // Check Fare
        case "checkfare":
          switch (length) {
            case 1:
            case 2:
              return new CommandReturnValues(false, List.copyOf(fares.getAllStations()));
            case 3:
              if (fares.getFare(args[2], args[3]) > 0d) {
                sender.sendMessage("Fare from "+args[1]+" to "+args[2]+": "+fares.getFare(args[1], args[2]));
                return new CommandReturnValues(true, List.copyOf(fares.getClasses(args[1], args[2])));
              } else {
                return new CommandReturnValues(false, List.copyOf(fares.getClasses(args[1], args[2])));
              }
            case 4:
              if (execute)
                sender.sendMessage(args[3]+" fare from "+args[1]+" to "+args[2]+": "+fares.getFare(args[1], args[2], args[3]));
              return new CommandReturnValues(true, null);
          }
          break;
    
    // Fare chart
        case "farechart":
          switch (length) {
            case 1:
              return new CommandReturnValues(false, List.copyOf(fares.getAllStations()));
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
          plugin.reloadConfig();
          fares.reload();
          lang.reload();
          owners.reload();
          break;
    
    // Coffers
        case "coffers":
          if (length == 1) {
            return sender instanceof Player ? new CommandReturnValues(Arrays.asList("empty", "view")) : new CommandReturnValues(Collections.singletonList("view"));
          } else if (length == 2 && args[1].equals("empty") && sender instanceof Player player) {
            // Get the companies the player owns
            List<String> companies = owners.getOwnedCompanies(player.getName());
            if (execute) {
              for (String company : companies) {
                double coffer = owners.getCoffers(company);
                sender.sendMessage(String.format("Received $%.2f from %s", coffer, company));
                Iciwi.economy.depositPlayer(player, coffer);
                owners.setCoffers(company, 0d);
              }
            }
            return new CommandReturnValues(true, companies);
          } else if (length == 3 && args[1].equals("empty") && sender instanceof Player player) {
            if (execute && owners.getOwnership(player.getName(), args[3])) {
              Iciwi.economy.depositPlayer(player, owners.getCoffers(args[2]));
              owners.setCoffers(args[2], 0d);
              return new CommandReturnValues(true);
            }
          } else if (length == 2 && args[2].equals("view")) {
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