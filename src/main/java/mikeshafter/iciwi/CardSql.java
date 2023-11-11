package mikeshafter.iciwi;

import mikeshafter.iciwi.config.Owners;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.time.Instant;
import java.util.*;


public class CardSql {

  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final Owners owners = new Owners();

  private Connection connect() {
    // SQLite connection string
    // "jdbc:sqlite:IciwiCards.db"
    String url = plugin.getConfig().getString("database");
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(Objects.requireNonNull(url));
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
    return conn;
  }

  /**
   * Initialise SQL tables
   */
  public void initTables() {
    // SQLite connection string
    // "jdbc:sqlite:IciwiCards.db"
    String url = plugin.getConfig().getString("database");

    // SQL statement for creating a new table
    ArrayList<String> sql = new ArrayList<>(31);
    sql.add("CREATE TABLE IF NOT EXISTS cards (serial TEXT, value TEXT, PRIMARY KEY (serial) ); ");
    sql.add("CREATE TABLE IF NOT EXISTS discounts (serial TEXT REFERENCES cards(serial) ON UPDATE CASCADE, name TEXT, start INTEGER, PRIMARY KEY (serial, name) ); ");

    // Logger tables
    sql.add("CREATE TABLE IF NOT EXISTS log_counts (id INTEGER PRIMARY KEY)");
    sql.add("INSERT OR IGNORE INTO log_counts(id) VALUES (1)");
    sql.add("CREATE TABLE IF NOT EXISTS log_master (id	INTEGER NOT NULL UNIQUE,timestamp	INTEGER NOT NULL,player_uuid	TEXT NOT NULL,PRIMARY KEY(id));");

    sql.add("CREATE TABLE IF NOT EXISTS log_entry (id	INTEGER NOT NULL,sign_x INTEGER, sign_y INTEGER, sign_z INTEGER,entry	TEXT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_prevjourney (id	INTEGER NOT NULL,entry	TEXT,fare	NUMERIC,class	text,exittime	INT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_railpass_store (id	INTEGER NOT NULL,name	TEXT NOT NULL,price	NUMERIC NOT NULL,percentage	NUMERIC NOT NULL,start	INTEGER NOT NULL,duration	INTEGER NOT NULL,operator	TEXT NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_card_use(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_exit (id	INTEGER NOT NULL,sign_x INTEGER, sign_y INTEGER, sign_z INTEGER,entry	TEXT NOT NULL,exit	text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_journey (id	INTEGER NOT NULL,subtotal	real NOT NULL,total	real NOT NULL,class	text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_railpass_use (id	INTEGER NOT NULL,name	text NOT NULL,price	NUMERIC NOT NULL,percentage	NUMERIC NOT NULL,start	INTEGER NOT NULL,duration	INTEGER NOT NULL,operator	TEXT NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_member (id	INTEGER NOT NULL,sign_x INTEGER, sign_y INTEGER, sign_z INTEGER,station	TEXT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_transfer (id	INTEGER NOT NULL,sign_x INTEGER, sign_y INTEGER, sign_z INTEGER,entry	TEXT NOT NULL,transfer	text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_ticket_use (id	INTEGER NOT NULL,from	TEXT NOT NULL,to	text NOT NULL,class	text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_card_use (id	INTEGER NOT NULL,serial	TEXT NOT NULL,value	NUMERIC,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_ticket_create (id	INTEGER NOT NULL,from	TEXT NOT NULL,to	text NOT NULL,class	text NOT NULL, fare NUMERIC NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_card_create (id	INTEGER NOT NULL,serial	TEXT NOT NULL,value	NUMERIC,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_card_topup (id	INTEGER NOT NULL,serial	TEXT NOT NULL,old_value	NUMERIC,added_value	NUMERIC,new_value	NUMERIC,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_railpass_extend (id	INTEGER NOT NULL,new	INTEGER NOT NULL,name	text NOT NULL,price	NUMERIC NOT NULL,percentage	NUMERIC NOT NULL,start	INTEGER NOT NULL,duration	INTEGER NOT NULL,operator	TEXT NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_card_refund (id	INTEGER NOT NULL,serial	TEXT NOT NULL,value	NUMERIC,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");

    try (Connection conn = DriverManager.getConnection(Objects.requireNonNull(url)); Statement statement = conn.createStatement()) {
      for (String s : sql) {
        statement.execute(s);
      }
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  /**
   * Creates a new Iciwi card
   * @param serial Serial number
   * @param value  Starting value of the card
   */
  public void newCard(String serial, double value) {
    String sql = "INSERT INTO cards(serial, value) VALUES(?, ?) ; ";

    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setDouble(2, Math.round(value*100.0)/100.0);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  /**
   * Deletes an existing Iciwi card
   * @param serial Serial number
   */
  public void deleteCard(String serial) {
    String sql = "DELETE FROM cards WHERE serial = ? ;";

    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }


  /**
   * Sets a rail pass for a certain card and operator
   *
   * @param serial Serial number
   * @param name   Name of the rail pass
   * @param start  Start time of the rail pass as number of seconds from the Java epoch of 1970-01-01T00:00:00Z.
   */
  public void setDiscount(String serial, String name, long start) {
    String sql = "INSERT INTO discounts VALUES (?, ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setString(2, name);
      statement.setLong(3, start);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }


  /**
   * Gets all the rail passes of a card. This method also deletes expired rail passes.
   * @param serial Serial number
   * @return Map in the format String name, Long start.
   */
  public Map<String, Long> getAllDiscounts(String serial) {
    String sql = "SELECT name, start FROM discounts WHERE serial = ?";
    HashMap<String, Long> returnValue = new HashMap<>();
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      ResultSet rs = statement.executeQuery();

      while (rs.next()) {
        String name = rs.getString(1);
        // Check if expired
        long expiry = getStart(serial, name) + owners.getRailPassDuration(name);

        if (expiry > Instant.now().getEpochSecond())
          returnValue.put(name, expiry);
        else {
          String sql1 = "DELETE FROM DISCOUNTS WHERE serial = ? AND name = ?";
          final PreparedStatement statement1 = conn.prepareStatement(sql1);
          statement1.setString(1, serial);
          statement1.setString(2, name);
          statement1.executeUpdate();
        }

      }

    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }

    return returnValue;
  }


  /**
   * Gets the start time of a certain railpass belonging to a card
   *
   * @param serial Serial number
   * @param name   Name of the discount (include operator)
   */
  public long getStart(String serial, String name) {
    String sql = "SELECT start FROM discounts WHERE serial = ? AND name = ?";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setString(2, name);
      ResultSet rs = statement.executeQuery();

      // Get the start date
      return rs.getLong(1);

    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      return 0L;
    }
  }


  /**
   * Adds a value to a card
   * @param serial Serial number
   * @param value  Value to be added
   */
  public void addValueToCard(String serial, double value) {
    updateCard(serial, getCardValue(serial)+value);
  }


  /**
   * Changes a value of a card
   * @param serial Serial number
   * @param value  New value of card
   */
  public void updateCard(String serial, double value) {
    String sql = "UPDATE cards SET value = ? WHERE serial = ?";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setDouble(1, Math.round(value*100.0)/100.0);
      statement.setString(2, serial);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }


  /**
   * Gets the value of a card
   * @param serial Serial number
   */
  public double getCardValue(String serial) {
    String sql = "SELECT value FROM cards WHERE serial = ?";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      ResultSet rs = statement.executeQuery();
      return Math.round(rs.getDouble("value")*100.0)/100.0;

    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      return 0d;
    }
  }


