package mikeshafter.iciwi.gui;
//import org.geysermc.cumulus.component.Component;
//import org.geysermc.cumulus.component.ToggleComponent;

import io.papermc.paper.registry.data.dialog.input.DialogInput;
public class FToggle implements IFormItem {

private final String id;
private boolean def;

public FToggle (String id, boolean def) {
    this.id = id;
    this.def = def;
}

public String getId () {
    return this.id;
}
public DialogInput asJava () {
    return DialogInput.bool(id, net.kyori.adventure.text.Component.text(id)).initial(def).build();
}

//public Component asBedrock () {
 //   return ToggleComponent.of(id, def);
//}
public Class<Boolean> returnType () {return Boolean.class;}
}
