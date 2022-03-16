package mikeshafter.iciwi.Tickets;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.Lang;
import mikeshafter.iciwi.Owners;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
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
  protected final Lang lang = new Lang(plugin);
  
  public CustomMachine(Player player, String station) {
    this.player = player;
    this.station = station;
  }
  
  public void open() {
    new AnvilGUI.Builder()
        .title("Key in the destination")
        .itemLeft(makeItem(Material.EMERALD, " "))
        .onLeftInputClick(HumanEntity::closeInventory)
        .text(" ")
        .plugin(this.plugin)
        .onComplete((player, endStation) -> {
              endStation = endStation.replace(" ", "");
              return price(makeItem(Material.PAPER, "", station, endStation));
            }
        )
        .open(this.player);
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
  
  public AnvilGUI.Response price(ItemStack incompleteTicket) {
    new AnvilGUI.Builder()
        .title("Key in the price")
        .itemLeft(makeItem(Material.EMERALD, ""))
        .onLeftInputClick(HumanEntity::closeInventory)
        .text(" ")
        .plugin(this.plugin)
        .onComplete((player, price) -> {
          price = price.replace(" ", "");
          if (isDouble(price) && incompleteTicket.hasItemMeta() && incompleteTicket.getItemMeta() != null) {
            // generate ticket meta and change display name
            ItemMeta itemMeta = incompleteTicket.getItemMeta();
            itemMeta.setDisplayName(lang.getString("train-ticket")+ChatColor.AQUA+" | Price: "+price);
            incompleteTicket.setItemMeta(itemMeta);
            // give money to station owner
            double d_price = Double.parseDouble(price);
            Owners owners = new Owners(plugin);
            owners.deposit(owners.getOwner(station), d_price);
            // take money from player
            Iciwi.economy.withdrawPlayer(player, d_price);
            // give ticket to player
            player.getInventory().addItem(incompleteTicket);
            player.sendMessage(String.format(lang.getString("generate-ticket-global"), price));
          }
          return AnvilGUI.Response.close();
        })
        .open(this.player);
    
    return AnvilGUI.Response.close();
  }
  
  private boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }
  
}
