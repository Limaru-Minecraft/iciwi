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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;


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
    }
    
    // Check Fare
    
    
    // Fare chart
    
    
    // Get ticket
    
    
    // Rail Pass
    
    
    // Redeem Card
    
    
    // Reload Config
    
    
    // Coffers
    
    
    // Odometer - Deprecated
    
    
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
}