package mikeshafter.iciwi;

import java.sql.*;
import java.util.LinkedList;


public class CardSql{
  private Connection connect() {
    // SQLite connection string
    String url = "jdbc:sqlite:IciwiCards.db";
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return conn;
  }

  public void initTables(String[] discounts) {
    // SQLite connection string
    String url = "jdbc:sqlite:IciwiCards.db";
  
    // SQL statement for creating a new table
    LinkedList<String> sql = new LinkedList<>();
    sql.add("CREATE TABLE IF NOT EXISTS cards (serial text, value real, PRIMARY KEY (serial_prefix, serial)); ");
    sql.add("CREATE TABLE IF NOT EXISTS log (serial text, start_station TEXT, end_station TEXT, price NUMERIC, FOREIGN KEY(serial) REFERENCES cards(serial), PRIMARY KEY(serial_prefix, serial), FOREIGN KEY(serial_prefix) REFERENCES cards(serial_prefix) )");
  
    for (String operator : discounts) {
      sql.add("CREATE TABLE IF NOT EXISTS "+operator+" (serial text, expiry integer, FOREIGN KEY (serial_prefix) references cards(serial_prefix), FOREIGN KEY (serial) references cards(serial), PRIMARY KEY (serial_prefix, serial)); ");
    }
  
    try (Connection conn = DriverManager.getConnection(url); Statement statement = conn.createStatement()) {
      for (String s : sql) {
        statement.execute(s);
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
  
  public void newCard(String serial, double value) {
    String sql = "INSERT INTO cards(serial, value) VALUES(?, ?)";
    
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setDouble(2, Math.round(value*100.0)/100.0);
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
  
  public void delCard(String serial) {
    String sql = "DELETE FROM cards WHERE serial = ?";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
  
  public void log(String serial, String start_station, String end_station, double price) {
    String sql = "INSERT INTO log (serial, start_station, end_station, price) VALUES (?, ?, ?, ?)";
    
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      statement.setString(2, start_station);
      statement.setString(3, end_station);
      statement.setDouble(4, price);
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
  
  public void setDiscount(String serial, String operator, long expiry) {
    String sql = expiry > 0 ? "INSERT INTO ? (serial, expiry) VALUES (?, ?)" : "DELETE FROM ? where serial = ?";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(2, serial);
      statement.setString(1, operator);
      statement.setLong(3, expiry);
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
  
  public void getDiscount(String serial) {
  
  }
  
  public double getCardValue(String serial) {
    String sql = "SELECT value FROM cards WHERE serial = ?";
    double returnValue = 0;
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial);
      ResultSet rs = statement.executeQuery();
      returnValue = rs.getDouble("value");
      
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    
    return Math.round(returnValue*100.0)/100.0;
  }
  
  public void updateCard(String serial, double value) {
    String sql = "UPDATE cards SET value=? WHERE serial=?";
    
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(2, serial);
      statement.setDouble(1, Math.round(value*100.0)/100.0);
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }
  
}
