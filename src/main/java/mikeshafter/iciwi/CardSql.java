package mikeshafter.iciwi;
import mikeshafter.iciwi.config.Owners;
import org.bukkit.plugin.Plugin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class CardSql {

private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
private final Owners owners = new Owners();

private Connection connect () {
// SQLite connection string
// "jdbc:sqlite:IciwiCards.db"
	String url = plugin.getConfig().getString("database");
	Connection conn = null;
	try {
		conn = DriverManager.getConnection(Objects.requireNonNull(url));
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
	return conn;
}

/**
 Initialise SQL tables
 */
public void initTables () {
	ArrayList<String> sql = new ArrayList<>(31);
	sql.add("CREATE TABLE IF NOT EXISTS cards (serial TEXT, value TEXT, PRIMARY KEY (serial) ); ");
	sql.add("CREATE TABLE IF NOT EXISTS discounts (serial TEXT REFERENCES cards(serial) ON UPDATE CASCADE, name TEXT, start INTEGER, PRIMARY KEY (serial, name) ); ");

	sql.add("CREATE TABLE IF NOT EXISTS log_master (id INTEGER NOT NULL UNIQUE,timestamp INTEGER NOT NULL,player_uuid TEXT NOT NULL,PRIMARY KEY(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_entry (id INTEGER NOT NULL,sign_x INTEGER, sign_y INTEGER, sign_z INTEGER,entry TEXT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_prevjourney (id INTEGER NOT NULL,entry TEXT,fare NUMERIC,class text,exittime INT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_railpass_store (id INTEGER NOT NULL,name TEXT NOT NULL,price NUMERIC NOT NULL,percentage NUMERIC NOT NULL,start INTEGER NOT NULL,duration INTEGER NOT NULL,operator TEXT NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_card_use(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_exit (id INTEGER NOT NULL,sign_x INTEGER, sign_y INTEGER, sign_z INTEGER,entry TEXT,exit TEXT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_journey (id INTEGER NOT NULL,subtotal real NOT NULL,total real NOT NULL,class text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_railpass_use (id INTEGER NOT NULL,name text NOT NULL,price NUMERIC NOT NULL,percentage NUMERIC NOT NULL,start INTEGER NOT NULL,duration INTEGER NOT NULL,operator TEXT NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_member (id INTEGER NOT NULL,sign_x INTEGER, sign_y INTEGER, sign_z INTEGER,station TEXT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_free_pass (id INTEGER NOT NULL,sign_x INTEGER, sign_y INTEGER, sign_z INTEGER,station TEXT,sign_type TEXT,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_transfer (id INTEGER NOT NULL,sign_x INTEGER, sign_y INTEGER, sign_z INTEGER,entry TEXT NOT NULL,transfer text NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_ticket_use (id INTEGER NOT NULL,entry TEXT NOT NULL,exit TEXT NOT NULL,class TEXT NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_card_use (id INTEGER NOT NULL,serial TEXT NOT NULL,value NUMERIC,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_ticket_create (id INTEGER NOT NULL,entry TEXT NOT NULL,exit text NOT NULL,class text NOT NULL, fare NUMERIC NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_card_create (id INTEGER NOT NULL,serial TEXT NOT NULL,value NUMERIC,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_card_topup (id INTEGER NOT NULL,serial TEXT NOT NULL,old_value NUMERIC,added_value NUMERIC,new_value NUMERIC,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_railpass_extend (id INTEGER NOT NULL,serial TEXT NOT NULL,name text NOT NULL,price NUMERIC NOT NULL,percentage NUMERIC NOT NULL,start INTEGER NOT NULL,duration INTEGER NOT NULL,operator TEXT NOT NULL,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");
	sql.add("CREATE TABLE IF NOT EXISTS log_card_refund (id INTEGER NOT NULL,serial TEXT NOT NULL,value NUMERIC,PRIMARY KEY(id),FOREIGN KEY(id) REFERENCES log_master(id));");

	try (Connection conn = this.connect(); Statement statement = conn.createStatement()) {
		for (String s : sql) statement.execute(s);
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}

/**
 Creates a new Iciwi card

 @param serial Serial number
 @param value  Starting value of the card */
public void newCard (String serial, double value) {
	String sql = "INSERT INTO cards(serial, value) VALUES(?, ?) ; ";

	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		statement.setDouble(2, Math.round(value * 100.0) / 100.0);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}

/**
 Deletes an existing Iciwi card

 @param serial Serial number */
public void deleteCard (String serial) {
	String sql = "DELETE FROM cards WHERE serial = ? ;";

	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Sets a rail pass for a certain card and operator

 @param serial Serial number
 @param name   Name of the rail pass
 @param start  Start time of the rail pass as number of seconds from the Java epoch of 1970-01-01T00:00:00Z. */
public void setDiscount (String serial, String name, long start) {
	String sql = "INSERT INTO discounts VALUES (?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		statement.setString(2, name);
		statement.setLong(3, start);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Gets all the rail passes of a card. This method also deletes expired rail passes.

 @param serial Serial number
 @return Map in the format String name, Long start. */
public Map<String, Long> getAllDiscounts (String serial) {
	String sql = "SELECT name, start FROM discounts WHERE serial = ?";
	HashMap<String, Long> returnValue = new HashMap<>();
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		ResultSet rs = statement.executeQuery();

		while (rs.next()) {
			String name = rs.getString(1);
			long expiry = getStart(serial, name) + owners.getRailPassDuration(name);
			if (expiry > Instant.now().getEpochSecond()) returnValue.put(name, expiry);
			else {
				String sql1 = "DELETE FROM DISCOUNTS WHERE serial = ? AND name = ?";
				final PreparedStatement statement1 = conn.prepareStatement(sql1);
				statement1.setString(1, serial);
				statement1.setString(2, name);
				statement1.executeUpdate();
			}

		}

	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}

	return returnValue;
}


/**
 Gets the start time of a certain railpass belonging to a card

 @param serial Serial number
 @param name   Name of the discount (include operator) */
public long getStart (String serial, String name) {
	String sql = "SELECT start FROM discounts WHERE serial = ? AND name = ?";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		statement.setString(2, name);
		ResultSet rs = statement.executeQuery();
		return rs.getLong(1);

	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
		return 0L;
	}
}


/**
 Adds a value to a card

 @param serial Serial number
 @param value  Value to be added */
public void addValueToCard (String serial, double value) {updateCard(serial, getCardValue(serial) + value);}


/**
 Changes a value of a card

 @param serial Serial number
 @param value  New value of card */
public void updateCard (String serial, double value) {
	String sql = "UPDATE cards SET value = ? WHERE serial = ?";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setDouble(1, Math.round(value * 100.0) / 100.0);
		statement.setString(2, serial);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Gets the value of a card

 @param serial Serial number */
public double getCardValue (String serial) {
	String sql = "SELECT value FROM cards WHERE serial = ?";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setString(1, serial);
		ResultSet rs = statement.executeQuery();
		return Math.round(rs.getDouble("value") * 100.0) / 100.0;

	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
		return 0d;
	}
}


/**
 Subtracts a value from a card

 @param serial Serial number
 @param value  Value to be subtracted */
public void subtractValueFromCard (String serial, double value) {updateCard(serial, getCardValue(serial) - value);}


/**
 Method to debug database

 @param sql SQL to run
 @return Values of the ResultSet returned */
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
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
		return null;
	}
}

/**
 This method returns the number of log counts in the file.
 If the file does not exist, it creates a new file with 1 in it.
 If the file exists but is empty, it returns 1.
 Otherwise, it reads the number from the file and returns it.

 @return the number of log counts */
public int getCount () {
	File file = new File("iciwi_id.txt");
	try {
		BufferedReader br = new BufferedReader(new FileReader(file));
		int number = Integer.parseInt(br.readLine());
		br.close();
		return number;
	}
	catch (FileNotFoundException e) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("iciwi_id.txt"));
			writer.write("1");
			writer.close();
			return 1;
		}
		catch (IOException x) {
			plugin.getLogger().warning(x.getLocalizedMessage());
			return -1;
		}
	}
	catch (IOException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
		return -1;
	}
}

