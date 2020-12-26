package mikeshafter.iciwi;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

import static mikeshafter.iciwi.Iciwi.economy;

public class Events implements Listener{
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event){  // Create ICIWI for new player
    Player player = event.getPlayer();
    if (!player.hasPlayedBefore()){
      plugin.getConfig().set(player.getName(), "");
    }
  }
  
  // Opening/Closing sequence in functions
  public void enter(String station, Player player){
    if (!station.isEmpty()){
      String playerName = player.getName();
      plugin.getConfig().set(playerName, station);
      player.sendMessage("Entered "+station);
    }
  }
  
  // Charge maximum fare
  public void maxfare(double fare, Player player, String message){
    player.sendMessage(message+" "+ChatColor.GOLD+"Fare: "+fare);
    economy.withdrawPlayer(player, fare);
  }
  
  int x;
  int y;
  int z;
  Material gateMaterial;
  BlockData gateData;
  
  public void exit(String inSystem, String station, Player player, String ticketType, double fare){
    String playerName = player.getName();
    player.sendMessage(ChatColor.GREEN+"Entered "+inSystem+". Exited "+station+". Fare: "+ChatColor.YELLOW+fare);
    if (ticketType.equals(inSystem)){
      player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);
    } else if (ticketType.equals("Remaining value:")){
      ItemMeta ticketMeta = player.getInventory().getItemInMainHand().getItemMeta();
      assert ticketMeta != null;
      List<String> lore = ticketMeta.getLore();
      assert lore != null;
      lore.set(1, String.valueOf(Double.parseDouble(lore.get(1))+8.00-fare));
      ticketMeta.setLore(lore);
      player.getInventory().getItemInMainHand().setItemMeta(ticketMeta);
    }
    plugin.getConfig().set(playerName, "");  // Sets config such that inSystem will be null the next time
  }
  
  
  @EventHandler
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
      
      // Check if sign is right clicked
      if (action == Action.RIGHT_CLICK_BLOCK && state instanceof Sign && blockData instanceof WallSign){
        Sign sign = (Sign) state;
        WallSign wallSign = (WallSign) blockData;
        Location location = sign.getLocation();
        BlockFace signDirection = wallSign.getFacing();  // Get sign direction
        String signLine0 = ChatColor.stripColor(sign.getLine(0));
        String station = ChatColor.stripColor(sign.getLine(1)).replaceAll("\\s+","");
        
        // Entry
        if (signLine0.equalsIgnoreCase("[Entry]")){
          String inSystem = plugin.getConfig().getString(player.getName());
          if (inSystem != null && !inSystem.isEmpty()){ // Max fare
            maxfare(8.0, player, ChatColor.RED+"You did not tap out of your previous journey! Maximum fare charged.");
            decideGate(signDirection, location);
            enter(station, player);
          } else {
      
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(0) != null;
            if (player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).equals(station)){
              decideGate(signDirection, location); // open fare gates
              enter(station, player);
            } else if (player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).equals("Remaining value:")){
              decideGate(signDirection, location);
              ItemMeta ticketMeta = player.getInventory().getItemInMainHand().getItemMeta();
              List<String> lore = ticketMeta.getLore();
              lore.set(1, String.valueOf(Double.parseDouble(lore.get(1))-8.0));
              ticketMeta.setLore(lore);
              player.getInventory().getItemInMainHand().setItemMeta(ticketMeta);
              enter(station, player);
            } else {
              player.sendMessage(ChatColor.RED+"Wrong ticket!");
            }
          }
        } else if (signLine0.equalsIgnoreCase("[Exit]")){
          String inSystem = plugin.getConfig().getString(player.getName());
          double fare = JSONmanager.getjson(station, inSystem);
    
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(1) != null;
          String ticketType = player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0);
    
          String temp = player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1);
          if (inSystem != null && !inSystem.isEmpty() && (temp.equals(station) || Double.parseDouble(temp) >= fare)){
            decideGate(signDirection, location); // open fare gates
            exit(inSystem, station, player, ticketType, fare);
          } else if (inSystem != null && !inSystem.isEmpty() && Double.parseDouble(temp) < fare){
            player.sendMessage(ChatColor.RED+"Wrong ticket! The fare for your journey is "+ChatColor.GOLD+fare+ChatColor.RED+".");
          } else if (inSystem != null && !inSystem.isEmpty()){
            player.sendMessage(ChatColor.RED+"Wrong ticket!");
          } else { // Max fare
            maxfare(8.0, player, ChatColor.RED+"You did not tap in! Maximum fare charged.");
            decideGate(signDirection, location);
            exit(inSystem, station, player, ticketType, 8.0);
          }
        } else if (signLine0.equalsIgnoreCase("[Transfer]")){
          String inSystem = plugin.getConfig().getString(player.getName());
    
          // If the player is already in the system
          if (inSystem != null && !inSystem.isEmpty()){
            player.sendMessage("Transfer: "+station);
            decideGate(signDirection, location);
          } else {
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(0) != null;
            if (player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).equals(station)){
              decideGate(signDirection, location); // open fare gates
              enter(station, player);
            } else {
              player.sendMessage(ChatColor.RED+"Wrong ticket!");
            }
          }
        }
  
        // === TICKET MACHINE ===
        else if (signLine0.equalsIgnoreCase("[Tickets]")||signLine0.equalsIgnoreCase("-Tickets-")||signLine0.equalsIgnoreCase("[Ticket Machine]")){
          CustomInventory tm = new CustomInventory();
          tm.newTM(player, station);
        }
      } // === END OF SIGN CLICK ===
    }
  }
}
