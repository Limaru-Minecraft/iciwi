package mikeshafter.iciwi.commands;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.ArgumentNode;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static mikeshafter.iciwi.util.ArgumentNode.of;

@Deprecated
public class OldCommands implements TabExecutor {
  @Override public boolean onCommand (@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    return commandStructure.onCommand(sender, args);
  }
  @Override public @Nullable List<String> onTabComplete (@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    return commandStructure.onTabComplete(sender, args);
  }

  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Owners owners = plugin.owners;
  private final Fares fares = plugin.fares;
  private final HashMap<Player, Odometer> odometer = new HashMap<>();

  private List<String> toStringList (List<Float> list) {
    ArrayList<String> stringList = new ArrayList<String>(list.size());
    for (int i = 0; i < list.size(); i++) {
      stringList.set(i, String.valueOf(list.get(i)));
    }
    return stringList;
  }

  private final ArgumentNode commandStructure = of("iciwi")
    .then(of("config")
      .then(of("reload")
        .executes((c, a, t) -> {
          plugin.reloadAllConfig();
          c.sendMessage("Reloaded all config!");
          return true;
        })
        .description("Reloads all config files")
      )
      .then(of("penalty")
        .then(of("amount", Float.class)
          .executes((c, a, n) -> {
            plugin.getConfig().set("penalty", n.getFloat(a, "amount"));
            plugin.saveConfig();
            return true;
          })
         .description("Sets the penalty given to fare evaders")
          )
        )
      .then(of("deposit")
        .then(of("amount", Float.class)
          .executes((c, a, n) -> {
            plugin.getConfig().set("deposit", n.getFloat(a, "amount"));
            plugin.saveConfig();
            return true;
          })
          .description("Sets the deposit paid when buying a new card")
          )
        )
      .then(of("initialcardvalues")
        .then(of("add")
          .then(of("amount", Double.class)
            .executes((c, a, n) -> {
              List<Double> priceArray = plugin.getConfig().getDoubleList("price-array");
              priceArray.add(Double.parseDouble(a[3]));
              plugin.getConfig().set("price-array", priceArray);
              plugin.saveConfig();
              return true;
            })
            .description("Adds a new option to the choices of card values")
            )
          )
        .then(of("remove")
          .then(of("amount", Float.class)
            .suggestions((c, a, n) -> toStringList(plugin.getConfig().getFloatList("price-array")))
            .executes((c, a, n) -> {
              List<Float> priceArray = plugin.getConfig().getFloatList("price-array");
              priceArray.remove(Float.parseFloat(a[3]));
              plugin.getConfig().set("price-array", priceArray);
              plugin.saveConfig();
              return true;
            })
            .description("Removes an option to the choices of card values")
            )
          )
        )
      .then(of("maxtransfertime")
        .then(of("amount", Long.class)
          .executes((c, a, n) -> {
            plugin.getConfig().set("max-transfer-time", Long.parseLong(a[2]));
            plugin.saveConfig();
            return true;
          })
          .description("Sets the maximum time allowed for an out-of-station transfer to happen.")
          )
        )
      .then(of("gateclosedelay")
        .then(of("closedelay", Integer.class)
          .executes((c, a, n) -> {
            plugin.getConfig().set("gate-close-delay", n.getInteger(a, "closedelay"));
            plugin.saveConfig();
            return true;
          })
          .description("Sets the duration of open fare gates.")
          )
        )
      .then(of("defaultclass")
        .then(of("classname", String.class)
          .executes((c, a, n) -> {
            plugin.getConfig().set("default-class", n.getString(a, "classname"));
            plugin.saveConfig();
            return true;
          })
          .description("Sets the fare class used by default when card payment is used.")
          )
        )
      )

