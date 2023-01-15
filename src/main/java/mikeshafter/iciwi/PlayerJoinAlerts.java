package mikeshafter.iciwi;

import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class PlayerJoinAlerts implements Listener {
  
  private final CardSql cardSql = new CardSql();
  private final Plugin plugin = getPlugin(Iciwi.class);
  private final Owners owners = new Owners(plugin);
  private final Lang lang = new Lang(plugin);
  
  @EventHandler
  public void playerJoin(PlayerJoinEvent event) {
    // Variables
    final Player player = event.getPlayer();
    
    // Get serial number of player's card
    for (ItemStack itemStack : player.getInventory()) {
      if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().getLore() != null && itemStack.getItemMeta().getLore().get(0).equals(lang.getString("serial-number"))) {
        String serial = itemStack.getItemMeta().getLore().get(1);
  
        //player.sendMessage(textMenu(serial));
      }
    }
  }
  
//  public TextComponent textMenu(String serial) {
//    // Return a menu
//    List<TextComponent> discountList = cardSql.getDiscountedOperators(serial).entrySet().stream()
//        .sorted(Map.Entry.comparingByValue())
//        .map(entry -> Component.text().content(
//                "\u00A76- \u00A7a"+entry.getKey()+"\u00a76 | Exp. "+String.format("\u00a7b%s\n", new Date(entry.getValue()*1000)))
//            .append(Component.text().content("\u00a76 | Extend \u00a7a"))
//            .append(owners.getRailPassDays(entry.getKey()).stream().map(days -> Component.text().content("["+days+"d: \u00a7a"+owners.getRailPassPrice(entry.getKey(), Long.parseLong(days))+"\u00a76]").clickEvent(ClickEvent.runCommand("/newdiscount "+serial+" "+entry.getKey()+" "+days))).toList())
//            .build()).toList();
//
//    TextComponent menu = Component.text().content("==== Rail Passes You Own ====\n").color(NamedTextColor.GOLD).build();
//
//    for (TextComponent displayEntry : discountList) menu = menu.append(displayEntry);
//    menu = menu.append(Component.text("\n"));
//
//    return menu;
//  }
}
