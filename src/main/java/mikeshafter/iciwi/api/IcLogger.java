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
import java.io.Serializable;

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
    try {
      this.icData = this.read();
    } catch (IOException | ClassNotFoundException e) {
      this.icData = new IcData();
      save();
      plugin.getLogger().info("No previous icData was found, creating one...");
    }
  }

  public IcData read() throws IOException, ClassNotFoundException {
    // read icData from file
    FileInputStream fis = new FileInputStream(file.toString());
    ObjectInputStream ois = new ObjectInputStream(fis);
    //Object o = ois.readObject();
    this.icData.readObject(ois);
    ois.close();
    return this.icData;
    //if (o instanceof IcData) return (IcData) o; else throw new ClassNotFoundException("The object in the IcData file is not of type IcData!");
  }
  
  /**
   * Records a new entry to the data map
   * Also works for editing
   * @param ukey Unique key to associate with map
   * @param map Map to record
   */
  public void record (String ukey, Map<String, Object> map) {
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
      this.icData.writeObject(oos);
      //oos.writeObject(this.icData);
      oos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private record Pair(String k, Object v) {}


  private static class IcData implements Serializable {
    private HashMap<Pair, LinkedList<String>> accessors = new HashMap<>();
    private HashMap<String, Map<String, Object>> data = new HashMap<>();

    // Serializer
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(accessors);
        out.writeObject(data);
    }

    // Deserializer
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        accessors = (HashMap<Pair, LinkedList<String>>) in.readObject();
        data = (HashMap<String, Map<String, Object>>) in.readObject();
    }

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
