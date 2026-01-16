package mikeshafter.iciwi.gui;

//import org.geysermc.cumulus.component.Component;
//import org.geysermc.cumulus.component.InputComponent;
import io.papermc.paper.registry.data.dialog.input.DialogInput;

public class FInput implements IFormItem {

private final String id;
private final String name;
private final String def;
//private final String placeholder;

public FInput (String id, String name, String def) {
    this.id = id;
    this.name = name;
    this.def = def;
}

public String getId () {
    return this.id;
}

public DialogInput asJava () {
    return DialogInput.text(id, net.kyori.adventure.text.Component.text(name)).initial(def).build();
}

//public Component asBedrock () {
    //return InputComponent.of(id, placeholder, def);
//}
public Class<String> returnType () {return String.class;}
}
