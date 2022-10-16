package mikeshafter.iciwi.commands;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.tickets.TicketMachine;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


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
      return new CommandReturnValues(false, Arrays.asList("checkfare", "farechart", "getticket", "railpass", "reload", "coffers"));
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
  
        // todo Fare chart - deprecated, will replace with better menu
        case "farechart":
          if (sender instanceof Player) {
            try {
              String station = args[1];
              String page = args[2];
              new TicketMachine((Player) sender, station).checkFares_1(Integer.parseInt(page));
              return new CommandReturnValues(true);
            } catch (NumberFormatException e) {
              sender.sendMessage("Not a page!");
              return new CommandReturnValues(true);
            }
          }
    
          // todo Get ticket
        case "getticket":
          if (sender instanceof Player) {
            String from = args[1];
            String to = args[2];
            ItemStack item = new ItemStack(Material.PAPER, 1);
            ItemMeta itemMeta = item.getItemMeta();
            assert itemMeta != null;
            itemMeta.displayName(lang.getComponent("train-ticket"));
            itemMeta.lore(Arrays.asList(Component.text(from), Component.text(to)));
            item.setItemMeta(itemMeta);
            ((Player) sender).getInventory().addItem(item);
            return new CommandReturnValues(true);
          }
    
          // todo Rail Pass
        case "railpass":
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
            if (sender.hasPermission("iciwi.coffers.viewall")) {
              sender.sendMessage("=== COFFERS OF EVERY COMPANY ===");
              for (String company : Objects.requireNonNull(owners.getConfigurationSection("Coffers")).getKeys(false)) {
                sender.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+owners.get().getDouble("Coffers."+company));
              }
            } else {
              Player player = (Player) sender;
              sender.sendMessage("=== COFFERS OF YOUR COMPANIES ===");
              for (String company : owners.getOwnedCompanies(player.getName())) {
                sender.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+owners.get().getDouble("Coffers."+company));
              }
            }
            return new CommandReturnValues(true);
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