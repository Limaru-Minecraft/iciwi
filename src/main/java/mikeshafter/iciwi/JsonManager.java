package mikeshafter.iciwi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class JsonManager {
  
  public static ArrayList<String> getAllStations() {
    final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
    
    File file = new File(plugin.getDataFolder(), "fares.json");
    
    try {
      Scanner scanner = new Scanner(file);
      StringBuilder contentBuilder = new StringBuilder();
      while (scanner.hasNextLine()) contentBuilder.append(scanner.nextLine());
      scanner.close();
      
      String content = contentBuilder.toString();
  
      JsonObject fares = JsonParser.parseString(content).getAsJsonObject();
      return fares.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toCollection(ArrayList::new));
      
    } catch (IOException|JsonSyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static double getFare(String entryStation, String exitStation) {
    final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
    
    File file = new File(plugin.getDataFolder(), "fares.json");
    
    try {
      Scanner scanner = new Scanner(file);
      StringBuilder contentBuilder = new StringBuilder();
      while (scanner.hasNextLine()) contentBuilder.append(scanner.nextLine());
      scanner.close();
  
      String content = contentBuilder.toString();//.replaceAll(", ", "").replaceAll("[\\[\\]]", "");
  
      JsonObject fares = JsonParser.parseString(content).getAsJsonObject();
  
      JsonObject entryStationJson = fares.getAsJsonObject(entryStation);
  
      if (entryStationJson != null)
        return entryStationJson.get(exitStation).getAsDouble();
      else
        return 0d;
  
    } catch (IOException|JsonSyntaxException e) {
      e.printStackTrace();
      return 0d;
    }
  }
  
  public static Map<String, Double> getFares(String station) {
    final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
    
    File file = new File(plugin.getDataFolder(), "fares.json");
    
    try {
      Scanner scanner = new Scanner(file);
      StringBuilder contentBuilder = new StringBuilder();
      while (scanner.hasNextLine()) contentBuilder.append(scanner.nextLine());
      scanner.close();
      
      String content = contentBuilder.toString();//.replaceAll(", ", "").replaceAll("[\\[\\]]", "");
  
      JsonObject fares = JsonParser.parseString(content).getAsJsonObject();
      
      JsonObject stationJson = fares.getAsJsonObject(station);
      
      if (stationJson != null) {
        return stationJson.entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getAsDouble()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      } else
        return null;
      
    } catch (IOException|JsonSyntaxException e) {
      e.printStackTrace();
      return null;
    }
  }
}
