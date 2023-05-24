package mikeshafter.iciwi.tickets;

import org.bukkit.inventory.ItemStack;

import mikeshafter.iciwi.util.Clickable;

public interface Machine {
  public Clickable[] getClickables ();
  public boolean useBottomInv ();
  public void setSelectedItem (ItemStack selectedItem);
  public ItemStack getSelectedItem ();
  public void onCardSelection ();
  public void setBottomInv(boolean b);
}
