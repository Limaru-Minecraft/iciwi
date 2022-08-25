package mikeshafter.iciwi.config;
import net.kyori.adventure.text.Component;
public class Lang extends CustomConfig {
  public Lang(org.bukkit.plugin.Plugin plugin) {super("lang.yml", plugin);}
  public Component getComponent(String path) {String val = super.get().getString(path);return val == null || val.equals("") ? null : Component.text(val);}
}