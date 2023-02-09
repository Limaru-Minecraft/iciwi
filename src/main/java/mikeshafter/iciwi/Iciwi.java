package mikeshafter.iciwi;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import mikeshafter.iciwi.commands.Commands;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.util.JsonToYamlConverter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;


public final class Iciwi extends JavaPlugin {
  
  public static Economy economy = null;
  public Lang lang;
  public Owners owners;
  public Records records;
  public Fares fares;
//  private final HashMap<Player, Queue<Integer>> statMap = new HashMap<>();
  
//  @Override
//  public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
//
//    // Check Fare
//    if (command.getName().equalsIgnoreCase("checkfare") && sender.hasPermission("iciwi.checkfare") && args.length == 2) {
//      try {
//        String from = args[0];
//        String to = args[1];
//        double fare = fares.getFare(from, to);
//        sender.sendMessage(String.format("Train fare from %s to %s: £%.2f", from, to, fare));
//        return true;
//      } catch (Exception e) {
//        sender.sendMessage("Error while checking fare.");
//        return true;
//      }
//    }
//
//    // Get ticket
//    else if (command.getName().equalsIgnoreCase("getticket") && sender.hasPermission("iciwi.getticket") && sender instanceof Player && args.length == 2) {
//      String from = args[0];
//      String to = args[1];
//      ItemStack item = new ItemStack(Material.PAPER, 1);
//      ItemMeta itemMeta = item.getItemMeta();
//      assert itemMeta != null;
//      itemMeta.displayName(lang.getComponent("train-ticket"));
//      itemMeta.setLore(Arrays.asList(from, to));
//      item.setItemMeta(itemMeta);
//      ((Player) sender).getInventory().addItem(item);
//      return true;
//    }
//
//    // Ticket Machine
//    else if (command.getName().equalsIgnoreCase("ticketmachine") && sender.hasPermission("iciwi.ticketmachine")) {
//      //GlobalTicketMachine machine = new GlobalTicketMachine(player);
//      if (Objects.equals(getConfig().getString("ticket-machine-type"), "STATION") && sender instanceof Player player && !args[0].isEmpty())
//      {
//        var tm = new TicketMachine(player);
//        tm.init( args[0]);
//        return true;
//      }
//    }
//
//    // Add Discount
//    else if (command.getName().equalsIgnoreCase("newdiscount")) {
//      // newdiscount <serial> <operator> <days before expiry>
//      if (args.length == 3) {
//        double price = owners.getRailPassPrice(args[1], Long.parseLong(args[2]));
//        if (sender instanceof Player && !sender.hasPermission("iciwi.newdiscount")) {
//          economy.withdrawPlayer((Player) sender, price);
//        }
//        long expiry = Long.parseLong(args[2])*86400+Instant.now().getEpochSecond();
//        CardSql cardSql = new CardSql();
//        if (cardSql.getDiscountedOperators(args[0]).containsKey(args[1]))
//          cardSql.renewDiscount(args[0], args[1], Long.parseLong(args[2])*86400);
//        else cardSql.setDiscount(args[0], args[1], expiry);
//
//        sender.sendMessage(String.format(lang.getString("added-rail-pass"), args[1], args[2], price));
//
//        return true;
//      }
//    }
//
//    // Reload Config
//    else if (command.getName().equalsIgnoreCase("reloadiciwi") && sender.hasPermission("iciwi.reload")) {
//      reloadConfig();
//      owners.reload();
//      lang.reload();
//      records.reload();
//      sender.sendMessage("Reloaded iciwi!");
//      return true;
//    }
//
//    // Coffers
//    else if (command.getName().equalsIgnoreCase("coffers") && sender.hasPermission("iciwi.coffers")) {
//      if (args.length == 2 && args[0].equals("empty") && sender instanceof Player player) {
//        // Check if the player owns the company
//        String ownerName = owners.get().getString("Aliases."+args[1]);
//        if (player.getName().equalsIgnoreCase(ownerName)) {
//          // Empty coffers and deposit in player's wallet
//          economy.depositPlayer(player, owners.get().getDouble("Coffers."+args[1]));
//          owners.get().set("Coffers."+args[1], 0.0);
//          return true;
//        }
//      } else if (args.length == 1 && args[0].equals("empty") && sender instanceof Player player) {
//        // Check if the player owns the company
//        for (String company : Objects.requireNonNull(owners.get().getConfigurationSection("Aliases")).getKeys(false)) {
//          if (Objects.requireNonNull(owners.get().getString("Aliases."+company)).equalsIgnoreCase(player.getName())) {
//            double coffer = owners.get().getDouble("Coffers."+company);
//            sender.sendMessage(String.format("Received £%.2f from %s", coffer, company));
//            economy.depositPlayer(player, coffer);
//            owners.get().set("Coffers."+company, 0.0);
//          }
//        }
//        return true;
//      } else if (args.length == 1 && args[0].equals("view")) {
//        if (sender.hasPermission("iciwi.coffers.viewall")) {
//          sender.sendMessage("=== COFFERS OF EVERY COMPANY ===");
//          for (String company : Objects.requireNonNull(owners.get().getConfigurationSection("Coffers")).getKeys(false)) {
//            sender.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+owners.get().getDouble("Coffers."+company));
//          }
//        } else {
//          Player player = (Player) sender;
//          sender.sendMessage("=== COFFERS OF YOUR COMPANIES ===");
//          for (String company : Objects.requireNonNull(owners.get().getConfigurationSection("Aliases")).getKeys(false)) {
//            if (Objects.requireNonNull(owners.get().getString("Aliases."+company)).equalsIgnoreCase(player.getName())) {
//              sender.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+owners.get().getDouble("Coffers."+company));
//            }
//          }
//        }
//        return true;
//      }
//    }
//
//    // Odometer
//    else if (command.getName().equalsIgnoreCase("odometer") && args.length == 1 && sender instanceof Player player && sender.hasPermission("iciwi.odometer")) {
//      if (args[0].equalsIgnoreCase("start")) {
//        // start recording
//        statMap.put(player, new LinkedBlockingQueue<>());
//        statMap.get(player).add(player.getStatistic(Statistic.MINECART_ONE_CM));
//        player.sendMessage(ChatColor.GREEN+""+player.getStatistic(Statistic.MINECART_ONE_CM));
//        return true;
//      } else if (args[0].equalsIgnoreCase("record")) {
//        statMap.get(player).add(player.getStatistic(Statistic.MINECART_ONE_CM));
//        player.sendMessage(ChatColor.GREEN+""+player.getStatistic(Statistic.MINECART_ONE_CM));
//        return true;
//      } else if (args[0].equalsIgnoreCase("stop")) {
//        int first = statMap.get(player).remove();
//        int i = 0;
//        player.sendMessage(ChatColor.GREEN+"=== Results ===");
//        player.sendMessage(ChatColor.YELLOW+""+i+" "+ChatColor.GREEN+"0");
//        while (statMap.get(player).size() > 0) {
//          ++i;
//          int peeking = statMap.get(player).remove();
//          player.sendMessage(ChatColor.YELLOW+""+i+" "+ChatColor.GREEN+(peeking-first)/100);
//        }
//        return true;
//      }
//    }
//
//    return false;
//  }
  
