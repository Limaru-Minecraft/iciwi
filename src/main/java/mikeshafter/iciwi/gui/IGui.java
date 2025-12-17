package mikeshafter.iciwi.gui;
import io.papermc.paper.dialog.Dialog;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.Form;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

public interface IGui {
default void open(Player player) {
	if (getServer().getPluginManager().getPlugin("Geyser-Spigot") != null) {
		GeyserConnection c = GeyserApi.api().connectionByUuid(player.getUniqueId());
		if (c != null) { GeyserApi.api().sendForm(player.getUniqueId(), this.asBedrock(player)); return; }
	}
	player.showDialog(this.asJava());
}
Dialog asJava();
Form asBedrock(Player player);
}
