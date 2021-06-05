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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static mikeshafter.iciwi.Iciwi.economy;
import static org.bukkit.Bukkit.getServer;


public class EventSigns implements Listener {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);

  private int x = 0;
  private int y = 2147483647;
  private int z = 0;
  Material gateMaterial;
  BlockData gateData;

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {  // Create InStation for new player
    Player player = event.getPlayer();
    if (!player.hasPlayedBefore()) {
      plugin.getConfig().set(player.getName(), "");
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
        if (signLine0.equalsIgnoreCase("[Entry]") && (player.getInventory().getItemInMainHand().getType() == Material.PAPER || player.getInventory().getItemInMainHand().getType() == Material.NAME_TAG) ) {
          // Check if the player tapped out
          String inSystem = plugin.getConfig().getString(player.getName());
          if (inSystem != null && !inSystem.isEmpty()){ // Max fare
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 0.7f);
            maxfare(player, ChatColor.RED+"You did not tap out of your previous journey! Maximum fare charged.");
            plugin.getConfig().set(player.getName(), "");
          }

          // they tapped out, so entry station normally
          else {
            // get ticket type
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
            assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(0) != null;
            List<String> lore = player.getInventory().getItemInMainHand().getItemMeta().getLore();

            // Ticket || Iciwi Card
            if (lore.get(0).equals(station) || ChatColor.stripColor(lore.get(0)).equals("Serial number:")) {
              decideGate(signDirection, location); // open fare gates
              entry(station, player, lore);
            } else player.sendMessage(ChatColor.RED+"Wrong ticket!");
          }
        }

        // === Exit ===
        else if (signLine0.equalsIgnoreCase("[Exit]")) {
          // Get the player's entry station (nullable)
          String inSystem = plugin.getConfig().getString(player.getName());
          // Get the fare
          double fare = JsonManager.getFare(station, inSystem);

          // Get ticket type
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(1) != null;
          List<String> lore = player.getInventory().getItemInMainHand().getItemMeta().getLore();
          String ticketType = lore.get(0);

          // Temp is the price/destination
          String paid = ticketType.equals("Serial number:") ? String.valueOf(new CardSql().getCardValue(lore.get(1))) : player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1);

          // Check if the second line is the name of the station || Check if the fare paid ≥ real fare
          if (paid.equals(station) || Double.parseDouble(paid) >= fare) {
            decideGate(signDirection, location); // open fare gates
            exit(inSystem, station, player, ticketType, fare);
          }
          // Wrong ticket
          else if (inSystem != null && !inSystem.isEmpty() && Double.parseDouble(paid) < fare)
            player.sendMessage(ChatColor.RED+"Wrong ticket! The fare for your journey is £"+ChatColor.GOLD+fare+ChatColor.RED+".");
          else if (inSystem != null && !inSystem.isEmpty())
            player.sendMessage(ChatColor.RED+"Wrong ticket!");

          else { // Max fare
            maxfare(player, ChatColor.RED+"You did not tap in! Maximum fare charged.");
            decideGate(signDirection, location);
            plugin.getConfig().set(player.getName(), "");
          }
        }

        // === Transfer ===
        else if (signLine0.equalsIgnoreCase("[Transfer]")) {
          // get ticket type
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(0) != null;
          List<String> lore = player.getInventory().getItemInMainHand().getItemMeta().getLore();

          // Ticket || Iciwi Card
          if (lore.get(0).equals(station) || ChatColor.stripColor(lore.get(0)).equals("Serial number:")) {
            decideGate(signDirection, location); // open fare gates
            entry(station, player, lore);
          } else player.sendMessage(ChatColor.RED+"Wrong ticket!");
        }

        // === TICKET MACHINE ===
        else if ((signLine0.equalsIgnoreCase("[Tickets]") || signLine0.equalsIgnoreCase("-Tickets-") || signLine0.equalsIgnoreCase("[Ticket Machine]")) && !sign.getLine(1).equals(ChatColor.BOLD+"Buy/Top Up")){
          new CustomInventory().newTM(player, station);
        }

        // === PAYMENT ===
        //TODO: Payment system, I have not figured out how to do this
        else if (sign.getLine(0).equalsIgnoreCase("[Payment]")){
          double amt = Double.parseDouble(sign.getLine(1));
          String playerName = sign.getLine(2);
          OfflinePlayer offlinePlayer = getServer().getOfflinePlayer(UUID.fromString(playerName));
          economy.depositPlayer(offlinePlayer, amt);
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta()).getLore() != null;
          assert Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().getLore()).get(0) != null;
          if (player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).equals("Remaining value:") && Double.parseDouble(player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1)) >= amt) {
            player.getInventory().getItemInMainHand().getItemMeta().getLore().set(1, String.format("£%.2f", Double.parseDouble(player.getInventory().getItemInMainHand().getItemMeta().getLore().get(1))-amt));
          } else player.sendMessage(ChatColor.RED+"Requires ICIWI card with at least the amount set on the sign!");
        }
      } // === END OF SIGN CLICK ===
    }
  }

  // Charge maximum fare
  private void maxfare(Player player, String message) {
    double fare;
    if (plugin.getConfig().get("penalty") != null) fare = Double.parseDouble((String) Objects.requireNonNull(plugin.getConfig().get("penalty")));
    else fare = 0d;
    player.sendMessage(message+" "+ChatColor.GOLD+"Fare: £"+fare);
    economy.withdrawPlayer(player, fare);
  }

  // Decide gate to open
  private void decideGate(BlockFace face, Location signLocation) {
    World world = signLocation.getWorld();
    x = signLocation.getBlockX();
    y = signLocation.getBlockY();
    z = signLocation.getBlockZ();

    if (face == BlockFace.SOUTH) {
      Location location = new Location(world, x-1, y, z-1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x--;
      z--;
    } else if (face == BlockFace.NORTH) {
      Location location = new Location(world, x+1, y, z+1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x++;
      z++;
    } else if (face == BlockFace.WEST) {
      Location location = new Location(world, x+1, y, z-1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x++;
      z--;
    } else if (face == BlockFace.EAST) {
      Location location = new Location(world, x-1, y, z+1);
      Block gate = location.getBlock();
      gateData = gate.getBlockData();
      gateMaterial = gate.getType();
      gate.setType(Material.AIR);
      x--;
      z++;
    }
  }

  // Entry sequence method
  private void entry(String station, Player player, List<String> lore) {
    if (!station.isEmpty()) {
      player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.4f);
      String playerName = player.getName();
      plugin.getConfig().set(playerName, station);
      player.sendMessage(ChatColor.GREEN+"Entered "+station);
      if (lore.get(0).equals("Serial number:")) {
        player.sendMessage(String.format(ChatColor.GREEN+"Remaining value: "+ChatColor.YELLOW+"£%.2f", new CardSql().getCardValue(lore.get(1))));
        //player.sendMessage(ChatColor.GREEN+"Remaining value: "+ChatColor.YELLOW+new CardSql().getCardValue(lore.get(1).substring(0, 2), Integer.parseInt(lore.get(1).substring(3))));
      }
    }
  }

  // Exit sequence method
  private void exit(String inSystem, String station, Player player, String ticketType, double fare) {
    String playerName = player.getName();
    if (fare > 0)
      player.sendMessage(String.format(ChatColor.GREEN+"Entered %s, Exited %s. Fare: "+ChatColor.YELLOW+"£%.2f", inSystem, station, fare));
    else player.sendMessage(ChatColor.GREEN+"Entered "+inSystem+". Exited "+station+".");
    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.4f);

    // Paper ticket
    if (ticketType.equals(inSystem))
      player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount()-1);

      // Iciwi card
    else if (ticketType.equals("Serial number:")) {
      ItemMeta ticketMeta = player.getInventory().getItemInMainHand().getItemMeta();
      assert ticketMeta != null;
      String serial = Objects.requireNonNull(ticketMeta.getLore()).get(1);
      // Serial is in the format "I"+sum+"-"+serial
      

      // Get SQL
      CardSql app = new CardSql();

      // TODO: Check for discounts
      HashSet<String> operatorSet = app.getOperatorsFromSerial(serial);
      for (String operator: operatorSet) {
        HashSet<String> operatorStationSet = app.getOperatorStations(operator);
    
        double discountPercentage = 1d; // TODO: different discount types
        double halfFare = fare * 0.5 * discountPercentage;
    
        if (operatorStationSet.contains(inSystem)) {
          fare -= halfFare;
        }
        if (operatorStationSet.contains(station)) {
          fare -= halfFare;
        }
        if (fare == 0) break;
      }

      // Update card value
      app.updateCard(serial, app.getCardValue(serial)-fare);
      // Log
      app.log(serial, inSystem, station, fare);
      player.sendMessage(String.format(ChatColor.GREEN+"Remaining value: "+ChatColor.YELLOW+"£%.2f", app.getCardValue(serial)));
    }
    // remove player from insystem
    plugin.getConfig().set(playerName, "");
  }

  @EventHandler  // If player walked through fare gate, close it
  public void CheckPlayerMove(PlayerMoveEvent event) {
    if (event.getFrom().getBlockX() == x && event.getFrom().getBlockY() == y && event.getFrom().getBlockZ() == z) {
      Location location = new Location(event.getPlayer().getWorld(), x, y, z);
      Block block = location.getBlock();
      // Wait 0.4s
      BukkitRunnable task = new BukkitRunnable() {
        @Override
        public void run() {
          if (gateData != null) block.setBlockData(gateData);
          x = 0;
          y = 2147483647;
          z = 0;
          gateMaterial = null;
          gateData = null;
        }
      };
      task.runTaskLater(plugin, 8);
    }
  }
}
