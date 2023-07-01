package mikeshafter.iciwi.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Owners;

import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import com.bergerkiller.bukkit.common.utils.TimeUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import cloud.commandframework.paper.PaperCommandManager;


@SuppressWarnings("Unused")
public class Commands {
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Owners owners = plugin.owners;
  private final Fares fares = plugin.fares;
  //private PaperCommandManager<CommandSender> manager;
  private final HashMap<Player, Odometer> odometer = new HashMap<>();

  public void setManager(PaperCommandManager<CommandSender> manager) {
    //this.manager = manager;
  }

  private String formatString(String message, String... items) {
    message = "§a" + message.replace("%s", "§e%s§a");
    return String.format(message, (Object[]) items);
  }

  @CommandMethod("iciwi reload")
  @CommandDescription("Reloads all configuration files")
  @CommandPermission("iciwi.reload")
  public void reload(final @NonNull CommandSender sender)
  {
    plugin.reloadAllConfig();
    sender.sendMessage("Reloaded all config!");
  }

  @CommandMethod("iciwi penalty <amount>")
  @CommandDescription("Sets the penalty penalty given to fare evaders")
  @CommandPermission("iciwi.penalty")
  public void penalty(final @NonNull CommandSender sender,
    final @NonNull @Argument("amount") Double amount)
  {
    plugin.getConfig().set("penalty", amount);
    plugin.saveConfig();
    sender.sendMessage("Updated penalty value with new amount!");
  }

  @CommandMethod("iciwi deposit <amount>")
  @CommandDescription("Sets the deposit paid when buying a new card")
  @CommandPermission("iciwi.deposit")
  public void deposit(final @NonNull CommandSender sender,
    final @NonNull @Argument("amount") Double amount)
  {
    plugin.getConfig().set("deposit", amount);
    plugin.saveConfig();
    sender.sendMessage("Updated deposit value with new amount!");
  }

  @CommandMethod("iciwi addpricelist <amount>")
  @CommandDescription("Adds an option to the choices of card values")
  @CommandPermission("iciwi.addpricelist")
  public void addpricelist(final @NonNull CommandSender sender,
    final @NonNull @Argument("amount") Double amount)
  {
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    priceArray.add(amount);
    plugin.getConfig().set("price-array", priceArray);
    plugin.saveConfig();
    sender.sendMessage("Added a new option to the price list.");
  }

  @Suggestions("card_price_list")
  public @NonNull List<String> suggestPriceList(final @NonNull CommandContext<CommandSender> ctx, final @NonNull String input) {
    return plugin.getConfig().getFloatList("price-array").stream().map(e -> String.format(".2f", e)).collect(Collectors.toList());
  }

  @CommandMethod("iciwi removepricelist <amount>")
  @CommandDescription("Adds an option to the choices of card values")
  @CommandPermission("iciwi.removepricelist")
  public void removepricelist(final @NonNull CommandSender sender,
    final @NonNull @Argument(value="amount", suggestions = "card_price_list") Double amount)
  {
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    priceArray.remove(amount);
    plugin.getConfig().set("price-array", priceArray);
    plugin.saveConfig();
    sender.sendMessage("Removed a new option from the price list.");
  }

  @CommandMethod("iciwi maxtransfertime <amount>")
  @CommandDescription("Sets the maximum time allowed for an out-of-station transfer to happen.")
  @CommandPermission("iciwi.maxtransfertime")
  public void maxtransfertime(final @NonNull CommandSender sender,
    final @NonNull @Argument("amount") String amount)
  {
    plugin.getConfig().set("max-transfer-time", TimeUtil.getTime(amount));
    plugin.saveConfig();
    sender.sendMessage("Set the maximum time allowed for an OSI.");
  }

  @CommandMethod("iciwi gateclosedelay <amount>")
  @CommandDescription("Sets the duration whereby fare gates open.")
  @CommandPermission("iciwi.gateclosedelay")
  public void gateclosedelay(final @NonNull CommandSender sender,
    final @NonNull @Argument("amount") Integer amount)
  {
    plugin.getConfig().set("gate-close-delay", amount);
    plugin.saveConfig();
    sender.sendMessage("Set the duration whereby fare gates open.");
  }

  @CommandMethod("iciwi defaultfareClass <fareClassname>")
  @CommandDescription("Sets the fare fareClass used by default when card payment is used.")
  @CommandPermission("iciwi.defaultfareClass")
  public void defaultfareClass(final @NonNull CommandSender sender,
      final @NonNull @Argument("fareClassname") String fareClassname)
  {
    plugin.getConfig().set("default-fareClass", fareClassname);
    plugin.saveConfig();
    sender.sendMessage("Set the default train fareClass.");
  }

