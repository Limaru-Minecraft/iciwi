package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import org.bukkit.entity.Player;

import java.util.Objects;


public class Payment {
  private final static Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final static Lang lang = plugin.lang;
  
  public static void pay(String serial, Player player, double price) {
    CardSql app = new CardSql();
    if (app.getCardValue(serial) >= price) {
      app.subtractValueFromCard(serial, price);
      double value = app.getCardValue(serial);
      player.sendMessage(String.format(lang.getString("pay-success-card"), price, value));
      
      if (Objects.equals(plugin.getConfig().getString("ticket-machine-type"), "GLOBAL")) {
        Owners owners = new Owners(plugin);
        owners.deposit(plugin.getConfig().getString("global-operator"), price);
      }
    }
    else pay(player, price);
  }
  
  public static void pay(Player player, double price) {
    Iciwi.economy.withdrawPlayer(player, price);
    player.sendMessage(lang.getString("cash-divert"));
    player.sendMessage(String.format(lang.getString("pay-success"), price));
  
    if (Objects.equals(plugin.getConfig().getString("ticket-machine-type"), "GLOBAL")) {
      Owners owners = new Owners(plugin);
      owners.deposit(plugin.getConfig().getString("global-operator"), price);
    }
  }
  
}
