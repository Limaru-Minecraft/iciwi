package mikeshafter.iciwi;

import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.time.Instant;
import java.util.*;


public class CardSql {
  
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
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
    LinkedList<String> sql = new LinkedList<>();
    sql.add("CREATE TABLE IF NOT EXISTS cards (serial TEXT, value TEXT, PRIMARY KEY (serial) ); ");
    sql.add("CREATE TABLE IF NOT EXISTS discounts (serial TEXT REFERENCES cards(serial) ON UPDATE CASCADE, name TEXT, start INTEGER, PRIMARY KEY (serial, name) ); ");
    sql.add("CREATE TABLE IF NOT EXISTS railpasses (name TEXT, operator TEXT, duration INTEGER, percentage REAL, price REAL, PRIMARY KEY (name) ); ");
    
    // Logger tables
    sql.add("CREATE TABLE IF NOT EXISTS log_counts (id INTEGER PRIMARY KEY)");
    sql.add("CREATE TABLE IF NOT EXISTS log_master (id	INTEGER NOT NULL UNIQUE,timestamp	INTEGER NOT NULL,player_uuid	TEXT NOT NULL,PRIMARY KEY(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_entry (id	INTEGER NOT NULL,signloc	BLOB,entry	TEXT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_prevjourney (id	INTEGER NOT NULL,entry	TEXT,fare	NUMERIC,class	text,exittime	INT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_railpass_store (id	INTEGER NOT NULL,name	TEXT NOT NULL,price	NUMERIC NOT NULL,percentage	NUMERIC NOT NULL,start	INTEGER NOT NULL,duration	INTEGER NOT NULL,operator	TEXT NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_card_use(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_exit (id	INTEGER NOT NULL,signloc	BLOB,entry	TEXT NOT NULL,exit	text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_journey (id	INTEGER NOT NULL,subtotal	real NOT NULL,total	real NOT NULL,class	text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_railpass_use (id	INTEGER NOT NULL,name	text NOT NULL,price	NUMERIC NOT NULL,percentage	NUMERIC NOT NULL,start	INTEGER NOT NULL,duration	INTEGER NOT NULL,operator	TEXT NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_member (id	INTEGER NOT NULL,signloc	BLOB,station	TEXT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_transfer (id	INTEGER NOT NULL,signloc	BLOB,entry	TEXT NOT NULL,transfer	text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_ticket_use (id	INTEGER NOT NULL,from	TEXT NOT NULL,to	text NOT NULL,class	text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_card_use (id	INTEGER NOT NULL,serial	TEXT NOT NULL,value	NUMERIC,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
    sql.add("CREATE TABLE IF NOT EXISTS log_ticket_create (id	INTEGER NOT NULL,from	TEXT NOT NULL,to	text NOT NULL,class	text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
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
        long expiry = getExpiry(serial, name);
        
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
   * Gets the expiry time of a certain railpass belonging to a card
   *
   * @param serial Serial number
   * @param name   Name of the discount (include operator)
   */
  public long getExpiry(String serial, String name) {
    String sql = "SELECT start FROM discounts WHERE serial = ? AND name = ?";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setString(2, name);
      ResultSet rs = statement.executeQuery();
      
      // Get the start date
      long start = rs.getLong(1);
      
      // Get the end date
      String sql1 = "SELECT duration FROM railpasses WHERE name = ?";
      final PreparedStatement statement1 = conn.prepareStatement(sql1);
      statement1.setString(1, name);
      rs = statement.executeQuery();
      long duration = rs.getLong(1);
      
      return start+duration;
      
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
   * Gets the rail passes sold by an operator
   * @param operator Operator to query
   */
  public SortedSet<String> getRailPassNames(String operator) {
    String sql = "SELECT name FROM railpasses WHERE operator = ? ;";
    SortedSet<String> set = new TreeSet<>();
    
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, operator);
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        set.add(rs.getString(1));
      }
      return set;
      
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      return null;
    }
  }

