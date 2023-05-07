package mikeshafter.iciwi.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import mikeshafter.iciwi.Iciwi;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Commands {

  Iciwi plugin = Iciwi.getPlugin(Iciwi.class);

  @CommandMethod("iciwi reload")
  @CommandDescription("Reloads all configuration files")
  @CommandPermission("iciwi.reload")
  public void reload(final @NonNull CommandSender sender) {
    plugin.reloadAllConfig();
    sender.sendMessage("Reloaded all config!");
  }

  @CommandMethod("iciwi penalty <amount>")
  @CommandDescription("Sets the penalty penalty given to fare evaders")
  @CommandPermission("iciwi.penalty") 
  public void penalty(final @NonNull CommandSender sender, final @NonNull @Argument("amount") Double amount) {
    plugin.getConfig().set("penalty", amount);
    plugin.saveConfig();
    sender.sendMessage("Updated penalty value with new amount!");
  }
 
  @CommandMethod("iciwi deposit <amount>")
  @CommandDescription("Sets the deposit paid when buying a new card")
  @CommandPermission("iciwi.deposit") 
  public void deposit(final @NonNull CommandSender sender, final @NonNull @Argument("amount") Double amount) {
    plugin.getConfig().set("deposit", amount);
    plugin.saveConfig();
    sender.sendMessage("Updated deposit value with new amount!");
  }

} 
