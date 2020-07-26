package mikeshafter.iciwi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager{
  
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  
  // ===== Files and file configs =====
  public FileConfiguration fareconfig;
  public File farefile;
  // ==================================
  
  public void setupFares(){
    if (!plugin.getDataFolder().exists()){
      plugin.getDataFolder().mkdir();
    }
    
    farefile = new File(plugin.getDataFolder(), "fares.yml");
    
    if (!farefile.exists()){
      try{
        farefile.createNewFile();
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"Created fares.yml!");
      } catch (IOException e){
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED+"Could not create fares.yml!");
      }
    }
    
    fareconfig = YamlConfiguration.loadConfiguration(farefile);
  }
  
  public FileConfiguration getFares(){
    return fareconfig;
  }
  
  public void saveFareConfig(){
    try{
      fareconfig.save(farefile);
      Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"fares.yml has been saved!");
    } catch (IOException e){
      Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED+"Could not save fares.yml!");
    }
  }
  
  public void reloadFareConfig(){
    fareconfig = YamlConfiguration.loadConfiguration(farefile);
    Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA+"fares.yml has been reloaded!");
  }
}
