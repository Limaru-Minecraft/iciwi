package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.config.*;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.IcCard;
import java.util.*;
import mikeshafter.iciwi.util.FareGateBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CardUtil {
  private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private static final Records records = new Records();
  private static final Lang lang = new Lang();
  private static final Owners owners = new Owners();
  private static final CardSql cardSql = new CardSql();

  private static final LinkedHashSet<Player> clickBuffer = new LinkedHashSet<>();


  /**
   * Prevent code from registering multiple accidental clicks
   * @param player Player who clicked
   * @return true if the player has clicked within the last 10 ticks, false otherwise
   */
  private static boolean onClick (Player player) {
    plugin.getServer().getScheduler().runTaskLater(plugin, () -> clickBuffer.remove(player), 10);
    return !clickBuffer.add(player);
  }


  /**
   * Register entry from a card
   * @param player Player who used the card
   * @param icCard The card to register
   * @param entryStation The station at which to enter
   * @return Whether entry was successful. If false, do not open the fare gate.
   */
  protected static boolean entry(Player player, IcCard icCard, String entryStation, Vector signLocationVector) {
    if (onClick(player)) return false;

    double value = icCard.getValue();
    String serial = icCard.getSerial();

    // don't parse if there is no serial
    if (serial == null || serial.equals("") || serial.isBlank()) return false;

    // reject entry if card has less than the minimum value
    if (value < plugin.getConfig().getDouble("min-amount")) {
      player.sendMessage(lang.getString("value-low"));
      return false;
    }

    // was the card already used to enter the network?
    if (!records.getStation(serial).equals("")) {
      player.sendMessage(lang.getString("cannot-pass"));
      if (plugin.getConfig().getBoolean("open-on-penalty")) {
        Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
        player.sendMessage(lang.getString("fare-evade"));
      } else return false;
    }

    // write the entry station and fare class
    records.setStation(serial, entryStation);
    records.setClass(serial, plugin.getConfig().getString("default-class"));

    // player has a transfer discount when they tap out and in within the time limit
    records.setTransfer(serial, System.currentTimeMillis() - records.getTimestamp(serial) < plugin.getConfig().getLong("max-transfer-time"));

    // confirmation
    player.sendMessage(String.format(lang.getString("tapped-in"), entryStation, value));
/*
    // logger
    String ukey = System.currentTimeMillis()+"_"+ player.getUniqueId();
    Map<String, Object> logMap = new HashMap<>(Map.ofEntries(
    Map.entry("timestamp", System.currentTimeMillis()),
    Map.entry("uuid", player.getUniqueId().toString()),
    Map.entry("function", "entry_card"),
    Map.entry("signloc", signLocationVector),
    Map.entry("station", entryStation)
    ));
    // check if we need to access Records to get OSI data if there is one
    if (records.getTransfer(icCard.getSerial())) {
    Map<String, Object> previousJourneyMap = Map.ofEntries(
    Map.entry("prevjourney_entry", records.getPreviousStation(serial)),
    Map.entry("prevjourney_fare", records.getCurrentFare(serial)),
    Map.entry("prevjourney_class", records.getClass(serial)),
    Map.entry("prevjourney_exittime", records.getTimestamp(serial))
    );
    logMap.putAll(previousJourneyMap);
    }

    // store IC Card details
    logMap.putAll(icCard.toMap());

    // record in logger
    Iciwi.icLogger.record(ukey, logMap);
*/
  return true;
  }


  /**
  * Register exit from a card
  * @param player Player who used the card
  * @param icCard The card to register
  * @param exitStation The station at which to exit
  * @return Whether exit was successful. If false, do not open the fare gate.
  */
  protected static boolean exit(Player player, IcCard icCard, String exitStation, Vector signLocationVector) {
    if (onClick(player)) return false;

	  Fares fares = new Fares();
    String serial = icCard.getSerial();

    // don't parse if there is no serial
    if (serial == null || serial.equals("") || serial.isBlank()) return false;

    String entryStation = records.getStation(serial);
    double value = icCard.getValue();
    double fare = fares.getCardFare(entryStation, exitStation, records.getClass(serial));

    // is the card not in the network?
    if (records.getStation(serial).equals("")) {
      if (plugin.getConfig().getBoolean("open-on-penalty")) {
        Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
        player.sendMessage(lang.getString("fare-evade"));
      }
      else {
        player.sendMessage(lang.getString("cannot-pass"));
        return false;
      }
    }

    // If an OSI is applicable, use the fare from the first entry station until the exit station
    if (records.getTransfer(serial)) {
      // fare if the player did not tap out
      double longFare = fares.getCardFare(records.getPreviousStation(serial), exitStation, records.getClass(serial));
      // the previous charged fare
      double previousFare = records.getCurrentFare(serial);
      // if the difference between the fares is less than the current fare, change the fare to that difference.
      if (longFare-previousFare < fare) fare = longFare-previousFare;
    }

    // Get the owners of stations and rail passes
    List<String> entryStationOwners = owners.getOwners(entryStation);
    List<String> exitStationOwners	= owners.getOwners(exitStation);
    String finalRailPass = null;
    double payPercentage = 1d;

    // Get cheapest discount
    for (var r : cardSql.getAllDiscounts(serial).keySet()) {
      if (( entryStationOwners.contains(owners.getRailPassOperator(r)) || exitStationOwners.contains(owners.getRailPassOperator(r)) ) &&
            owners.getRailPassPercentage(r) < payPercentage )
      {
        finalRailPass = r;
        payPercentage = owners.getRailPassPercentage(r);
      }
    }

    // Set final fare
    fare *= payPercentage;

    // check if card value is low
    if (value < fare) {
      player.sendMessage(lang.getString("value-low"));
      return false;
    }

    // withdraw fare from card
    icCard.withdraw(fare);

    // set details for future transfer
    records.setTimestamp(serial, System.currentTimeMillis());
    records.setPreviousStation(serial, entryStation);
    records.setStation(serial, null);
    records.setCurrentFare(serial, fare);

    // send (value - fare) as the value variable is not updated
    player.sendMessage(String.format(lang.getString("tapped-out"), exitStation, fare, value - fare));
/*
    // log in IcLogger
    String ukey = System.currentTimeMillis()+"_"+ player.getUniqueId();
    Map<String, Object> logMap = new HashMap<>(Map.ofEntries(
      Map.entry("timestamp", System.currentTimeMillis()),
      Map.entry("uuid", player.getUniqueId().toString()),
      Map.entry("function", "exit_card"),
      Map.entry("signloc", signLocationVector),
      Map.entry("entry_station", entryStation),
      Map.entry("exit_station", exitStation),
      Map.entry("journey_finalfare", fare),
      Map.entry("journey_originalfare", fares.getCardFare(entryStation, exitStation, records.getClass(serial))),
      Map.entry("journey_class", records.getClass(serial))
    ));

    // check if a railpass is used, and if so, add the railpass to the log
    if (finalRailPass != null) {
      Map<String, Object> railPassMap = Map.ofEntries(
        Map.entry("journey_railpass_used", finalRailPass),
        Map.entry("journey_railpass_price", owners.getRailPassPrice(finalRailPass)),
        Map.entry("journey_railpass_percentage", owners.getRailPassPercentage(finalRailPass)),
        Map.entry("journey_railpass_start", cardSql.getStart(serial, finalRailPass)),
        Map.entry("journey_railpass_duration", owners.getRailPassDuration(finalRailPass)),
        Map.entry("journey_railpass_operator", owners.getRailPassOperator(finalRailPass))
      );
      logMap.putAll(railPassMap);
    }

    // check if we need to access Records to get OSI data if there is one
    if (records.getTransfer(icCard.getSerial())) {
      Map<String, Object> previousJourneyMap = Map.ofEntries(
        Map.entry("prevjourney_entry", records.getPreviousStation(serial)),
        Map.entry("prevjourney_fare", records.getCurrentFare(serial)),
        Map.entry("prevjourney_class", records.getClass(serial)),
        Map.entry("prevjourney_exittime", records.getTimestamp(serial))
      );
      logMap.putAll(previousJourneyMap);
    }

    // store IC Card details
    logMap.putAll(icCard.toMap());

    // record in logger
    Iciwi.icLogger.record(ukey, logMap);
*/

    return true;
  }


  /**
   * Check if a card has a railpass
   * @param player Player who used the card
   * @param icCard The card used
   * @param station The station at which the sign is placed
   * @return Whether checks were successful. If false, do not open the fare gate.
   */
  protected static boolean member(Player player, IcCard icCard, String station, Vector signLocationVector) {
    if (onClick(player)) return false;

    // Get the serial number of the card
    String serial = icCard.getSerial();

    // Get the owners of the station and the card's rail passes
    List<String> stationOwners = owners.getOwners(station);

    // Get the owners of the card's rail passes
    Set<String> railPasses = cardSql.getAllDiscounts(serial).keySet();

    // Check if the card has a rail pass belonging to the station's operator
    for (String r : railPasses) {
      if (stationOwners.contains(owners.getRailPassOperator(r))) {
        player.sendMessage(lang.getString("member-gate"));
/*
        // logger
        String ukey = System.currentTimeMillis()+"_"+ player.getUniqueId();
        Map<String, Object> logMap = new HashMap<>(Map.ofEntries(
          Map.entry("timestamp", System.currentTimeMillis()),
          Map.entry("uuid", player.getUniqueId().toString()),
          Map.entry("function", "member_card"),
          Map.entry("signloc", signLocationVector),
          Map.entry("station", station),
          Map.entry("pass", r)
        ));

        // store IC Card details
        logMap.putAll(icCard.toMap());

        // record in logger
        Iciwi.icLogger.record(ukey, logMap);
*/
        return true;
      }
    }

    // If the player does not have such a rail pass, return false
    return false;
  }


  /**
   * Stops and starts a journey without allowing for an OSI
   * @param player Player who used the card
   * @param icCard The card used
   * @param station The station at which the sign is placed
   * @return Whether checks were successful. If false, do not open the fare gate.
   */
  protected static boolean transfer(Player player, IcCard icCard, String station, Vector signLocationVector) {
    if (onClick(player)) return false;

	Fares fares = new Fares();
    String serial = icCard.getSerial();

    // If an OSI was detected, cancel OSI capability
    if (records.getTransfer(serial)) {
      records.setTransfer(serial, false);
      player.sendMessage(lang.getString("transfer-cancel-osi"));
      return true;
    }

    // Else perform normal exit, then entry sequence
    String entryStation = records.getStation(serial);
    double value = icCard.getValue();
    double fare = fares.getCardFare(entryStation, station, records.getClass(serial));

    // is the card already in the network?
    if (records.getStation(serial).equals("")) {
      player.sendMessage(lang.getString("cannot-pass"));
      if (plugin.getConfig().getBoolean("open-on-penalty")) {
        Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
        player.sendMessage(lang.getString("fare-evade"));
      } else return false;
    }

    // Get the owners of stations and rail passes
    List<String> entryStationOwners = owners.getOwners(entryStation);
    List<String> exitStationOwners	= owners.getOwners(station);
    String finalRailPass = null;
    double payPercentage = 1d;

    // Get cheapest discount
    for (var r : cardSql.getAllDiscounts(serial).keySet()) {
      if (( entryStationOwners.contains(owners.getRailPassOperator(r)) || exitStationOwners.contains(owners.getRailPassOperator(r)) ) &&
        owners.getRailPassPercentage(r) < payPercentage )
      {
        finalRailPass = r;
        payPercentage = owners.getRailPassPercentage(r);
      }
    }

    // Set final fare
    fare *= payPercentage;

    // check if card value is low
    if (value < fare) {
      player.sendMessage(lang.getString("value-low"));
      return false;
    }

    icCard.withdraw(fare);

    // set details for future transfer
    records.setTimestamp(serial, System.currentTimeMillis());
    records.setPreviousStation(serial, entryStation);
    records.setStation(serial, null);
    records.setCurrentFare(serial, fare);

    // Perform entry sequence
    // reject entry if card has less than the minimum value
    if (value < plugin.getConfig().getDouble("min-amount")) return false;

    // was the card already used to enter the network?
    if (records.getStation(serial).equals("")) {
      player.sendMessage(lang.getString("cannot-pass"));
      if (plugin.getConfig().getBoolean("open-on-penalty")) {
        Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
        player.sendMessage(lang.getString("fare-evade"));
      } else return false;
    }

    // write the entry station
    records.setStation(serial, entryStation);
    records.setClass(serial, plugin.getConfig().getString("default-class"));

    // player has a transfer discount when they tap out and in within the time limit
    records.setTransfer(serial, System.currentTimeMillis() - records.getTimestamp(serial) < plugin.getConfig().getLong("max-transfer-time"));

    // confirmation
    player.sendMessage(String.format(lang.getString("tapped-out"), entryStation, value));
/*
    // log in IcLogger
    String ukey = System.currentTimeMillis()+"_"+ player.getUniqueId();
    Map<String, Object> logMap = new HashMap<>(Map.ofEntries(
      Map.entry("timestamp", System.currentTimeMillis()),
      Map.entry("uuid", player.getUniqueId().toString()),
      Map.entry("function", "transfer_card"),
      Map.entry("signloc", signLocationVector),
      Map.entry("entry_station", entryStation),
      Map.entry("transfer_station", station),
      Map.entry("journey_finalfare", fare),
      Map.entry("journey_originalfare", fares.getCardFare(entryStation, station, records.getClass(serial))),
      Map.entry("journey_class", records.getClass(serial))
    ));

    // check if a railpass is used, and if so, add the railpass to the log
    if (finalRailPass != null) {
      Map<String, Object> railPassMap = Map.ofEntries(
        Map.entry("journey_railpass_used", finalRailPass),
        Map.entry("journey_railpass_price", owners.getRailPassPrice(finalRailPass)),
        Map.entry("journey_railpass_percentage", owners.getRailPassPercentage(finalRailPass)),
        Map.entry("journey_railpass_start", cardSql.getStart(serial, finalRailPass)),
        Map.entry("journey_railpass_duration", owners.getRailPassDuration(finalRailPass)),
        Map.entry("journey_railpass_operator", owners.getRailPassOperator(finalRailPass))
      );
      logMap.putAll(railPassMap);
    }

    // check if we need to access Records to get OSI data if there is one
    if (records.getTransfer(icCard.getSerial())) {
      Map<String, Object> previousJourneyMap = Map.ofEntries(
        Map.entry("prevjourney_entry", records.getPreviousStation(serial)),
        Map.entry("prevjourney_fare", records.getCurrentFare(serial)),
        Map.entry("prevjourney_class", records.getClass(serial)),
        Map.entry("prevjourney_exittime", records.getTimestamp(serial))
      );
      logMap.putAll(previousJourneyMap);
    }

    // store IC Card details
    logMap.putAll(icCard.toMap());

    // record in logger
    Iciwi.icLogger.record(ukey, logMap);
*/
    return true;
  }


  /**
   * Opens a fare gate
   * @param signAction Main action of the sign, without any flags
   * @param signText Text on the sign for quick accessing
   * @param sign The sign itself
   * @return the Location and Runnable to close the fare gate
   */
  protected static Object[] openGate (String signAction, String[] signText, Sign sign) {
    String signLine0 = signText[0];

    // Get the sign's direction and reference block
    BlockFace signFacing = BlockFace.SOUTH;
    Block referenceBlock = sign.getBlock();
    if (sign.getBlockData() instanceof org.bukkit.block.data.type.WallSign w) {
      signFacing = w.getFacing();
      referenceBlock = sign.getLocation().clone().add(signFacing.getOppositeFace().getDirection()).getBlock();
    }
    else if (sign.getBlockData() instanceof org.bukkit.block.data.type.Sign s) signFacing = s.getRotation();
    signFacing = toCartesian(signFacing);

    // Get the fare gate flags. Tenary avoids the error with String#substring when returning an empty string.
    String args;
    if (signAction.length() == signLine0.length()) args = "";
    else args = signLine0.substring(signAction.length());

    int flags = 0;
    flags |= args.contains("V") ? 1	: 0;	// Validator
    flags |= args.contains("L") ? 2	: 0;	// Lefty
    flags |= args.contains("S") ? 4	: 0;	// Sideways
    flags |= args.contains("D") ? 8	: 0;	// Double
    flags |= args.contains("R") ? 16 : 0;	// Redstone
    flags |= args.contains("E") ? 32 : 0;	// Eye-level
    flags |= args.contains("F") ? 64 : 0;	// Fare gate

    // Get the relative position(s) of the fare gate block(s).
    Vector[] relativePositions = toPos(signFacing, flags);

    // Gate close functions
    Object[] closeGate = {new Location[relativePositions.length], new Runnable[relativePositions.length]};

    // Get the absolute position(s) of the fare gate block(s) (reference block location + relative block vector).
    for (int i = 0; i < relativePositions.length && i < 2; i++) {
      ((Location[]) closeGate[0])[i] = referenceBlock.getLocation().clone().add(relativePositions[i]);
      Block currentBlock = ((Location[]) closeGate[0])[i].getBlock();

      // If openable, open it!
      if (currentBlock.getBlockData() instanceof Openable openable) {
        openable.setOpen(true);
        currentBlock.setBlockData(openable);

        ((Runnable[]) closeGate[1])[i] = () -> {
          openable.setOpen(false);
          currentBlock.setBlockData(openable);
        };

      }

      // If powerable, power it!
      else if (currentBlock.getBlockData() instanceof Powerable powerable) {
        powerable.setPowered(true);
        currentBlock.setBlockData(powerable);

        ((Runnable[]) closeGate[1])[i] = () -> {
          powerable.setPowered(false);
          currentBlock.setBlockData(powerable);
        };
      }

      // If glass pane, create a FareGateBlock object and open
      else if (currentBlock.getBlockData() instanceof Fence || currentBlock.getBlockData() instanceof GlassPane) {
        BlockFace direction = i == 0 ? toFace(toBuildDirection(signFacing, flags)).getOppositeFace() : toFace(toBuildDirection(signFacing, flags));
        FareGateBlock fgBlock = new FareGateBlock(currentBlock, direction, 100);
        fgBlock.openGate();

        ((Runnable[]) closeGate[1])[i] = fgBlock::closeGate;
      }

      // Otherwise, set to air
      else {
        var data = currentBlock.getBlockData();
        currentBlock.setType(Material.AIR);

        ((Runnable[]) closeGate[1])[i] = () -> currentBlock.setBlockData(data);
      }

      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ((Runnable[]) closeGate[1])[i], plugin.getConfig().getLong("gate-close-delay"));
    }
    return closeGate;
  }


  private static BlockFace toFace(Vector vector) {
    if (Objects.equals(vector, BlockFace.EAST.getDirection()) ) return BlockFace.EAST;
    if (Objects.equals(vector, BlockFace.SOUTH.getDirection())) return BlockFace.SOUTH;
    if (Objects.equals(vector, BlockFace.WEST.getDirection()) ) return BlockFace.WEST;
    if (Objects.equals(vector, BlockFace.NORTH.getDirection())) return BlockFace.NORTH;
    else return BlockFace.SELF;
  }

  /**
   * Changes arbitrary sign directions into cartesian (one cardinal direction only) directions.
   * This method has a clockwise bias.
   * @param face the original direction
   * @return the altered direction
   */
  private static BlockFace toCartesian (BlockFace face) {
    if (!face.isCartesian()) {
      return switch (face) {
        case NORTH_WEST, NORTH_NORTH_WEST, NORTH_NORTH_EAST -> BlockFace.NORTH;
        case NORTH_EAST, EAST_NORTH_EAST, EAST_SOUTH_EAST -> BlockFace.EAST;
        case SOUTH_EAST, SOUTH_SOUTH_EAST, SOUTH_SOUTH_WEST -> BlockFace.SOUTH;
        case SOUTH_WEST, WEST_SOUTH_WEST, WEST_NORTH_WEST -> BlockFace.WEST;
        default -> face;
      };
    } else return face;
  }

  /**
   * Gets the direction to build and animate fare gates in.
   * The animation direction should be the opposite direction to the build direction.
   * @param signDirection the direction of the sign
   * @param flags the flags to be applied
   * @return the direction to build fare gates in.
   */
  private static Vector toBuildDirection (BlockFace signDirection, int flags) {
    if ((flags & 1 | flags & 16) != 0) return new Vector();	// Validator and Redstone: no animation/double gate allowed
    else if ((flags & 4) != 0) return signDirection.getDirection();	// Sideways
    else if ((flags & 2) != 0) return signDirection.getDirection().getCrossProduct(new Vector(0, -1, 0));	// Lefty
    else return signDirection.getDirection().getCrossProduct(new Vector(0, 1, 0));	// Normal
  }

  /**
   * Gets the relative positions of the fare gate blocks, with direction accounted for
   * The length of the returned Vector[] can be of length 0, 1, or 2.
   * @param signDirection the sign's facing direction
   * @param flags the flags to be applied
   * @return The positions of the fare gate blocks.
   */
  private static Vector[] toPos (BlockFace signDirection, int flags) {
    // length 0 if validator
    if ((flags & 1) != 0) return new Vector[0];

    // initialise vector array and default position vector
    Vector[] v = (flags & 8) == 0 ? new Vector[1] : new Vector[2];
    v[0] = signDirection.getDirection().getCrossProduct(new Vector(0, 1, 0));

    // parse default, S, E, R, F flags
    if ((flags &  4) != 0) v[0].add(signDirection.getDirection());
    if ((flags & 32) != 0) v[0].subtract(new Vector(0, 1, 0));
    if ((flags & 16) != 0) v[0] = new Vector(0, -2, 0);
    if ((flags & 64) != 0) v[0] = new Vector(0, 2, 0);
    // parse L flag
    if ((flags & 2) != 0 && (signDirection == BlockFace.SOUTH || signDirection == BlockFace.NORTH)) v[0].multiply(new Vector(-1, 1, 1));
    if ((flags & 2) != 0 && (signDirection == BlockFace.EAST  || signDirection == BlockFace.WEST )) v[0].multiply(new Vector(1, 1, -1));

    // parse D flag
    if ((flags & 8) != 0) v[1] = v[0].clone().add(toBuildDirection(signDirection, flags));

    return v;
  }

}
