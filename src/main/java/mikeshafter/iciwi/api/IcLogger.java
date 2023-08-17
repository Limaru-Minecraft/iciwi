package mikeshafter.iciwi.api;

import mikeshafter.iciwi.Iciwi;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class IcLogger {

  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Path file = Paths.get(plugin.getConfig().getString("logger-file"));

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
      byte[] bytes = finalString.getBytes(StandardCharsets.UTF_8);
      Files.write(file, bytes, StandardOpenOption.APPEND);
    } catch (IOException e) {
      plugin.getLogger().warning(file + " is not writable, please check permissions!");
    }
  }
}