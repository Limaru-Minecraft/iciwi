package mikeshafter.iciwi.faregate;

import mikeshafter.iciwi.Iciwi;
import mikeshafter.iciwi.api.FareGate;
import mikeshafter.iciwi.api.IcCard;
import mikeshafter.iciwi.config.Lang;
import mikeshafter.iciwi.config.Records;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.block.Sign;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClassChange extends FareGate {

	private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);
	private final Lang lang = new Lang();

	public ClassChange() {
		super();
		super.setSignLine0(lang.getString("classchange"));
	}

	@Override
	public void onInteract(Player player, ItemStack item, String[] signText, Sign sign) {
		// Get new class
		String newClass = IciwiUtil.stripColor(signText[1]);

		// Wax sign
		sign.setWaxed(true);
		sign.update(true);

		// Check if card
		if (item.getType() == Material.valueOf(plugin.getConfig().getString("card.material")) && IciwiUtil.loreCheck(item)) {

			// TODO: if card, access records.yml and change the class to newClass
      IcCard icCard = IciwiUtil.IcCardFromItem(item);
      if (icCard != null) {
        String serial = icCard.getSerial();
        final Records records = new Records();
        records.setClass(serial, newClass);
        player.sendMessage(String.format(lang.getString("class-changed"), newClass));
  		}
    }
  }

}
