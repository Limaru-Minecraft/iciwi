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

  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Path file = Paths.get("iciwi.dat");
  private IcData icData  = new IcData();

  public IcLogger() {
    try {
      Files.createFile(file);
    } catch (FileAlreadyExistsException e) {
      plugin.getLogger().info("Using existing file: " + file);
    } catch (IOException e) {
      plugin.getLogger().warning("Unable to create a file: " + file + ", check permissions!");
    }

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
  
  public void record (Map<String, Object> map) {
    // get unique key
    if (map.get("uuid") instanceof String uuid && map.get("timestamp") instanceof String timestamp) {
      String ukey = uuid + timestamp;
      // remove unneeded fields as they are part of the unique key
      map.remove("uuid");
      map.remove("timestamp");
      // set to data
      this.icData.set(ukey, map);
    }
  }

  public List<Map<String, Object>> get (String category, Object value) {
    return this.icData.get(new Pair<>(category, value));
  }

  public void save() {
    // output icData to file
    try {
      FileOutputStream fos = new FileOutputStream(file.toString());
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(icData);
      oos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private class Pair<K, V> {
    private K k;
    private V v;
    public Pair (K k, V v) {
      this.k = k;
      this.v = v;
    }
    public K getK() { return k; }
    public V getV() { return v; }
  }


  private class IcData {
    private HashMap<Pair<String, Object>, LinkedList<String>> accessors;
    private HashMap<String, Map<String, Object>> data;

    public List<Map<String, Object>> get (Pair<String, Object> accessor) {
      LinkedList<String> dataKeys = accessors.get(accessor);
      return dataKeys.stream().map(k -> data.get(k)).toList();
    }

    public void set (String key, Map<String, Object> map) {
      // loop through every key in map
      for (String mapKey : map.keySet()) {
        this.accessors.putIfAbsent( new Pair<String, Object>(mapKey, map.get(mapKey)) , new LinkedList<>() );
        this.accessors.get( new Pair<String, Object>(mapKey, map.get(mapKey)) ).add(key);
      }
      this.data.put(key, map);
    }
  }
}
