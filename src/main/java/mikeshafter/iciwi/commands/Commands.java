package mikeshafter.iciwi.commands;

import cloud.commandframework.annotations.*;
import cloud.commandframework.paper.PaperCommandManager;
import mikeshafter.iciwi.Iciwi;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.*;
import org.bukkit.entity.Player;
import cloud.commandframework.context.CommandContext;
import java.util.stream.Collectors;

@SuppressWarnings("Unused")
public class Commands {
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Owners owners = plugin.owners;
  private final Fares fares = plugin.fares;
  privatePaperCommandManager manager;

  public void setManager(PaperCommandManager manager) {
    this.manager = manager;
  }

  public formatString(String message, String... items) {

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
    sender.sendMessage("Added a new option to the price list.")
  }

  // TODO: Test if the list changes while typing, if not alter code such that it does so.
  @Suggestions("card_price_list")
  public @NonNull List<String> suggestPriceList(final @NonNull CommandContext<CommandSender> ctx, final @NonNull String input) {
    return plugin.getConfig().getFloatList("price-array").stream().map(e -> String.format(".2f", e)).collect(Collectors.toList());
  }

  @CommandMethod("iciwi removepricelist <amount>")
  @CommandDescription("Adds an option to the choices of card values")
  @CommandPermission("iciwi.removepricelist")
  public void removepricelist(final @NonNull CommandSender sender,
    final @NonNull @Argument("amount") Double amount)
  {
    List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
    priceArray.remove(amount);
    plugin.getConfig().set("price-array", priceArray);
    plugin.saveConfig();
    sender.sendMessage("Removed a new option from the price list.")
  }

  // TODO: change this to a time format maybe?
  @CommandMethod("iciwi maxtransfertime <amount>")
  @CommandDescription("Sets the maximum time allowed for an out-of-station transfer to happen.")
  @CommandPermission("iciwi.maxtransfertime")
  public void maxtransfertime(final @NonNull CommandSender sender,
    final @NonNull @Argument("amount") Long amount)
  {
    plugin.getConfig().set("max-transfer-time", amount);
    plugin.saveConfig();
    sender.sendMessage("Set the maximum time allowed for an OSI.")
  }

  @CommandMethod("iciwi gateclosedelay <amount>")
  @CommandDescription("Sets the duration whereby fare gates open.")
  @CommandPermission("iciwi.gateclosedelay")
  public void gateclosedelay(final @NonNull CommandSender sender,
    final @NonNull @Argument("amount") Integer amount)
  {
    plugin.getConfig().set("gate-close-delay", amount);
    plugin.saveConfig();
    sender.sendMessage("Set the duration whereby fare gates open.")
  }

  @CommandMethod("iciwi defaultclass <classname>")
  @CommandDescription("Sets the fare class used by default when card payment is used.")
  @CommandPermission("iciwi.defaultclass")
  public void defaultclass(final @NonNull CommandSender sender,
      final @NonNull @Argument("classname") String classname)
  {
    plugin.getConfig().set("default-class", classname);
    plugin.saveConfig();
    sender.sendMessage("Set the default train class.")
  }

  @Suggestions("company_list")
  public @NonNull List<String> suggestCompanyList(final @NonNull CommandContext<CommandSender> ctx, final @NonNull String input) {
    return owners.getAllCompanies().stream().toList();
  }

  @CommandMethod("iciwi owners alias set <company> <username>")
  @CommandDescription("Sets the revenue collector for a company.")
  @CommandPermission("iciwi.owners.alias")
  public void owners_alias_set(final @NonNull CommandSender sender,
      final @NonNull @Argument("company") String company,
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
      final @NonNull @Argument("company") String company)
  {
    owners.set("Aliases."+company, null);
    owners.save();
    sender.sendMessage(formatString("The coffers of %s will no longer be sent to anyone.", company));
  }

  @CommandMethod("iciwi owners operator <station> add <company>")
  @CommandDescription("Adds an owning company to a station.")
  @CommandPermission("iciwi.owners.operator")
  public void owners_operator_add(final @NonNull CommandSender sender,
      final @NonNull @Argument("station") String station,
      final @NonNull @Argument("company") String company)
  {
    owners.addOwner(station, company);
    owners.save();
  }

  @CommandMethod("iciwi owners operator <station> remove <company>")
  @CommandDescription("Removes an owning company from a station.")
  @CommandPermission("iciwi.owners.operator")
  public void owners_operator_remove(final @NonNull CommandSender sender,
      final @NonNull @Argument("station") String station,
      final @NonNull @Argument("company") String company)
  {
    owners.addOwner(station, company);
    owners.save();
  }

