package mikeshafter.iciwi.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.CloudExamplePlugin;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.text;


public class Commands {
  
  // Cloud Command Framework
  private BukkitCommandManager<CommandSender> manager;
  private BukkitAudiences bukkitAudiences;
  private MinecraftHelp<CommandSender> minecraftHelp;
  private CommandConfirmationManager<CommandSender> confirmationManager;
  private AnnotationParser<CommandSender> annotationParser;
  
  // Iciwi config
  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Lang lang = new Lang();
  private final Fares fares = new Fares();
  private final Owners owners = new Owners();
  private final CardSql cardSql = new CardSql();
  
  public Commands () {
    
    // This is a function that will provide a command execution coordinator that parses and executes commands asynchronously
    final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
        AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build();
  
    // This function maps the command sender type of our choice to the bukkit command sender.
    // However, in this example we use the Bukkit command sender, and so we just need to map it to itself
    final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
    try {
      this.manager = new PaperCommandManager<>(
          /* Owning plugin */ plugin,
          /* Coordinator function */ executionCoordinatorFunction,
          /* Command Sender -> C */ mapperFunction,
          /* C -> Command Sender */ mapperFunction
      );
    } catch (final Exception e) {
      plugin.getLogger().severe("Failed to initialize the commands class!");
      /* Disable the plugin */
      plugin.getServer().getPluginManager().disablePlugin(plugin);
      return;
    }
    
    // Create a BukkitAudiences instance (adventure) in order to use the minecraft-extras help system
    this.bukkitAudiences = BukkitAudiences.create(plugin);
    
    // Create the Minecraft help menu system
    this.minecraftHelp = new MinecraftHelp<>(
        /* Help Prefix */ "/iciwi help",
        /* Audience mapper */ this.bukkitAudiences::sender,
        /* Manager */ this.manager
    );
    
    // Register Brigadier mappings
    if (this.manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
      this.manager.registerBrigadier();
    }
    
    // Register asynchronous completions
    if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
      ((PaperCommandManager<CommandSender>) this.manager).registerAsynchronousCompletions();
    }

    // Create the confirmation this.manager. This allows us to require certain commands to be confirmed before they can be executed
    this.confirmationManager = new CommandConfirmationManager<>(
        /* Timeout */ 30L,
        /* Timeout unit */ TimeUnit.SECONDS,
        /* Action when confirmation is required */ context -> context.getCommandContext().getSender().sendMessage(
        ChatColor.RED + "Confirmation required. Confirm using /iciwi confirm."),
        /* Action when no confirmation is pending */ sender -> sender.sendMessage(
        ChatColor.RED + "You don't have any pending commands.")
    );
    
    // Register the confirmation processor. This will enable confirmations for commands that require it
    this.confirmationManager.registerConfirmationProcessor(this.manager);
    
    // Create the annotation parser. This allows you to define commands using methods annotated with @CommandMethod
    final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                                                                            CommandMeta.simple()
                                                                                // This will allow you to decorate commands with descriptions
                                                                                .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                                                                                .build();
    this.annotationParser = new AnnotationParser<>(
        /* Manager */ this.manager,
        /* Command sender type */ CommandSender.class,
        /* Mapper for command meta instances */ commandMetaFunction
    );
    
    // Override the default exception handlers
    new MinecraftExceptionHandler<CommandSender>()
        .withInvalidSyntaxHandler()
        .withInvalidSenderHandler()
        .withNoPermissionHandler()
        .withArgumentParsingHandler()
        .withCommandExecutionHandler()
        .withDecorator(
            component -> text()
                             .append(text("[", NamedTextColor.DARK_GRAY))
                             .append(text("Example", NamedTextColor.GOLD))
                             .append(text("] ", NamedTextColor.DARK_GRAY))
                             .append(component).build()
        ).apply(this.manager, this.bukkitAudiences::sender);

    // Create the commands
    this.constructCommands();
  }
  
  
  private void constructCommands () {
    // Add a custom permission checker
    // no permission checker needed for Iciwi
    
    // Parse all @CommandMethod-annotated methods
    this.annotationParser.parse(this);
    
    // Parse all @CommandContainer-annotated classes
    try {
      this.annotationParser.parseContainers();
    } catch (final Exception e) {
      e.printStackTrace();
    }
    
    // Base command builder
    final Command.Builder<CommandSender> builder = this.manager.commandBuilder("iciwi");
    
    // Add a confirmation command
    this.manager.command(builder.literal("confirm")
        .meta(CommandMeta.DESCRIPTION, "Confirm a pending command")
        .handler(this.confirmationManager.createConfirmationExecutionHandler())
    );
  }
  
  @Suggestions("station-list")
  public List<String> getAllStations (final CommandContext<CommandSender> context, final String input) {
    return new ArrayList<>(fares.getAllStations());
  }
  
//  @Suggestions("fare-class")
//  public List<String> getFareClasses (final CommandContext<CommandSender> context, final String input) {
//
//  }
  
  @CommandMethod("iciwi checkfare <start-station> <end-station> <fare-class>")
  @CommandDescription("Displays the fare between two stations")
  public void checkFare(
      final CommandSender sender,
      final @Argument(value="start-station", suggestions="station-list") String startStation,
      final @Argument(value="end-station", suggestions="station-list") String endStation,
      final @Argument(value="fare-class") String fareClass
      ) {
    sender.sendMessage(String.valueOf(fares.getFare(startStation, endStation, fareClass)));
  }

  @CommandMethod("iciwi railpass <name>")
  
}