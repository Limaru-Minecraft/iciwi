package mikeshafter.iciwi.tickets;

import com.bergerkiller.bukkit.common.utils.CommonUtil;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.JsonManager;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.InputDialogSubmitText;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.regex.Pattern;


public class CustomMachine {

  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final Player player;
  private final String station;
  private final Lang lang = new Lang(plugin);
  private final Fares fares = new Fares(plugin);
  private final Owners owners = new Owners(plugin);
  private Component terminal;
  private final ArrayList<String> stationList = JsonManager.getAllStations();

  public CustomMachine(Player player, String station) {
    this.player = player;
    this.station = station;
    InputDialogSubmitText submitText = new InputDialogSubmitText((Iciwi) plugin, player) {
  
      // TODO: Remake this so that inventory contents are updated as the player types
      @Override
      public void onOpen() {
        super.onOpen();
        this.setDescription(lang.getString("enter-text-description"));
      }
  
      @Override
      public void onAccept(String text) {
    
        // Sort stations based on relevance
        TreeMap<Float, String> m = new TreeMap<>();
        List<String> e = null;
        if (stationList != null && stationList.size() != 0) {
          for (String stationName : stationList) {
            m.put(relevance(text, stationName), stationName);
          }
          e = m.values().stream().toList();
        } // else e is empty
    
        // Place each station into an inventory to be shown to the player
        Inventory inventory = plugin.getServer().createInventory(null, 54, lang.getComponent("select-station"));
        if (e != null) {
          for (int i = 0; i < e.size() && i < 54; i++) {
            inventory.setItem(i, makeItem(Material.GLOBE_BANNER_PATTERN, Component.text(e.get(i))));
          }
        } // if e is empty, the inventory will be empty
        player.openInventory(inventory);
      }
  
    };
    // Open anvil on next tick due to problems with same-tick opening
    CommonUtil.nextTick(submitText::open);
  }
  
  public float relevance(String search, String match) {
    // Ignore case
    search = search.toLowerCase();
    match = match.toLowerCase();
    
    // Optimisation
    if (match.equals(search)) return 1f;

    /*
    Search = the search term
    Match = a string containing the search term
    match.length() >= search.length()
    Relevance = percentage of letters equal to the sequence in searchResult.
    */
    
    // Required variables
    int searchLength = search.length();
    int matchLength = match.length();
    
    // If the match contains the search term, it is relevant, thus we give a positive score
    if (match.contains(search)) return ((float) searchLength)/matchLength;
    
    // If the match does not contain the search term, but contains parts of it, we give a divided score
    // The score is calculated by s_x/x*m where s is the search term length, x is the number of characters in the search term not matched,
    //   and m is the match term length.
    
    /* At this point match does not contain search */
    for (int i = searchLength; i >= 2; i--) { // i is length of substring
      for (int j = 0; j+i <= searchLength; j++) {
        String subSearch = search.substring(j, j+i);
        if (match.contains(subSearch)) {
          // found match, calculate relevance
          return ((float) i)/(searchLength-i)/matchLength;
        }
      }
    }
    
    // if no match found, return 0f (search failed)
    return 0f;
  }
  
  public void setTerminal(Component terminal) {
    this.terminal = terminal;
  }
  
  public void selectClass() {
    Inventory inventory = plugin.getServer().createInventory(null, 36, Component.text(lang.getString("select-class")));
    fares.getFaresFromDestinations(station, terminal.toString()).forEach((fareClass, fare) -> inventory.addItem(makeItem(Material.PAPER, Component.text(fareClass), Component.text(fare))));
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
      Component priceComponent = item.getItemMeta().displayName();
      double price = 0d;
      if (priceComponent != null && isDouble(priceComponent.toString()))
        price = Double.parseDouble(priceComponent.toString());
      Component fareClass = Objects.requireNonNull(item.getItemMeta().lore()).get(0);
      
      if (Iciwi.economy.getBalance(player) >= price) {
        Iciwi.economy.withdrawPlayer(player, price);
        owners.deposit(owners.getOwner(station), price/2);
        owners.deposit(owners.getOwner(terminal.toString()), price/2);
        player.sendMessage(String.format(lang.getString("generate-ticket-custom"), fareClass.toString(), station, terminal));
        player.getInventory().remove(item);
        player.getInventory().addItem(makeItem(Material.PAPER, lang.getComponent("train-ticket"), Component.text(station), terminal, fareClass));
      } else player.sendMessage(lang.getString("not-enough-money"));
    }
  }
  
  private ItemStack makeItem(final Material material, final Component displayName, final Component... lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.displayName(displayName);
    itemMeta.lore(Arrays.asList(lore));
    item.setItemMeta(itemMeta);
    return item;
  }
  
  private boolean isDouble(String s) {
    return Pattern.matches(("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|"+"(\\.(\\p{Digit}+)([eE][+-]?(\\p{Digit}+))?)|"+"(("+"(0[xX](\\p{XDigit}+)(\\.)?)|"+"(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+))"+")[pP][+-]?(\\p{Digit}+)))"+"[fFdD]?))"+"[\\x00-\\x20]*"), s);
  }
  
}
