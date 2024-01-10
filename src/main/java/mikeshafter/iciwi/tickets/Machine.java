package mikeshafter.iciwi.tickets;

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
}
