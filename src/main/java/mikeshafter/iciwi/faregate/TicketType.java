package mikeshafter.iciwi.faregate;

import org.bukkit.Material;

import java.util.Objects;

import static mikeshafter.iciwi.faregate.CardUtil.plugin;

public enum TicketType {
	TICKET(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(plugin.getConfig().getString("ticket.material"))))),
	CARD(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(plugin.getConfig().getString("card.material"))))),
	RAIL_PASS(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(plugin.getConfig().getString("railpass.material")))));

	private final Material material;

TicketType (Material material) { this.material = material; }
public Material getMaterial () { return material; }

static TicketType asTicketType (Material material) {
	if (material == TICKET.material) return TICKET;
	else if (material == CARD.material) return CARD;
	else if (material == RAIL_PASS.material) return RAIL_PASS;
	else return null;
}
}
