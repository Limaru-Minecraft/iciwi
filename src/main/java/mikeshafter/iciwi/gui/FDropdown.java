package mikeshafter.iciwi.gui;

import java.util.ArrayList;
import java.util.List;

import org.geysermc.cumulus.component.Component;
import org.geysermc.cumulus.component.DropdownComponent;

import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput.OptionEntry;

public class FDropdown implements IFormItem {

private final String id;
private ArrayList<String> options = new ArrayList<>();

public FDropdown (String id, ArrayList<String> options) {
    this.id = id;
    this.options = options;
}

public String getId () {
    return this.id;
}

public DialogInput asJava () {
    List<OptionEntry> entries = options.stream().map(o -> {
        return OptionEntry.create(o, net.kyori.adventure.text.Component.text(o), false);
    }).toList();
    return DialogInput.singleOption(id, net.kyori.adventure.text.Component.text(id), entries).labelVisible(true).build();
}

public Component asBedrock () {
    return DropdownComponent.of(id, options, 0);
}
}
