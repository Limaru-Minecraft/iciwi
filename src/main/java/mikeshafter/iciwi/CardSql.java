package mikeshafter.iciwi;

import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class CardSql{

  Plugin plugin = getPlugin(Iciwi.class);

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

  public void initTables() {
    // SQLite connection string
    // "jdbc:sqlite:IciwiCards.db"
    String url = plugin.getConfig().getString("database");
  
    // SQL statement for creating a new table
    LinkedList<String> sql = new LinkedList<>();
    sql.add("CREATE TABLE IF NOT EXISTS cards (serial text, value real, PRIMARY KEY (serial) ); ");
    sql.add("CREATE TABLE IF NOT EXISTS discounts (serial text REFERENCES cards(serial) ON UPDATE CASCADE, operator text, expiry integer, PRIMARY KEY (serial, operator) ); ");
  
    try (Connection conn = DriverManager.getConnection(Objects.requireNonNull(url)); Statement statement = conn.createStatement()) {
      for (String s : sql) {
        statement.execute(s);
      }
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void newCard(String serial, double value) {
    String sql = "INSERT INTO cards(serial, value) VALUES(?, ?)";

    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setDouble(2, Math.round(value*100.0)/100.0);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }

  public void delCard(String serial) {
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
   * @param serial   Serial number
   * @param operator TOC of the rail pass
   * @param expiry   Expiry time, in seconds after Java epoch
   */
  public void setDiscount(String serial, String operator, long expiry) {
    String sql = "INSERT INTO discounts VALUES (?, ?, ?)";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setString(2, operator);
      statement.setLong(3, expiry);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }
  
  /**
   * Sets a rail pass for a certain card and operator
   *
   * @param serial   Serial number
   * @param operator TOC of the rail pass
   * @param expiry   Extension time, in seconds.
   */
  public void renewDiscount(String serial, String operator, long expiry) {
    String sql = "UPDATE discounts SET expiry = ? WHERE serial = ? AND operator = ?";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setLong(1, expiry+getExpiry(serial, operator));
      statement.setString(2, serial);
      statement.setString(3, operator);
      statement.executeUpdate();
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }
  }
  
  public long getExpiry(String serial, String operator) {
    String sql = "SELECT expiry FROM discounts WHERE serial = ? AND operator = ?";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setString(2, operator);
      ResultSet rs = statement.executeQuery();
      
      return rs.getLong(1);
      
    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
      return 0L;
    }
  }
  
  public Map<String, Long> getDiscountedOperators(String serial) {
    String sql = "SELECT operator, expiry FROM discounts WHERE serial = ?";
    HashMap<String, Long> returnValue = new HashMap<>();
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      ResultSet rs = statement.executeQuery();
      
      while (rs.next()) {
        assert false;
        String operator = rs.getString(1);
        // Check if expired
        long expiry = rs.getLong(2);
        
        if (expiry > Instant.now().getEpochSecond())
          returnValue.put(operator, expiry);
        else {
          String sql1 = "DELETE FROM DISCOUNTS WHERE serial = ? AND operator = ?";
          final PreparedStatement statement1 = conn.prepareStatement(sql1);
          statement1.setString(1, serial);
          statement1.setString(2, operator);
          statement1.executeUpdate();
        }

      }

    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }

    return returnValue;
  }

  public void addValueToCard(String serial, double value) {
    plugin.getServer().getLogger().info(serial+" "+value);
    updateCard(serial, getCardValue(serial)+value);
  }

  public void subtractValueFromCard(String serial, double value) {
    updateCard(serial, getCardValue(serial)-value);
  }

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

  public double getCardValue(String serial) {
    String sql = "SELECT value FROM cards WHERE serial = ?";
    double returnValue = 0;
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      ResultSet rs = statement.executeQuery();
      returnValue = rs.getDouble("value");

    } catch (SQLException e) {
      plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
    }

    return Math.round(returnValue*100.0)/100.0;
  }

}
