
package mikeshafter.iciwi.tickets;

import com.bergerkiller.bukkit.common.utils.CommonUtil;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.Clickable;
import mikeshafter.iciwi.util.InputDialogSubmitText;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import static mikeshafter.iciwi.util.MachineUtil.*;


public class CustomMachine implements Machine {

  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Player player;
  private final String station;
  private final Lang lang = new Lang(plugin);
  private final Fares fares = new Fares(plugin);
  private final Owners owners = new Owners(plugin);
  private ItemStack[] playerInv;
  private Clickable[] clickables;
  private Component terminal;
  private final Set<String> stationList = fares.getAllStations();

  public CustomMachine(Player player, String station) {
    this.player = player;
    this.station = station;
    Listener listener = new EventListener();
    var submitText = new InputDialogSubmitText(plugin, player) {
    
      @Override
      public void onTextChanged() {
      
        // Clear the player's upper inventory
        for (int i = 3; i < 30; i++) {
          player.getInventory().setItem(i, null);
        }
      
        // Get the string keyed in by the player
        String text = super.getText();
  
        // Sort stations based on relevance
        String[] stations = relevanceSort(text, stationList.toArray(String[]::new));
  
        // Place each station the player's inventory
        if (stations != null) {
          for (int i = 9; i < 36; i++) {
            player.getInventory().setItem(i, makeItem(Material.GLOBE_BANNER_PATTERN, 0, Component.text(stations[i-9])));
          }
        }
      }
    
      @Override
      public void onOpen() {
        super.onOpen();
        this.setDescription(lang.getString("enter-text-description"));
      
        // Save player's inventory
        playerInv = player.getInventory().getContents();
      }
    
      @Override
      public void onAccept(String text) {
        onClose();
      }
    
      @Override
      public void onCancel() {
        onClose();
      }
    
      @Override
      public void onClose() {
        for (int i = 0; i < playerInv.length; i++)
          player.getInventory().setItem(i, playerInv[i]);
      }
    };
  
    // Start listening
    Bukkit.getPluginManager().registerEvents(listener, plugin);
  
    // Open anvil on next tick due to problems with same-tick opening
    CommonUtil.nextTick(submitText::open);
  }
  
  public void selectClass() {
    // End station as a String
    String end = parseComponent(terminal);
    // Create inventory and create clickables
    TreeMap<String, Double> fareClasses = fares.getFaresFromDestinations(station, end);
    int invSize = roundUp(fareClasses.size(), 9);
    Inventory inventory = plugin.getServer().createInventory(null, invSize, Component.text(lang.getString("select-class")));
    this.clickables = new Clickable[invSize];

    var fareIterator = fareClasses.entrySet().iterator();

    for (int i = 0; i < fareClasses.size() && i < 54 && fareIterator.hasNext(); i++) {
      var entry = fareIterator.next();
      var item = makeItem(Material.PAPER, 0, Component.text(entry.getKey()), Component.text(entry.getValue()));
      this.clickables[i] = Clickable.of(item, (event) -> {
        ItemStack ticket = generateTicket(station, end, parseComponent(item.getItemMeta().displayName()));
        if (ticket != null) player.getInventory().addItem(generateTicket(station, end, parseComponent(item.getItemMeta().displayName()) ));
        event.setCancelled(true);
        player.closeInventory();
      });
    }
    setItems(this.clickables, inventory);
    player.openInventory(inventory);
  }

  protected ItemStack generateTicket(String from, String to, String fareClass) {
    // Find the price
    double price = fares.getFare(from, to, fareClass);

    // Let the player pay for the ticket
    if (Iciwi.economy.getBalance(this.player) >= price) {
      Iciwi.economy.withdrawPlayer(this.player, price);

      // find owners of the current station and deposit accordingly
      List<String> ownersList = owners.getOwners(from);
      for (String owner : ownersList)
        owners.deposit(owner, price / 2 / ownersList.size());

      // find owners of the station the ticket goes to and deposit accordingly
      ownersList = owners.getOwners(to);
      ownerCount = ownersList.size();
      for (String owner : ownersList)
        owners.deposit(owner, price / 2 / ownersList.size());
      
      // Get ticket materials
      Material ticketMaterial = Material.valueOf(plugin.getConfig().getString("ticket.material"));
      int customModelData = plugin.getConfig().getInt("ticket.custom-model-data");
      
      // Generate ticket
      return makeItem(ticketMaterial, customModelData, lang.getComponent("train-ticket"), Component.text(station), terminal, fareClass);
    }

    else {
      // Not enough money
      player.sendMessage(lang.getString("not-enough-money"));
      return null;
    }
  }
  