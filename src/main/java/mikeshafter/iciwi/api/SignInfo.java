package mikeshafter.iciwi.api;
import mikeshafter.iciwi.util.IciwiUtil;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public record SignInfo(ItemStack item, List<String> lore, String[] signText, Sign sign) {

@Override
public ItemStack item () {
	return item;
}

@Override
public List<String> lore () {
	return lore;
}

@Override
public String[] signText () {
	return signText;
}

public String station () {
	return IciwiUtil.stripColor(signText[1]);
}

@Override
public Sign sign () {
	return sign;
}
}