/**
 Increments the count in the file by 1 and returns the updated count.

 @return the updated count in the file */
public int incrementAndGetCount () {
	File file = new File("iciwi_id.txt");
	try {
		BufferedReader br = new BufferedReader(new FileReader(file));
		int number = Integer.parseInt(br.readLine());
		br.close();
		BufferedWriter writer = new BufferedWriter(new FileWriter("iciwi_id.txt"));
		writer.write(String.valueOf(number + 1));
		writer.close();
		return number + 1;
	}
	catch (FileNotFoundException e) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("iciwi_id.txt"));
			writer.write("1");
			writer.close();
			return 1;
		}
		catch (IOException ex) {
			plugin.getLogger().warning(ex.getLocalizedMessage());
			return -1;
		}
	}
	catch (IOException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
		return -1;
	}
}

/**
 Inserts a new log entry with the current timestamp and the player's UUID into the log_master table.

 @param player_uuid the UUID of the player */
public void logMaster (String player_uuid) {
	long timestamp = System.currentTimeMillis();
	String sql = "INSERT INTO log_master (id, timestamp, player_uuid) VALUES ( ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, incrementAndGetCount());
		statement.setLong(2, timestamp);
		statement.setString(3, player_uuid);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}

/**
 Inserts a new log entry with the current timestamp and the player's UUID into the log_master table.

 @param signX the x coordinate of the sign
 @param signY the y coordinate of the sign
 @param signZ the z coordinate of the sign
 @param entry the log entry */
