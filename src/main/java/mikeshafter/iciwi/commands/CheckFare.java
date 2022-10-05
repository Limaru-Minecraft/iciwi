package mikeshafter.iciwi.commands;
import org.bukkit.command.TabExecutor;
import mikeshafter.iciwi.Iciwi;
import java.util.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import mikeshafter.iciwi.config.*;
import org.bukkit.plugin.Plugin;

public class CheckFare implements TabExecutor {
  Fares fares = new Fares();

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender.hasPermission("iciwi.checkfare") && args.length == 2) {
      try {
        String from = args[0];
        String to = args[1];
        double fare = fares.getFare(from, to);
        sender.sendMessage(String.format("Train fare from %s to %s: £%.2f", from, to, fare));
        return true;
      } catch (Exception e) {
        sender.sendMessage("Error while checking fare.");
        return true;
      }
    }
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (sender.hasPermission("iciwi.checkfare") && args.length <= 2) {
      return fares.getAllStations().stream().toList();
    }
  }
}