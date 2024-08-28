package mikeshafter.iciwi.api;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public record SignInfo (ItemStack item, String[] signText, Sign sign) {
public List<String> lore () {
	var c = item.lore();
	return (c == null) ? new ArrayList<>() : IciwiUtil.parseComponents(c);
}
public String station () { return IciwiUtil.stripColor(signText[1]); }
}