public void logEntry (int signX, int signY, int signZ, String entry) {
	String sql = "INSERT INTO log_entry (id, sign_x, sign_y, sign_z, entry) VALUES ( ?, ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());
		statement.setInt(2, signX);
		statement.setInt(3, signY);
		statement.setInt(4, signZ);
		statement.setString(5, entry);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Inserts a new log entry into the log_prevjourney table with the specified log entry, fare, fare class, and exit time.

 @param prev_entry     the log entry
 @param prev_fare      the fare for the log entry
 @param prev_fareClass the class of the fare
 @param exitTime       the exit time in seconds from the Java epoch of 1970-01-01T00:00:00Z */
public void logPrevJourney (String prev_entry, double prev_fare, String prev_fareClass, long exitTime) {
	String sql = "INSERT INTO log_prevjourney (id, entry, fare, class, exittime) VALUES ( ?, ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());
		statement.setString(2, prev_entry);
		statement.setDouble(3, prev_fare);
		statement.setString(4, prev_fareClass);
		statement.setLong(5, exitTime);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Inserts a new log entry into the log_exit table with the specified sign coordinates, entry, and exit.

 @param signX the x coordinate of the sign
 @param signY the y coordinate of the sign
 @param signZ the z coordinate of the sign
 @param entry the log entry
 @param exit  the log exit */
public void logExit (int signX, int signY, int signZ, String entry, String exit) {
	String sql = "INSERT INTO log_exit (id, sign_x, sign_y, sign_z, entry, exit) VALUES ( ?, ?, ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());

		statement.setInt(2, signX);
		statement.setInt(3, signY);
		statement.setInt(4, signZ);
		statement.setString(5, entry);
		statement.setString(6, exit);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Inserts a new log entry into the log_journey table with the specified subtotal, total, and fare class

 @param subtotal  the subtotal of the journey
 @param total     the total cost of the journey
 @param fareClass the class of the fare for the journey */
public void logJourney (double subtotal, double total, String fareClass) {
	String sql = "INSERT INTO log_journey (id, subtotal, total, class) VALUES ( ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());

		statement.setDouble(2, subtotal);
		statement.setDouble(3, total);
		statement.setString(4, fareClass);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Inserts a new log entry into the log_member table with the specified sign coordinates and station name.

 @param signX   the x coordinate of the sign
 @param signY   the y coordinate of the sign
 @param signZ   the z coordinate of the sign
 @param station the station information */
public void logMember (int signX, int signY, int signZ, String station) {
	String sql = "INSERT INTO log_member (id, sign_x, sign_y, sign_z, station) VALUES ( ?, ?, ?, ?, ?)";

	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());

		statement.setInt(2, signX);
		statement.setInt(3, signY);
		statement.setInt(4, signZ);
		statement.setString(5, station);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Inserts a new log entry when players use a paper rail pass.
 This is separate from the normal gate-specific signs as all fare gates work as member signs when a paper pass is used.

 @param signX   the x coordinate of the sign
 @param signY   the y coordinate of the sign
 @param signZ   the z coordinate of the sign
 @param station the station information
 @param signType the type of sign */
public void logFreePass (int signX, int signY, int signZ, String station, String signType) {
	String sql = "INSERT INTO log_free_pass (id, sign_x, sign_y, sign_z, station, sign_type) VALUES ( ?, ?, ?, ?, ?, ?)";

	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());

		statement.setInt(2, signX);
		statement.setInt(3, signY);
		statement.setInt(4, signZ);
		statement.setString(5, station);
		statement.setString(6, signType);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Inserts a new log entry into the log_transfer table with the specified sign coordinates, entry station, and transfer station.

 @param signX    the x coordinate of the sign
 @param signY    the y coordinate of the sign
 @param signZ    the z coordinate of the sign
 @param entry    the log entry
 @param transfer the log transfer */
public void logTransfer (int signX, int signY, int signZ, String entry, String transfer) {
	String sql = "INSERT INTO log_transfer (id, sign_x, sign_y, sign_z, entry, transfer) VALUES ( ?, ?, ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());

		statement.setInt(2, signX);
		statement.setInt(3, signY);
		statement.setInt(4, signZ);
		statement.setString(5, entry);
		statement.setString(6, transfer);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}

/**
 Inserts a new log entry into the log_railpass_store table with the specified name, price, percentage, start time, duration, and operator.

 @param name       the name of the rail pass
 @param price      the price of the rail pass
 @param percentage the percentage discount of the rail pass
 @param start      the start time of the rail pass as number of seconds from the Java epoch of 1970-01-01T00:00:00Z
 @param duration   the duration of the rail pass in seconds
 @param operator   the operator of the rail pass */
public void logRailpassStore (String name, double price, double percentage, long start, long duration, String operator) {
	String sql = "INSERT INTO log_railpass_store (id, name, price, percentage, start, duration, operator) VALUES ( ?, ?, ?, ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());

		statement.setString(2, name);
		statement.setDouble(3, price);
		statement.setDouble(4, percentage);
		statement.setLong(5, start);
		statement.setLong(6, duration);
		statement.setString(7, operator);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Inserts a new log entry into the log_railpass_use table with the specified name, price, percentage, start time, duration, and operator.

 @param name       the name of the rail pass
 @param price      the price of the rail pass
 @param percentage the percentage discount of the rail pass
 @param start      the start time of the rail pass as number of seconds from the Java epoch of 1970-01-01T00:00:00Z
 @param duration   the duration of the rail pass in seconds
 @param operator   the operator of the rail pass */
public void logRailpassUse (String name, double price, double percentage, long start, long duration, String operator) {
	String sql = "INSERT INTO log_railpass_use (id, name, price, percentage, start, duration, operator) VALUES ( ?, ?, ?, ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());

		statement.setString(2, name);
		statement.setDouble(3, price);
		statement.setDouble(4, percentage);
		statement.setLong(5, start);
		statement.setLong(6, duration);
		statement.setString(7, operator);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Inserts a new log entry into the log_ticket_use table with the specified from station, to station, and travel class.

 @param from        the starting station
 @param to          the destination station
 @param travelClass the class of the travel */
public void logTicketUse (String from, String to, String travelClass) {
	String sql = "INSERT INTO log_ticket_use (id, from, to, class) VALUES ( ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());
		statement.setString(2, from);
		statement.setString(3, to);
		statement.setString(4, travelClass);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}


/**
 Inserts a new log entry into the log_card_use table with the specified serial and value.

 @param serial Serial number of the card */
public void logCardUse (String serial) {
	String sql = "INSERT INTO log_card_use (id, serial, value) VALUES ( ?, ?, (SELECT value FROM cards WHERE serial = ?))";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());

		statement.setString(2, serial);
		statement.setString(3, serial);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
	Map<String, Long> currentPasses = getAllDiscounts(serial);
	for (var name : currentPasses.keySet()) {
		logRailpassStore(name, owners.getRailPassPrice(name), owners.getRailPassPercentage(name), getStart(serial, name), owners.getRailPassDuration(name), owners.getRailPassOperator(name));
	}
}

/**
 Inserts a new log entry into the log_ticket_create table with the specified from station, to station, and travel class.

 @param from        the starting station
 @param to          the destination station
 @param travelClass the class of the travel */
public void logTicketCreate (String from, String to, String travelClass, double fare) {
	String sql = "INSERT INTO log_ticket_create (id, from, to, class, fare) VALUES ( ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());
		statement.setString(2, from);
		statement.setString(3, to);
		statement.setString(4, travelClass);
		statement.setDouble(5, fare);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}
/**
 Inserts a new log entry into the log_card_create table with the specified serial and value.

 @param serial Serial number of the card
 @param value  Value of the card */
public void logCardCreate (String serial, double value) {
	String sql = "INSERT INTO log_card_create (id, serial, value) VALUES ( ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());
		statement.setString(2, serial);
		statement.setDouble(3, value);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}
/**
 Inserts a new log entry into the log_card_topup table with the specified serial, old value, added value, and new value.

 @param serial     Serial number of the card
 @param oldValue   Old value of the card
 @param addedValue Value added to the card
 @param newValue   New value of the card */
public void logCardTopup (String serial, double oldValue, double addedValue, double newValue) {
	String sql = "INSERT INTO log_card_topup (id, serial, old_value, added_value, new_value) VALUES ( ?, ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());
		statement.setString(2, serial);
		statement.setDouble(3, oldValue);
		statement.setDouble(4, addedValue);
		statement.setDouble(5, newValue);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}
/**
 Inserts a new log entry into the log_railpass_extend table with the specified serial, name, price, percentage, start time, duration, and operator.

 @param serial     Serial number of the rail pass
 @param name       Name of the rail pass
 @param price      Price of the rail pass
 @param percentage Percentage discount of the rail pass
 @param start      Start time of the rail pass as number of seconds from the Java epoch of 1970-01-01T00:00:00Z
 @param duration   Duration of the rail pass in seconds
 @param operator   Operator of the rail pass */
public void logRailpassExtend (String serial, String name, double price, double percentage, long start, long duration, String operator) {
	String sql = "INSERT INTO log_railpass_extend (id, serial, name, price, percentage, start, duration, operator) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());
		statement.setString(2, serial);
		statement.setString(3, name);
		statement.setDouble(4, price);
		statement.setDouble(5, percentage);
		statement.setLong(6, start);
		statement.setLong(7, duration);
		statement.setString(8, operator);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}
/**
 Inserts a new log entry into the log_card_refund table with the specified serial and value.

 @param serial Serial number of the card
 @param value  Value of the card */
public void logCardRefund (String serial, double value) {
	String sql = "INSERT INTO log_card_refund (id, serial, value) VALUES ( ?, ?, ?)";
	try (Connection conn = this.connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
		statement.setInt(1, getCount());
		statement.setString(2, serial);
		statement.setDouble(3, value);
		statement.executeUpdate();
	}
	catch (SQLException e) {
		plugin.getLogger().warning(e.getLocalizedMessage());
	}
}

}
