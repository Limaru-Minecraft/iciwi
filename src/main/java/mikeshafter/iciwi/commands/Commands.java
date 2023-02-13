package mikeshafter.iciwi.commands;

import mikeshafter.iciwi.Iciwi;
import static mikeshafter.iciwi.util.ArgumentNode.*;
import mikeshafter.iciwi.util.ArgumentNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Commands implements TabExecutor {
  @Override public boolean onCommand (@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    return commandStructure.onCommand(sender, args);
  }

  @Override public @Nullable List<String> onTabComplete (@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    return commandStructure.onTabComplete(sender, args);
  }

  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);

  private final ArgumentNode commandStructure = of("iciwi")
    .then(of("config")
      .then(of("reload")
        .executes((c, a, t) -> plugin.reloadAllConfig()))
      .then(of("penalty")
        .then(of("amount", Float.class)
          .executes((c, a, n) -> {
            plugin.getConfig().set("penalty", n.getFloat(a, "amount"));
            plugin.saveConfig();
            return true;
          })))
      .then(of("deposit")
        .then(of("amount", Float.class)
          .executes((c, a, n) -> {
            plugin.getConfig().set("deposit", n.getFloat(a, "amount"));
            plugin.saveConfig();
            return true;
          })))
      .then(of("initialcardvalues")
        .then(of("add")
          .then(of("amount", Float.class)
            .executes((c, a, n) -> {
              List<Float> priceArray = plugin.getConfig().getFloatList("price-array");
              priceArray.add(Float.parseFloat(a[3]));
              plugin.getConfig().set("price-array", priceArray);
              plugin.saveConfig();
              return true;
            })))
        .then(of("remove")
          .then(of("amount", Float.class)
            .executes((c, a, n) -> {
              List<Float> priceArray = plugin.getConfig().getFloatList("price-array");
              priceArray.remove(Float.parseFloat(a[3]));
              plugin.getConfig().set("price-array", priceArray);
              plugin.saveConfig();
              return true;
            }))))
      .then(of("maxtransfertime")
        .then(of("amount", Long.class)
          .executes((c, a, n) -> {
            plugin.getConfig().set("max-transfer-time", Long.parseLong(a[2]));
            plugin.saveConfig();
            return true;
          })))
      .then(of("gateclosedelay")
        .then(of("closedelay", Integer.class)
          .executes((c, a, n) -> {
            plugin.getConfig().set("gate-close-delay", n.getInteger(a, "closedelay"));
            plugin.saveConfig();
            return true;
          })))
      .then(of("defaultclass")
        .then(of("classname", String.class)
          .executes((c, a, n) -> {
            plugin.getConfig().set("default-class", n.getString(a, "classname"));
            plugin.saveConfig();
            return true;
          }))))

    .then(of("owners")
      .then(of("alias")
        .then(of("set")
          .then(of("company", String.class)
            .then(of("username", String.class)
              .executes((c, a, n) -> {
                plugin.owners.set("Aliases."+n.getString(a, "company"), n.getString(a,"username"));
                plugin.owners.save();
                return true;
              }))))
        .then(of("unset")
          .then(of("company", String.class)
            .executes((c, a, n) -> {
              plugin.owners.set("Aliases."+n.getString(a, "company"), null);
              plugin.owners.save();
              return true;
            }))))
      .then(of("operator")
        .then(of("station", String.class)
          .then(of("add")
            .then(of("company", String.class)
              .executes((c, a, n) -> {
                plugin.owners.addOwner(n.getString(a, "station"), n.getString(a, "company"));
                plugin.owners.save();
                return true;
              })))
          .then(of("remove")
            .then(of("company", String.class)
              .executes((c, a, n) -> {
                plugin.owners.removeOwner(n.getString(a, "station"), n.getString(a, "company"));
                plugin.owners.save();
                return true;
              })))
          .then(of("set")
            .then(of("company", String.class)
              .executes((c, a, n) -> {
                plugin.owners.setOwners(n.getString(a, "station"), Collections.singletonList(n.getString(a, "company")));
                plugin.owners.save();
                return true;
              })))
          .then(of("delete")
            .executes((c, a, n) -> {
              plugin.owners.set("Operators."+n.getString(a, "station"), null);
              plugin.owners.save();
              return true;
            }))))
      .then(of("railpass")
        .then(of("name", String.class)
          .then(of("operator")
            .then(of("company", String.class)
              .executes((c, a, n) -> {
                plugin.owners.set("RailPasses."+n.getString(a,"name")+".operator", n.getString(a, "company"));
                plugin.owners.save();
                return true;
              })))
          .then(of("duration")
            .then(of("duration", Long.class)
              .executes((c, a, n) -> {
                plugin.owners.set("RailPasses."+n.getString(a,"name")+".duration", n.getLong(a, "duration"));
                plugin.owners.save();
                return true;
              })))
          .then(of("price")
            .then(of("price", Float.class)
              .executes((c, a, n) -> {
                plugin.owners.set("RailPasses."+n.getString(a,"name")+".price", n.getFloat(a, "price"));
                plugin.owners.save();
                return true;
              })))
          .then(of("percentage")
            .then(of("paidpercentage", Float.class)
              .executes((c, a, n) -> {
                plugin.owners.set("RailPasses."+n.getString(a,"name")+".percentage", n.getFloat(a, "paidpercentage"));
                plugin.owners.save();
                return true;
              })))
          .then(of("delete")
            .executes((c, a, n) -> {
              plugin.owners.set("RailPasses."+n.getString(a,"name"), null);
              plugin.owners.save();
              return true;
            })))))

    .then(of("fares")
      .then(of("set")
        .then(of("start", String.class)
          .then(of("end", String.class)
            .then(of("class", String.class)
              .then(of("price", Float.class).executes((c, a, n) -> {
                plugin.fares.setFare(n.getString(a, "start"), n.getString(a, "end"), n.getString(a, "class"), n.getFloat(a, "price"));
                return true;
              }))))))
      .then(of("unset")
        .then(of("start", String.class)
          .then(of("end", String.class)
            .then(of("class", String.class).executes((c, a, n) -> {
              plugin.fares.set(n.getString(a, "start")+"."+n.getString(a, "end")+"."+n.getString(a, "class"), null);
              plugin.fares.save();
              return true;
            })))))
      .then(of("deletejourney")
        .then(of("start", String.class)
          .then(of("end", String.class).executes((c, a, n) -> {
            plugin.fares.set(n.getString(a, "start")+"."+n.getString(a, "end"), null);
            plugin.fares.save();
            return true;
          }))))
      .then(of("deletestation")
        .then(of("start", String.class).executes((c, a, n) -> {
          plugin.fares.set(n.getString(a, "start"), null);
          plugin.fares.save();
          return true;
        }))))

    .then(of("coffers")
      .then(of("withdraw")
        .then(of("company", String.class)
          .executes((c, a, n) -> {
            return true;
          })))
      .then(of("withdrawall")
        .executes((c, a, n) -> {
          return true;
        }))
      .then(of("view")
        .executes((c, a, n) -> {
          return true;
        })))

    .then(of("odometer")
      .then(of("start-lap")
        .executes((c, a, n) -> {
          return true;
        }))
      .then(of("stop-reset")
        .executes((c, a, n) -> {
          return true;
        })))
//    .build()
    ;

//  public static void registerCompletions(@NotNull Commodore commodore, PluginCommand pluginCommand) {
//
//    commodore.register(pluginCommand, commandStructure);
//  }
}
