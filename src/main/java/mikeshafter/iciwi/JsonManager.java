package mikeshafter.iciwi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class JsonManager {

  public static double getFare(String entryStation, String exitStation) {
    Plugin plugin = getPlugin(Iciwi.class);
  
    File file = new File(plugin.getDataFolder(), "fares.json");
  
    try {
      Scanner scanner = new Scanner(file);
      StringBuilder contentBuilder = new StringBuilder();
      while (scanner.hasNextLine()) {
        contentBuilder.append(scanner.nextLine());
      }
    
      //String content = nextLine.toString();
      String content = contentBuilder.toString();//.replaceAll(", ", "").replaceAll("[\\[\\]]", "");
      //plugin.getServer().getConsoleSender().sendMessage(content); //TODO: Debug
    
      JsonObject fares = new JsonParser().parse(content).getAsJsonObject();
    
      JsonObject entryStationJson = fares.getAsJsonObject(entryStation);
      return entryStationJson.get(exitStation).getAsDouble();
    
    } catch (IOException|JsonSyntaxException e) {
      e.printStackTrace();
      return 0d;
    }
  }
}
