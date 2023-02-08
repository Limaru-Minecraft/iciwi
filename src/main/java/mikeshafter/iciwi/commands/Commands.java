package mikeshafter.iciwi.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.lucko.commodore.Commodore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Commands implements TabExecutor {
  @Override public boolean onCommand (@NotNull CommandSender sender, @NotNull Command command, String alias, @NotNull String[] args) {
    return false;
  }

  @Override public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
      @NotNull String[] args) {
    return null;
  }

  public static void registerCompletions(Commodore commodore, PluginCommand command) {
    commodore.register(command, LiteralArgumentBuilder.literal("iciwi")
      .then(LiteralArgumentBuilder.literal("config")
        .then(LiteralArgumentBuilder.literal("reload"))
        .then(LiteralArgumentBuilder.literal("penalty") 
          .then(RequiredArgumentBuilder.argument("amount", FloatArgumentType.floatArg()) ) 
        )
        .then(LiteralArgumentBuilder.literal("deposit") 
          .then(RequiredArgumentBuilder.argument("amount", FloatArgumentType.floatArg()) ) 
        )
        .then(LiteralArgumentBuilder.literal("initialcardvalues")
          .then(LiteralArgumentBuilder.literal("add") 
            .then(RequiredArgumentBuilder.argument("amount", FloatArgumentType.floatArg()) )
          )
          .then(LiteralArgumentBuilder.literal("remove") 
            .then(RequiredArgumentBuilder.argument("amount", FloatArgumentType.floatArg()) )
          )
        )
        .then(LiteralArgumentBuilder.literal("maxtransfertime") .then(RequiredArgumentBuilder.argument("transfertime", LongArgumentType.longArg()) ) )
        .then(LiteralArgumentBuilder.literal("gateclosedelay") .then(RequiredArgumentBuilder.argument("closedelay", IntegerArgumentType.integer()) ) )
        .then(LiteralArgumentBuilder.literal("defaultclass") .then(RequiredArgumentBuilder.argument("classname", StringArgumentType.word()) ) )
      )

      .then(LiteralArgumentBuilder.literal("owners")
        .then(LiteralArgumentBuilder.literal("alias")
          .then(LiteralArgumentBuilder.literal("set")
            .then(RequiredArgumentBuilder.argument("company", StringArgumentType.word()) 
              .then(RequiredArgumentBuilder.argument("username", StringArgumentType.word()) )
            )
          )
          .then(LiteralArgumentBuilder.literal("unset")
            .then(RequiredArgumentBuilder.argument("company", StringArgumentType.word()) )
          )
        )
        .then(LiteralArgumentBuilder.literal("operator")
          .then(RequiredArgumentBuilder.argument("station", StringArgumentType.word())
            .then(LiteralArgumentBuilder.literal("add")
              .then(RequiredArgumentBuilder.argument("company", StringArgumentType.word()) )
            )
            .then(LiteralArgumentBuilder.literal("remove")
              .then(RequiredArgumentBuilder.argument("company", StringArgumentType.word()) )
            )
            .then(LiteralArgumentBuilder.literal("set")
              .then(RequiredArgumentBuilder.argument("company", StringArgumentType.word()) )
            )
            .then(LiteralArgumentBuilder.literal("delete")
            )
          )
        )
        .then(LiteralArgumentBuilder.literal("railpass")
          .then(RequiredArgumentBuilder.argument("name", StringArgumentType.word())
            .then(LiteralArgumentBuilder.literal("operator")
              .then(RequiredArgumentBuilder.argument("company", StringArgumentType.word()))
            )
            .then(LiteralArgumentBuilder.literal("duration")
              .then(RequiredArgumentBuilder.argument("duration", StringArgumentType.word()))
            )
            .then(LiteralArgumentBuilder.literal("price")
              .then(RequiredArgumentBuilder.argument("price", FloatArgumentType.floatArg()))
            )
            .then(LiteralArgumentBuilder.literal("percentage")
              .then(RequiredArgumentBuilder.argument("paidpercentage", FloatArgumentType.floatArg()))
            )
            .then(LiteralArgumentBuilder.literal("delete"))
          )
        )
      )

      .then(LiteralArgumentBuilder.literal("fares")
        .then(LiteralArgumentBuilder.literal("set")
          .then(RequiredArgumentBuilder.argument("start", StringArgumentType.word())
            .then(RequiredArgumentBuilder.argument("end", StringArgumentType.word())
              .then(RequiredArgumentBuilder.argument("class", StringArgumentType.word())
                .then(RequiredArgumentBuilder.argument("price", FloatArgumentType.floatArg()))            
              )
            )
          )
        )
        .then(LiteralArgumentBuilder.literal("unset")
          .then(RequiredArgumentBuilder.argument("start", StringArgumentType.word())
            .then(RequiredArgumentBuilder.argument("end", StringArgumentType.word())
              .then(RequiredArgumentBuilder.argument("class", StringArgumentType.word()))
            )
          )
        )
        .then(LiteralArgumentBuilder.literal("deletejourney")
          .then(RequiredArgumentBuilder.argument("start", StringArgumentType.word())
            .then(RequiredArgumentBuilder.argument("end", StringArgumentType.word()))
          )
        )
        .then(LiteralArgumentBuilder.literal("deletestation")
          .then(RequiredArgumentBuilder.argument("start", StringArgumentType.word()))
        )
      )

      .then(LiteralArgumentBuilder.literal("coffers")
        .then(LiteralArgumentBuilder.literal("withdraw")
          .then(RequiredArgumentBuilder.argument("company", StringArgumentType.word()))
        )
        .then(LiteralArgumentBuilder.literal("withdrawall"))
        .then(LiteralArgumentBuilder.literal("view"))
      )

      .then(LiteralArgumentBuilder.literal("odometer")
        .then(LiteralArgumentBuilder.literal("start-lap"))
        .then(LiteralArgumentBuilder.literal("stop-reset"))
      )

      .then(LiteralArgumentBuilder.literal("railpass")
        .then(RequiredArgumentBuilder.argument("name", StringArgumentType.word())
          .then(RequiredArgumentBuilder.argument("serial", StringArgumentType.word()))
        )
      )
    .build()
    );
  }
}
