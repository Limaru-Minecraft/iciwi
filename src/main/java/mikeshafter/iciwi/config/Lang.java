package mikeshafter.iciwi.config;
import net.kyori.adventure.text.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import mikeshafter.iciwi.Iciwi;

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

public String createRichMessage (String header, List<String> body, Map<String, Object> values) {
	Map<String, String> newValues = values.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
		if (e.getValue() instanceof Double d) return Iciwi.economy.format(d);
		else return String.valueOf(e.getValue());
	}));
	return createRichMessage(header, this.getString("head-color"), this.getString("body-color"), body, 2, newValues);
}
public String createRichMessage (String header, String mainColor, String varColor, List<String> body, int bodyIndent, Map<String, String> values) {
	StringBuilder mainClrStart = new StringBuilder("<>").insert(1, mainColor);
	StringBuilder mainClrEnd = new StringBuilder("</>").insert(2, mainColor);
	StringBuilder varClrStart = new StringBuilder("<>").insert(1, varColor);
	StringBuilder varClrEnd = new StringBuilder("</>").insert(2, varColor);
	StringBuilder fHeader = new StringBuilder("===  ===").insert(4, header);
	String footer = "=".repeat(fHeader.length()-1);

	StringBuilder main = new StringBuilder().append(mainClrStart).append(fHeader).append("<br>");
	for (String item : body) {
		for (Map.Entry<String, String> entry : values.entrySet()) {
			item = item.replace("{" + entry.getKey() + "}", entry.getValue());
		}
		if (!item.contains("{") ) main.append(varClrStart).append(" ".repeat(bodyIndent)).append(item).append(varClrEnd).append("<br>");
	}
	main.append(footer).append(mainClrEnd);
	return main.toString();
}
}