    .then(of("owners")
      .then(of("alias")
        .then(of("set")
          .suggestions(owners.getAllCompanies().stream().toList())
          .then(of("company", String.class)
            .then(of("username", String.class)
              .executes((c, a, n) -> {
                owners.set("Aliases."+n.getString(a, "company"), n.getString(a,"username"));
                owners.save();
                return true;
              })
              .description("Sets the revenue collector for a company.")
              )
            )
          )
        .then(of("unset")
          .suggestions((c, a, n) -> {
            if (c instanceof Player player)
              return owners.getOwnedCompanies(player.getName());
            else
              return owners.getAllCompanies().stream().toList();
          })
          .then(of("company", String.class)
            .executes((c, a, n) -> {
              owners.set("Aliases."+n.getString(a, "company"), null);
              owners.save();
              return true;
            })
            .description("Removes the revenue collector for a company.")
            )
          )
        )
      .then(of("operator")
        .suggestions((c, a, n) -> fares.getAllStations().stream().toList())
        .then(of("station", String.class)
          .then(of("add")
            .suggestions((c, a, n) -> {
              if (c instanceof Player player)
                return owners.getOwnedCompanies(player.getName());
              else
                return owners.getAllCompanies().stream().toList();
            })
            .then(of("company", String.class)
              .executes((c, a, n) -> {
                owners.addOwner(n.getString(a, "station"), n.getString(a, "company"));
                owners.save();
                return true;
              })
              .description("Adds an owning company to a station.")
              )
            )
          .then(of("remove")
            .suggestions((c, a, n) -> {
              if (c instanceof Player player)
                return owners.getOwnedCompanies(player.getName());
              else
                return owners.getAllCompanies().stream().toList();
            })
            .then(of("company", String.class)
              .executes((c, a, n) -> {
                owners.removeOwner(n.getString(a, "station"), n.getString(a, "company"));
                owners.save();
                return true;
              })
              .description("Removes an owning company from a station.")
              )
            )
          .then(of("set")
            .suggestions((c, a, n) -> {
              if (c instanceof Player p)
                return owners.getOwnedCompanies(p.getName());
              else
                return owners.getAllCompanies().stream().toList();
            })
            .then(of("company", String.class)
              .executes((c, a, n) -> {
                owners.setOwners(n.getString(a, "station"), Collections.singletonList(n.getString(a, "company")));
                owners.save();
                return true;
              })
              .description("Sets the owning company of a station.")
              )
            )
          .then(of("delete")
            .executes((c, a, n) -> {
              owners.set("Operators."+n.getString(a, "station"), null);
              owners.save();
              return true;
            })
            .description("Removes all owning companies of a station.")
            )
          )
        )
      .then(of("railpass")
        .suggestions(owners.getAllRailPasses().stream().toList())
        .then(of("name", String.class)
          .then(of("operator")
            .then(of("company", String.class)
              .suggestions(owners.getAllCompanies().stream().toList())
              .executes((c, a, n) -> {
                owners.set("RailPasses."+n.getString(a,"name")+".operator", n.getString(a, "company"));
                owners.save();
                return true;
              })
              .description("Sets the rail company that owns the given railpass.")
              )
            )
          .then(of("duration")
            .then(of("duration", Long.class)
              .executes((c, a, n) -> {
                owners.set("RailPasses."+n.getString(a,"name")+".duration", n.getLong(a, "duration"));
                owners.save();
                return true;
              })
              .description("Sets the duration that the given railpass is active.")
              )
            )
          .then(of("price")
            .then(of("price", Float.class)
              .executes((c, a, n) -> {
                owners.set("RailPasses."+n.getString(a,"name")+".price", n.getFloat(a, "price"));
                owners.save();
                return true;
              })
              .description("Sets the price of the given railpass.")    
              )
            )
          .then(of("percentage")
            .then(of("paidpercentage", Float.class)
              .executes((c, a, n) -> {
                owners.set("RailPasses."+n.getString(a,"name")+".percentage", n.getFloat(a, "paidpercentage"));
                owners.save();
                return true;
              })
              .description("Sets the percentage paid by the card holder when they use the railpass.")
              )
            )
          .then(of("delete")
            .executes((c, a, n) -> {
              owners.set("RailPasses."+n.getString(a,"name"), null);
              owners.save();
              return true;
            })
            .description("Deletes a railpass.")
            )
          )
        )
      )

