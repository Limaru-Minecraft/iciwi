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
    sql.add("CREATE TABLE IF NOT EXISTS 'log_master' ('id'	INTEGER NOT NULL UNIQUE,'timestamp'	INTEGER NOT NULL,'username'	TEXT NOT NULL,PRIMARY KEY('id' AUTOINCREMENT));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_entry' ('signloc'	BLOB,'entry'	TEXT,'id'	INTEGER NOT NULL,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_prevjourney' ('id'	INTEGER NOT NULL,'entry'	TEXT,'fare'	NUMERIC,'class'	text,'exittime'	INT,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_railpass_store' ('id'	INTEGER NOT NULL,'name'	TEXT NOT NULL,'price'	NUMERIC NOT NULL,'percentage'	NUMERIC NOT NULL,'start'	INTEGER NOT NULL,'duration'	INTEGER NOT NULL,'operator'	TEXT NOT NULL,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_card_use'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_exit' ('id'	INTEGER NOT NULL,'signloc'	BLOB,'entry'	TEXT NOT NULL,'exit'	text NOT NULL,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_journey' ('id'	INTEGER NOT NULL,'subtotal'	real NOT NULL,'total'	real NOT NULL,'class'	text NOT NULL,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_railpass_use' ('id'	INTEGER NOT NULL,'name'	text NOT NULL,'price'	NUMERIC NOT NULL,'percentage'	NUMERIC NOT NULL,'start'	INTEGER NOT NULL,'duration'	INTEGER NOT NULL,'operator'	TEXT NOT NULL,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_member' ('id'	INTEGER NOT NULL,'signloc'	BLOB,'station'	TEXT,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_transfer' ('id'	INTEGER NOT NULL,'signloc'	BLOB,'entry'	TEXT NOT NULL,'transfer'	text NOT NULL,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_ticket_use' ('id'	INTEGER NOT NULL,'from'	TEXT NOT NULL,'to'	text NOT NULL,'class'	text NOT NULL,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_card_use' ('id'	INTEGER NOT NULL,'serial'	TEXT NOT NULL,'value'	NUMERIC,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_ticket_create' ('id'	INTEGER NOT NULL,'from'	TEXT NOT NULL,'to'	text NOT NULL,'class'	text NOT NULL,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_card_create' ('id'	INTEGER NOT NULL,'serial'	TEXT NOT NULL,'value'	NUMERIC,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_card_topup' ('id'	INTEGER NOT NULL,'serial'	TEXT NOT NULL,'old_value'	NUMERIC,'added_value'	NUMERIC,'new_value'	NUMERIC,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_railpass_extend' ('id'	INTEGER NOT NULL,'new'	INTEGER NOT NULL,'name'	text NOT NULL,'price'	NUMERIC NOT NULL,'percentage'	NUMERIC NOT NULL,'start'	INTEGER NOT NULL,'duration'	INTEGER NOT NULL,'operator'	TEXT NOT NULL,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");
    sql.add("CREATE TABLE IF NOT EXISTS 'log_card_refund' ('id'	INTEGER NOT NULL,'serial'	TEXT NOT NULL,'value'	NUMERIC,PRIMARY KEY('id'),FOREIGN KEY('id') REFERENCES 'log_master'('id'));");

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


  /**
   * Logs data into Iciwi's logger
   * @param player Player that executed action
   * @param timestamp Timestamp of the action
   * 
   */
  
}