  @CommandMethod("iciwi owners operator <station> set <company>")
  @CommandDescription("Sets the owning company of a station.")
  @CommandPermission("iciwi.owners.operator")
  public void owners_operator_set(final @NonNull CommandSender sender,
      final @NonNull @Argument("station") String station,
      final @NonNull @Argument("company") String company)
  {
    owners.setOwners(station, Collections.singletonList(company));
    owners.save();
  }

  @CommandMethod("iciwi owners operator <station> delete")
  @CommandDescription("Removes all owning companies of a station.")
  @CommandPermission("iciwi.owners.operator")
  public void owners_operator_delete(final @NonNull CommandSender sender,
      final @NonNull @Argument("station") String station)
  {
    owners.set("Operators."+station, null);
    owners.save();
  }

  @CommandMethod("iciwi owners railpass <name> operator <company>")
  @CommandDescription("Sets the rail company that owns the given railpass.")
  @CommandPermission("iciwi.owners.railpass")
  public void owners_railpass_operator(final @NonNull CommandSender sender,
      final @NonNull @Argument("name") String name,
      final @NonNull @Argument("company") String company)
  {
    owners.set("RailPasses."+name+".operator", company);
    owners.save();
  }

  @CommandMethod("iciwi owners railpass <name> duration <duration>")
  @CommandDescription("Sets the duration that the given railpass is active.")
  @CommandPermission("iciwi.owners.railpass")
  public void owners_railpass_duration(final @NonNull CommandSender sender,
      final @NonNull @Argument("name") String name,
      final @NonNull @Argument("duration") Long duration)
  {
    owners.set("RailPasses."+name+".duration", duration);
    owners.save();
  }

  @CommandMethod("iciwi owners railpass <name> price <price>")
  @CommandDescription("Sets the price of the given railpass.")
  @CommandPermission("iciwi.owners.railpass")
  public void owners_railpass_price(final @NonNull CommandSender sender,
      final @NonNull @Argument("name") String name,
      final @NonNull @Argument("price") Double price)
  {
    owners.set("RailPasses."+name+".price", price);
    owners.save();
  }

  @CommandMethod("iciwi owners railpass <name> percentage <paidpercentage>")
  @CommandDescription("Sets the percentage paid by the card holder when they use the railpass.")
  @CommandPermission("iciwi.owners.railpass")
  public void owners_railpass_percentage(final @NonNull CommandSender sender,
      final @NonNull @Argument("name") String name,
      final @NonNull @Argument("paidpercentage") Double paidpercentage)
  {
    owners.set("RailPasses."+name+".percentage", paidpercentage);
    owners.save();
  }

  @CommandMethod("iciwi owners railpass <name> delete")
  @CommandDescription("Deletes a railpass.")
  @CommandPermission("iciwi.owners.railpass")
  public void owners_railpass_delete(final @NonNull CommandSender sender,
      final @NonNull @Argument("name") String name)
  {
    owners.set("RailPasses."+name, null);
    owners.save();
  }

  @CommandMethod("iciwi fares set <start> <end> <class> <price>")
  @CommandDescription("Creates a new fare.")
  @CommandPermission("iciwi.fares.set")
  public void fares_set(final @NonNull CommandSender sender,
    final @NonNull @Argument("start") String start,
    final @NonNull @Argument("end") String end,
    final @NonNull @Argument("class") String class,
    final @NonNull @Argument("price") String price)
  {
    fares.setFare(start, end, class, price);
  }

  @CommandMethod("iciwi fares unset <start> <end> <class>")
  @CommandDescription("Deletes a fare.")
  @CommandPermission("iciwi.fares.unset")
  public void fares_set(final @NonNull CommandSender sender,
    final @NonNull @Argument("start") String start,
    final @NonNull @Argument("end") String end,
    final @NonNull @Argument("class") String class)
  {
    fares.unsetFare(start, end, class);
  }

  @CommandMethod("iciwi fares deletejourney <start> <end>")
  @CommandDescription("Deletes all fares between a start and end point.")
  @CommandPermission("iciwi.fares.deletejourney")
  public void fares_set(final @NonNull CommandSender sender,
    final @NonNull @Argument("start") String start,
    final @NonNull @Argument("end") String end)
  {
    fares.deleteJourney(start, end);
  }

  @CommandMethod("iciwi fares deletestation <start>")
  @CommandDescription("Removes a station and all its associated fares from the data.")
  @CommandPermission("iciwi.fares.deletestation")
  public void fares_set(final @NonNull CommandSender sender,
    final @NonNull @Argument("start") String start)
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
