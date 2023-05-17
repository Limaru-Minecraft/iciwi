
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
import java.util.function.Function;

import static mikeshafter.iciwi.util.MachineUtil.*;

public class CustomMachine implements Machine {

  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Player player;
  private final String station;
  private final Lang lang = new Lang(plugin);
  private final Fares fares = new Fares(plugin);
  private final Owners owners = new Owners(plugin);
  private ItemStack[] items;
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
        items = player.getInventory().getContents();
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
        for (int i = 0; i < items.length; i++)
          player.getInventory().setItem(i, items[i]);
      }
    };
  
    // Start listening
    Bukkit.getPluginManager().registerEvents(listener, plugin);
  
    // Open anvil on next tick due to problems with same-tick opening
    CommonUtil.nextTick(submitText::open);
  }
  
  /**
   * Sort an Iterable based on each string's relevance.
   */
  public String[] relevanceSort(String pattern, String[] values) {
    Arrays.sort(values, (v1, v2) -> Float.compare(relevance(pattern, v2), relevance(pattern, v1)));
    return values;
  }

  /**
   * Puts the items of a clickable[] into an inventory.
   *
   * @param clickables The clickable[] stated above.
   * @param inventory  The inventory stated above.
   */
  private void setItems(Clickable[] clickables, Inventory inventory) {
    Function<Clickable[], ItemStack[]> getItems = (c) -> {
      ItemStack[] items = new ItemStack[c.length];
      for (int i = 0; i < c.length; i++)
        if (c[i] != null)
          items[i] = c[i].getItem();
      return items;
    };
    inventory.setStorageContents(getItems.apply(clickables));
  }

  /**
   * Relevance function
   */
  public float relevance(String pattern, String term) {
    // Ignore case
    pattern = pattern.toLowerCase();
    term = term.toLowerCase();
    
    // Optimisation
    if (term.equals(pattern)) return 1f;

    /*
    Search = the pattern term
    Match = a string containing the pattern term
    term.length() >= pattern.length()
    Relevance = percentage of letters equal to the sequence in searchResult.
    */
    
    // Required variables
    int searchLength = pattern.length();
    int matchLength = term.length();
    
    // If the term contains the pattern term, it is relevant, thus we give a full score
    if (term.contains(pattern)) return ((float) searchLength)/matchLength;
    
    // If the term does not contain the pattern term, but contains parts of it, we give a divided score
    // The score is calculated by s_x/x*m where s is the pattern term length, x is the number of characters in the pattern term not matched,
    //   and m is the term length.
    
    /* At this point term does not contain pattern */
    for (int i = searchLength; i >= 2; i--) { // i is length of substring
      for (int j = 0; j+i <= searchLength; j++) {
        String subSearch = pattern.substring(j, j+i);
        if (term.contains(subSearch)) {
          // found term, calculate relevance
          return ((float) i)/(searchLength-i)/matchLength;
        }
      }
    }
    
    // if no term found, return 0f (pattern failed)
    return 0f;
  }
  
  public void setTerminal(Component terminal) {
    this.terminal = terminal;
  }
  
  public void selectClass() {
    // Create inventory and create items
    TreeMap<String, Double> fareClasses = fares.getFaresFromDestinations(station, parseComponent(terminal));
    Inventory inventory = plugin.getServer().createInventory(null, fareClasses.size() < 54 ? roundUp(fareClasses.size(), 9) : 54, Component.text(lang.getString("select-class")));
    Clickable[] clickables = new Clickable[54];

    var fareIterator = fareClasses.entrySet().iterator();

    for (int i = 0; i < fareClasses.size() && i < 54 && fareIterator.hasNext(); i++) {
      var entry = fareIterator.next();
      var item = makeItem(Material.PAPER, 0, Component.text(entry.getKey()), Component.text(entry.getValue()));
      clickables[i] = Clickable.of(item,
        (event) -> generateTicket(item));
    }
    setItems(clickables, inventory);
    player.openInventory(inventory);
  }
  
  public void generateTicket(ItemStack item) {  //TODO: Payment using Iciwi and bank cards
    /*
    ItemStack item format:
      DisplayName: Class
      Lore[0]: Price
    Ticket format:
      DisplayName: &aTrain Ticket
      Lore[0]: From
      Lore[1]: To
      Lore[2]: Class
     */
    if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().lore() != null) {
      Component priceComponent = Objects.requireNonNull(item.getItemMeta().lore()).get(0);
      double price = 0d;
      if (priceComponent != null && isDouble(parseComponent(priceComponent)))
        price = Double.parseDouble(parseComponent(priceComponent));
      Component fareClass = item.getItemMeta().displayName();
  
      if (Iciwi.economy.getBalance(player) >= price) {
        Iciwi.economy.withdrawPlayer(player, price);
        
        // find owners of the current station and deposit accordingly
        List<String> ownersList = owners.getOwners(station);
        int ownerCount = ownersList.size();
        for (String owner : ownersList)
          owners.deposit(owner, price/2/ownerCount);
  
        // find owners of the station the ticket goes to and deposit accordingly
        ownersList = owners.getOwners(parseComponent(terminal));
        ownerCount = ownersList.size();
        for (String owner : ownersList)
          owners.deposit(owner, price/2/ownerCount);
        
        //player.sendMessage(String.format(lang.getString("generate-ticket-custom"), parseComponent(fareClass), station, parseComponent(terminal)));
        // Get ticket generator
        Material ticketMaterial = Material.valueOf(plugin.getConfig().getString("ticket.material"));
        int customModelData = plugin.getConfig().getInt("ticket.custom-model-data");
        // Generate card
        player.getInventory().addItem(makeItem(ticketMaterial, customModelData, lang.getComponent("train-ticket"), Component.text(station), terminal, fareClass));
      } else player.sendMessage(lang.getString("not-enough-money"));
    }
  }
  
  private class EventListener implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
      Inventory inventory = event.getClickedInventory();
      ItemStack item = event.getCurrentItem();
      if (inventory == null) return;
      
      if (item != null && item.hasItemMeta() && item.getItemMeta() != null) {
        Component itemName = item.getItemMeta().displayName();
        Component inventoryName = event.getView().title();
        
        if (inventoryName.equals(lang.getComponent("select-class"))) {
          generateTicket(item);
          inventory.close();
          CommonUtil.unregisterListener(this);
        } else {
          inventory.close();
          setTerminal(itemName);
          selectClass();
        }
      }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
      CommonUtil.unregisterListener(this);
    }
    
  }

  @Override
  public Clickable[] getClickables() {
    return null;
  }

  @Override
  public boolean useBottomInv() {
    return false;
  }

  @Override
  public void setSelectedItem(ItemStack selectedItem) {
  }

  @Override
  public ItemStack getSelectedItem() {
    return null;
  }

  @Override
  public void onCardSelection() {
  }

  @Override
  public void setBottomInv(boolean b) {}
  
}
