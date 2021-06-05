package mikeshafter.iciwi;

import mikeshafter.iciwi.iciwiTM.CustomInventory;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;


public final class Iciwi extends JavaPlugin implements CommandExecutor{

  public static Economy economy = null;
  public boolean destroy = true;

  @Override
  public void onDisable(){
    saveConfig();
    getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"ICIWI: Made by Mineshafter61. Thanks for using!");
  }

  @Override
  public void onEnable() { // Use config to store station names and fares

    // === Economy ===
    boolean eco = setupEconomy();

    // === Config ===

    // === Register events ===
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    getServer().getPluginManager().registerEvents(new CustomInventory(), this);
    getServer().getPluginManager().registerEvents(new EventSigns(), this);


    // === Destroy minecarts ===
    Bukkit.dispatchCommand(console, "train destroyall");
    Bukkit.dispatchCommand(console, "ekillall minecarts world");

    // === Periodic train destroyer ===


    // === SQL ===
    CardSql app = new CardSql();
    app.initTables();

    getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"ICIWI Plugin has been invoked!");
  }

  // === Config ===


  private boolean setupEconomy(){
    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null){
      economy = economyProvider.getProvider();
    }
    return (economy != null);
  }


  @Override public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args){
    if (command.getName().equalsIgnoreCase("checkfare") && sender.hasPermission("iciwi.checkfare")) {
      try {
        String from = args[0];
        String to = args[1];
        double fare = JsonManager.getFare(to, from);
        sender.sendMessage("Train fare from "+from+" to "+to+": "+fare);
        return true;
      } catch (Exception e) {
        sender.sendMessage("Error while checking fare.");
      }
    } else if (command.getName().equalsIgnoreCase("ticketmachine") && sender.hasPermission("iciwi.ticketmachine")) {
      if (sender instanceof Player && !args[0].isEmpty()) {
        Player player = (Player) sender;
        String station = args[0];
        CustomInventory inventory = new CustomInventory();
        inventory.newTM(player, station);
        return true;
      } else {
        sender.sendMessage("Usage: /ticketmachine <station>");
        return false;
      }
    } else if (command.getName().equalsIgnoreCase("traindestroydelay") && sender.hasPermission("iciwi.traindestroydelay")) {
      destroy = false;
      Bukkit.broadcastMessage("§b[§aICIWI§b] §fTrainDestroy rescheduled.");
      return true;
    } else if (command.getName().equalsIgnoreCase("newdiscount") && sender.hasPermission("iciwi.newdiscount")) {
      long expiry = Long.parseLong(args[2])*86400+Instant.now().getEpochSecond();
      new CardSql().setDiscount(args[0], args[1], expiry);
      return true;
    } else if (command.getName().equalsIgnoreCase("redeemcard") && sender.hasPermission("iciwi.redeemcard")) {
      if (sender instanceof Player && !args[0].isEmpty()) {
        Player player = (Player) sender;
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
    return false;
  }
}
