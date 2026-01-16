package mikeshafter.iciwi;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.tickets.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import mikeshafter.iciwi.util.IciwiUtil;
import mikeshafter.iciwi.util.TimeArgumentType;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class IciwiCommands {
private static final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
private static final Owners owners = plugin.owners;
private static final Fares fares = plugin.fares;
private final MiniMessage miniMessage = MiniMessage.miniMessage();
private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();

private String formatString (String message, String... items) {
    message = "§a" + message.replace("%s", "§e%s§a");
    return String.format(message, (Object[]) items);
}

// Suggestions
private static CompletableFuture<Suggestions> suggestCompany (final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
    owners.getAllCompanies().forEach(builder::suggest);
    return builder.buildFuture();
}

private static CompletableFuture<Suggestions> suggestStation (final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
    fares.getAllStarts().forEach(builder::suggest);
    return builder.buildFuture();
}

private static CompletableFuture<Suggestions> suggestClass (final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
    fares.getAllClasses().forEach(builder::suggest);
    return builder.buildFuture();
}

private CompletableFuture<Suggestions> suggestEnd (final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
    fares.getDestinations(ctx.getArgument("start", String.class)).forEach(builder::suggest);
    return builder.buildFuture();
}

private CompletableFuture<Suggestions> suggestTravelClass (final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
    fares.getClasses(ctx.getArgument("start", String.class), ctx.getArgument("end", String.class)).forEach(builder::suggest);
    return builder.buildFuture();
}

private static CompletableFuture<Suggestions> suggestRailpass (final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder) {
    owners.getAllRailPasses().forEach(builder::suggest);
    return builder.buildFuture();
}

// Reload command
private final LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload")
    .requires(source -> source.getSender().hasPermission("iciwi.reload"))
    .executes(ctx -> {
        plugin.reloadAllConfig();
        ctx.getSource().getSender().sendMessage("Reloaded all config!");
        return 1;
    });

// Config commands
private final LiteralArgumentBuilder<CommandSourceStack> penalty = Commands.literal("penalty")
    .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
        .requires(source -> source.getSender().hasPermission("iciwi.config.penalty"))
        .executes(ctx -> {
            plugin.getConfig().set("penalty", ctx.getArgument("amount", Double.class));
            plugin.saveConfig();
            ctx.getSource().getSender().sendMessage("Updated penalty value with new amount!");
            return 1;
        })
    );

private final LiteralArgumentBuilder<CommandSourceStack> deposit = Commands.literal("deposit")
    .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
        .requires(source -> source.getSender().hasPermission("iciwi.config.deposit"))
        .executes(ctx -> {
            plugin.getConfig().set("deposit", ctx.getArgument("amount", Double.class));
            plugin.saveConfig();
            ctx.getSource().getSender().sendMessage("Updated deposit value with new amount!");
            return 1;
        })
    );

private final LiteralArgumentBuilder<CommandSourceStack> addPriceList = Commands.literal("addpricelist")
    .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
        .requires(source -> source.getSender().hasPermission("iciwi.config.addpricelist"))
        .executes(ctx -> {
            List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
            priceArray.add(ctx.getArgument("amount", Double.class));
            plugin.getConfig().set("price-array", priceArray);
            plugin.saveConfig();
            ctx.getSource().getSender().sendMessage("Added a new option to the price list.");
            return 1;
        })
    );

private final LiteralArgumentBuilder<CommandSourceStack> removePriceList = Commands.literal("removepricelist")
    .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
        .requires(source -> source.getSender().hasPermission("iciwi.config.removepricelist"))
        .executes(ctx -> {
            List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
            priceArray.remove(ctx.getArgument("amount", Double.class));
            plugin.getConfig().set("price-array", priceArray);
            plugin.saveConfig();
            ctx.getSource().getSender().sendMessage("Removed a new option from the price list.");
            return 1;
        })
    );

