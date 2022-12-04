package mikeshafter.iciwi.util;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;


public class ClickItem {
  private ItemStack item;
  private Consumer<InventoryClickEvent> consumer;
  
  private ClickItem(ItemStack item, Consumer<InventoryClickEvent> consumer) {
    this.item = item;
    this.consumer = consumer;
  }
  
  public static ClickItem empty(ItemStack item) {
    return of(item, e -> {});
  }
  
  public static ClickItem of(ItemStack item, Consumer<InventoryClickEvent> consumer) {
    return new ClickItem(item, consumer);
  }
  
  public void run (InventoryClickEvent e) { consumer.accept(e); }
  
  public ItemStack getItem() { return item; }
}
