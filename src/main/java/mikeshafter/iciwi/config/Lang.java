package mikeshafter.iciwi.config;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class Lang extends CustomConfig {
public Lang () {super("lang.yml");}
public Component getComponent (String path) {
	var s = super.getString(path);
	return s.isEmpty() ? Component.text("Error: No text input for path: " + path) : Component.text(s);
}
@Override public String getString (@NotNull String path) {
	var s = super.getString(path);
	return s.isEmpty() ? "Error: No text input for path: " + path : s;
}
}