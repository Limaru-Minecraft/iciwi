package mikeshafter.iciwi.tickets;

import com.bergerkiller.bukkit.common.utils.CommonUtil;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import mikeshafter.iciwi.util.InputDialogAnvil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;


public class CustomMachine {

  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
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
    InputDialogAnvil submitText = new InputDialogAnvil((Iciwi) plugin, player) {
  
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
          for (int i = 3; i < 30; i++) {
            player.getInventory().setItem(i, makeItem(Material.GLOBE_BANNER_PATTERN, Component.text(stations[i])));
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
      public void onClose() {
        for (int i = 0; i < items.length; i++)
          player.getInventory().setItem(i, items[i]);
      }
  
    };
    // Open anvil on next tick due to problems with same-tick opening
    CommonUtil.nextTick(submitText::open);
  }
  
  /**
   * Sort an Iterable based on each string's relevance.
   */
  public String[] relevanceSort(String pattern, String[] values) {
    Arrays.sort(values, (v1, v2) -> Float.compare(relevance(pattern, v1), relevance(pattern, v2)));
    return values;
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
    //   and m is the term term length.
    
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
