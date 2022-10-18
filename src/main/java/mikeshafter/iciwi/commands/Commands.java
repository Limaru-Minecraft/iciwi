package mikeshafter.iciwi.commands;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class Commands implements TabExecutor {
  
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Lang lang = new Lang();
  private final Fares fares = new Fares();
  private final Owners owners = new Owners();
  
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    return inCommand(sender, command, label, args, true).execute();
  }
  
  private CmdRntVal inCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, boolean execute) {
    // args.length drivers
    int length = args.length-1;
    
    // initial method
    if (length == 0) {
      return new CmdRntVal(false, Arrays.asList("checkfare", "farechart", "getticket", "railpass", "reload", "coffers"));
    } else {
      
      switch (args[0]) {
        // Check Fare
        case "checkfare":
          if (length == 1 || length == 2) {
            return new CmdRntVal(false, List.copyOf(fares.getAllStations()));
          } else if (length == 3) {
            if (execute && fares.getFare(args[1], args[2]) > 0d) {
              sender.sendMessage("Fare from "+args[1]+" to "+args[2]+": "+fares.getFare(args[1], args[2]));
              return new CmdRntVal(true);
            } else {
              return new CmdRntVal(List.copyOf(fares.getClasses(args[1], args[2])));
            }
          } else if (length == 4) {
            if (execute) {
              sender.sendMessage(args[3]+" fare from "+args[1]+" to "+args[2]+": "+fares.getFare(args[1], args[2], args[3]));
              return new CmdRntVal(true);
            } else {
              return new CmdRntVal(false);
            }
          }
          break;
  
        // Get ticket
        case "getticket":
          if (length == 1 || length == 2) {
            return new CmdRntVal(List.copyOf(fares.getAllStations()));
          } else if (length == 3) {
            if (execute && sender instanceof Player) {
              ItemStack item = new ItemStack(Material.PAPER, 1);
              ItemMeta itemMeta = item.getItemMeta();
              assert itemMeta != null;
              itemMeta.displayName(lang.getComponent("train-ticket"));
              itemMeta.lore(Arrays.asList(Component.text(args[1]), Component.text(args[2])));
              item.setItemMeta(itemMeta);
              ((Player) sender).getInventory().addItem(item);
              return new CmdRntVal(true);
            } else {
              return new CmdRntVal(List.copyOf(fares.getClasses(args[1], args[2])));
            }
          } else if (length == 4) {
            if (execute && sender instanceof Player) {
              ItemStack item = new ItemStack(Material.PAPER, 1);
              ItemMeta itemMeta = item.getItemMeta();
              assert itemMeta != null;
              itemMeta.displayName(lang.getComponent("train-ticket"));
              itemMeta.lore(Arrays.asList(Component.text(args[1]), Component.text(args[2]), Component.text(args[3])));
              item.setItemMeta(itemMeta);
              ((Player) sender).getInventory().addItem(item);
              return new CmdRntVal(true);
            } else {
              return new CmdRntVal();
            }
          }
          break;
  
        // Rail Pass
        case "railpass":
          // iciwi railpass <serial> <railpassname>
          if (length == 1) {
            return new CmdRntVal();  //Iciwi cards' serials should not be listed
          } else if (length == 2) {
            return new CmdRntVal(owners.getConfigurationSection("RailPasses").getKeys(false).stream().toList());
          } else if (length == 3 && execute) {
            CardSql cardSql = new CardSql();
            if (cardSql.getAllDiscounts(args[0]).containsKey(args[1]))
              // Extend by <duration>
              cardSql.setDiscount(args[0], args[1], cardSql.getExpiry(args[0], args[1])+owners.getRailPassDuration(args[1]));
            else
              // New rail pass
              cardSql.setDiscount(args[0], args[1], Instant.now().getEpochSecond());
          }
          break;
  
        // Reload Config
        case "reload":
          if (execute) {
            plugin.reloadConfig();
            fares.reload();
            lang.reload();
            owners.reload();
            sender.sendMessage("Reloaded iciwi!");
            return new CmdRntVal(true);
          }
          break;
          
          // Coffers
        case "coffers":
          if (length == 1) {
            return sender instanceof Player ? new CmdRntVal(Arrays.asList("empty", "view")) : new CmdRntVal(Collections.singletonList("view"));
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
            return new CmdRntVal(true, companies);
          } else if (length == 3 && args[1].equals("empty") && sender instanceof Player player) {
            if (execute && owners.getOwnership(player.getName(), args[3])) {
              Iciwi.economy.depositPlayer(player, owners.getCoffers(args[2]));
              owners.setCoffers(args[2], 0d);
              return new CmdRntVal(true);
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
            return new CmdRntVal(true);
          }
          break;
          
      }
    }
    return new CmdRntVal();
  }
  
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    return inCommand(sender, command, label, args, false).tabComplete();
  }
  
}


record CmdRntVal(boolean execute, List<String> tabComplete) {
  public CmdRntVal() {
    this(false, null);
  }
  
  public CmdRntVal(boolean execute) {
    this(execute, null);
  }
  
  public CmdRntVal(List<String> tabComplete) {
    this(false, tabComplete);
  }
}