    .then(of("fares")
      .then(of("set")
      .suggestions(fares.getAllStations().stream().toList())
        .then(of("start", String.class)
        .suggestions(fares.getAllStations().stream().toList())
          .then(of("end", String.class)
            .then(of("class", String.class)
              .then(of("price", Float.class)
                .executes((c, a, n) -> {
                  fares.setFare(n.getString(a, "start"), n.getString(a, "end"), n.getString(a, "class"), n.getFloat(a, "price"));
                  return true;
                })
                .description("Creates a new fare.")
                )
              )
            )
          )
        )
      .then(of("unset")
      .suggestions(fares.getAllStations().stream().toList())
        .then(of("start", String.class)
        .suggestions(fares.getAllStations().stream().toList())
          .then(of("end", String.class)
            .then(of("class", String.class)
              .executes((c, a, n) -> {
                fares.set(n.getString(a, "start")+"."+n.getString(a, "end")+"."+n.getString(a, "class"), null);
                fares.save();
                return true;
              })
              .description("Deletes a fare.")
              )
            )
          )
        )
      .then(of("deletejourney")
      .suggestions(fares.getAllStations().stream().toList())
        .then(of("start", String.class)
        .suggestions(fares.getAllStations().stream().toList())
          .then(of("end", String.class)
            .executes((c, a, n) -> {
              fares.set(n.getString(a, "start")+"."+n.getString(a, "end"), null);
              fares.save();
              return true;
            })
            .description("Deletes all fares between a start and end point.")
            )
          )
        )
      .then(of("deletestation")
      .suggestions(fares.getAllStations().stream().toList())
        .then(of("start", String.class)
          .executes((c, a, n) -> {
            fares.set(n.getString(a, "start"), null);
            fares.save();
            return true;
          })
          .description("Removes a station and all its associated fares from the data.")
          )
        )
      )

    .then(of("coffers")
      .then(of("withdraw")
        .suggestions((c, a, n) -> {
          if (c instanceof Player player)
            return owners.getOwnedCompanies(player.getName());
          else
            return owners.getAllCompanies().stream().toList();
        })
        .then(of("company", String.class)
          .executes((c, a, n) -> {
            String company = n.getString(a, "company");
            if (c instanceof Player player && owners.getOwnership(player.getName(), company)) {
              Iciwi.economy.depositPlayer(player, owners.getCoffers(company));
              owners.setCoffers(company, 0d);
            }
            return true;
          })
          .description("Withdraws the coffers of a single company that you own.")    
          )
        )
      .then(of("withdrawall")
        .executes((c, a, n) -> {
          if (c instanceof Player player) {
            List<String> ownedCompanies = owners.getOwnedCompanies(player.getName());
            for (String company : ownedCompanies) {
              Iciwi.economy.depositPlayer(player, owners.getCoffers(company));
              owners.setCoffers(company, 0d);
            }
          }
          return true;
        })
        .description("Withdraws all company costs that you own.")    
        )
      .then(of("view")
        .executes((c, a, n) -> {
          if (c instanceof Player p){
          p.sendMessage("=== COFFERS OF YOUR COMPANIES ===");
            List<String> ownedCompanies = owners.getOwnedCompanies(p.getName());
            for (String company : ownedCompanies) {
              p.sendMessage(ChatColor.GREEN+company+" : "+ChatColor.YELLOW+owners.getCoffers(company));
            }
          }
          return true;
        })
        .description("View how much each company earned.")
        )
      )

    // Structure of the list: [1 bit recording status and 31 bit previous record, distances...]
    .then(of("odometer")
      .then(of("start-lap")
        .executes((c, a, n) -> {
          if (c instanceof Player player) {
            int distance = player.getStatistic(Statistic.MINECART_ONE_CM);
            if (odometer.containsKey(player)) {
              Odometer playerMeter = odometer.get(player);
              if (playerMeter.recording) {
                // lap
                int displacement = distance - playerMeter.lastRecord + playerMeter.recorded;
                playerMeter.recorded = 0;
                playerMeter.distances.add(displacement);
                playerMeter.lastRecord = distance;
                player.sendMessage("Lap: " + displacement);
              } else {
                // start
                playerMeter.lastRecord = distance;
                playerMeter.recording = true;
                player.sendMessage("Started recording!");
              }
            } else {
              odometer.put(player, new Odometer(new ArrayList<>(), distance, true));
              player.sendMessage("Started recording!");
            }

          }
          return true;
        })
        .description("Starts or laps the train odometer.")
        )
      .then(of("stop-reset")
        .executes((c, a, n) -> {
          if (c instanceof Player player) {
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
          return true;
        })
        .description("Stops or reset the train odometer.")
        )
      );
}
