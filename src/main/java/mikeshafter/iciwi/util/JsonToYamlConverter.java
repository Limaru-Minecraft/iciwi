package mikeshafter.iciwi.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import mikeshafter.iciwi.Iciwi;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;


public class JsonToYamlConverter {
  public static void main() {
    
    final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
    final File json = new File(plugin.getDataFolder(), "fares.json");
    File yamlF = new File(plugin.getDataFolder(), "fares.yml");
    YamlConfiguration yaml = new YamlConfiguration();
    
    try {
      yaml.load(yamlF);
      Scanner jsonScanner = new Scanner(json);
      StringBuilder contentBuilder = new StringBuilder();
      while (jsonScanner.hasNextLine()) contentBuilder.append(jsonScanner.nextLine());
      jsonScanner.close();
      
      String content = contentBuilder.toString();//.replaceAll(", ", "").replaceAll("[\\[\\]]", "");
      JsonObject fares = JsonParser.parseString(content).getAsJsonObject();
      ArrayList<String> stations = fares.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toCollection(ArrayList::new));
      
      for (String entryStation : stations) {
        JsonObject faresFromStation = fares.getAsJsonObject(entryStation);
        ArrayList<String> endpoints = faresFromStation.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toCollection(ArrayList::new));
        
        for (String exitStation : endpoints) {
          Double fare = faresFromStation.get(exitStation).getAsDouble();
          yaml.set(entryStation+"."+exitStation+"."+plugin.getConfig().get("default-class"), fare);
        }
      }
      
      yaml.save(yamlF);
      
    } catch (IOException|JsonSyntaxException|InvalidConfigurationException e) {
      e.printStackTrace();
    }
  }
}
