package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.config.*;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.IcCard;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import mikeshafter.iciwi.util.FareGateBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Fence;
import org.bukkit.entity.Player;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.util.Vector;


public class CardUtil {
  private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private static final Records records = new Records();
  private static final Lang lang = new Lang();
  private static final Fares fares = new Fares();
  private static final Owners owners = new Owners();
  private static final CardSql cardSql = new CardSql();


  /**
   * Register entry from a card
   * @param player Player who used the card
   * @param icCard The card to register
   * @param entryStation The station at which to enter
   * @return Whether entry was successful. If false, do not open the fare gate.
   */
  protected static boolean entry(Player player, IcCard icCard, String entryStation) {
    double value = icCard.getValue();
    String serial = icCard.getSerial();

    // reject entry if card has less than the minimum value
    if (value < plugin.getConfig().getDouble("min-amount")) return false;

    // was the card already used to enter the network?
    if (records.getString("station."+serial) == null) {
      player.sendMessage(lang.getString("cannot-pass"));
      if (plugin.getConfig().getBoolean("open-on-penalty")) {
        Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
        player.sendMessage(lang.getString("fare-evade"));
      } else return false;
    }

    // write the entry station to records
    records.set("station." + serial, entryStation);

    // check whether the player tapped out and in within the time limit
    if (System.currentTimeMillis() - records.getLong("timestamp." + serial) < plugin.getConfig().getLong("max-transfer-time"))
      records.set("has-transfer." + serial, true);
    else
      records.set("has-transfer." + serial, false);

    // confirmation
    player.sendMessage(String.format(lang.getString("tapped-in"), entryStation, value));
    return true;
  }


  /**
  * Register exit from a card
  * @param player Player who used the card
  * @param icCard The card to register
  * @param exitStation The station at which to exit
  * @return Whether exit was successful. If false, do not open the fare gate.
  */
  protected static boolean exit(Player player, IcCard icCard, String exitStation) {
    String serial = icCard.getSerial();
    String entryStation = records.getString("entry-station."+serial);
    double value = icCard.getValue();
    double fare = fares.getFare(entryStation, exitStation, plugin.getConfig().getString("default-class") /* TODO: Change this to use actual fare classes */);

    // is the card already in the network?
    if (records.getString("station."+serial) == null) {
      player.sendMessage(lang.getString("cannot-pass"));
      if (plugin.getConfig().getBoolean("open-on-penalty")) {
        Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
        player.sendMessage(lang.getString("fare-evade"));
      } else return false;
    }

    // If an OSI is applicable, use the fare from the first entry station until the exit station
    if (records.getBoolean("has-transfer."+serial)) {
      // fare if the player did not tap out
      double longFare = fares.getFare(records.getString("previous-entry-station."+serial), exitStation);
      // the previous charged fare
      double previousFare = records.getDouble("current-fare."+serial);
      // if the difference between the fares is less than the current fare, change the fare to that difference.
      if (longFare-previousFare < fare) fare = longFare-previousFare;
    }

    // Get the owners of stations and rail passes
    List<String> entryStationOwners = owners.getOwners(entryStation);
    List<String> exitStationOwners	= owners.getOwners(exitStation);
    Set<String> railPasses = cardSql.getAllDiscounts(serial).keySet();
    Set<String> railPassOwners = new HashSet<>();
    railPasses.forEach(r -> railPassOwners.add(owners.getRailPassOperator(r)));

    if (IciwiUtil.any(railPassOwners, entryStationOwners) && IciwiUtil.any(railPassOwners, exitStationOwners)) {
      // Get cheapest discount
      double payPercentage = 1d;
      for (var r: railPasses) {
        if (payPercentage > owners.getRailPassPercentage(r)) payPercentage = owners.getRailPassPercentage(r);
      }

      // Set final fare
      fare *= payPercentage;
    }

    // check if card value is low
    if (value < fare) {
      player.sendMessage(lang.getString("value-low"));
      return false;
    }

    icCard.withdraw(fare);

    // set details for future transfer
    records.set("timestamp."+serial, System.currentTimeMillis());
    records.set("previous-entry-station."+serial, entryStation);
    records.set("entry-station."+serial, null);
    records.set("current-fare."+serial, fare);

    return true;
  }


