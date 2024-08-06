package mikeshafter.iciwi.tickets;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.lang.Runnable;
import mikeshafter.iciwi.util.Clickable;

public interface Machine {
default Runnable getClickInvItemRunnable () {return null;}
Clickable[] getClickables ();
boolean useBottomInv ();
void setSelectedItem (ItemStack selectedItem);
ItemStack getSelectedItem ();
void setBottomInv (boolean b);

/**
Puts the items of a clickable[] into an inventory.

@param clickables The clickable[] stated above.
@param inventory  The inventory stated above. */
default void setItems (Clickable[] clickables, Inventory inventory) {
	ItemStack[] items = new ItemStack[clickables.length];
	for (int i = 0; i < clickables.length; i++)
		if (clickables[i] != null) items[i] = clickables[i].getItem();
	inventory.setStorageContents(items);
}
}
