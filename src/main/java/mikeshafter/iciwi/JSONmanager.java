package mikeshafter.iciwi;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class JSONmanager{
  
  private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  
  public static double getjson(String station, String inSystem){
    
    JSONParser parser = new JSONParser();
    
    try{
      // Get file
      File file = new File(plugin.getDataFolder(), "fares.json");
      Scanner scanner = new Scanner(file);
      LinkedList<String> jsonList = new LinkedList<>();
      while (scanner.hasNextLine()){
        jsonList.add(scanner.nextLine());
      }
      String jsonstring = jsonList.toString().replaceAll(", ", "").replaceAll("[\\[\\]]", "");
      
      // Parse json
      Object obj = parser.parse(jsonstring);
      JSONObject dict = (JSONObject) obj;
      
      // From var inSystem to var station
      // Return fare
      // {"inSystem":{"station":fare,"station":fare},"inSystem":{"station":fare,"station":fare}}
      JSONObject from = (JSONObject) dict.get(inSystem);
      return (double) from.get(station);
      
    } catch (FileNotFoundException e){
      e.printStackTrace();
      return 0;
    } catch (ParseException e){
      e.printStackTrace();
      return 0;
    }
  }
}
