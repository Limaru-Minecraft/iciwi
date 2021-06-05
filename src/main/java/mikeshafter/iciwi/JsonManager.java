package mikeshafter.iciwi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Logger;

public class JsonManager{
  
  private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  
  public static JSONObject getJsonFromFile(File file) {
    JSONParser parser = new JSONParser();
  
    try {
      // Get file
      //File file = new File(plugin.getDataFolder(), "fares.json");
      Scanner scanner = new Scanner(file);
      LinkedList<String> jsonList = new LinkedList<>();
      while (scanner.hasNextLine()) {
        jsonList.add(scanner.nextLine());
      }
    
      // Parse json
      String jsonString = jsonList.toString().replaceAll(", ", "").replaceAll("[\\[\\]]", "");
      Object obj = parser.parse(jsonString);
      return (JSONObject) obj;
    } catch (FileNotFoundException|ParseException e) {
      Logger log = Bukkit.getLogger();
      log.info(ChatColor.RED+"Exception in fares.json! Check that you have it linted!");
      return new JSONObject();
    }
  }
  
  public static double getFare(String station, String inSystem) {
    JSONObject fareDict = getJsonFromFile(new File(plugin.getDataFolder(), "fares.json"));
    if (fareDict.get(inSystem) != null) {
      JSONObject from = (JSONObject) fareDict.get(inSystem);
      if (from.get(station) != null) {
        return (double) from.get(station);
      } else {
        Logger log = Bukkit.getLogger();
        log.info(ChatColor.RED+"Fare "+inSystem+"->"+station+" is not set up correctly!");
        return -1d;
      }
    } else {
      Logger log = Bukkit.getLogger();
      log.info(ChatColor.RED+"Station "+inSystem+" is not set up correctly!");
      return 0d;
    }
  }
  
  public static void setFare(String station, String inSystem, double fare) {
    JSONObject fareDict = getJsonFromFile(new File(plugin.getDataFolder(), "fares.json"));

  }
  
}