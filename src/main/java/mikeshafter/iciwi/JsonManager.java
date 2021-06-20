package mikeshafter.iciwi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class JsonManager {
  
  private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  
  public static double getFare(String entryStation, String exitStation) {
    String path = plugin.getDataFolder()+"/fares.json";
    try (Stream<String> lines = Files.lines(Paths.get(path))) {
      
      String content = lines.collect(Collectors.joining(System.lineSeparator()));
      JsonObject fares = new JsonParser().parse(content).getAsJsonObject();
      
      JsonObject entryStationJson = fares.getAsJsonObject(entryStation);
      return entryStationJson.get(exitStation).getAsDouble();
      
    } catch (IOException e) {
      e.printStackTrace();
      return 0d;
    }
  }
}