package mikeshafter.iciwi;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static mikeshafter.iciwi.Iciwi.economy;
import static org.bukkit.Bukkit.getServer;

public class EventSigns implements Listener{
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
  int x;
  int y;
  int z;
  Material gateMaterial;
  BlockData gateData;
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event){  // Create InStation for new player
    Player player = event.getPlayer();
    if (!player.hasPlayedBefore()){
      plugin.getConfig().set(player.getName(), "");
    }
  }
  
  // Charge maximum fare
  public void maxfare(double fare, Player player, String message){
    player.sendMessage(message+" "+ChatColor.GOLD+"Fare: "+fare);
    economy.withdrawPlayer(player, fare);
  }
  
  // Opening/Closing sequence in functions
  public void enter(String station, Player player){
    if (!station.isEmpty()){
      player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.4f);
      String playerName = player.getName();
      plugin.getConfig().set(playerName, station);
      player.sendMessage("Entered "+station);
    }
  }
  
  public void exit(String inSystem, String station, Player player, String ticketType, double fare){
    String playerName = player.getName();
    player.sendMessage(ChatColor.GREEN+"Entered "+inSystem+". Exited "+station+". Fare: "+ChatColor.YELLOW+fare);
    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.4f);
  
    // Paper ticket
    if (ticketType.equals(inSystem))
      player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
    
      // Iciwi card
    else if (ticketType.equals("Remaining value:")){
      ItemMeta ticketMeta = player.getInventory().getItemInMainHand().getItemMeta();
      assert ticketMeta != null;
      List<String> lore = ticketMeta.getLore();
      assert lore != null;
    
      // Deduct fare from lore[1]
      lore.set(1, String.format("%.2f", Double.parseDouble(lore.get(1))-fare));
      ticketMeta.setLore(lore);
      player.getInventory().getItemInMainHand().setItemMeta(ticketMeta);
    }
    // remove player from insystem
    plugin.getConfig().set(playerName, "");
  }
  
  
  @EventHandler  // If player walked through fare gate, close it
  public void CheckPlayerMove(PlayerMoveEvent event){
    if (event.getFrom().getBlockX() == x && event.getFrom().getBlockY() == y && event.getFrom().getBlockZ() == z){
      Location location = new Location(event.getPlayer().getWorld(), x, y, z);
      Block block = location.getBlock();
      block.setType(gateMaterial);
      block.setBlockData(gateData);
    }
  }
  
  
  // Decide gate to open
  public void decideGate(BlockFace face, Location signLocation){
    World world = signLocation.getWorld();
    x = signLocation.getBlockX();
    y = signLocation.getBlockY();
    z = signLocation.getBlockZ();
  
    if (face == BlockFace.SOUTH){
      Location location = new Location(world, x-1, y, z-1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x--;
      z--;
    } else if (face == BlockFace.NORTH){
      Location location = new Location(world, x+1, y, z+1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x++;
      z++;
    } else if (face == BlockFace.WEST){
      Location location = new Location(world, x+1, y, z-1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x++;
      z--;
    } else if (face == BlockFace.EAST){
      Location location = new Location(world, x-1, y, z+1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x--;
      z++;
    }
  }
  
  @EventHandler
  public void signClick(PlayerInteractEvent event){
    if (event.getClickedBlock() != null){
      Action action = event.getAction();
      Player player = event.getPlayer();
      Block block = event.getClickedBlock();
      BlockState state = block.getState();
      BlockData blockData = block.getBlockData();
  
      if (action == Action.RIGHT_CLICK_BLOCK && state instanceof Sign && blockData instanceof WallSign){
        Sign sign = (Sign) state;
        WallSign wallSign = (WallSign) blockData;
        Location location = sign.getLocation();
        BlockFace signDirection = wallSign.getFacing();
        String signLine0 = ChatColor.stripColor(sign.getLine(0));
        String station = ChatColor.stripColor(sign.getLine(1)).replaceAll("\\s+", "");
        // Including a left click is problematic when editing signs, so we'll not do that
    
        // === Entry ===
        if (signLine0.equalsIgnoreCase("[Entry]")){
          // Check if the player tapped out
          String inSystem = plugin.getConfig().getString(player.getName());
          if (inSystem != null && !inSystem.isEmpty()){ // Max fare
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 0.7f);
            maxfare(8.0, player, ChatColor.RED+"You did not tap out of your previous journey! Maximum fare charged.");
            decideGate(signDirection, location);
            enter(station, player);
          }
      
          // they tapped out, so enter station normally
          else {
            // get ticket type
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(0) != null;
            String ticketType = player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0);
        
            // Ticket || Iciwi Card
            if (ticketType.equals(station) || ChatColor.stripColor(ticketType).equals("Remaining value:")){
              decideGate(signDirection, location); // open fare gates
              enter(station, player);
            } else player.sendMessage(ChatColor.RED+"Wrong ticket!");
          }
        }

        // === Exit ===
        else if (signLine0.equalsIgnoreCase("[Exit]")){
          // Get the player's entry station (nullable)
          String inSystem = plugin.getConfig().getString(player.getName());
          // Get the fare
          double fare = JsonManager.getJson(station, inSystem);
      
          // Get ticket type
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(1) != null;
          String ticketType = player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0);
      
          // temp is remaining value of Iciwi Card
          String temp = player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1);
      
          // Check if the fare paid ≥ real fare || Check if the second line is the name of the station
          if (Double.parseDouble(temp) >= fare || (inSystem != null && !inSystem.isEmpty() && temp.equals(station))){
            decideGate(signDirection, location); // open fare gates
            exit(inSystem, station, player, ticketType, fare);
          }
          // Wrong ticket
          else if (inSystem != null && !inSystem.isEmpty() && Double.parseDouble(temp) < fare)
            player.sendMessage(ChatColor.RED+"Wrong ticket! The fare for your journey is "+ChatColor.GOLD+fare+ChatColor.RED+".");
          else if (inSystem != null && !inSystem.isEmpty())
            player.sendMessage(ChatColor.RED+"Wrong ticket!");
      
          else { // Max fare
            maxfare(8.0, player, ChatColor.RED+"You did not tap in! Maximum fare charged.");
            decideGate(signDirection, location);
            exit(inSystem, station, player, ticketType, 8.0);
          }
        }

        // === Transfer ===
        else if (signLine0.equalsIgnoreCase("[Transfer]")){
          String inSystem = plugin.getConfig().getString(player.getName());
          player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 0.7f);
          // If the player is already in the system
          if (inSystem != null && !inSystem.isEmpty()){
            player.sendMessage("Transfer: "+station);
            decideGate(signDirection, location);
          }
      
          // If the player is not in the system
          else {
            if (Objects.requireNonNull(Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore()).get(0).equals(station)){
              // open fare gates, "enter" method will print out station name to player
              decideGate(signDirection, location);
              enter(station, player);
            } else player.sendMessage(ChatColor.RED+"Wrong ticket!");
          }
        }
  
        // === TICKET MACHINE ===
        else if (signLine0.equalsIgnoreCase("[Tickets]") || signLine0.equalsIgnoreCase("-Tickets-") || signLine0.equalsIgnoreCase("[Ticket Machine]")){
          new CustomInventory().newTM(player, station);
        }

        // === PAYMENT ===
        else if (sign.getLine(0).equalsIgnoreCase("[Payment]")){
          double amt = Double.parseDouble(sign.getLine(1));
          String playerName = sign.getLine(2);
          OfflinePlayer offlinePlayer = getServer().getOfflinePlayer(UUID.fromString(playerName));
          economy.depositPlayer(offlinePlayer, amt);
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(0) != null;
          if (player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).equals("Remaining value:") && Double.parseDouble(player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1)) >= amt){
            player.getInventory().getItemInMainHand().getItemMeta().getLore().set(1, String.format("£%.2f", Double.parseDouble(player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1))-amt));
          } else player.sendMessage(ChatColor.RED+"Requires ICIWI card with at least the amount set on the sign!");
        }
      } // === END OF SIGN CLICK ===
    }
  }
}
