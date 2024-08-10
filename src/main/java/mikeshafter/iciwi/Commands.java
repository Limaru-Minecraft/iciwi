package mikeshafter.iciwi;

import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Quoted;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Owners;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import com.bergerkiller.bukkit.common.utils.TimeUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@SuppressWarnings("unused")
public class Commands {
	private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
	private final Owners owners = plugin.owners;
	private final Fares fares = plugin.fares;
	private final CloudSimpleHandler cloud = new CloudSimpleHandler();

	public CloudSimpleHandler getHandler() { return cloud; }

	public void enable(Iciwi plugin) {
		cloud.enable(plugin);
		cloud.annotations(this);
		cloud.helpCommand(Collections.singletonList("iciwi"), "Shows information about all of Iciwi's commands");
	}

	private String formatString(String message, String... items) {
		message = "§a" + message.replace("%s", "§e%s§a");
		return String.format(message, (Object[]) items);
	}

	@Suggestions("company_list")
	public @NonNull List<String> suggestCompanyList(
			final @NonNull CommandContext<CommandSender> ctx,
			final @NonNull String input
	) {
		return owners.getAllCompanies().stream().toList();
	}

	@Suggestions("start_list")
	public List<String> suggestStartList (
			final @NonNull CommandContext<CommandSender> ctx,
			final @NonNull String input
	) {
		return fares.getAllStarts().stream().toList();
	}

@Suggestions("fareclass_list")
public List<String> suggestClassList(
	final @NonNull CommandContext<CommandSender> ctx,
	final @NonNull String input
) {
	return fares.getAllClasses().stream().toList();
}

	@Suggestions("railpass_list")
	public List<String> suggestRailPassList(
			final @NonNull CommandContext<CommandSender> ctx,
			final @NonNull String input
	) {
		return owners.getAllRailPasses().stream().toList();
	}

@Suggestions("player_list")
public List<String> suggestPlayerList(
	final @NonNull CommandContext<CommandSender> ctx,
	final @NonNull String input
) {
	return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
}

	@Command("iciwi reload")
	@CommandDescription("Reloads all configuration files")
	@Permission("iciwi.reload")
	public void reload(
			final @NonNull CommandSender sender,
			final Iciwi plugin
	) {
		plugin.reloadAllConfig();
		sender.sendMessage("Reloaded all config!");
	}

