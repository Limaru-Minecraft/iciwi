package mikeshafter.iciwi.experimental;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.entity.Player;
import net.milkbowl.vault.economy.Economy;
import java.math.BigDecimal;

public class EconomyHandler {
  
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private static Economy economy = null;
  
  public EconomyHandler () {
    // Vault
    org.bukkit.plugin.RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    if (economyProvider != null) {
      economy = economyProvider.getProvider();
    }
  }

  public boolean hasEconomy() {
    return (economy != null);
  }
    
  public static void withdrawPlayer(Player player, BigDecimal amount) {
    economy.withdrawPlayer(player, amount.doubleValue());
  }

  public static void depositPlayer(Player player, BigDecimal amount) {
    economy.depositPlayer(player, amount.doubleValue());
  }

  public static void withdrawPlayer(Player player, double amount) {
    economy.withdrawPlayer(player, amount);
  }

  public static void depositPlayer(Player player, double amount) {
    economy.depositPlayer(player, amount);
  }

  public static double getBalance (Player player) {
    return economy.getBalance(player);
  }
  
}