  /**
   * Subtracts a value from a card
   * @param serial Serial number
   * @param value  Value to be subtracted
   */
  public void subtractValueFromCard(String serial, double value) {
    updateCard(serial, getCardValue(serial)-value);
  }


  /**
   * Logger method
   * @return the current log id (AFTER incrementing)
   */
  public int incrementCount() {
    String update = "UPDATE log_counts SET id = id + 1";
    String select = "SELECT max(id) FROM log_counts";
    try (Connection conn = this.connect();
         PreparedStatement updateStatement = conn.prepareStatement(update);
         PreparedStatement selectStatement = conn.prepareStatement(select)) {
      updateStatement.executeUpdate();
      ResultSet rs = selectStatement.executeQuery();
      return rs.getInt("id");
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      return 0;
    }
  }

  /**
   * Logger method
   */
  public void logMaster(String player_uuid) {
    long timestamp = System.currentTimeMillis();
    String sql = "INSERT INTO log_master (id, timestamp, player_uuid) VALUES ( (SELECT max(id) FROM log_counts), ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setLong(1, timestamp);
      statement.setString(2, player_uuid);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  /**
   * Logger method
   */
  public void logEntry(int signX, int signY, int signZ, String entry) {
    String sql = "INSERT INTO log_entry (id, sign_x, sign_y, sign_z, entry) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?, ?)";

    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, signX);
      statement.setInt(2, signY);
      statement.setInt(3, signZ);
      statement.setString(4, entry);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }


  /**
   * Logger method
   */
  public void logPrevJourney(String prev_entry, double prev_fare, String prev_fareClass, long exitTime) {
    String sql = "INSERT INTO log_prevjourney (id, entry, fare, class, exittime) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, prev_entry);
      statement.setDouble(2, prev_fare);
      statement.setString(3, prev_fareClass);
      statement.setLong(4, exitTime);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }


