package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import mikeshafter.iciwi.Owners;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Objects;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class TicketMachineListener implements Listener {
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final CardSql app = new CardSql();
  private final Owners owners = new Owners(plugin);
  private final Lang lang = new Lang(plugin);
  double val;
  private String serial, station;
  private String operator;
  private ArrayList<String[]> daysList; // {{days, price}, ...}
  private TicketMachine machine;
  
  
  @EventHandler
  public void TMSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign sign) {
      String signLine0 = ChatColor.stripColor(sign.getLine(0));
      String station = ChatColor.stripColor(sign.getLine(1)).replaceAll("\\s+", "");
      Player player = event.getPlayer();
      
      if ((signLine0.equalsIgnoreCase("[Tickets]") || signLine0.equalsIgnoreCase("-Tickets-") || signLine0.equalsIgnoreCase("[Ticket Machine]")) && !sign.getLine(1).equals(ChatColor.BOLD+"Buy/Top Up")) {
        // Figure out which ticket machine is to be used
        String machineType = plugin.getConfig().getString("ticket-machine-type");
        if (Objects.equals(machineType, "COMPANY")) {
          String company = owners.getOwner(station);
          machine = new CompanyTicketMachine(player, company);
          machine.newTM_0();
        } else if (Objects.equals(machineType, "GLOBAL")) {
          machine = new GlobalTicketMachine(player);
          machine.newTM_0();
        } else {
          machine = new TicketMachine(player, station);
          machine.newTM_0();
        }
      }
    }
  }
  
  @EventHandler
  public void TMClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    if (inventory == null) return;
    
    if (item != null && item.hasItemMeta() && item.getItemMeta() != null) {
      String itemName = item.getItemMeta().getDisplayName();
      String inventoryName = event.getView().getTitle();
      
      // == Page 0 ==
      if (inventoryName.equals(ChatColor.DARK_BLUE+"Ticket Machine")) {
        
        if (itemName.equals(ChatColor.GREEN+"New Single Journey Ticket")) machine.newSingleJourneyTicket_1();
        else if (itemName.equals(ChatColor.YELLOW+"Adjust Fares")) machine.adjustFares_1();
        else if (itemName.equals(ChatColor.LIGHT_PURPLE+"ICIWI Card Operations")) machine.iciwiCardOperations_1();
        else if (itemName.equals(ChatColor.AQUA+"Check Fares")) machine.checkFares_1();
        
      }
    }
  }
}
