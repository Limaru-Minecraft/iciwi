package mikeshafter.iciwi.api;

import mikeshafter.iciwi.Iciwi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

public class IcLogger {

  private final Path file = Paths.get("iciwi.dat");
  private IcData icData;

  public IcLogger() {
    Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
    try {
      Files.createFile(file);
    } catch (FileAlreadyExistsException e) {
      plugin.getLogger().info("Using existing file: " + file);
    } catch (IOException e) {
      plugin.getLogger().warning("Unable to create a file: " + file + ", check permissions!");
    }
  }

  public void read() {
    // read icData from file
    try {
      FileInputStream fis = new FileInputStream(file.toString());
      ObjectInputStream ois = new ObjectInputStream(fis);
      Object o = ois.readObject();
      if (o instanceof IcData) this.icData = (IcData) o;
      ois.close();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Records a new entry to the data map
   * Also works for editing
   * @param ukey Unique key to associate with map
   * @param map Map to record
   */
  public void record (String ukey, Map<String, Object> map) {
    this.read();
    this.icData.put(ukey, map);
    this.save();
  }

  /**
   * Gets all entries with the specified category-value pair.
   * @param category Category to look into
   * @param value Value to check for
   * @return List of the map objects that are the entries.
   */
  public List<Map<String, Object>> get (String category, Object value) {
    this.read();
    return this.icData.get(new Pair(category, value));
  }

  /**
   * Saves the data to the file.
   */
  public void save() {
    // output icData to file
    try {
      FileOutputStream fos = new FileOutputStream(file.toString());
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(this.icData);
      oos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private record Pair(String k, Object v) {}


  private static class IcData {
    private final HashMap<Pair, LinkedList<String>> accessors = new HashMap<>();
    private final HashMap<String, Map<String, Object>> data = new HashMap<>();

    public List<Map<String, Object>> get (Pair accessor) {
      LinkedList<String> dataKeys = accessors.get(accessor);
      return dataKeys.stream().map(data::get).toList();
    }

    public void put (String key, Map<String, Object> map) {
      // loop through every key in map
      for (String mapKey : map.keySet()) {
        this.accessors.putIfAbsent( new Pair(mapKey, map.get(mapKey)) , new LinkedList<>() );
        this.accessors.get( new Pair(mapKey, map.get(mapKey)) ).add(key);
      }
      this.data.put(key, map);
    }
  }
}