  /**
   * Logger method
   */
  public void logExit(int signX, int signY, int signZ, String entry, String exit) {
    String sql = "INSERT INTO log_exit (id, sign_x, sign_y, sign_z, entry, exit) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?, ?, ?)";

    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, signX);
      statement.setInt(2, signY);
      statement.setInt(3, signZ);
      statement.setString(4, entry);
      statement.setString(5, exit);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }


  /**
   * Logger method
   */
  public void logJourney(double subtotal, double total, String fareClass) {
    String sql = "INSERT INTO log_journey (id, subtotal, total, class) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setDouble(1, subtotal);
      statement.setDouble(2, total);
      statement.setString(3, fareClass);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }


  /**
   * Logger method
   */
  public void logMember(int signX, int signY, int signZ, String station) {
    String sql = "INSERT INTO log_member (id, sign_x, sign_y, sign_z, station) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?, ?)";

    try (Connection conn = this.connect();  PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, signX);
      statement.setInt(2, signY);
      statement.setInt(3, signZ);
      statement.setString(4, station);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }


  /**
   * Logger method
   */
  public void logTransfer(int signX, int signY, int signZ, String entry, String transfer) {
    String sql = "INSERT INTO log_transfer (id, sign_x, sign_y, sign_z, entry, transfer) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?, ?, ?)";

    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setInt(1, signX);
      statement.setInt(2, signY);
      statement.setInt(3, signZ);
      statement.setString(4, entry);
      statement.setString(5, transfer);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logRailpassStore(String name, double price, double percentage, long start, long duration, String operator) {
    String sql = "INSERT INTO log_railpass_store (id, name, price, percentage, start, duration, operator) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?, ?, ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {

      statement.setString(1, name);
      statement.setDouble(2, price);
      statement.setDouble(3, percentage);
      statement.setLong(4, start);
      statement.setLong(5, duration);
      statement.setString(6, operator);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logRailpassUse(String name, double price, double percentage, long start, long duration, String operator) {
    String sql = "INSERT INTO log_railpass_use (id, name, price, percentage, start, duration, operator) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?, ?, ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {

      statement.setString(1, name);
      statement.setDouble(2, price);
      statement.setDouble(3, percentage);
      statement.setLong(4, start);
      statement.setLong(5, duration);
      statement.setString(6, operator);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logTicketUse(String from, String to, String travelClass) {
    String sql = "INSERT INTO log_ticket_use (id, from, to, class) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {

      statement.setString(1, from);
      statement.setString(2, to);
      statement.setString(3, travelClass);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logCardUse(String serial) {
    String sql = "INSERT INTO log_card_use (id, serial, value) VALUES ( (SELECT max(id) FROM log_counts), ?, (SELECT value FROM cards WHERE serial = ?))";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setString(2, serial);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
    Map<String, Long> currentPasses = getAllDiscounts(serial);
    for (var name : currentPasses.keySet()) {
      logRailpassStore(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), getStart(serial, name), owners.getRailPassDuration(name), owners.getRailPassOperator(name));
    }
  }

  public void logTicketCreate(String from, String to, String travelClass, double fare) {
    String sql = "INSERT INTO log_ticket_create (id, from, to, class, fare) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {

      statement.setString(1, from);
      statement.setString(2, to);
      statement.setString(3, travelClass);
      statement.setDouble(4, fare);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logCardCreate(String serial, double value) {
    String sql = "INSERT INTO log_card_create (id, serial, value) VALUES ( (SELECT max(id) FROM log_counts), ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {

      statement.setString(1, serial);
      statement.setDouble(2, value);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logCardTopup(String serial, double oldValue, double addedValue, double newValue) {
    String sql = "INSERT INTO log_card_topup (id, serial, old_value, added_value, new_value) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {

      statement.setString(1, serial);
      statement.setDouble(2, oldValue);
      statement.setDouble(3, addedValue);
      statement.setDouble(4, newValue);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logRailpassExtend(int newDuration, String name, double price, double percentage, long start, long duration, String operator) {
    String sql = "INSERT INTO log_railpass_extend (id, new, name, price, percentage, start, duration, operator) VALUES ( (SELECT max(id) FROM log_counts), ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {

      statement.setInt(1, newDuration);
      statement.setString(2, name);
      statement.setDouble(3, price);
      statement.setDouble(4, percentage);
      statement.setLong(5, start);
      statement.setLong(6, duration);
      statement.setString(7, operator);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logCardRefund(String serial, double value) {
    String sql = "INSERT INTO log_card_refund (id, serial, value) VALUES ( (SELECT max(id) FROM log_counts), ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setDouble(2, value);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

}