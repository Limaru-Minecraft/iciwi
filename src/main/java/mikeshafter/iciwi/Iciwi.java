package mikeshafter.iciwi;

import mikeshafter.iciwi.FareGates.FareGateListener;
import mikeshafter.iciwi.Tickets.TicketMachineListener;
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
import java.util.Objects;


public final class Iciwi extends JavaPlugin implements CommandExecutor, TabCompleter {

  public static Economy economy = null;
  public Lang lang;
  public Owners owners;
  public Records records;


  @Override
  public void onEnable() { // Use config to store station names and fares

    // === Economy ===
    boolean eco = setupEconomy();


    // === Load config files ===
    lang = new Lang(this);
    owners = new Owners(this);
    records = new Records(this);

    this.getConfig().options().copyDefaults(true);
    lang.get().options().copyDefaults(true);
    owners.get().options().copyDefaults(true);
    records.get().options().copyDefaults(true);

    saveConfig();
    lang.save();
    owners.save();
    records.save();


    // === SQL ===
    CardSql app = new CardSql();
    app.initTables();


    // === Register events ===
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    getServer().getPluginManager().registerEvents(new FareGateListener(), this);
//    getServer().getPluginManager().registerEvents(new TBarrier(), this);
    getServer().getPluginManager().registerEvents(new TicketMachineListener(), this);

    getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"ICIWI Plugin has been invoked!");
  }


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
        sender.sendMessage(String.format("Train fare from %s to %s: £%.2f", from, to, fare));
        return true;
      } catch (Exception e) {
        sender.sendMessage("Error while checking fare.");
      }

    } else if (command.getName().equalsIgnoreCase("ticketmachine") && sender.hasPermission("iciwi.ticketmachine")) {
      if (sender instanceof Player player && !args[0].isEmpty()) {
        String station = args[0];
//        TicketM ticketMachine = new TicketM();
//        ticketMachine.newTM(player, station);
        return true;
      } else {
        sender.sendMessage("Usage: /ticketmachine <station>");
        return false;
      }

    } else if (command.getName().equalsIgnoreCase("newdiscount") && sender.hasPermission("iciwi.newdiscount")) {
      // newdiscount <serial> <operator> <days before expiry>
      if (args.length == 3) {
        long expiry = Long.parseLong(args[2])*86400+Instant.now().getEpochSecond();
        new CardSql().setDiscount(args[0], args[1], expiry);
        return true;
      }
    } else if (command.getName().equalsIgnoreCase("redeemcard") && sender.hasPermission("iciwi.redeemcard")) {
      if (sender instanceof Player player && !args[0].isEmpty()) {
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
      reloadConfig();
      owners.reload();
      lang.reload();
      records.reload();
      return true;

    } else if (command.getName().equalsIgnoreCase("coffers") && sender.hasPermission("iciwi.coffers")) {
      if (args.length == 2 && args[0].equals("empty") && sender instanceof Player player) {
        // Check if the player owns the company
        String ownerName = owners.get().getString("Aliases."+args[1]);
        if (player.getName().equalsIgnoreCase(ownerName)) {
          // Empty coffers and deposit in player's wallet
          economy.depositPlayer(player, owners.get().getDouble("Coffers."+args[1]));
          owners.get().set("Coffers."+args[1], 0.0);
          return true;
        }
      } else if (args.length == 1 && args[0].equals("empty") && sender instanceof Player player) {
        // Check if the player owns the company
        for (String company : Objects.requireNonNull(owners.get().getConfigurationSection("Aliases")).getKeys(false)) {
          if (Objects.requireNonNull(owners.get().getString("Aliases."+company)).equalsIgnoreCase(player.getName())) {
            double coffer = owners.get().getDouble("Coffers."+company);
            sender.sendMessage(String.format("Received £%.2f from %s", coffer, company));
            economy.depositPlayer(player, coffer);
            owners.get().set("Coffers."+company, 0.0);
          }
        }
        return true;
      } else if (args.length == 1 && args[0].equals("view")) {
        if (sender.hasPermission("iciwi.coffers.viewall")) {
          sender.sendMessage("=== COFFERS OF EVERY COMPANY ===");
          for (String company : Objects.requireNonNull(owners.get().getConfigurationSection("Coffers")).getKeys(false)) {
            sender.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+owners.get().getDouble("Coffers."+company));
          }
        } else {
          Player player = (Player) sender;
          sender.sendMessage("=== COFFERS OF YOUR COMPANIES ===");
          for (String company : Objects.requireNonNull(owners.get().getConfigurationSection("Aliases")).getKeys(false)) {
            if (Objects.requireNonNull(owners.get().getString("Aliases."+company)).equalsIgnoreCase(player.getName())) {
              sender.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+owners.get().getDouble("Coffers."+company));
            }
          }
        }
        return true;
      }
    }

    return false;
  }


  private boolean setupEconomy() {
    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null) {
      economy = economyProvider.getProvider();
    }
    return (economy != null);
  }
}