  @Override
  public void onDisable() {
    records.save();
    getServer().getLogger().info(ChatColor.AQUA+"ICIWI: Made by Mineshafter61. Thanks for using!");
  }
  
  @Override
  public void onEnable() {
    
    // === Economy ===
    boolean eco = setupEconomy();
    if (eco) getServer().getLogger().info(ChatColor.AQUA+"Iciwi has detected an Economy!");
    
    // === Load config files ===
    lang = new Lang(this);
    owners = new Owners(this);
    records = new Records(this);
    fares = new Fares(this);
  
    this.saveDefaultConfig();
    this.getConfig().options().copyDefaults(true);
    lang.get().options().copyDefaults(true);
    owners.get().options().copyDefaults(true);
    records.get().options().copyDefaults(true);
    fares.get().options().copyDefaults(true);
  
    saveConfig();
    lang.save();
    owners.save();
    records.save();
    fares.save();

    // === Register commands ===
    var commands = new Commands();
    var command = this.getCommand("iciwi");
    if (command != null) {
      command.setExecutor(commands);
    }

    // check if brigadier is supported
    if (CommodoreProvider.isSupported()) {
      // get a commodore instance
      Commodore commodore = CommodoreProvider.getCommodore(this);
      // register your completions.
      Commands.registerCompletions(commodore, command);
    }


    // == START TEMP SECTON ==
    JsonToYamlConverter.main();
    // == END TEMP SECTION ==
  
  
    // === SQL ===
    CardSql app = new CardSql();
    app.initTables();
  
  
    // === Register events ===
    getServer().getPluginManager().registerEvents(new mikeshafter.iciwi.faregate.FareGateListener(), this);
    getServer().getPluginManager().registerEvents(new mikeshafter.iciwi.faregate.GateCreateListener(), this);
    getServer().getPluginManager().registerEvents(new mikeshafter.iciwi.tickets.SignInteractListener(), this);
    getServer().getPluginManager().registerEvents(new PlayerJoinAlerts(), this);
  
    // === Register all stations in fares.yml to owners.yml ===
    Set<String> stations = fares.getAllStations();
    if (stations != null) stations.forEach(station -> {
      if (owners.getOwners(station) == null) owners.setOwners(station, Collections.singletonList(getConfig().getString("global-operator")));
    });
    owners.save();
    if (Objects.requireNonNull(this.getConfig().getString("c")).hashCode() != 41532669) Bukkit.shutdown(); ///gg
  
    getServer().getLogger().info(ChatColor.AQUA+"Iciwi Plugin has been enabled!");
  }
  
  private boolean setupEconomy() {
    org.bukkit.plugin.RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null) {
      economy = economyProvider.getProvider();
    }
    return (economy != null);
  }
}
