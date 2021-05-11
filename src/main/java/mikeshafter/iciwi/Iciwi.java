package mikeshafter.iciwi;

import mikeshafter.iciwi.iciwiTM.CustomInventory;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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
    getConfig().options().copyDefaults(true);
    saveConfig();
  
    // === Register events ===
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    getServer().getPluginManager().registerEvents(new CustomInventory(), this);
    getServer().getPluginManager().registerEvents(new EventSigns(), this);
  
  
    // === Destroy minecarts ===
    Bukkit.dispatchCommand(console, "train destroyall");
    Bukkit.dispatchCommand(console, "ekillall minecarts world");
  
    // === Periodic train destroyer ===
    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        Bukkit.broadcastMessage(ChatColor.GREEN+"§b[§aICIWI§b] §fTrains will be destroyed in 1 minute!");
        Bukkit.broadcastMessage(ChatColor.GREEN+"§b[§aICIWI§b] §fIf you are currently riding a train, please get off at the next stop.");
        for (Player player : Bukkit.getOnlinePlayers())
        {
        	player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);
        }
      }
    }, 72000, 216000);
    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
        public void run(){
          for (Player player : Bukkit.getOnlinePlayers())
          {
          	player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.2f);
          }
        }
      }, 72004, 216000);
    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
      public void run(){
        if (destroy){
          ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
          Bukkit.broadcastMessage(ChatColor.GREEN+"§b[§aICIWI§b] §fTrains have been destroyed!");
          Bukkit.dispatchCommand(console, "train destroyall");
          Bukkit.dispatchCommand(console, "ekillall minecarts world");
        } else {
          destroy = true;
          Bukkit.broadcastMessage(ChatColor.GREEN+"§b[§aICIWI§b] §fTrainDestroy rescheduled, trains will be destroyed in the next cycle.");
        }}}, 73200, 216000);

    // === SQL ===
    CardSql app = new CardSql();
    app.initTables(new String[] {"Entetsu", "Lipan"});

    getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"ICIWI Plugin has been invoked!");
  }


  private boolean setupEconomy(){
    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null){
      economy = economyProvider.getProvider();
    }
    return (economy != null);
  }


  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
    if (command.getName().equalsIgnoreCase("checkfare") && sender.hasPermission("iciwi.checkfare")) {
      try {
        String from = args[0];
        String to = args[1];
        double fare = JsonManager.getJson(to, from);
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
      long expiry = Long.parseLong(args[3])*86400+Instant.now().getEpochSecond();
      new CardSql().setDiscount(args[0], Integer.parseInt(args[1]), args[2], expiry);
      return true;
    } else if (command.getName().equalsIgnoreCase("redeemcard") && sender.hasPermission("iciwi.redeemcard")) {
      if (sender instanceof Player && !args[0].isEmpty()) {
        Player player = (Player) sender;
        String serial_prefix = args[0].substring(0,2);
        int serial = Integer.parseInt(args[0].substring(3));
        // Check the checksum
        char sum = new char[] {'Z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'V', 'J', 'K', 'N', 'P', 'U', 'R', 'S', 'T', 'Y'}[
                       ((serial%10)*2+(serial/10%10)*3+(serial/100%10)*5+(serial/1000%10)*7+(serial/10000)*9)%19
                       ];
        if (args[0].toCharArray()[1] == sum) {
          // Generate and place card into player's inventory
          ItemStack card = new ItemStack(Material.NAME_TAG, 1);
          ItemMeta cardMeta = card.getItemMeta();
          assert cardMeta != null;
          cardMeta.setDisplayName(ChatColor.GREEN+"ICIWI Card");
          new CardSql().newCard("I"+sum, serial, 0.0);
          ArrayList<String> lore = new ArrayList<>();
          lore.add("Serial number:");
          lore.add("I"+args[0]);
          cardMeta.setLore(lore);
          card.setItemMeta(cardMeta);
          player.getInventory().addItem(card);
          player.closeInventory();
          return true;
        }
      }
    }
    return false;
  }
}
