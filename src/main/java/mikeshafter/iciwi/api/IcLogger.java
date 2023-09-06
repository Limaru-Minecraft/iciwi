package mikeshafter.iciwi.api;

import mikeshafter.iciwi.Iciwi;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

public class IcLogger {

  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Path file = Paths.get("log.csv");

  public IcLogger() {
    try {
      Files.createFile(file);
    } catch (FileAlreadyExistsException e) {
      plugin.getLogger().info("Using existing file: " + file);
    } catch (IOException e) {
      plugin.getLogger().warning("Unable to create a file: " + file + ", check permissions!");
    }
  }
  
  public void record (String... data) {
    try {
      String finalString = String.join(",", data);
      Files.writeString(file, finalString, StandardOpenOption.APPEND);
    } catch (IOException e) {
      plugin.getLogger().warning(file + " is not writable, please check permissions!");
    }
  }
}
