package mikeshafter.iciwi.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import io.papermc.paper.dialog.Dialog;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.Form;
import org.bukkit.entity.Player;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.TextColor;

@SuppressWarnings("UnstableApiUsage")
public class GForm implements IGui {

private final String title;
private final String content;
private final ArrayList<IFormItem> formItems;
private final Consumer<GuiContext> submitAction;

private GForm (Builder builder) {
	this.title = builder.title;
	this.content = builder.content;
	this.formItems = builder.formItems;
	this.submitAction = builder.submitAction;
}

public static class Builder {
	private String title;
	private String content;
	private final ArrayList<IFormItem> formItems = new ArrayList<>();
	private Consumer<GuiContext> submitAction = null;

	public Builder title (String title) {
		this.title = title;
		return this;
	}

	public Builder content (String content) {
		this.content = content;
		return this;
	}

	public Builder item (IFormItem formItem) {
		this.formItems.add(formItem);
		return this;
	}

	public Builder action (Consumer<GuiContext> action) {
		this.submitAction = action;
		return this;
	}

	public GForm build () {
		return new GForm(this);
	}
}

@Override
public Dialog asJava () {
	List<DialogInput> inputs = this.formItems.stream().map(IFormItem::asJava).toList();
	formItems.get(0).getId();
	return Dialog.create(builder -> builder.empty()
		.base(DialogBase.builder(Component.text(title))
			.body(List.of(DialogBody.plainMessage(Component.text(content))))
			.inputs(inputs)
			.canCloseWithEscape(true)
			.build()
		)
		.type(DialogType.confirmation(
			ActionButton.create(
				Component.text("Confirm", TextColor.color(0xAEFFC1)),
				Component.text("Click to confirm your input."),
				150,
				DialogAction.customClick(
					(view, audience) -> {
						if (audience instanceof Player) {
							// TODO: handle form submission
							formItems.get(0).asJava();
							view.getFloat(content);
							view.getBoolean(content);
							view.getText(content);
						}
					},
					ClickCallback.Options.builder()
						.uses(1) // Set the number of uses for this callback. Defaults to 1
						.lifetime(ClickCallback.DEFAULT_LIFETIME) // Set the lifetime of the callback. Defaults to 12 hours
						.build()
				)
			),
			ActionButton.create(
				Component.text("Cancel", TextColor.color(0xFFA0B1)),
				Component.text("Click to discard your input."),
				150,
				null // If we set the action to null, it doesn't do anything and closes the dialog
			)
		))
	);
}

@Override
public Form asBedrock (Player player) {
	List<org.geysermc.cumulus.component.Component> inputs = this.formItems.stream().map(IFormItem::asBedrock).toList();
	CustomForm.Builder formBuilder = CustomForm.builder()
		.title(title);
	for (org.geysermc.cumulus.component.Component input : inputs) {
		formBuilder.component(input);
	}
	formBuilder.validResultHandler((f, response) -> {
		while (response.isNextPresent()) {
			var next = response.next();
			if (next instanceof Integer) {

			}
			else if (next instanceof Float) {

			}
			else if (next instanceof Boolean) {

			}
			else if (next instanceof String) {

			}
		}
		// TODO: handle form submission
	});
	return formBuilder.build();
}

}
