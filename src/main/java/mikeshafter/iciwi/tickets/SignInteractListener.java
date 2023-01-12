package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.util.MachineUtil;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

import static mikeshafter.iciwi.util.MachineUtil.parseComponent;


public class SignInteractListener implements Listener {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final Lang lang = new Lang(plugin);
  protected static final HashMap<Player, Machine> machineHashMap = new HashMap<>();
  
  @EventHandler
  public void onSignPlace(SignChangeEvent event) {
    String line = parseComponent(event.line(0));
    Player player = event.getPlayer();
  
    // General Ticket machine
    if (ChatColor.stripColor(line).contains(lang.getString("tickets"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-ticket-machine"));
      } else event.setCancelled(true);
    }
  
    // Rail Pass machine
    if (ChatColor.stripColor(line).contains(lang.getString("passes"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-pass-machine"));
      } else event.setCancelled(true);
    }
  
    // Direct Ticket machine
    if (ChatColor.stripColor(line).contains(lang.getString("custom-tickets"))) {
      if (player.hasPermission("iciwi.create")) {
        player.sendMessage(lang.getString("create-custom-machine"));
      } else event.setCancelled(true);
    }
  
  }
  
  @EventHandler(priority = EventPriority.LOWEST)
  public void onSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign sign) {
      String signLine0 = MachineUtil.parseComponent(sign.line(0));
      Player player = event.getPlayer();

      // === Normal ticket machine ===

      if (signLine0.equalsIgnoreCase("["+lang.getString("tickets")+"]"))
      {
        String station = ((TextComponent) sign.line(1)).content().replaceAll("\\s+", "");
        TicketMachine machine = new TicketMachine(player);
        machine.init(station);
        machineHashMap.put(player, machine);
      }

      // === Rail pass machine ===
      else if (signLine0.equalsIgnoreCase("["+lang.getString("passes")+"]"))
      {
        // future
      }

      // === Custom machine ===

      else if (signLine0.equalsIgnoreCase("["+lang.getString("custom-tickets")+"]"))
      {
        String station = MachineUtil.parseComponent(sign.line(1)).replaceAll("\\s+", "");
        CustomMachine machine = new CustomMachine(player, station);
        machineHashMap.put(player, machine);
      }
    }
  }
}
