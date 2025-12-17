package mikeshafter.iciwi.gui;

import java.util.ArrayList;
import java.util.List;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.*;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.SimpleForm;

import io.papermc.paper.registry.data.dialog.ActionButton;

@SuppressWarnings("UnstableApiUsage")
public class GBranch implements IGui {
private final String title;
private final String content;
private final ArrayList<GButton> buttons;

private GBranch (Builder builder) {
	this.title = builder.title;
	this.content = builder.content;
	this.buttons = builder.buttons;
}

public static class Builder {
	private String title;
	private String content;
	private final ArrayList<GButton> buttons = new ArrayList<>();

	public Builder title(String title) {
		this.title = title;
		return this;
	}

	public Builder content(String content) {
		this.content = content;
		return this;
	}

	public Builder button(GButton button) {
		this.buttons.add(button);
		return this;
	}

	public GBranch build() {
		return new GBranch(this);
	}
}

public Dialog asJava () {
	List<ActionButton> buttons = this.buttons.stream().map(GButton::asJava).toList();
	return Dialog.create(builder -> builder.empty()
		.base(DialogBase.builder(Component.text(title))
			.body(List.of(DialogBody.plainMessage(Component.text(content))))
			.canCloseWithEscape(true)
			.build()
		)
		.type(DialogType.multiAction(buttons).build())
	);
}

public Form asBedrock (Player player) {
	SimpleForm.Builder form = SimpleForm.builder()
		.title(title)
		.content(content);
	for (GButton btn : buttons) {
		form.button(btn.asBedrock());
	}
	form.validResultHandler((f, response) -> buttons.get(response.clickedButtonId()).getAction().accept(player));
	return form.build();
}
	
}
