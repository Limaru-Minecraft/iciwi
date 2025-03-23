package mikeshafter.iciwi;
import mikeshafter.iciwi.config.Owners;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CardSql {

private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private final Owners owners = plugin.owners;

private Connection connect () {
	// SQLite connection string
	// "jdbc:sqlite:IciwiCards.db"
	String url = plugin.getConfig().getString("database");
	Connection conn = null;
	try {
		conn = DriverManager.getConnection(Objects.requireNonNull(url));
	} catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
	return conn;
}

/**
 * Initialise SQL tables
 */
public void initTables () {
	try (Connection conn = this.connect(); Statement statement = conn.createStatement()) {
		statement.execute("CREATE TABLE IF NOT EXISTS cards (serial TEXT, value TEXT, PRIMARY KEY (serial) ); ");
		statement.execute("CREATE TABLE IF NOT EXISTS discounts (serial TEXT REFERENCES cards(serial) ON UPDATE CASCADE, name TEXT, start INTEGER, PRIMARY KEY (serial, name) ); ");
	} catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}

/**
 * Creates a new Iciwi card
 *
 * @param serial Serial number
 * @param value  Starting value of the card
 */
public void newCard (String serial, double value) {
	String sql = "INSERT INTO cards(serial, value) VALUES(?, ?) ; ";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		statement.setDouble(2, Math.round(value * 100.0) / 100.0);
		statement.executeUpdate();
	} catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}

/**
 * Deletes an existing Iciwi card
 *
 * @param serial Serial number
 */
public void deleteCard (String serial) {
	String sql = "DELETE FROM cards WHERE serial = ? ;";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		statement.executeUpdate();
	} catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 * Sets a rail pass for a certain card and operator
 *
 * @param serial Serial number
 * @param name   Name of the rail pass
 * @param start  Start time of the rail pass as number of seconds from the Java epoch of 1970-01-01T00:00:00Z.
 */
public void setDiscount (String serial, String name, long start) {
	String sql = "INSERT INTO discounts VALUES (?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		statement.setString(2, name);
		statement.setLong(3, start);
		statement.executeUpdate();
	} catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 * Gets all the rail passes of a card. This method also deletes expired rail passes.
 *
 * @param serial Serial number
 * @return Map in the format String name, Long start.
 */
public Map<String, Long> getAllDiscounts (String serial) {
	String sql = "SELECT name, start FROM discounts WHERE serial = ?";
	HashMap<String, Long> returnValue = new HashMap<>();
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		ResultSet rs = statement.executeQuery();

		while (rs.next()) {
			String name = rs.getString(1);
			long expiry = getStart(serial, name) + owners.getRailPassDuration(name);
			if (expiry > System.currentTimeMillis()) {returnValue.put(name, expiry);}
			else {
				String sql1 = "DELETE FROM DISCOUNTS WHERE serial = ? AND name = ?";
				final PreparedStatement statement1 = conn.prepareStatement(sql1);
				statement1.setString(1, serial);
				statement1.setString(2, name);
				statement1.executeUpdate();
			}
		}
	} catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
	return returnValue;
}


/**
 * Gets the start time of a certain railpass belonging to a card
 *
 * @param serial Serial number
 * @param name   Name of the discount (include operator)
 */
public long getStart (String serial, String name) {
	String sql = "SELECT start FROM discounts WHERE serial = ? AND name = ?";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		statement.setString(2, name);
		ResultSet rs = statement.executeQuery();
		return rs.getLong(1);

	} catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
		return 0L;
	}
}


/**
 * Adds a value to a card
 *
 * @param serial Serial number
 * @param value  Value to be added
 */
public void addValueToCard (String serial, double value) {updateCard(serial, getCardValue(serial) + value);}


/**
 * Changes a value of a card
 *
 * @param serial Serial number
 * @param value  New value of card
 */
public void updateCard (String serial, double value) {
	String sql = "UPDATE cards SET value = ? WHERE serial = ?";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setDouble(1, Math.round(value * 100.0) / 100.0);
		statement.setString(2, serial);
		statement.executeUpdate();
	} catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 * Gets the value of a card
 *
 * @param serial Serial number
 */
public double getCardValue (String serial) {
	String sql = "SELECT value FROM cards WHERE serial = ?";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		ResultSet rs = statement.executeQuery();
		return Math.round(rs.getDouble("value") * 100.0) / 100.0;

	} catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
		return 0d;
	}
}


/**
 * Subtracts a value from a card
 *
 * @param serial Serial number
 * @param value  Value to be subtracted
 */
public void subtractValueFromCard (String serial, double value) {updateCard(serial, getCardValue(serial) - value);}


/**
 * Method to debug database
 *
 * @param sql SQL to run
 * @return Values of the ResultSet returned
 */
public String[][] runSql (String sql) {
	try (Connection conn = this.connect(); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
		if (preparedStatement.execute()) {
			ResultSet resultSet = preparedStatement.executeQuery();
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnsNumber = metaData.getColumnCount();
			ArrayList<String[]> r = new ArrayList<>();
			int c = 0;
			while (resultSet.next()) {
				r.add(new String[columnsNumber]);
				for (int i = 1; i <= columnsNumber; i++) {
					r.get(c)[i - 1] = resultSet.getString(i);
				}
				c++;
			}
			String[][] s = new String[r.size()][columnsNumber];
			for (int i = 0; i < r.size(); i++) {
				s[i] = r.get(i);
			}
			return s;
		}
		else {
			long a = preparedStatement.executeLargeUpdate();
			return new String[][]{{String.valueOf(a)}};
		}
	} catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
		return null;
	}
}
}
