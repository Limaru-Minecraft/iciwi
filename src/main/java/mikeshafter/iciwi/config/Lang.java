package mikeshafter.iciwi.config;
import net.kyori.adventure.text.Component;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import mikeshafter.iciwi.util.IciwiUtil;

public class Lang extends CustomConfig {
public Lang () {super("lang.yml");}
public Component getComponent (String path) {
	var s = super.getString(path);
	return s.isEmpty() ? Component.text("Error: No text input for path: " + path) : Component.text(s);
}
@Override public String getString (@NotNull String path) {
	var s = super.getString(path);
	return s.isEmpty() ? "Error: Nag server owner to put path into lang.yml: " + path : s;
}
public String createRichMessage (String header, String mainColor, String varColor, List<String> body, int bodyIndent, Map<String, String> values) {
	StringBuilder colorStart = new StringBuilder("<>").insert(1, mainColor);
	StringBuilder colorEnd = new StringBuilder("</>").insert(2, mainColor);
	StringBuilder varClrStart = new StringBuilder("<>").insert(1, varColor);
	StringBuilder varClrEnd = new StringBuilder("</>").insert(2, varColor);
	StringBuilder fHeader = new StringBuilder("===  ===").insert(4, header);
	String footer = "=============";
	
	StringBuilder main = new StringBuilder().append(colorStart).append(fHeader).append("<br>");
	for (String item : body) {
		main.append(varClrStart).append(" ".repeat(bodyIndent)).append(item).append(varClrEnd).append("<br>");
	}
	main.append(colorEnd).append(footer);
	return IciwiUtil.format(main.toString(), values);
}
}