  @Suggestions("company_list")
  public @NonNull List<String> suggestCompanyList(final @NonNull CommandContext<CommandSender> ctx, final @NonNull String input) {
    return owners.getAllCompanies().stream().toList();
  }

  @Suggestions("station_list")
  public List<String> suggestStationList(final @NonNull CommandContext<CommandSender> ctx, final @NonNull String input) {
    return fares.getAllStations().stream().toList();
  }

  @Suggestions("railpass_list")
  public List<String> suggestRailPassList(final @NonNull CommandContext<CommandSender> ctx, final @NonNull String input) {
    return owners.getAllRailPasses().stream().toList();
  }

  @CommandMethod("iciwi owners alias set <company> <username>")
  @CommandDescription("Sets the revenue collector for a company.")
  @CommandPermission("iciwi.owners.alias")
  public void owners_alias_set(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "company", suggestions = "company_list") String company,
      final @NonNull @Argument("username") String username)
  {
    owners.set("Aliases."+company, username);
    owners.save();
    sender.sendMessage(formatString("The coffers of %s will now be sent to %s.", company, username));
  }

  @CommandMethod("iciwi owners alias unset <company>")
  @CommandDescription("Removes the revenue collector for a company.")
  @CommandPermission("iciwi.owners.alias")
  public void owners_alias_unset(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "company", suggestions = "company_list") String company)
  {
    owners.set("Aliases."+company, null);
    owners.save();
    sender.sendMessage(formatString("The coffers of %s will no longer be sent to anyone.", company));
  }

  @CommandMethod("iciwi owners operator <station> add <company>")
  @CommandDescription("Adds an owning company to a station.")
  @CommandPermission("iciwi.owners.operator")
  public void owners_operator_add(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "station", suggestions = "station_list") String station,
      final @NonNull @Argument(value = "company", suggestions = "company_list") String company)
  {
    owners.addOwner(station, company);
    owners.save();
  }

  @CommandMethod("iciwi owners operator <station> remove <company>")
  @CommandDescription("Removes an owning company from a station.")
  @CommandPermission("iciwi.owners.operator")
  public void owners_operator_remove(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "station", suggestions = "station_list") String station,
      final @NonNull @Argument(value = "company", suggestions = "company_list") String company)
  {
    owners.addOwner(station, company);
    owners.save();
  }

  @CommandMethod("iciwi owners operator <station> set <company>")
  @CommandDescription("Sets the owning company of a station.")
  @CommandPermission("iciwi.owners.operator")
  public void owners_operator_set(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "station", suggestions = "station_list") String station,
      final @NonNull @Argument(value = "company", suggestions = "company_list") String company)
  {
    owners.setOwners(station, Collections.singletonList(company));
    owners.save();
  }

  @CommandMethod("iciwi owners operator <station> delete")
  @CommandDescription("Removes all owning companies of a station.")
  @CommandPermission("iciwi.owners.operator")
  public void owners_operator_delete(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "station", suggestions = "station_list") String station)
  {
    owners.set("Operators."+station, null);
    owners.save();
  }

  @CommandMethod("iciwi owners railpass <name> operator <company>")
  @CommandDescription("Sets the rail company that owns the given railpass.")
  @CommandPermission("iciwi.owners.railpass")
  public void owners_railpass_operator(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "name", suggestions = "railpass_list") String name,
      final @NonNull @Argument(value = "company", suggestions = "company_list") String company)
  {
    owners.set("RailPasses."+name+".operator", company);
    owners.save();
  }

  @CommandMethod("iciwi owners railpass <name> duration <duration>")
  @CommandDescription("Sets the duration that the given railpass is active.")
  @CommandPermission("iciwi.owners.railpass")
  public void owners_railpass_duration(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "name", suggestions = "railpass_list") String name,
      final @NonNull @Argument("duration") Long duration)
  {
    owners.set("RailPasses."+name+".duration", duration);
    owners.save();
  }

  @CommandMethod("iciwi owners railpass <name> price <price>")
  @CommandDescription("Sets the price of the given railpass.")
  @CommandPermission("iciwi.owners.railpass")
  public void owners_railpass_price(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "name", suggestions = "railpass_list") String name,
      final @NonNull @Argument("price") Double price)
  {
    owners.set("RailPasses."+name+".price", price);
    owners.save();
  }

  @CommandMethod("iciwi owners railpass <name> percentage <paidpercentage>")
  @CommandDescription("Sets the percentage paid by the card holder when they use the railpass.")
  @CommandPermission("iciwi.owners.railpass")
  public void owners_railpass_percentage(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "name", suggestions = "railpass_list") String name,
      final @NonNull @Argument("paidpercentage") Double paidpercentage)
  {
    owners.set("RailPasses."+name+".percentage", paidpercentage);
    owners.save();
  }

  @CommandMethod("iciwi owners railpass <name> delete")
  @CommandDescription("Deletes a railpass.")
  @CommandPermission("iciwi.owners.railpass")
  public void owners_railpass_delete(final @NonNull CommandSender sender,
      final @NonNull @Argument(value = "name", suggestions = "railpass_list") String name)
  {
    owners.set("RailPasses."+name, null);
    owners.save();
  }

  @CommandMethod("iciwi fares set <start> <end> <fareClass> <price>")
  @CommandDescription("Creates a new fare.")
  @CommandPermission("iciwi.fares.set")
  public void fares_set(final @NonNull CommandSender sender,
    final @NonNull @Argument(value = "start", suggestions = "station_list") String start,
    final @NonNull @Argument(value = "end", suggestions = "station_list") String end,
    final @NonNull @Argument("fareClass") String fareClass,
    final @NonNull @Argument("price") Double price)
  {
    fares.setFare(start, end, fareClass, price);
  }

  @CommandMethod("iciwi fares unset <start> <end> <fareClass>")
  @CommandDescription("Deletes a fare.")
  @CommandPermission("iciwi.fares.unset")
  public void fares_set(final @NonNull CommandSender sender,
    final @NonNull @Argument(value = "start", suggestions = "station_list") String start,
    final @NonNull @Argument(value = "end", suggestions = "station_list") String end,
    final @NonNull @Argument("fareClass") String fareClass)
  {
    fares.unsetFare(start, end, fareClass);
  }

  @CommandMethod("iciwi fares deletejourney <start> <end>")
  @CommandDescription("Deletes all fares between a start and end point.")
  @CommandPermission("iciwi.fares.deletejourney")
  public void fares_set(final @NonNull CommandSender sender,
    final @NonNull @Argument(value = "start", suggestions = "station_list") String start,
    final @NonNull @Argument(value = "end", suggestions = "station_list") String end)
  {
    fares.deleteJourney(start, end);
  }

  @CommandMethod("iciwi fares deletestation <start>")
  @CommandDescription("Removes a station and all its associated fares from the data.")
  @CommandPermission("iciwi.fares.deletestation")
  public void fares_set(final @NonNull CommandSender sender,
    final @NonNull @Argument(value = "start", suggestions = "station_list") String start)
  {
    fares.deleteStation(start);
  }

  @CommandMethod("odometer start-lap")
  @CommandDescription("Starts/laps an odometer, like a stopwatch.")
  public void odometerStart(final @NonNull Player player) {
    int distance = player.getStatistic(Statistic.MINECART_ONE_CM);
    if (odometer.containsKey(player)) {
      Odometer playerMeter = odometer.get(player);
      if (playerMeter.recording) {
        // stop
        playerMeter.recorded = distance - playerMeter.lastRecord;
        playerMeter.recording = false;
        player.sendMessage("Stopped recording!");
        for (int i = 0; i < playerMeter.distances.size(); i++) {
          player.sendMessage(i + " - " + playerMeter.distances.get(i));
        }
      } else {
        // reset
        playerMeter.lastRecord = distance;
        playerMeter.distances = new ArrayList<>();
        playerMeter.recorded = 0;
        player.sendMessage("Reset memory!");
      }
    } else {
      odometer.put(player, new Odometer(new ArrayList<>(), distance, false));
    }
  }

  @CommandMethod("odometer stop-reset")
  @CommandDescription("Stops/resets an odometer, like a stopwatch.")
  public void odometerStop(final @NonNull Player player) {
    int distance = player.getStatistic(Statistic.MINECART_ONE_CM);
    if (odometer.containsKey(player)) {
      Odometer playerMeter = odometer.get(player);
      if (playerMeter.recording) {
        // stop
        playerMeter.recorded = distance - playerMeter.lastRecord;
        playerMeter.recording = false;
        player.sendMessage("Stopped recording!");
        for (int i = 0; i < playerMeter.distances.size(); i++) {
          player.sendMessage(i + " - " + playerMeter.distances.get(i));
        }
      } else {
        // reset
        playerMeter.lastRecord = distance;
        playerMeter.distances = new ArrayList<>();
        playerMeter.recorded = 0;
        player.sendMessage("Reset memory!");
      }
    } else {
      odometer.put(player, new Odometer(new ArrayList<>(), distance, false));
    }
  }

}