private final LiteralArgumentBuilder<CommandSourceStack> maxTransferTime = Commands.literal("maxtransfertime")
    .then(Commands.argument("timeamt", TimeArgumentType.time())
        .requires(source -> source.getSender().hasPermission("iciwi.config.maxtransfertime"))
        .executes(ctx -> {
            plugin.getConfig().set("max-transfer-time", ctx.getArgument("timeamt", Long.class));
            plugin.saveConfig();
            ctx.getSource().getSender().sendMessage("Set the maximum time allowed for an OSI.");
            return 1;
        })
    );

private final LiteralArgumentBuilder<CommandSourceStack> gateCloseDelay = Commands.literal("gateclosedelay")
    .then(Commands.argument("timeamt", TimeArgumentType.time())
        .requires(source -> source.getSender().hasPermission("iciwi.config.gateclosedelay"))
        .executes(ctx -> {
            plugin.getConfig().set("gate-close-delay", ctx.getArgument("timeamt", Long.class));
            plugin.saveConfig();
            ctx.getSource().getSender().sendMessage("Set the duration whereby fare gates open.");
            return 1;
        })
    );

private final LiteralArgumentBuilder<CommandSourceStack> closeAfterPass = Commands.literal("closeafterpass")
    .then(Commands.argument("timeamt", TimeArgumentType.time())
        .requires(source -> source.getSender().hasPermission("iciwi.config.closeafterpass"))
        .executes(ctx -> {
            plugin.getConfig().set("close-after-pass", ctx.getArgument("timeamt", Long.class));
            plugin.saveConfig();
            ctx.getSource().getSender().sendMessage("Set the duration for which the gates are still open after a player walks through.");
            return 1;
        })
    );

private final LiteralArgumentBuilder<CommandSourceStack> defaultFareClass = Commands.literal("defaultfareclass")
    .then(Commands.argument("fareclass", StringArgumentType.string())
        .requires(source -> source.getSender().hasPermission("iciwi.config.defaultfareclass"))
        .executes(ctx -> {
            plugin.getConfig().set("default-fare-class", ctx.getArgument("fareclass", String.class));
            plugin.saveConfig();
            ctx.getSource().getSender().sendMessage("Set the default train fareClass.");
            return 1;
        })
    );

private final LiteralArgumentBuilder<CommandSourceStack> config = Commands.literal("config")
    .then(penalty)
    .then(deposit)
    .then(addPriceList)
    .then(removePriceList)
    .then(maxTransferTime)
    .then(gateCloseDelay)
    .then(closeAfterPass)
    .then(defaultFareClass);

// Owners commands

private final LiteralArgumentBuilder<CommandSourceStack> alias = Commands.literal("alias")
    .requires(source -> source.getSender().hasPermission("iciwi.owners.alias"))
    .then(Commands.literal("set")
        .then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
            .then(Commands.argument("username", StringArgumentType.string()).suggests((ctx, builder) -> {
                plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .forEach(builder::suggest);
                return builder.buildFuture();
            })
                .executes(ctx -> {
                    String company = ctx.getArgument("company", String.class);
                    String username = ctx.getArgument("username", String.class);
                    owners.set("Aliases." + company, username);
                    owners.save();
                    ctx.getSource().getSender().sendMessage(formatString("The revenue of %s will now be sent to %s.", company, username));
                    return 1;
                })
            )
        )
    )
    .then(Commands.literal("unset")
        .then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
            .executes(ctx -> {
                String company = ctx.getArgument("company", String.class);
                owners.set("Aliases." + company, null);
                owners.save();
                ctx.getSource().getSender().sendMessage(formatString("The revenue of %s will no longer be sent to anyone.", company));
                return 1;
            })
        )
    );