	@Command("iciwi penalty <amount>")
	@CommandDescription("Sets the penalty penalty given to fare evaders")
	@Permission("iciwi.penalty")
	public void penalty(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "amount") Double amount
	) {
		plugin.getConfig().set("penalty", amount);
		plugin.saveConfig();
		sender.sendMessage("Updated penalty value with new amount!");
	}

	@Command("iciwi deposit <amount>")
	@CommandDescription("Sets the deposit paid when buying a new card")
	@Permission("iciwi.deposit")
	public void deposit(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "amount") Double amount
	) {
		plugin.getConfig().set("deposit", amount);
		plugin.saveConfig();
		sender.sendMessage("Updated deposit value with new amount!");
	}

	@Command("iciwi addpricelist <amount>")
	@CommandDescription("Adds an option to the choices of card values")
	@Permission("iciwi.addpricelist")
	public void addpricelist(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "amount") Double amount
	) {
		List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
		priceArray.add(amount);
		plugin.getConfig().set("price-array", priceArray);
		plugin.saveConfig();
		sender.sendMessage("Added a new option to the price list.");
	}

	@Suggestions("card_price_list")
	public @NonNull List<String> suggestPriceList(
			final @NonNull CommandContext<CommandSender> ctx,
			final @NonNull String input
	) {
		return plugin.getConfig().getFloatList("price-array").stream().map(e -> String.format("%.2f", e)).collect(Collectors.toList());
	}

	@Command("iciwi removepricelist <amount>")
	@CommandDescription("Adds an option to the choices of card values")
	@Permission("iciwi.removepricelist")
	public void removepricelist(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "amount", suggestions = "card_price_list") Double amount
	) {
		List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
		priceArray.remove(amount);
		plugin.getConfig().set("price-array", priceArray);
		plugin.saveConfig();
		sender.sendMessage("Removed a new option from the price list.");
	}

	@Command("iciwi maxtransfertime <amount>")
	@CommandDescription("Sets the maximum time allowed for an out-of-station transfer to happen.")
	@Permission("iciwi.maxtransfertime")
	public void maxtransfertime(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "amount") String amount
	) {
		plugin.getConfig().set("max-transfer-time", TimeUtil.getTime(amount));
		plugin.saveConfig();
		sender.sendMessage("Set the maximum time allowed for an OSI.");
	}

	@Command("iciwi gateclosedelay <amount>")
	@CommandDescription("Sets the duration whereby fare gates open.")
	@Permission("iciwi.gateclosedelay")
	public void gateclosedelay(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "amount") Long amount
	) {
		plugin.getConfig().set("gate-close-delay", amount);
		plugin.saveConfig();
		sender.sendMessage("Set the duration whereby fare gates open.");
	}

	@Command("iciwi closeafterpass <amount>")
	@CommandDescription("Sets the duration for which the gates are still open after a player walks through.")
	@Permission("iciwi.closeafterpass")
	public void closeafterpass(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "amount") Long amount
	) {
		plugin.getConfig().set("close-after-pass", amount);
		plugin.saveConfig();
		sender.sendMessage("Set the duration for which the gates are still open after a player walks through.");
	}

	@Command("iciwi defaultfareClass <fareclass>")
	@CommandDescription("Sets the fare fareClass used by default when card payment is used.")
	@Permission("iciwi.defaultfareclass")
	public void default_fare_class(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "fareclass", suggestions = "fareclass_list") String c
	) {
		plugin.getConfig().set("default-fare-class", c);
		plugin.saveConfig();
		sender.sendMessage("Set the default train fareClass.");
	}

	@Command("iciwi owners alias set <company> <username>")
	@CommandDescription("Sets the revenue collector for a company.")
	@Permission("iciwi.owners.alias")
	public void owners_alias_set(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "company", suggestions = "company_list") String company,
			final @NonNull @Argument(value = "username", suggestions = "player_list") String username
	) {
		owners.set("Aliases." + company, username);
		owners.save();
		sender.sendMessage(formatString("The revenue of %s will now be sent to %s.", company, username));
	}

	@Command("iciwi owners alias unset <company>")
	@CommandDescription("Removes the revenue collector for a company.")
	@Permission("iciwi.owners.alias")
	public void owners_alias_unset(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "company", suggestions = "company_list") String company
	) {
		owners.set("Aliases." + company, null);
		owners.save();
		sender.sendMessage(formatString("The revenue of %s will no longer be sent to anyone.", company));
	}

	@Command("iciwi owners operator <station> add <company>")
	@CommandDescription("Adds an owning company to a station.")
	@Permission("iciwi.owners.operator")
	public void owners_operator_add(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "station", suggestions = "start_list") String station,
			final @NonNull @Argument(value = "company", suggestions = "company_list") String company
	) {
		owners.addOwner(station, company);
		owners.save();
		sender.sendMessage(formatString("%s now operates %s.", company, station));
	}

	@Command("iciwi owners operator <station> remove <company>")
	@CommandDescription("Removes an owning company from a station.")
	@Permission("iciwi.owners.operator")
	public void owners_operator_remove(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "station", suggestions = "start_list") String station,
			final @NonNull @Argument(value = "company", suggestions = "company_list") String company
	) {
		owners.removeOwner(station, company);
		owners.save();
		sender.sendMessage(formatString("%s no longer operates %s.", company, station));
	}

	@Command("iciwi owners operator <station> set <company>")
	@CommandDescription("Sets the owning company of a station.")
	@Permission("iciwi.owners.operator")
	public void owners_operator_set(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "station", suggestions = "start_list") String station,
			final @NonNull @Argument(value = "company", suggestions = "company_list") String company
	) {
		owners.setOwners(station, Collections.singletonList(company));
		owners.save();
		sender.sendMessage(formatString("%s is now the sole operator of %s.", company, station));
	}

	@Command("iciwi owners operator <station> delete")
	@CommandDescription("Removes all owning companies of a station.")
	@Permission("iciwi.owners.operator")
	public void owners_operator_delete(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "station", suggestions = "start_list") String station
	) {
		owners.set("Operators." + station, null);
		owners.save();
		sender.sendMessage(formatString("No company is now operating %s.", station));
	}

