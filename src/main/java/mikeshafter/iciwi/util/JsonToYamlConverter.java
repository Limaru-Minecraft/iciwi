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
    File yamlF = new File(plugin.getDataFolder(), "fares.yml");

    try {
      File json = new File(plugin.getDataFolder(), "fares.json");
      Scanner yamlScanner = new Scanner(yamlF);
      if (yamlScanner.hasNext()) {
        yamlScanner.close();
        return;
      } // don't process if yamlF already has data
  
      YamlConfiguration yaml = new YamlConfiguration();
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
      yamlScanner.close();
      
    }
    catch (IOException e) {
      Iciwi.getPlugin(Iciwi.class).getLogger().warning("fares.json could not be found! If you already have a fares.yml, please ignore this warning.");
    }
    catch (InvalidConfigurationException e) {
      Iciwi.getPlugin(Iciwi.class).getLogger().warning("fares.json is wrongly configured!");
    }
    catch (JsonSyntaxException e) {
      Iciwi.getPlugin(Iciwi.class).getLogger().warning("fares.yml is wrongly configured!");
    }
  }
}