private final LiteralArgumentBuilder<CommandSourceStack> operator = Commands.literal("operator")
    .requires(source -> source.getSender().hasPermission("iciwi.owners.operator"))
    .then(Commands.argument("station", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .then(Commands.literal("list")
            .executes(ctx -> {
                String station = ctx.getArgument("station", String.class);
                List<String> o = owners.getOwners(station);
                for (String s : o) ctx.getSource().getSender().sendMessage(formatString(s));
                return 1;
            })
        )
        .then(Commands.literal("add")
            .then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
                .executes(ctx -> {
                    String station = ctx.getArgument("station", String.class);
                    String company = ctx.getArgument("company", String.class);
                    owners.addOwner(station, company);
                    owners.save();
                    ctx.getSource().getSender().sendMessage(formatString("%s now operates %s.", company, station));
                    return 1;
                })
            )
        )
        .then(Commands.literal("remove")
            .then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
                .executes(ctx -> {
                    String station = ctx.getArgument("station", String.class);
                    String company = ctx.getArgument("company", String.class);
                    owners.removeOwner(station, company);
                    owners.save();
                    ctx.getSource().getSender().sendMessage(formatString("%s no longer operates %s.", company, station));
                    return 1;
                })
            )
        )
        .then(Commands.literal("set")
            .then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
                .executes(ctx -> {
                    String station = ctx.getArgument("station", String.class);
                    String company = ctx.getArgument("company", String.class);
                    owners.setOwners(station, Collections.singletonList(company));
                    owners.save();
                    ctx.getSource().getSender().sendMessage(formatString("%s is now the sole operator of %s.", company, station));
                    return 1;
                })
            )
        )
        .then(Commands.literal("delete")
            .executes(ctx -> {
                String station = ctx.getArgument("station", String.class);
                owners.set("Operators." + station, null);
                owners.save();
                ctx.getSource().getSender().sendMessage(formatString("No company is now operating %s.", station));
                return 1;
            })
        )
    );

private final LiteralArgumentBuilder<CommandSourceStack> editRailPass = Commands.literal("edit").then(Commands.argument("name", StringArgumentType.string())
    .then(Commands.literal("operator").then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
        .executes(ctx -> {
            String name = ctx.getArgument("name", String.class);
            String company = ctx.getArgument("company", String.class);
            owners.set("RailPasses." + name + ".operator", company);
            owners.save();
            ctx.getSource().getSender().sendMessage(formatString("The railpass %s is now owned by %s", name, company));
            return 1;
        })
    ))
    .then(Commands.literal("duration").then(Commands.argument("duration", TimeArgumentType.time())
        .executes(ctx -> {
            String name = ctx.getArgument("name", String.class);
            Long duration = ctx.getArgument("duration", Long.class);
            owners.set("RailPasses." + name + ".duration", duration);
            owners.save();
            ctx.getSource().getSender().sendMessage(formatString("The duration of railpass %s is now %s", name, IciwiUtil.getTimeString(duration)));
            return 1;
        })
    ))
    .then(Commands.literal("price").then(Commands.argument("price", DoubleArgumentType.doubleArg(0d))
        .executes(ctx -> {
            String name = ctx.getArgument("name", String.class);
            Double price = ctx.getArgument("price", Double.class);
            owners.set("RailPasses." + name + ".price", price);
            owners.save();
            ctx.getSource().getSender().sendMessage(formatString("The price of railpass %s is now %s", name, String.valueOf(price)));
            return 1;
        })
    ))
    .then(Commands.literal("percentage").then(Commands.argument("percentage", DoubleArgumentType.doubleArg(0d, 1d))
        .executes(ctx -> {
            String name = ctx.getArgument("name", String.class);
            Double pp = ctx.getArgument("percentage", Double.class);
            owners.set("RailPasses." + name + ".percentage", pp);
            owners.save();
            ctx.getSource().getSender().sendMessage(formatString("The payment percentage of railpass %s is now %s", name, String.valueOf(pp)));
            return 1;
        })
    ))
);

