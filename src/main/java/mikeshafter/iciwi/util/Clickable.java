package mikeshafter.iciwi.util;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;


public class Clickable {
  private ItemStack item;
  private Consumer<InventoryClickEvent> consumer;
  
  private Clickable(ItemStack item, Consumer<InventoryClickEvent> consumer) {
    this.item = item;
    this.consumer = consumer;
  }
  
  public static Clickable empty(ItemStack item) {
    return of(item, e -> {});
  }
  
  public static Clickable of(ItemStack item, Consumer<InventoryClickEvent> consumer) {
    return new Clickable(item, consumer);
  }
  
  public void run (InventoryClickEvent e) { consumer.accept(e); }
  
  public ItemStack getItem() { return item; }
}
