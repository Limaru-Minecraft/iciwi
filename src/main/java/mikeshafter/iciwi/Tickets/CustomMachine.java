package mikeshafter.iciwi.Tickets;

import com.bergerkiller.bukkit.common.map.widgets.MapWidgetSubmitText;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.JsonManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;


public class CustomMachine extends MapWidgetSubmitText {
  
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final Player player;
  private final String station;
  private final ArrayList<String> stationList = JsonManager.getAllStations();
  
  public CustomMachine(Player player, String station) {
    this.player = player;
    this.station = station;
    this.setDescription("Enter a search term for a station");
    this.onActivate();
  }
  
  @Override
  public void onAccept(String text) {
    
    // Sort stations based on relevance
    TreeMap<Float, String> m = new TreeMap<>();
    for (int i = 0; i < 54; i++) {
      m.put(relevance(text, stationList.get(i)), stationList.get(i));
    }
    List<String> e = m.values().stream().toList();
    
    // Place each station into an inventory to be shown to the player
    Inventory inventory = plugin.getServer().createInventory(null, 54, "Select station");
    for (int i = 0; i < 54; i++) {
      inventory.setItem(i, makeItem(Material.GLOBE_BANNER_PATTERN, e.get(i)));
    }
    player.openInventory(inventory);
    
  }
  
  private float relevance(String search, String match) {
    /* 
    Search = the search term
    Match = a string containing the search term
    Relevance = percentage of letters equal to the sequence in searchResult.
    */
    
    // Required variables
    int searchLength = search.length();
    int matchLength = match.length();
    
    // If the match contains the search term, it is relevant, thus we give a positive score
    if (match.contains(search)) return (float) searchLength/matchLength;
    
    // If the match does not contain the search term, but contains parts of it, we give a divided score
    // The score is calculated by s_x/x*m where s is the search term length, x is the number of characters in the search term not matched,
    //   and m is the match term length.
    
    /* At this point match does not contain search */
    for (int substrLength = searchLength; substrLength >= 2; substrLength--) { // i is length of substring
      for (int j = 0; j+substrLength <= searchLength; j++) {
        String subSearch = search.substring(j, j+substrLength);
        int subLength = subSearch.length();
        if (match.contains(subSearch)) {
          // found match, calculate relevance
          return (float) subLength/(searchLength-substrLength)*matchLength;
        }
      }
    }
    
    // if no match found, return 0f (search failed)
    return 0f;
  }
  
  private ItemStack makeItem(final Material material, final String displayName, final String... lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta itemMeta = item.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    itemMeta.setLore(Arrays.asList(lore));
    item.setItemMeta(itemMeta);
    return item;
  }
  
  private boolean isDouble(String s) {
    return Pattern.matches(("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|"+"(\\.(\\p{Digit}+)([eE][+-]?(\\p{Digit}+))?)|"+"(("+"(0[xX](\\p{XDigit}+)(\\.)?)|"+"(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+))"+")[pP][+-]?(\\p{Digit}+)))"+"[fFdD]?))"+"[\\x00-\\x20]*"), s);
  }
  
}
