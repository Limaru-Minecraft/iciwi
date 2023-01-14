package mikeshafter.iciwi.tickets;

import mikeshafter.iciwi.util.Clickable;

public interface Machine
{
  public Clickable[] getClickables ();
  public boolean useBottomInventory ();
}