@Command("iciwi railpass set <name> <company> <duration> <amount> <paidpercentage>")
@CommandDescription("Sets the rail company that owns the given railpass.")
@Permission("iciwi.owners.railpass")
public void owners_railpass_set(
	final @NonNull CommandSender sender,
	final Iciwi plugin,
	final @NonNull @Argument(value = "name") String name,
	final @NonNull @Argument(value = "company", suggestions = "company_list") String company,
	final @NonNull @Argument(value = "duration") String duration,
	final @NonNull @Argument(value = "amount") Double price,
	final @NonNull @Argument(value = "paidpercentage") Double pp
) {
	owners.setRailPassInfo(name, company, duration, price, pp);
	owners.save();
	sender.sendMessage(formatString("New rail pass created!"));
}

	@Command("iciwi railpass edit <name> operator <company>")
	@CommandDescription("Sets the rail company that owns the given railpass.")
	@Permission("iciwi.owners.railpass")
	public void owners_railpass_operator(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "name", suggestions = "railpass_list") String name,
			final @NonNull @Argument(value = "company", suggestions = "company_list") String company
	) {
		owners.set("RailPasses." + name + ".operator", company);
		owners.save();
		sender.sendMessage(formatString("The railpass %s is now owned by %s", name, company));
	}

	@Command("iciwi railpass edit <name> duration <duration>")
	@CommandDescription("Sets the duration that the given railpass is active.")
	@Permission("iciwi.owners.railpass")
	public void owners_railpass_duration(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "name", suggestions = "railpass_list") String name,
			final @NonNull @Argument(value = "duration") String duration
	) {
		owners.set("RailPasses." + name + ".duration", duration);
		owners.save();
		sender.sendMessage(formatString("The duration of railpass %s is now %s", name, duration));
	}

	@Command("iciwi railpass edit <name> price <amount>")
	@CommandDescription("Sets the price of the given railpass.")
	@Permission("iciwi.owners.railpass")
	public void owners_railpass_price(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "name", suggestions = "railpass_list") String name,
			final @NonNull @Argument(value = "amount") Double price
	) {
		owners.set("RailPasses." + name + ".price", price);
		owners.save();
		sender.sendMessage(formatString("The price of railpass %s is now %s", name, String.valueOf(price)));
	}

	@Command("iciwi railpass edit <name> percentage <paidpercentage>")
	@CommandDescription("Sets the percentage paid by the card holder when they use the railpass.")
	@Permission("iciwi.owners.railpass")
	public void owners_railpass_percentage(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "name", suggestions = "railpass_list") String name,
			final @NonNull @Argument(value = "paidpercentage") Double pp
	) {
		owners.set("RailPasses." + name + ".percentage", pp);
		owners.save();
		sender.sendMessage(formatString("The payment percentage of railpass %s is now %s", name, String.valueOf(pp)));
	}

	@Command("iciwi railpass delete <name>")
	@CommandDescription("Deletes a railpass.")
	@Permission("iciwi.owners.railpass")
	public void owners_railpass_delete(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "name", suggestions = "railpass_list") String name
	) {
		owners.set("RailPasses." + name, null);
		owners.save();
		sender.sendMessage(formatString("Railpass %s has been deleted", name));
	}

@Command("iciwi operatorticket <company> <price>")
@CommandDescription("Creates/deletes an operator ticket. Set <amount> to 0 for deletion.")
@Permission("iciwi.owners.railpass")
public void owners_operatorticket(
	final @NonNull CommandSender sender,
	final Iciwi plugin,
	final @NonNull @Argument(value = "company", suggestions = "company_list") String company,
	final @NonNull @Argument(value = "price") Double price
) {
	owners.setOperatorTicket(company, price);
	owners.save();
	sender.sendMessage(formatString("Single journey tickets for %s has been set to %s.", company, String.valueOf(price)));
}

