package mikeshafter.iciwi.tm;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.regex.Pattern;

import static mikeshafter.iciwi.tm.MakeButton.makeButton;


public class DefectiveTicketMachine implements Listener {
  private final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  
  
  double val;
  
  
  @EventHandler
  public void TMSignClick(PlayerInteractEvent event) {
    if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign) {
      Sign sign = (Sign) event.getClickedBlock().getState();
      String signLine0 = ChatColor.stripColor(sign.getLine(0));
      String station = ChatColor.stripColor(sign.getLine(1)).replaceAll("\\s+", "");
      
      if ((signLine0.equalsIgnoreCase("[Tickets]") || signLine0.equalsIgnoreCase("-Tickets-") || signLine0.equalsIgnoreCase("[Ticket Machine]")) && !sign.getLine(1).equals(ChatColor.BOLD+"Buy/Top Up")) {
        this.newTM(event.getPlayer(), station);
      }
    }
  }
  
  
  public void newTM(Player player, String station) {
    Inventory tm = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Ticket Machine");
    
    // Single Journey Ticket
    tm.setItem(1, makeButton(Material.PAPER, ChatColor.GREEN+"New Single Journey Ticket", station));
    tm.setItem(3, makeButton(Material.MAP, ChatColor.YELLOW+"Adjust Fares"));
    tm.setItem(5, makeButton(Material.NAME_TAG, ChatColor.LIGHT_PURPLE+"ICIWI Card Operations", station));
    tm.setItem(7, makeButton(Material.BOOK, ChatColor.AQUA+"Check Fares", station));
    
    player.openInventory(tm);
  }
  
  
  @EventHandler
  public void TMClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    Inventory inventory = event.getClickedInventory();
    ItemStack item = event.getCurrentItem();
    if (inventory == null) return;
    
    // if there is no item clicked
    if (item == null || !item.hasItemMeta()) {
      return;
    }
    
    // === newTM method ===
    if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Ticket Machine")) {
      event.setCancelled(true);
      val = 0;
      String name = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
      
      // Buy a single journey ticket
      if (name.equals(ChatColor.GREEN+"New Single Journey Ticket")) {
        player.closeInventory();
        String station = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getLore()).get(0);
        Keypad keypad = new Keypad();
        keypad.keypad(player, station, 0d, 0d);
      }
      
      // Adjust fares
      else if (name.equals(ChatColor.YELLOW+"Adjust Fares")) {
        player.closeInventory();
        Inventory selectTicket = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select Ticket...");
        player.sendMessage(player.getName()+" DEBUG 2c");  // TODO: DEBUG
        player.openInventory(selectTicket);
      }
      
      // Check Value and Top Up
      else if (name.equals(ChatColor.LIGHT_PURPLE+"ICIWI Card Operations")) {
        player.closeInventory();
        
        boolean skipSelectCard = true;
        // Check if player holds an ICIWI card
        for (ItemStack i : player.getInventory().getContents()) {
          if (i != null && i.hasItemMeta() && i.getItemMeta() != null && i.getItemMeta().hasLore() && i.getItemMeta().getLore() != null && i.getItemMeta().getLore().get(0).equals("Serial number:")) {
            // If yes, make the player select the card
            Inventory selectCard = plugin.getServer().createInventory(null, 9, ChatColor.DARK_BLUE+"Select ICIWI Card...");
            // Keep station name
            String station = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getLore()).get(0);
            selectCard.setItem(4, makeButton(Material.LIGHT_GRAY_STAINED_GLASS_PANE, station));
            // Open inventory
            player.openInventory(selectCard);
            skipSelectCard = false;
            break;
          }
        }
        if (skipSelectCard) {
          player.closeInventory();
          CardPriceSelector cardPriceSelector = new CardPriceSelector();
          cardPriceSelector.cardPrice(player, null);
        }
        // Else, proceed straight to buying a new card
      }
      
      // Check fares
      else if (name.equals(ChatColor.AQUA+"Check Fares")) {
        player.closeInventory();
        String station = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getCurrentItem()).getItemMeta()).getLore()).get(0);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw "+player.getName()+" {\"text\":\">> Fare chart <<\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://mineshafter61.github.io/LimaruSite/farecharts/"+station+".png\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"Click to view the fare chart.\"}}");
      }
    }


// === SELECT METHODS ===
    
    // Select Ticket... (next operation: keypad - adjust fares)
    else if (event.getView().getTitle().contains(ChatColor.DARK_BLUE+"Select Ticket...")) {
      event.setCancelled(true);
      // Get station ticket is from
      if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null) {
        player.closeInventory();
        String station = item.getItemMeta().getLore().get(0);
        String ticketPrice = item.getItemMeta().getLore().get(1);
        if (isDouble(ticketPrice)) new Keypad().keypad(player, station, 0, Double.parseDouble(ticketPrice));
        else player.sendMessage(ChatColor.RED+"Invalid ticket! Direct tickets cannot be adjusted!");
      }
    }
    
    
    // Select ICIWI card... (next operation: card ops)
    else if (event.getView().getTitle().equals(ChatColor.DARK_BLUE+"Select ICIWI Card...")) {
      event.setCancelled(true);
      if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().getLore() != null && item.getItemMeta().getLore().get(0).equals("Serial number:")) {
        player.closeInventory();
        String serial = item.getItemMeta().getLore().get(1);
        String station = Objects.requireNonNull(Objects.requireNonNull(inventory.getItem(4)).getItemMeta()).getDisplayName();
        CardOperations cardOperations = new CardOperations();
        cardOperations.cardOps(player, serial, station);
      }
    }
  }
  
  private boolean isDouble(String s) {
    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    final String Exp = "[eE][+-]?"+Digits;
    final String fpRegex = ("[\\x00-\\x20]*"+"[+-]?("+"NaN|"+"Infinity|"+"((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+"(\\."+Digits+"("+Exp+")?)|"+"(("+"(0[xX]"+HexDigits+"(\\.)?)|"+"(0[xX]"+HexDigits+"?(\\.)"+HexDigits+")"+")[pP][+-]?"+Digits+"))"+"[fFdD]?))"+"[\\x00-\\x20]*");
    return Pattern.matches(fpRegex, s);
  }
}
