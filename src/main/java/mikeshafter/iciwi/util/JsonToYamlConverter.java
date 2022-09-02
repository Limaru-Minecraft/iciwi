package mikeshafter.iciwi.util;

import mikeshafter.iciwi.Iciwi;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class JsonToYamlConverter {
  public static void main() {
    File jsonFile = new File(Iciwi.getPlugin(Iciwi.class).getDataFolder(), "fares.json");
    File yamlFile = new File(Iciwi.getPlugin(Iciwi.class).getDataFolder(), "fares.yml");

    try {
      Scanner scanner = new Scanner(jsonFile);
      StringBuilder jsonBuilder = new StringBuilder();
      while (scanner.hasNextLine()) jsonBuilder.append(scanner.nextLine());
      scanner.close();
      String json = jsonBuilder.toString();

      // convert all doubles to {"Second Class": double}
      json = json.replaceAll("(\\d+\\.\\d+)", "{'Second Class': $1 }");

      // write to file
      FileWriter fileWriter = new FileWriter(yamlFile);
      fileWriter.write(json);
      fileWriter.close();
    
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
