
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import static mikeshafter.iciwi.util.IciwiUtil.*;


public class CustomMachine implements Machine {

  private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
  private final Player player;
  private final String station;
  private final Lang lang = plugin.lang;
  private final Fares fares = plugin.fares;
  private final Owners owners = plugin.owners;
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
        // generate ticket
        ItemStack ticket = generateTicket(station, end, parseComponent(item.getItemMeta().displayName()));
        // add item to player's inventory
        if (ticket != null) player.getInventory().addItem(ticket);
        // the drill on last step
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
      for (String owner : ownersList)
        owners.deposit(owner, price / 2 / ownersList.size());

      // Get ticket materials
      Material ticketMaterial = Material.valueOf(plugin.getConfig().getString("ticket.material"));
      int customModelData = plugin.getConfig().getInt("ticket.custom-model-data");

      // Generate ticket
      return makeItem(ticketMaterial, customModelData, lang.getComponent("train-ticket"), Component.text(from), Component.text(from), Component.text(fareClass));
    }

    else {
      // Not enough money
      player.sendMessage(lang.getString("not-enough-money"));
      return null;
    }
  }

  private class EventListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
      Inventory inventory = event.getClickedInventory();
      ItemStack item = event.getCurrentItem();
      if (inventory == null) return;

      if (item != null && item.hasItemMeta() && item.getItemMeta() != null) {
        inventory.close();
        setTerminal(item.getItemMeta().displayName());
        CommonUtil.unregisterListener(this);
        selectClass();
      }
    }
  }

  /**
   * Sort an array based on each string's relevance.
   *
   * @param pattern The pattern to compare relevance with
   * @param values The array to sort
   * @return Sorted array
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
    ItemStack[] items = new ItemStack[clickables.length];
    for (int i = 0; i < clickables.length; i++)
      if (clickables[i] != null) items[i] = clickables[i].getItem();
    inventory.setStorageContents(items);
  }

  /**
   * Relevance function
   *
   * @param pattern The pattern (search) term
   * @param term The term that contains the pattern term
   * @return Relevance value
   */
  public float relevance(String pattern, String term) {
    // Ignore case
    pattern = pattern.toLowerCase();
    term = term.toLowerCase();

    // Optimisation
    if (term.equals(pattern)) return 1f;

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

  @Override
  public Clickable[] getClickables() {
    return clickables;
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
