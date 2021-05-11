package mikeshafter.iciwi.iciwiTM;

import mikeshafter.iciwi.Iciwi;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;


public class TicketMachine implements Listener {
  final Plugin plugin = Iciwi.getPlugin(Iciwi.class);
  private final InvItem[] itemArray = new InvItem[54];
  private Player player;
  private String station;
  
  public TicketMachine(Player player, String sta) {
    String machineTitle = ChatColor.DARK_BLUE+"Ticket Machine";  //TODO: i18n
    Inventory tm = plugin.getServer().createInventory(null, 54, machineTitle);
    defaultValues();
    for (int i = 0; i < 54; i++) {
      tm.setItem(i, itemArray[i]);
    }
  }
  
  public void defaultValues() {
    itemArray[0] = new InvItem(Material.PAPER, ChatColor.GREEN+"New Single Journey Ticket");
    itemArray[9] = new InvItem(Material.NAME_TAG, ChatColor.YELLOW+"Check Value/Top Up ICIWI Card");
    itemArray[18] = new InvItem(Material.DARK_OAK_SIGN, ChatColor.AQUA+"Check fares");
    itemArray[27] = new InvItem(Material.NAME_TAG, ChatColor.GOLD+"Other ICIWI Card services...");
    itemArray[36] = new InvItem(Material.PAPER, ChatColor.LIGHT_PURPLE+"Other Ticket services...");
    itemArray[45] = new InvItem(Material.BARRIER, ChatColor.RED+"Close Menu");
    
    for (int[] i : new int[][] {{3, 1}, {4, 2}, {5, 3}, {12, 4}, {13, 5}, {14, 6}, {21, 7}, {22, 8}, {23, 9}, {31, 0}}) {
      itemArray[i[0]] = new InvItem(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(i[1]));
    }
    itemArray[30] = new InvItem(Material.RED_STAINED_GLASS_PANE, "CLEAR");
    itemArray[32] = new InvItem(Material.LIME_STAINED_GLASS_PANE, "ENTER");
  }
  
  public TicketMachine(Player player, String sta, int[] custom) {
    String machineTitle = ChatColor.DARK_BLUE+"Ticket Machine";  //TODO: i18n
    Inventory tm = plugin.getServer().createInventory(null, 54, machineTitle);
    defaultValues();
    for (int i = 0; i < 54; i++) {
      tm.setItem(i, itemArray[i]);
    }
  }
  
  public void setItem(int i, InvItem item) {
    itemArray[i] = item;
  }
  
  @EventHandler
  public void tmClick() {
  
  }
}


class InvItem extends ItemStack {
  public InvItem(Material material, int amount, String displayName, String lore0, String lore1) {
    super.setType(material);
    super.setAmount(amount);
    ItemMeta meta = super.getItemMeta();
    assert meta != null;
    meta.setDisplayName(displayName);
    ArrayList<String> lore = new ArrayList<>();
    lore.add(lore0);
    lore.add(lore1);
    meta.setLore(lore);
    super.setItemMeta(meta);
  }
  
  public InvItem(Material material, int amount, String displayName) {
    super.setType(material);
    super.setAmount(amount);
    ItemMeta meta = super.getItemMeta();
    assert meta != null;
    meta.setDisplayName(displayName);
    super.setItemMeta(meta);
  }
  
  public InvItem(Material material, String displayName, String lore0, String lore1) {
    super.setType(material);
    super.setAmount(1);
    ItemMeta meta = super.getItemMeta();
    assert meta != null;
    meta.setDisplayName(displayName);
    ArrayList<String> lore = new ArrayList<>();
    lore.add(lore0);
    lore.add(lore1);
    meta.setLore(lore);
    super.setItemMeta(meta);
  }
  
  public InvItem(Material material, String displayName) {
    super.setType(material);
    super.setAmount(1);
    ItemMeta meta = super.getItemMeta();
    assert meta != null;
    meta.setDisplayName(displayName);
    super.setItemMeta(meta);
  }
}