private final LiteralArgumentBuilder<CommandSourceStack> railpass = Commands.literal("railpass")
    .requires(source -> source.getSender().hasPermission("iciwi.owners.railpass"))
    .then(Commands.literal("list")
        .executes(ctx -> {
            var o = owners.getAllRailPasses();
            for (String s : o) ctx.getSource().getSender().sendMessage(formatString(s));
            return 1;
        })
    )
    .then(Commands.literal("set")
        .then(Commands.argument("name", StringArgumentType.string())
            .then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
                .then(Commands.argument("duration", TimeArgumentType.time())
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0d))
                        .then(Commands.argument("paidpercentage", DoubleArgumentType.doubleArg(0d, 1d))
                            .executes(ctx -> {
                                String name = ctx.getArgument("name", String.class);
                                String company = ctx.getArgument("company", String.class);
                                Long duration = ctx.getArgument("duration", Long.class);
                                Double price = ctx.getArgument("price", Double.class);
                                Double pp = ctx.getArgument("paidpercentage", Double.class);
                                owners.setRailPassInfo(name, company, duration, price, pp);
                                owners.save();
                                ctx.getSource().getSender().sendMessage(formatString("New rail pass created!"));
                                return 1;
                            })
                        )
                    )
                )
            )
        )
    )
    .then(Commands.literal("delete")
        .then(Commands.argument("name", StringArgumentType.string()).suggests(IciwiCommands::suggestRailpass)
            .executes(ctx -> {
                String name = ctx.getArgument("name", String.class);
                owners.set("RailPasses." + name, null);
                owners.save();
                ctx.getSource().getSender().sendMessage(formatString("Railpass %s has been deleted", name));
                return 1;
            })
        )
    )
    .then(editRailPass);

private final LiteralArgumentBuilder<CommandSourceStack> flatticket = Commands.literal("flatticket")
    .requires(source -> source.getSender().hasPermission("iciwi.owners.flatticket"))
    .then(Commands.literal("set")
        .then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
            .then(Commands.argument("price", DoubleArgumentType.doubleArg(0d))
                .executes(ctx -> {
                    String company = ctx.getArgument("company", String.class);
                    Double price = ctx.getArgument("price", Double.class);
                    owners.setOperatorTicket(company, price);
                    owners.save();
                    ctx.getSource().getSender().sendMessage(formatString("Single journey tickets for %s has been set to %s.", company, String.valueOf(price)));
                    return 1;
                })
            )
        )
    )
    .then(Commands.literal("remove")
        .then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
            .executes(ctx -> {
                String company = ctx.getArgument("company", String.class);
                owners.setOperatorTicket(company, 0d);
                owners.save();
                ctx.getSource().getSender().sendMessage(formatString("Single journey tickets for %s has been removed.", company));
                return 1;
            })
        )
    );

private final LiteralArgumentBuilder<CommandSourceStack> farecap = Commands.literal("farecap")
    .requires(source -> source.getSender().hasPermission(("iciwi.owners.farecap")))
    .then(Commands.literal("set")
        .then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0d))
                .then(Commands.argument("duration", TimeArgumentType.time())
                    .executes(ctx -> {
                        String company = ctx.getArgument("company", String.class);
                        Double amount = ctx.getArgument("amount", Double.class);
                        Long duration = ctx.getArgument("duration", Long.class);
                        owners.setFareCapAmt(company, amount);
                        owners.setFareCapDuration(company, duration);
                        owners.save();
                        ctx.getSource().getSender().sendMessage(formatString("The fare cap for %s has been set to %s, valid for %s.", company, String.valueOf(amount), String.valueOf(duration)));
                        return 1;
                    })
                )
            )
        )
    )
    .then(Commands.literal("remove")
        .then(Commands.argument("company", StringArgumentType.string()).suggests(IciwiCommands::suggestCompany)
            .executes(ctx -> {
                String company = ctx.getArgument("company", String.class);
                owners.setFareCapAmt(company, 0d);
                owners.setFareCapDuration(company, 0L);
                owners.save();
                ctx.getSource().getSender().sendMessage(formatString("The fare cap for %s has been removed.", company));
                return 1;
            })
        )
    );


