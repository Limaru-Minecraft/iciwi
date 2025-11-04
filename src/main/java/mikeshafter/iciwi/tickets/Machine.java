package mikeshafter.iciwi.tickets;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.lang.Runnable;
import java.util.ArrayList;
import mikeshafter.iciwi.util.Clickable;

public interface Machine {
default Runnable getClickInvItemRunnable () {return null;}
Clickable[] getClickables ();
boolean useBottomInv ();
void setSelectedItem (ItemStack selectedItem);
ItemStack getSelectedItem ();
void setBottomInv (boolean b);

/**
 * Puts the items of a clickable[] into an inventory.
 * 
 * @param clickables The clickable[] stated above.
 * @param inventory  The inventory stated above.
 */
default void setItems (Clickable[] clickables, Inventory inventory) {
	ItemStack[] items = new ItemStack[clickables.length];
	for (int i = 0; i < clickables.length; i++)
		if (clickables[i] != null) items[i] = clickables[i].getItem();
	inventory.setStorageContents(items);
}

/**
 * Spread items evenly across n slots in an array
 *
 * @param n     Number of slots
 * @param items Items to set.
 * @return Final spreaded array
 */
default Clickable[] justify (int n, ArrayList<Clickable> items) {
	// optimisation
	int l = items.size();
	if (n == l) return items.toArray(new Clickable[l]);
	// Case where there's no padding; create an array with n elements
	Clickable[] arr = new Clickable[n];
	for (int i = 1; i <= l; i++) arr[i * n / (l + 1)] = items.get(i - 1);
	return arr;
}

/**
 * Spread items evenly across n slots in an array
 *
 * @param n     Number of slots
 * @param items Items to set.
 * @return Final spreaded array
 */
default Clickable[] alignLeft (int n, ArrayList<Clickable> items) {
	return items.toArray(new Clickable[items.size()]);
}
}
