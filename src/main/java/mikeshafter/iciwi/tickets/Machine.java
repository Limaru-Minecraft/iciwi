package mikeshafter.iciwi.tickets;

import org.bukkit.inventory.ItemStack;

import mikeshafter.iciwi.util.Clickable;

public interface Machine
{
  public Clickable[] getClickables ();
  public boolean useBottomInventory ();
  public void setSelectedItem (ItemStack selectedItem);
  public ItemStack getSelectedItem ();
  public void onCardSelection ();
}
