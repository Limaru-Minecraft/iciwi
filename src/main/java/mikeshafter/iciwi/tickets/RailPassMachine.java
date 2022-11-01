package mikeshafter.iciwi.tickets;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RailPassMachine extends TicketMachine {
  
  private final String operator;
  
  public RailPassMachine(Player player, String operator) {
    super(player, null);
    this.operator = operator;
  }
  
  public void railPass_3(String serial) {
    super.railPass_3(serial, operator);
  }
}
