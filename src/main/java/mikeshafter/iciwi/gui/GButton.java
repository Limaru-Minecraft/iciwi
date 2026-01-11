package mikeshafter.iciwi.gui;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.component.ButtonComponent;
import org.geysermc.cumulus.util.FormImage;

import static mikeshafter.iciwi.util.IciwiUtil.parseComponent;

import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class GButton {
private final Component content;
private final Component tooltip;
private final FormImage.Type imageType;
private final String imageUri;

public Consumer<Player> getAction () {
	return action;
}

private final Consumer<Player> action;

private GButton (Builder builder) {
	this.content = builder.content;
	this.tooltip = builder.tooltip;
	this.imageType = builder.imageType;
	this.imageUri = builder.imageUri;
	this.action = builder.action;
}

public static Builder builder() {
	return new Builder();
}

public static class Builder {
	private Component content = Component.empty();
	private Component tooltip = Component.empty();
	private FormImage.Type imageType = FormImage.Type.PATH;
	private String imageUri = "";
	private Consumer<Player> action = (p) -> {};

	public Builder content(Component content) {
		this.content = content;
		return this;
	}

	public Builder tooltip(Component tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	public Builder image(FormImage.Type type, String uri) {
		this.imageType = type;
		this.imageUri = uri;
		return this;
	}

	public Builder onClick(Consumer<Player> action) {
		this.action = action;
		return this;
	}

	public GButton build() {
		return new GButton(this);
	}
}

public ActionButton asJava () {
	DialogAction dialogAction = DialogAction.staticAction(ClickEvent.callback(audience -> {
		if (audience instanceof Player player) action.accept(player);
	}));
	return ActionButton.create(this.content, this.tooltip, 150, dialogAction);
}

public ButtonComponent asBedrock () {
	if (this.imageType == null || this.imageUri == null) {
		return ButtonComponent.of(parseComponent(this.content));
	}
	return ButtonComponent.of(parseComponent(this.content), this.imageType, this.imageUri);
}
}
