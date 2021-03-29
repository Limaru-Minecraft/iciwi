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
    String url = "jdbc:sqlite:plugins/Iciwi/IciwiCards.db";

    // SQL statement for creating a new table
    LinkedList<String> sql = new LinkedList<>();
    sql.add("CREATE TABLE IF NOT EXISTS cards (serial_prefix text, serial integer unique, value real, PRIMARY KEY (serial_prefix, serial)); ");
    sql.add("CREATE TABLE IF NOT EXISTS log (serial_prefix text, serial integer, start_station text, end_station text, price real, FOREIGN KEY (serial_prefix) references cards(serial_prefix) FOREIGN KEY (serial) references cards(serial), PRIMARY KEY (serial_prefix, serial); ");

    for (String operator : discounts) {
      sql.add("CREATE TABLE IF NOT EXISTS "+operator+" (serial_prefix text, serial integer, expiry integer, FOREIGN KEY (serial_prefix) references cards(serial_prefix), FOREIGN KEY (serial) references cards(serial), PRIMARY KEY (serial_prefix, serial)); ");
    }

    try (Connection conn = DriverManager.getConnection(url); Statement statement = conn.createStatement()) {
      for (String s : sql) {
        statement.execute(s);
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public void newCard(String serial_prefix, int serial, double value) {
    String sql = "INSERT INTO cards(serial_prefix, serial, value) VALUES(?, ?, ?)";

    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial_prefix);
      statement.setInt(2, serial);
      statement.setDouble(3, Math.round(value*100.0)/100.0);
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public void delCard(String serial_prefix, int serial) {
    String sql = "DELETE FROM cards WHERE serial_prefix = ? AND serial = ?";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)){
      statement.setString(1, serial_prefix);
      statement.setDouble(2, serial);
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public void log(String serial_prefix, int serial, String start_station, String end_station, double price) {
    String sql = "INSERT INTO log (serial_prefix, serial, start_station, end_station, price) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, serial_prefix);
      statement.setInt(2, serial);
      statement.setString(3, start_station);
      statement.setString(4, end_station);
      statement.setDouble(5, price);
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public void setDiscount(String serial_prefix, int serial, String operator, long expiry) {
    String sql = expiry > 0 ? "INSERT INTO ? (serial_prefix, serial, expiry) VALUES (?, ?, ?)" : "DELETE FROM ? where serial_prefix = ? and serial = ?";
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(2, serial_prefix);
      statement.setInt(3, serial);
      statement.setString(1, operator);
      statement.setLong(4, expiry);
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public double getCardValue(String serial_prefix, int serial) {
    String sql = "SELECT value FROM cards WHERE serial_prefix = ? AND serial = ?";
    double returnValue = 0;
    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)){
      statement.setString(1, serial_prefix);
      statement.setInt(2, serial);
      ResultSet rs = statement.executeQuery();
      returnValue = rs.getDouble("value");

    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    return returnValue;
  }

  public void updateCard(String serial_prefix, int serial, double value) {
    String sql = "UPDATE cards SET value=? WHERE serial_prefix=? and serial=?";

    try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(2, serial_prefix);
      statement.setInt(3, serial);
      statement.setDouble(1, Math.round(value*100.0)/100.0);
      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

}
