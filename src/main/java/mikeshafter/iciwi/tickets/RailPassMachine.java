package mikeshafter.iciwi.tickets;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RailPassMachine extends TicketMachine {
  
  private final String operator;
  
  public RailPassMachine(Player player, String operator) {
    super(player, null);
    this.operator = operator;
    super.newRailPass(serial, operator);
  }
  
  public void railPass(String serial) {
    super.railPass(serial, operator);
  }
}
