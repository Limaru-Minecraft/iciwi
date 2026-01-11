package mikeshafter.iciwi.gui;

import static mikeshafter.iciwi.util.IciwiUtil.parseComponent;

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

private final Component title;
private final Component content;
private final ArrayList<IFormItem> formItems;
private final Consumer<GuiContext> submitAction;

private GForm (Builder builder) {
	this.title = builder.title;
	this.content = builder.content;
	this.formItems = builder.formItems;
	this.submitAction = builder.submitAction;
}

public static Builder builder() {
	return new Builder();
}

public static class Builder {
	private Component title;
	private Component content;
	private final ArrayList<IFormItem> formItems = new ArrayList<>();
	private Consumer<GuiContext> submitAction = null;

	public Builder title (Component title) {
		this.title = title;
		return this;
	}

	public Builder content (Component content) {
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
		.base(DialogBase.builder(title)
			.body(List.of(DialogBody.plainMessage(content)))
			.inputs(inputs)
			.canCloseWithEscape(true)
			.afterAction(DialogBase.DialogAfterAction.CLOSE)
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
GuiContext ctx = GuiContext.fromJava(formItems, view);
this.submitAction.accept(ctx);
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
				DialogAction.customClick((view, audience) -> {}, ClickCallback.Options.builder()
					.uses(1) // Set the number of uses for this callback. Defaults to 1
					.lifetime(ClickCallback.DEFAULT_LIFETIME) // Set the lifetime of the callback. Defaults to 12 hours
					.build()
				)
			)
		))
	);
}

@Override
public Form asBedrock (Player player) {
	List<org.geysermc.cumulus.component.Component> inputs = this.formItems.stream().map(IFormItem::asBedrock).toList();
	CustomForm.Builder formBuilder = CustomForm.builder()
		.title(parseComponent(title));
	for (org.geysermc.cumulus.component.Component input : inputs) {
		formBuilder.component(input);
	}
	formBuilder.validResultHandler((f, response) -> {
GuiContext ctx = GuiContext.fromBedrock(formItems, response);
this.submitAction.accept(ctx);
	});
	return formBuilder.build();
}

}
