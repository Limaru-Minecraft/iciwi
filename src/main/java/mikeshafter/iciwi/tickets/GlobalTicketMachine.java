package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.Iciwi;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Objects;


public class GlobalTicketMachine extends TicketMachine {

  public GlobalTicketMachine(Player player) {
    super(player, null);
  }

  public void newTM_0() {
    Inventory tm = plugin.getServer().createInventory(null, 9, lang.getComponent("ticket-machine"));
  
    tm.setItem(1, super.makeItem(Material.PAPER, lang.getComponent("menu-new-ticket"), Component.text(plugin.getConfig().getDouble("global-ticket-price"))));
    tm.setItem(4, super.makeItem(Material.NAME_TAG, lang.getComponent("card-operations")));
    tm.setItem(7, super.makeItem(Material.BOOK, lang.getComponent("check-fares")));
  
    getPlayer().openInventory(tm);
  }
  
  @Override
  public void cardOperations_2(String serial) {
    Inventory i = plugin.getServer().createInventory(null, 9, Component.text(String.format(lang.getString("card-operation")+"%s", serial)));
    double cardValue = cardSql.getCardValue(serial);
    i.setItem(0, makeItem(Material.NAME_TAG, lang.getComponent("card-details"), Component.text(String.format(lang.getComponent("serial-number")+"%s", serial)), Component.text(String.format(lang.getString("remaining-value")+lang.getString("currency")+"%.2f", cardValue))));
    i.setItem(1, makeItem(Material.MAGENTA_WOOL, lang.getComponent("new-card")));
    i.setItem(2, makeItem(Material.CYAN_WOOL, lang.getComponent("top-up-card")));
    i.setItem(3, makeItem(Material.LIME_WOOL, lang.getComponent("menu-rail-pass"), Component.text(Objects.requireNonNull(plugin.getConfig().getString("global-operator")))));
    i.setItem(4, makeItem(Material.ORANGE_WOOL, lang.getComponent("refund-card")));
    super.getPlayer().openInventory(i);
  }
  
  public void generateTicket(double value) {
    if (Iciwi.economy.getBalance(getPlayer()) >= value) {
      Iciwi.economy.withdrawPlayer(getPlayer(), value);
    }
    getPlayer().sendMessage(String.format(lang.getString("generate-ticket-global"), value));
    getPlayer().getInventory().addItem(makeItem(Material.PAPER, lang.getComponent("train-ticket"), lang.getComponent("global-ticket"), Component.text(String.format("%.2f", value))));
  }
  
}