  /**
   * Check if a card has a railpass
   * @param player Player who used the card
   * @param icCard The card used
   * @param station The station at which the sign is placed
   * @return Whether checks were successful. If false, do not open the fare gate.
   */
  protected static boolean member(Player player, IcCard icCard, String station) {
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
  protected static boolean transfer(Player player, IcCard icCard, String station) {
    String serial = icCard.getSerial();

    // If an OSI was detected, cancel OSI capability
    if (records.getBoolean("has-transfer." + serial)) {
      records.set("has-transfer." + serial, false);
      player.sendMessage(lang.getString("transfer-cancel-osi"));
      return true;
    }

    // Else perform normal exit, then entry sequence
    String entryStation = records.getString("entry-station."+serial);
    double value = icCard.getValue();
    double fare = fares.getFare(entryStation, station, plugin.getConfig().getString("default-class") /* TODO: Change this to use actual fare classes */);

    // is the card already in the network?
    if (records.getString("station."+serial) == null) {
      player.sendMessage(lang.getString("cannot-pass"));
      if (plugin.getConfig().getBoolean("open-on-penalty")) {
        Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
        player.sendMessage(lang.getString("fare-evade"));
      } else return false;
    }

    // Get the owners of stations and rail passes
    List<String> entryStationOwners = owners.getOwners(entryStation);
    List<String> exitStationOwners	= owners.getOwners(station);
    Set<String> railPasses = cardSql.getAllDiscounts(serial).keySet();
    Set<String> railPassOwners = new HashSet<>();
    railPasses.forEach(r -> railPassOwners.add(owners.getRailPassOperator(r)));

    if (IciwiUtil.any(railPassOwners, entryStationOwners) && IciwiUtil.any(railPassOwners, exitStationOwners)) {
      // Get cheapest discount
      double payPercentage = 1d;
      for (var r: railPasses) {
        if (payPercentage > owners.getRailPassPercentage(r)) payPercentage = owners.getRailPassPercentage(r);
      }

      // Set final fare
      fare *= payPercentage;
    }

    // check if card value is low
    if (value < fare) {
      player.sendMessage(lang.getString("value-low"));
      return false;
    }

    icCard.withdraw(fare);

    // set details for future transfer
    records.set("timestamp."+serial, System.currentTimeMillis());
    records.set("previous-entry-station."+serial, entryStation);
    records.set("entry-station."+serial, null);
    records.set("current-fare."+serial, fare);

    // Perform entry sequence
    // reject entry if card has less than the minimum value
    if (value < plugin.getConfig().getDouble("min-amount")) return false;

    // was the card already used to enter the network?
    if (records.getString("station."+serial) == null) {
      player.sendMessage(lang.getString("cannot-pass"));
      if (plugin.getConfig().getBoolean("open-on-penalty")) {
        Iciwi.economy.withdrawPlayer(player, plugin.getConfig().getDouble("penalty"));
        player.sendMessage(lang.getString("fare-evade"));
      } else return false;
    }

    // write the entry station to records
    records.set("station." + serial, entryStation);

    // check whether the player tapped out and in within the time limit
    if (System.currentTimeMillis() - records.getLong("timestamp." + serial) < plugin.getConfig().getLong("max-transfer-time"))
      records.set("has-transfer." + serial, true);
    else
      records.set("has-transfer." + serial, false);

    // confirmation
    player.sendMessage(String.format(lang.getString("tapped-in"), entryStation, value));
    return true;
  }


  /**
   * Opens a fare gate
   * @param signText Text on the sign for quick accessing
   * @param sign The sign itself
   */
  protected static void openGate(String signAction, String[] signText, Sign sign) {
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

    // Get the fare gate flags
    String args = signLine0.substring(signAction.length() + 1, signLine0.length() - 1);

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

    // Get the absolute position(s) of the fare gate block(s) (reference block location + relative block vector).
    for (int i = 0; i < relativePositions.length && i < 2; i++) {
      Block currentBlock = referenceBlock.getLocation().clone().add(relativePositions[i]).getBlock();

      // Gate close function
      Runnable closeGate;

      // If openable, open it!
      if (currentBlock.getBlockData() instanceof Openable openable) {
        openable.setOpen(true);
        currentBlock.setBlockData(openable);

        closeGate = () -> {
          openable.setOpen(false);
          currentBlock.setBlockData(openable);
        };

      }

      // If powerable, power it!
      else if (currentBlock.getBlockData() instanceof Powerable powerable) {
        powerable.setPowered(true);
        currentBlock.setBlockData(powerable);

        closeGate = () -> {
          powerable.setPowered(false);
          currentBlock.setBlockData(powerable);
        };

      }

      // If glass pane, create a FareGateBlock object and open
      else if (currentBlock.getBlockData() instanceof Fence) {
        BlockFace direction = i == 0 ? toFace(toBuildDirection(signFacing, flags)).getOppositeFace() : toFace(toBuildDirection(signFacing, flags));
        FareGateBlock fgBlock = new FareGateBlock(currentBlock, direction, 100);
        fgBlock.openGate();

        closeGate = fgBlock::closeGate;

      }

      // Otherwise, set to air
      else {
        var data = currentBlock.getBlockData();
        currentBlock.setType(Material.AIR);

        closeGate = () -> currentBlock.setBlockData(data);
      }

      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, closeGate, plugin.getConfig().getLong("gate-close-delay"));

    }
  }

// TESTABLE BEGIN

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
  public static BlockFace toCartesian (BlockFace face) {
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
  public static Vector toBuildDirection (BlockFace signDirection, int flags) {
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
  public static Vector[] toPos (BlockFace signDirection, int flags) {
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

    // return
    return v;
  }

// TESTABLE END

}
