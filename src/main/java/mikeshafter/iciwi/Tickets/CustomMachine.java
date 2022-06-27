package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.regex.Pattern;


public class CustomMachine {
  
  protected final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  protected final String station;
  protected final Player player;
  protected String searchResult;
  protected final Lang lang = new Lang(plugin);
  
  public CustomMachine(Player player, String station) {
    this.player = player;
    this.station = station;
  }

  public void open() {
    // Use BKCL to show an anvil gui for searches. After searching, run void select().
  }

  public void select() {
    // String searchResult is the search result, display all stations containing that string, in order of relevance.
    // Relevance definition: Percentage of letters equal to the sequence in searchResult.
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
    if (match.contains(search)) return searchLength/matchLength;
    
    // If the match does not contain the search term, but contains parts of it, we give a divided score
    // The score is calculated by s_x/x*m where s is the search term length, x is the number of characters in the search term not matched,
    //   and m is the match term length.

    /* At this point match does not contain search */
    for (int i = l; i >= 2; i--) { // i is length of substring
      for (int j = 0; j + i <= searchLength; j++) {
        String subSearch = search.substring(j, j+i);
        int subLength = subSearch.length();
        if (match.contains(subSearch)) {
          // found match, calculate relevance
          return subLength / (searchLength-i) * matchLength;
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
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }
  
}
