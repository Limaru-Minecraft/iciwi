package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.CardSql;
import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.config.Fares;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Owners;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.*;

import static mikeshafter.iciwi.util.MachineUtil.componentToString;
import static mikeshafter.iciwi.util.MachineUtil.isDouble;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;


public class TicketMachine {
  
  // Attributes
  private final String station;
  private final Player player;
  private Inventory i;
  private final Listener listener;

  // Constant helper classes
  private final CardSql cardSql = new CardSql();
  private final Owners owners = new Owners();
  private final Lang lang = new Lang();
  
  public TicketMachine(Player player, String station) {
    this.player = player;
    this.station = station;
    
    i = plugin.getServer().createInventory(null, 9, lang.getComponent("ticket-machine"));
    i.setItem(2, new ItemClickListener(makeItem(Material.PAPER, lang.getComponent("menu-new-ticket")), this) {   
      @Override public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(); if (super.isClicked) new CustomMachine(super.getMachine().getPlayer(), super.getMachine().getStation());
      }
    }.item);
    
    i.setItem(6, new ItemClickListener(makeItem(Material.NAME_TAG, lang.getComponent("card-operations")), this) {
      @Override public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(); if (super.isClicked) machine.cardOperations();
      }
    }.item);

    player.openInventory(i);
  }


  public void cardOperations() {
    // check if player has a card
    for (ItemStack item : player.getInventory().getContents()) {
      if (item != null && item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().lore() != null && Objects.requireNonNull(item.getItemMeta().lore()).get(0).equals(lang.getComponent("serial-number"))) {
        i = plugin.getServer().createInventory(null, 9, lang.getComponent("select-card"));
        
        this.listener = new ItemClickListener(this) {
          @Override public void onInventoryClick(InventoryClickEvent event) {
            Inventory inventory = event.getClickedInventory();
            ItemStack item = event.getCurrentItem();
            // todo: implement code to check if the item is an Iciwi Card by checking metadata instead
            if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasLore() && item.getItemMeta().lore() != null && Objects.requireNonNull(item.getItemMeta().lore()).get(0).equals(lang.getComponent("serial-number"))) {
              machine.cardOperations(new IciwiCard(item));
            }
            CommonUtil.unregisterListener(this);
          }
        };
        
        player.openInventory(i);
        return;
      }
    }
    this.newIciwiCard();
  }


  public void cardOperations(IciwiCard iciwiCard) {
    i = plugin.getServer().createInventory(null, 9, lang.getComponent("card-operation"));
    i.setItem(0, makeItem(Material.NAME_TAG, lang.getComponent("card-details"), Component.text(String.format(lang.getString("serial-number")+" %s", serial)), Component.text(String.format(lang.getString("remaining-value")+lang.getString("currency")+"%.2f", iciwiCard.getValue()))));
    i.setItem(1, new ItemClickListener(makeItem(Material.MAGENTA_WOOL, lang.getComponent("new-card")), this) {
      @Override public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event); if (super.isClicked) 
        {
          newIciwiCard();
        } CommonUtil.unregisterListener(this);
      }
    }.item);
    i.setItem(2, new ItemClickListener(makeItem(Material.CYAN_WOOL, lang.getComponent("top-up-card")), this) {
      @Override public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event); if (super.isClicked) 
        {
          topUp(iciwiCard);
        } CommonUtil.unregisterListener(this);
      }
    }.item);
    i.setItem(3, new ItemClickListener(makeItem(Material.LIME_WOOL, lang.getComponent("menu-rail-pass"), Component.text(owners.getOwner(this.station))), this) {
      @Override public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event); if (super.isClicked) 
        {
          newRailPass(owners.getOwner(this.station));
        } CommonUtil.unregisterListener(this);
      }
    }.item);
    i.setItem(4, new ItemClickListener(makeItem(Material.ORANGE_WOOL, lang.getComponent("refund-card")), this) {
      @Override public void onInventoryClick(InventoryClickEvent event) {
        super.onInventoryClick(event); if (super.isClicked) 
        {
          
        } CommonUtil.unregisterListener(this);
      }
    }.item);
  }


  public void newRailPass(String operator) {
    
  }

  
  public void topUp(IciwiCard iciwiCard) {
    
  }

  
  public void newIciwiCard() {
    
  }
  
  
  public Player getPlayer() {
    return player;
  }

  
  private class ItemClickListener implements Listener {

    private final Iciwi plugin = getPlugin(Iciwi.class);
    public ItemStack item;
    private final TicketMachine machine;
    private Integer slot;
    public boolean isClicked;

    // For in-GUI button clicking
    public onItemClickListener (ItemStack item, TicketMachine machine, int slot) {
      Bukkit.getPluginManager().registerEvents(this, plugin);
      this.machine = machine;
      this.item = item;
      this.slot = slot;
    }

    // For in-inventory item selection
    public onItemClickListener (TicketMachine machine) {
      Bukkit.getPluginManager().registerEvents(this, plugin);
      this.machine = machine;
      this.item = null;
      this.slot = null;
    }

    public TicketMachine getMachine() {
      return this.machine;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
      isClicked = event.getRawSlot() == slot;
      CommonUtil.unregisterListener(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
      CommonUtil.unregisterListener(this);
    }
  }
  
}
