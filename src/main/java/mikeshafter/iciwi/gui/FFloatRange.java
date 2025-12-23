package mikeshafter.iciwi.gui;
import org.geysermc.cumulus.component.Component;
import org.geysermc.cumulus.component.SliderComponent;

import io.papermc.paper.registry.data.dialog.input.DialogInput;
public class FFloatRange implements IFormItem {
private final String id;
private float min;
private float max;
private float step;
private float def = min;

public FFloatRange (String id, float min, float max, float step, float def) {
    this.id = id;
    this.min = min;
    this.max = max;
    this.step = step;
    this.def = def;
}

public FFloatRange (String id, float min, float max, float step) {
    this.id = id;
    this.min = min;
    this.max = max;
    this.step = step;
}

public String getId () {
    return this.id;
}

public DialogInput asJava () {
    return DialogInput.numberRange(id, net.kyori.adventure.text.Component.text(id), min, max).step(step).initial(def).build();
}

public Component asBedrock () {
    return SliderComponent.of(id, min, max, step, def);
}
}
