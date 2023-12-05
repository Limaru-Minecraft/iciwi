package mikeshafter.iciwi.config;
import net.kyori.adventure.text.Component;
public class Lang extends CustomConfig {
public Lang(org.bukkit.plugin.Plugin plugin) {super("lang.yml", plugin);}
public Lang() {super("lang.yml");}
public Component getComponent(String path) {var s = super.getString(path);return s.equals("") ? Component.text("Error: No text input for path: "+path) : Component.text(s);}
@Override public String getString(String path) {var s = super.getString(path);return s.equals("") ? "Error: No text input for path: "+path : s;}
}