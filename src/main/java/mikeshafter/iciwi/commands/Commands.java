package mikeshafter.iciwi.commands;

import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.tickets.GlobalTicketMachine;
import mikeshafter.iciwi.tickets.TicketMachine;
import mikeshafter.iciwi.util.JsonToYamlConverter;
import mikeshafter.iciwi.Iciwi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Commands implements TabExecutor {

  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  public Lang lang;
  public Owners owners;
  public Records records;
  public Fares fares;
  private HashMap<Player, Queue<Integer>> statMap = new HashMap<>();
 
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    
    // Check Fare
    if (command.getName().equalsIgnoreCase("checkfare") && sender.hasPermission("iciwi.checkfare") && args.length == 2) {
      try {
        String from = args[0];
        String to = args[1];
        double fare = JsonManager.getFare(from, to);
        sender.sendMessage(String.format("Train fare from %s to %s: £%.2f", from, to, fare));
        return true;
      } catch (Exception e) {
        sender.sendMessage("Error while checking fare.");
        return true;
      }
    }
    
    // Fare chart
    else if (command.getName().equalsIgnoreCase("farechart") && sender.hasPermission("iciwi.farechart") && args.length == 2 && sender instanceof Player player) {
      try {
        String station = args[0];
        String page = args[1];
        new TicketMachine(player, station).checkFares_1(Integer.parseInt(page));
        return true;
      } catch (NumberFormatException e) {
        sender.sendMessage("Not a page!");
        return true;
      }
    }
    
    // Get ticket
    else if (command.getName().equalsIgnoreCase("getticket") && sender.hasPermission("iciwi.getticket") && sender instanceof Player && args.length == 2) {
      String from = args[0];
      String to = args[1];
      ItemStack item = new ItemStack(Material.PAPER, 1);
      ItemMeta itemMeta = item.getItemMeta();
      assert itemMeta != null;
      itemMeta.setDisplayName(lang.getString("train-ticket"));
      itemMeta.setLore(Arrays.asList(new String[] {from, to}));
      item.setItemMeta(itemMeta);
      ((Player) sender).getInventory().addItem(item);
      return true;
    }
    
    // Ticket Machine
    else if (command.getName().equalsIgnoreCase("ticketmachine") && sender.hasPermission("iciwi.ticketmachine")) {
      if (plugin.getConfig().getString("ticket-machine-type").equals("STATION") && sender instanceof Player player && !args[0].isEmpty()) {
        TicketMachine machine = new TicketMachine(player, args[0]);
        return true;
      }
      else if (plugin.getConfig().getString("ticket-machine-type").equals("GLOBAL") && sender instanceof Player player && args[0].isEmpty()) {
        GlobalTicketMachine machine = new GlobalTicketMachine(player);
        return true;
      }
      else {
        return false;
      }
    }
    
    // Add Discount
    else if (command.getName().equalsIgnoreCase("newdiscount")) {
      // newdiscount <serial> <operator> <days before expiry>
      if (args.length == 3) {
        double price = owners.getRailPassPrice(args[1], Long.parseLong(args[2]));
        if (sender instanceof Player && !sender.hasPermission("iciwi.newdiscount")) {
          plugin.economy.withdrawPlayer((Player) sender, price);
        }
        long expiry = Long.parseLong(args[2])*86400+Instant.now().getEpochSecond();
        CardSql cardSql = new CardSql();
        if (cardSql.getDiscountedOperators(args[0]).containsKey(args[1]))
          cardSql.renewDiscount(args[0], args[1], Long.parseLong(args[2])*86400);
        else cardSql.setDiscount(args[0], args[1], expiry);
        
        sender.sendMessage(String.format(lang.getString("added-rail-pass"), args[1], args[2], price));
        
        return true;
      }
    }
    
    // Redeem Card
    else if (command.getName().equalsIgnoreCase("redeemcard") && sender.hasPermission("iciwi.redeemcard")) {
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
    }
    
    // Reload Config
    else if (command.getName().equalsIgnoreCase("reloadiciwi") && sender.hasPermission("iciwi.reload")) {
      plugin.reloadConfig();
      owners.reload();
      lang.reload();
      records.reload();
      sender.sendMessage("Reloaded iciwi!");
      return true;
    }
    
    // Coffers
    else if (command.getName().equalsIgnoreCase("coffers") && sender.hasPermission("iciwi.coffers")) {
      if (args.length == 2 && args[0].equals("empty") && sender instanceof Player player) {
        // Check if the player owns the company
        String ownerName = owners.get().getString("Aliases."+args[1]);
        if (player.getName().equalsIgnoreCase(ownerName)) {
          // Empty coffers and deposit in player's wallet
          plugin.economy.depositPlayer(player, owners.get().getDouble("Coffers."+args[1]));
          owners.get().set("Coffers."+args[1], 0.0);
          return true;
        }
      } else if (args.length == 1 && args[0].equals("empty") && sender instanceof Player player) {
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
      } else if (args.length == 1 && args[0].equals("view")) {
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
    
    // Odometer
    else if (command.getName().equalsIgnoreCase("odometer") && args.length == 1 && sender instanceof Player && sender.hasPermission("iciwi.odometer")) {
      Player player = (Player) sender;
      if (args[0].equalsIgnoreCase("start")) {
        // start recording
        statMap.put(player, new LinkedBlockingQueue<Integer>());
        statMap.get(player).add(player.getStatistic(Statistic.MINECART_ONE_CM));
        player.sendMessage(ChatColor.GREEN+""+player.getStatistic(Statistic.MINECART_ONE_CM));
        return true;
      } else if (args[0].equalsIgnoreCase("record")) {
        statMap.get(player).add(player.getStatistic(Statistic.MINECART_ONE_CM));
        player.sendMessage(ChatColor.GREEN+""+player.getStatistic(Statistic.MINECART_ONE_CM));
        return true;
      } else if (args[0].equalsIgnoreCase("stop")) {
        int first = statMap.get(player).remove();
        int i = 0;
        player.sendMessage(ChatColor.GREEN+"=== Results ===");
        player.sendMessage(ChatColor.YELLOW+""+i+" "+ChatColor.GREEN+"0");
        while (statMap.get(player).size() > 0) {
          ++i;
          int peeking = statMap.get(player).remove();
          player.sendMessage(ChatColor.YELLOW+""+i+" "+ChatColor.GREEN+(peeking-first)/100);
        }
        return true;
      }
    }
  
    return false;
  }
}