private final LiteralArgumentBuilder<CommandSourceStack> owner = Commands.literal("owners")
    .then(alias)
    .then(operator)
    .then(flatticket)
    .then(railpass)
    .then(farecap);

private final LiteralArgumentBuilder<CommandSourceStack> faresSet = Commands.literal("set")
    .requires(source -> source.getSender().hasPermission("iciwi.fares.set"))
    .then(Commands.argument("start", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .then(Commands.argument("end", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
            .then(Commands.argument("fareclass", StringArgumentType.string()).suggests(IciwiCommands::suggestClass)
                .then(Commands.argument("price", DoubleArgumentType.doubleArg())
                    .executes(ctx -> {
                        String start = ctx.getArgument("start", String.class);
                        String end = ctx.getArgument("end", String.class);
                        String fareclass = ctx.getArgument("fareclass", String.class);
                        Double price = ctx.getArgument("price", Double.class);
                        // Run getOwners to register station owners
                        owners.getOwners(start);
                        fares.setFare(start, end, fareclass, price);
                        ctx.getSource().getSender().sendMessage(formatString("A new fare from %s to %s using the class %s has been set to: %s", start, end, fareclass, String.valueOf(price)));
                        return 1;
                    })))));

private final LiteralArgumentBuilder<CommandSourceStack> faresCheck = Commands.literal("check")
    .requires(source -> source.getSender().hasPermission("iciwi.fares.set"))
    .then(Commands.argument("start", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .then(Commands.argument("end", StringArgumentType.string()).suggests(this::suggestEnd)
            .then(Commands.argument("fareclass", StringArgumentType.string()).suggests(this::suggestTravelClass)
                .executes(ctx -> {
                    String start = ctx.getArgument("start", String.class);
                    String end = ctx.getArgument("end", String.class);
                    String fareclass = ctx.getArgument("fareclass", String.class);
                    Set<String> s;
                    if (end == null) s = fares.getDestinations(start);
                    else if (fareclass == null) s = fares.getClasses(start, end);
                    else s = Collections.singleton(String.valueOf(fares.getFare(start, end, fareclass)));
                    s.forEach(ctx.getSource().getSender()::sendMessage);
                    return 1;
                }))));

private final LiteralArgumentBuilder<CommandSourceStack> faresCheckClass = Commands.literal("checkclass")
    .requires(source -> source.getSender().hasPermission("iciwi.fares.checkclass"))
    .then(Commands.argument("start", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .then(Commands.argument("fareclass", StringArgumentType.string()).suggests(IciwiCommands::suggestClass)
            .executes(ctx -> {
                String start = ctx.getArgument("start", String.class);
                String fareClass = ctx.getArgument("fareClass", String.class);
                var s = fares.getFares(start, fareClass);
                s.forEach((a, b) -> ctx.getSource().getSender().sendMessage(a, String.valueOf(b)));
                return 1;
            })));

private final LiteralArgumentBuilder<CommandSourceStack> faresUnset = Commands.literal("unset")
    .requires(source -> source.getSender().hasPermission("iciwi.fares.unset"))
    .then(Commands.argument("start", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .then(Commands.argument("end", StringArgumentType.string()).suggests(this::suggestEnd)
            .then(Commands.argument("fareclass", StringArgumentType.string()).suggests(this::suggestTravelClass)
                .executes(ctx -> {
                    String start = ctx.getArgument("start", String.class);
                    String end = ctx.getArgument("end", String.class);
                    String fareClass = ctx.getArgument("fareclass", String.class);
                    fares.unsetFare(start, end, fareClass);
                    ctx.getSource().getSender().sendMessage(formatString("The fare from %s to %s using the class %s has been deleted.", start, end, fareClass));
                    return 1;
                }))));

private final LiteralArgumentBuilder<CommandSourceStack> faresDelJourney = Commands.literal("deletejourney")
    .requires(source -> source.getSender().hasPermission("iciwi.fares.deletejourney"))
    .then(Commands.argument("start", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .then(Commands.argument("end", StringArgumentType.string()).suggests(this::suggestEnd)
            .executes(ctx -> {
                String start = ctx.getArgument("start", String.class);
                String end = ctx.getArgument("end", String.class);
                fares.deleteJourney(start, end);
                ctx.getSource().getSender().sendMessage(formatString("All fares from %s to %s has been deleted.", start, end));
                return 1;
            })));

private final LiteralArgumentBuilder<CommandSourceStack> faresDelStation = Commands.literal("deletestation")
    .requires(source -> source.getSender().hasPermission("iciwi.fares.deletestation"))
    .then(Commands.argument("start", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .executes(ctx -> {
            String start = ctx.getArgument("start", String.class);
            fares.deleteStation(start);
            ctx.getSource().getSender().sendMessage(formatString("All fares to all stations from %s has been deleted.", start));
            return 1;
        }));

private final LiteralArgumentBuilder<CommandSourceStack> fare = Commands.literal("fares")
    .then(faresSet)
    .then(faresCheck)
    .then(faresCheckClass)
    .then(faresUnset)
    .then(faresDelJourney)
    .then(faresDelStation);

private final LiteralArgumentBuilder<CommandSourceStack> machineTicket = Commands.literal("ticket")
    .requires(source -> source.getSender().hasPermission("iciwi.machine.ticket"))
    .then(Commands.argument("station", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .executes(ctx -> {
            if (!(ctx.getSource().getSender() instanceof Player player)) return 0;
            String station = ctx.getArgument("station", String.class);
            final TicketMachine machine = new TicketMachine(player);
            machine.init(station);
            return 1;
        }));

private final LiteralArgumentBuilder<CommandSourceStack> machineCard = Commands.literal("card")
    .requires(source -> source.getSender().hasPermission("iciwi.machine.card"))
    .then(Commands.argument("station", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .executes(ctx -> {
            if (!(ctx.getSource().getSender() instanceof Player player)) return 0;
            String station = ctx.getArgument("station", String.class);
            final CardMachine machine = new CardMachine(player);
            machine.init(station);
            return 1;
        }));

private final LiteralArgumentBuilder<CommandSourceStack> machineCustom = Commands.literal("custom")
    .requires(source -> source.getSender().hasPermission("iciwi.machine.custom"))
    .then(Commands.argument("station", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .executes(ctx -> {
            if (!(ctx.getSource().getSender() instanceof Player player)) return 0;
            String station = ctx.getArgument("station", String.class);
            final CustomMachine machine = new CustomMachine(player, station);
            return 1;
        }));

private final LiteralArgumentBuilder<CommandSourceStack> machineRailPass = Commands.literal("railpass")
    .requires(source -> source.getSender().hasPermission("iciwi.machine.railpass"))
    .then(Commands.argument("station", StringArgumentType.string()).suggests(IciwiCommands::suggestStation)
        .executes(ctx -> {
            if (!(ctx.getSource().getSender() instanceof Player player)) return 0;
            String station = ctx.getArgument("station", String.class);
            final RailPassMachine machine = new RailPassMachine(player);
            machine.init(station);
            return 1;
        }));

private final LiteralArgumentBuilder<CommandSourceStack> machine = Commands.literal("machine")
    .then(machineTicket)
    .then(machineCard)
    .then(machineCustom)
    .then(machineRailPass);

// Final build
public final LiteralCommandNode<CommandSourceStack> main = Commands.literal("iciwi")
    .then(reload)
    .then(config)
    .then(owner)
    .then(alias)
    .then(operator)
    .then(flatticket)
    .then(railpass)
    .then(farecap)
    .then(machine)
    .then(fare)
    .build();
}