  public void logEntry(String player_uuid, Vector signloc, String entry) {
    long timestamp = System.currentTimeMillis();
    
    String incrCountSql = "UPDATE log_counts SET id = id + 1";
    String logMasterSql = "INSERT INTO log_master (id, timestamp, player_uuid) VALUES ((SELECT id FROM log_counts), ?, ?)";
    String logEntrySql = "INSERT INTO log_entry (id, signloc, entry) VALUES ((SELECT id FROM log_counts), ?, ?)";
    
    try (Connection conn = this.connect(); 
      PreparedStatement logMasterStmt = conn.prepareStatement(logMasterSql);
      PreparedStatement logEntryStmt = conn.prepareStatement(logEntrySql);
      PreparedStatement incrCountStmt = conn.prepareStatement(incrCountSql)) {

      logMasterStmt.setInt(1, id);
      logMasterStmt.setLong(2, timestamp);
      logMasterStmt.setString(3, player_uuid);
      logMasterStmt.executeUpdate();

      logEntryStmt.setInt(1, id);
      logEntryStmt.setObject(2, signloc);
      logEntryStmt.setString(3, entry);
      logEntryStmt.executeUpdate();

      incrCountStmt.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logEntryAndPrevJourney(String player_uuid, Vector signloc, String entry, String fare, String travelClass, long exitTime) {
      long timestamp = System.currentTimeMillis();

      String incrCountSql = "UPDATE log_counts SET id = id + 1";
      String logMasterSql = "INSERT INTO log_master (id, timestamp, player_uuid) VALUES ((SELECT id FROM log_counts), ?, ?)";
      String logEntrySql = "INSERT INTO log_entry (id, signloc, entry) VALUES ((SELECT id FROM log_counts), ?, ?)";
      String logPrevJourneySql = "INSERT INTO log_prevjourney (id, entry, fare, class, exittime) VALUES ((SELECT id FROM log_counts), ?, ?, ?, ?)";

      try (Connection conn = this.connect(); 
          PreparedStatement logMasterStmt = conn.prepareStatement(logMasterSql);
          PreparedStatement logEntryStmt = conn.prepareStatement(logEntrySql);
          PreparedStatement logPrevJourneyStmt = conn.prepareStatement(logPrevJourneySql);
          PreparedStatement incrCountStmt = conn.prepareStatement(incrCountSql)) {

          logMasterStmt.setLong(1, timestamp);
          logMasterStmt.setString(2, player_uuid);
          logMasterStmt.executeUpdate();

          logEntryStmt.setObject(1, signloc);
          logEntryStmt.setString(2, entry);
          logEntryStmt.executeUpdate();

          logPrevJourneyStmt.setString(1, entry);
          logPrevJourneyStmt.setString(2, fare);
          logPrevJourneyStmt.setString(3, travelClass);
          logPrevJourneyStmt.setLong(4, exitTime);
          logPrevJourneyStmt.executeUpdate();

          incrCountStmt.executeUpdate();
      } catch (SQLException e) {
          plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

  public void logExitAndJourney(String player_uuid, Vector signloc, String entry, String exit, double fare) {
    long timestamp = System.currentTimeMillis();

    String incrCountSql = "UPDATE log_counts SET count = count + 1";
    String logMasterSql = "INSERT INTO log_master (id, timestamp, player_uuid) VALUES ((SELECT count FROM log_counts), ?, ?)";
    String logExitSql = "INSERT INTO log_exit (id, signloc, entry, exit, fare) VALUES ((SELECT count FROM log_counts), ?, ?, ?, ?)";
    String logJourneySql = "INSERT INTO log_journey (id, entry, exit, fare) VALUES ((SELECT count FROM log_counts), ?, ?, ?)";

    try (Connection conn = this.connect(); 
       PreparedStatement logMasterStmt = conn.prepareStatement(logMasterSql);
       PreparedStatement logExitStmt = conn.prepareStatement(logExitSql);
       PreparedStatement logJourneyStmt = conn.prepareStatement(logJourneySql);
       PreparedStatement incrCountStmt = conn.prepareStatement(incrCountSql)) {

      logMasterStmt.setLong(1, timestamp);
      logMasterStmt.setString(2, player_uuid);
      logMasterStmt.executeUpdate();

      logExitStmt.setObject(1, signloc);
      logExitStmt.setString(2, entry);
      logExitStmt.setString(3, exit);
      logExitStmt.setDouble(4, fare);
      logExitStmt.executeUpdate();

      logJourneyStmt.setString(1, entry);
      logJourneyStmt.setString(2, exit);
      logJourneyStmt.setDouble(3, fare);
      logJourneyStmt.executeUpdate();

      incrCountStmt.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logExitAndPrevJourney(String player_uuid, Vector signloc, String entry, String exit, String fare, String travelClass, long exitTime) {
      long timestamp = System.currentTimeMillis();

      String incrCountSql = "UPDATE log_counts SET id = id + 1";
      String logMasterSql = "INSERT INTO log_master (id, timestamp, player_uuid) VALUES ((SELECT id FROM log_counts), ?, ?)";
    String logJourneySql = "INSERT INTO log_journey (id, entry, exit, fare) VALUES ((SELECT count FROM log_counts), ?, ?, ?)";
      String logPrevJourneySql = "INSERT INTO log_prevjourney (id, entry, fare, class, exittime) VALUES ((SELECT id FROM log_counts), ?, ?, ?, ?)";
      String logExitSql = "INSERT INTO log_exit (id, signloc, entry, exit) VALUES ((SELECT id FROM log_counts), ?, ?, ?)";

      try (Connection conn = this.connect(); 
          PreparedStatement logMasterStmt = conn.prepareStatement(logMasterSql);
            PreparedStatement logJourneyStmt = conn.prepareStatement(logJourneySql);
          PreparedStatement logPrevJourneyStmt = conn.prepareStatement(logPrevJourneySql);
          PreparedStatement logExitStmt = conn.prepareStatement(logExitSql);
          PreparedStatement incrCountStmt = conn.prepareStatement(incrCountSql)) {

          logMasterStmt.setLong(1, timestamp);
          logMasterStmt.setString(2, player_uuid);
          logMasterStmt.executeUpdate();

        logJourneyStmt.setString(1, entry);
        logJourneyStmt.setString(2, exit);
        logJourneyStmt.setDouble(3, fare);
        logJourneyStmt.executeUpdate();

          logPrevJourneyStmt.setString(1, entry);
          logPrevJourneyStmt.setString(2, fare);
          logPrevJourneyStmt.setString(3, travelClass);
          logPrevJourneyStmt.setLong(4, exitTime);
          logPrevJourneyStmt.executeUpdate();

          logExitStmt.setObject(1, signloc);
          logExitStmt.setString(2, entry);
          logExitStmt.setString(3, exit);
          logExitStmt.executeUpdate();

          incrCountStmt.executeUpdate();
      } catch (SQLException e) {
          plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

  public void logMember(String player_uuid, Vector signloc, String station) {
    long timestamp = System.currentTimeMillis();

    String incrCountSql = "UPDATE log_counts SET id = id + 1";
    String logMasterSql = "INSERT INTO log_master (id, timestamp, player_uuid) VALUES ((SELECT id FROM log_counts), ?, ?)";
    String logMemberSql = "INSERT INTO log_member (id, signloc, station) VALUES ((SELECT id FROM log_counts), ?, ?)";

    try (Connection conn = this.connect(); 
        PreparedStatement logMasterStmt = conn.prepareStatement(logMasterSql);
        PreparedStatement logMemberStmt = conn.prepareStatement(logMemberSql);
        PreparedStatement incrCountStmt = conn.prepareStatement(incrCountSql)) {

        logMasterStmt.setLong(1, timestamp);
        logMasterStmt.setString(2, player_uuid);
        logMasterStmt.executeUpdate();

        logMemberStmt.setObject(1, signloc);
        logMemberStmt.setString(2, station);
        logMemberStmt.executeUpdate();

        incrCountStmt.executeUpdate();
    } catch (SQLException e) {
        plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void logTransferAndPrevJourney(String player_uuid, Vector signloc, String entry, String transfer, String fare, String travelClass, long exitTime) {
      long timestamp = System.currentTimeMillis();

      String incrCountSql = "UPDATE log_counts SET id = id + 1";
      String logMasterSql = "INSERT INTO log_master (id, timestamp, player_uuid) VALUES ((SELECT id FROM log_counts), ?, ?)";
      String logJourneySql = "INSERT INTO log_journey (id, entry, exit, fare) VALUES ((SELECT count FROM log_counts), ?, ?, ?)";
      String logPrevJourneySql = "INSERT INTO log_prevjourney (id, entry, fare, class, exittime) VALUES ((SELECT id FROM log_counts), ?, ?, ?, ?)";
      String logTransferSql = "INSERT INTO log_transfer (id, signloc, entry, transfer) VALUES ((SELECT id FROM log_counts), ?, ?, ?)";

      try (Connection conn = this.connect(); 
          PreparedStatement logMasterStmt = conn.prepareStatement(logMasterSql);
          PreparedStatement logJourneyStmt = conn.prepareStatement(logJourneySql);
          PreparedStatement logPrevJourneyStmt = conn.prepareStatement(logPrevJourneySql);
          PreparedStatement logTransferStmt = conn.prepareStatement(logTransferSql);
          PreparedStatement incrCountStmt = conn.prepareStatement(incrCountSql)) {

          logMasterStmt.setLong(1, timestamp);
          logMasterStmt.setString(2, player_uuid);
          logMasterStmt.executeUpdate();

          logJourneyStmt.setString(1, entry);
          logJourneyStmt.setString(2, transfer);
          logJourneyStmt.setString(3, fare);
          logJourneyStmt.executeUpdate();

          logPrevJourneyStmt.setString(1, entry);
          logPrevJourneyStmt.setString(2, fare);
          logPrevJourneyStmt.setString(3, travelClass);
          logPrevJourneyStmt.setLong(4, exitTime);
          logPrevJourneyStmt.executeUpdate();

          logTransferStmt.setObject(1, signloc);
          logTransferStmt.setString(2, entry);
          logTransferStmt.setString(3, transfer);
          logTransferStmt.executeUpdate();

          incrCountStmt.executeUpdate();
      } catch (SQLException e) {
          plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

  public void logTransferAndJourney(String player_uuid, Vector signloc, String entry, String transfer, String exit, double fare) {
      long timestamp = System.currentTimeMillis();

      String incrCountSql = "UPDATE log_counts SET count = count + 1";
      String logMasterSql = "INSERT INTO log_master (id, timestamp, player_uuid) VALUES ((SELECT count FROM log_counts), ?, ?)";
      String logExitSql = "INSERT INTO log_exit (id, signloc, entry, exit, fare) VALUES ((SELECT count FROM log_counts), ?, ?, ?, ?)";
      String logTransferSql = "INSERT INTO log_transfer (id, signloc, entry, transfer) VALUES ((SELECT count FROM log_counts), ?, ?, ?)";
      String logJourneySql = "INSERT INTO log_journey (id, entry, exit, fare) VALUES ((SELECT count FROM log_counts), ?, ?, ?)";

      try (Connection conn = this.connect(); 
         PreparedStatement logMasterStmt = conn.prepareStatement(logMasterSql);
         PreparedStatement logExitStmt = conn.prepareStatement(logExitSql);
         PreparedStatement logTransferStmt = conn.prepareStatement(logTransferSql);
         PreparedStatement logJourneyStmt = conn.prepareStatement(logJourneySql);
         PreparedStatement incrCountStmt = conn.prepareStatement(incrCountSql)) {

        logMasterStmt.setLong(1, timestamp);
        logMasterStmt.setString(2, player_uuid);
        logMasterStmt.executeUpdate();

        logExitStmt.setObject(1, signloc);
        logExitStmt.setString(2, entry);
        logExitStmt.setString(3, exit);
        logExitStmt.setDouble(4, fare);
        logExitStmt.executeUpdate();

        logTransferStmt.setObject(1, signloc);
        logTransferStmt.setString(2, entry);
        logTransferStmt.setString(3, transfer);
        logTransferStmt.executeUpdate();

        logJourneyStmt.setString(1, entry);
        logJourneyStmt.setString(2, exit);
        logJourneyStmt.setDouble(3, fare);
        logJourneyStmt.executeUpdate();

        incrCountStmt.executeUpdate();
      } catch (SQLException e) {
        plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

  public void logRailpassStore(int id, String name, double price, double percentage, long start, long duration, String operator) {
      String sql = "INSERT INTO log_railpass_store (id, name, price, percentage, start, duration, operator) VALUES (?, ?, ?, ?, ?, ?, ?)";
      try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
          statement.setInt(1, id);
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

  public void logRailpassUse(int id, String name, double price, double percentage, long start, long duration, String operator) {
      String sql = "INSERT INTO log_railpass_use (id, name, price, percentage, start, duration, operator) VALUES (?, ?, ?, ?, ?, ?, ?)";
      try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
          statement.setInt(1, id);
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

  public void logTicketUse(int id, String from, String to, String travelClass) {
      String sql = "INSERT INTO log_ticket_use (id, from, to, class) VALUES (?, ?, ?, ?)";
      try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
          statement.setInt(1, id);
          statement.setString(2, from);
          statement.setString(3, to);
          statement.setString(4, travelClass);
          statement.executeUpdate();
      } catch (SQLException e) {
          plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

  public void logCardUse(int id, String serial, double value) {
      String sql = "INSERT INTO log_card_use (id, serial, value) VALUES (?, ?, ?)";
      try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
          statement.setInt(1, id);
          statement.setString(2, serial);
          statement.setDouble(3, value);
          statement.executeUpdate();
      } catch (SQLException e) {
          plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

  public void logTicketCreate(int id, String from, String to, String travelClass) {
      String sql = "INSERT INTO log_ticket_create (id, from, to, class) VALUES (?, ?, ?, ?)";
      try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
          statement.setInt(1, id);
          statement.setString(2, from);
          statement.setString(3, to);
          statement.setString(4, travelClass);
          statement.executeUpdate();
      } catch (SQLException e) {
          plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

  public void logCardCreate(int id, String serial, double value) {
      String sql = "INSERT INTO log_card_create (id, serial, value) VALUES (?, ?, ?)";
      try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
          statement.setInt(1, id);
          statement.setString(2, serial);
          statement.setDouble(3, value);
          statement.executeUpdate();
      } catch (SQLException e) {
          plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

  public void logCardTopup(int id, String serial, double oldValue, double addedValue, double newValue) {
      String sql = "INSERT INTO log_card_topup (id, serial, old_value, added_value, new_value) VALUES (?, ?, ?, ?, ?)";
      try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
          statement.setInt(1, id);
          statement.setString(2, serial);
          statement.setDouble(3, oldValue);
          statement.setDouble(4, addedValue);
          statement.setDouble(5, newValue);
          statement.executeUpdate();
      } catch (SQLException e) {
          plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

  public void logRailpassExtend(int id, int newDuration, String name, double price, double percentage, long start, long duration, String operator) {
      String sql = "INSERT INTO log_railpass_extend (id, new, name, price, percentage, start, duration, operator) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
          statement.setInt(1, id);
          statement.setInt(2, newDuration);
          statement.setString(3, name);
          statement.setDouble(4, price);
          statement.setDouble(5, percentage);
          statement.setLong(6, start);
          statement.setLong(7, duration);
          statement.setString(8, operator);
          statement.executeUpdate();
      } catch (SQLException e) {
          plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

  public void logCardRefund(int id, String serial, double value) {
      String sql = "INSERT INTO log_card_refund (id, serial, value) VALUES (?, ?, ?)";
      try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
          statement.setInt(1, id);
          statement.setString(2, serial);
          statement.setDouble(3, value);
          statement.executeUpdate();
      } catch (SQLException e) {
          plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      }
  }

}