@Command("iciwi farecap <company> <amount> <duration>")
@CommandDescription("Creates/deletes a fare cap. Set <amount> to 0 for deletion.")
@Permission("iciwi.owners.railpass")
public void owners_operatorticket(
	final @NonNull CommandSender sender,
	final Iciwi plugin,
	final @NonNull @Argument(value = "company", suggestions = "company_list") String company,
	final @NonNull @Argument(value = "amount") Double amount,
	final @NonNull @Argument(value = "duration") String duration
) {
	owners.setFareCapAmt(company, amount);
	owners.setFareCapDuration(company, duration);
	owners.save();
	sender.sendMessage(formatString("The fare cap for %s has been set to %s, valid for %s.", company, String.valueOf(amount), duration));
}

	@Command("iciwi fares set <start> <end> <fareClass> <price>")
	@CommandDescription("Creates a new fare.")
	@Permission("iciwi.fares.set")
	public void fares_set(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "start", suggestions = "start_list") String start,
			final @NonNull @Argument(value = "end", suggestions = "start_list") String end,
			final @NonNull @Argument(value = "fareClass", suggestions = "fareclass_list") String fareClass,
			final @NonNull @Argument(value = "price") Double price
	) {
		// Run getOwners to register station owners
		owners.getOwners(start);
		fares.setFare(start, end, fareClass, price);
		sender.sendMessage(formatString("A new fare from %s to %s using the class %s has been set to: %s", start, end, fareClass, String.valueOf(price)));
	}

	@Command("iciwi fares check <start> [end] [fareClass]")
	@CommandDescription("Either checks for all destinations from a station, all the fare classes for a journey or the fare between two stations for a fare class.")
	@Permission("iciwi.fares.check")
	public void fares_check(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "start", suggestions = "start_list") String start,
			final @Argument(value = "end", suggestions = "start_list") String end,
			final @Argument(value = "fareClass", suggestions = "fareclass_list") String fareClass
	) {
		Set<String> s;
		if (end == null) s = fares.getDestinations(start);
		else if (fareClass == null) s = fares.getClasses(start, end);
		else s = Collections.singleton(String.valueOf(fares.getFare(start, end, fareClass)));

		s.forEach(sender::sendMessage);
	}

	@Command("iciwi fares unset <start> <end> <fareClass>")
	@CommandDescription("Deletes a fare.")
	@Permission("iciwi.fares.unset")
	public void fares_unset(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "start", suggestions = "start_list") String start,
			final @NonNull @Argument(value = "end", suggestions = "start_list") String end,
			final @NonNull @Argument(value = "fareClass", suggestions = "fareclass_list") String fareClass
	) {
		fares.unsetFare(start, end, fareClass);
		sender.sendMessage(formatString("The fare from %s to %s using the class %s has been deleted.", start, end, fareClass));
	}

	@Command("iciwi fares deletejourney <start> <end>")
	@CommandDescription("Deletes all fares between a start and end point.")
	@Permission("iciwi.fares.deletejourney")
	public void delete_journey (
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "start", suggestions = "start_list") String start,
			final @NonNull @Argument(value = "end", suggestions = "start_list") String end
	) {
		fares.deleteJourney(start, end);
		sender.sendMessage(formatString("All fares from %s to %s has been deleted.", start, end));
	}

	@Command("iciwi fares deletestation <start>")
	@CommandDescription("Removes a station and all its associated fares from the data.")
	@Permission("iciwi.fares.deletestation")
	public void delete_station(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "start", suggestions = "start_list") String start
	) {
		fares.deleteStation(start);
		sender.sendMessage(formatString("All fares to all stations from %s has been deleted.", start));
	}

	@Command("iciwi debug sql <sql>")
	@CommandDescription("Runs raw SQL in the iciwi database.")
	@Permission("iciwi.debug.sql")
	public void debug_sql(
			final @NonNull CommandSender sender,
			final Iciwi plugin,
			final @NonNull @Argument(value = "sql") @Quoted String sql
	) {
		CardSql cardSql = new CardSql();
		String[][] table = cardSql.runSql(sql);
		for (String[] strings : table) sender.sendMessage(String.join(", ", strings));
	}
}
