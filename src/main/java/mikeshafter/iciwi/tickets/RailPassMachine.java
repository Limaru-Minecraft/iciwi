package mikeshafter.iciwi.tickets;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RailPassMachine extends TicketMachine {
  
  private final String operator;
  
  public RailPassMachine(Player player, String operator) {
    super(player, null);
    this.operator = operator;
  }
  
  @Override
  public void newTM_0() {
    Inventory j = plugin.getServer().createInventory(null, 9, lang.getString("select-card-rail-pass"));
    player.openInventory(j);
  }
  
  public void railPass_3(String serial) {
    super.railPass_3(serial, operator);
  }
}
