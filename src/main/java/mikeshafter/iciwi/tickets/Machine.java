package mikeshafter.iciwi.tickets;

import org.bukkit.inventory.ItemStack;

import mikeshafter.iciwi.util.Clickable;

public interface Machine {
  Clickable[] getClickables ();
  boolean useBottomInv ();
  void setSelectedItem (ItemStack selectedItem);
  ItemStack getSelectedItem ();
  void onCardSelection ();
  void setBottomInv(boolean b);
}
