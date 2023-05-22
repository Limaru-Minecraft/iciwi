package mikeshafter.iciwi.config;
import net.kyori.adventure.text.Component;
public class Lang extends CustomConfig {
  public Lang(org.bukkit.plugin.Plugin plugin) {super("lang.yml", plugin);}
  public Lang() {super("lang.yml");}
  public Component getComponent(String path) {String val = super.getString(path);return val == null ? Component.text("") : Component.text(val);}
}