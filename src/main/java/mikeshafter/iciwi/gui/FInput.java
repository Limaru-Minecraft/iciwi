package mikeshafter.iciwi.gui;

//import org.geysermc.cumulus.component.Component;
//import org.geysermc.cumulus.component.InputComponent;
import io.papermc.paper.registry.data.dialog.input.DialogInput;

public class FInput implements IFormItem {

private final String id;
private final String def;
//private final String placeholder;

public FInput (String id, String def/*, String placeholder*/) {
    this.id = id;
    //this.placeholder = placeholder;
    this.def = def;
}

public String getId () {
    return this.id;
}

public DialogInput asJava () {
    return DialogInput.text(id, net.kyori.adventure.text.Component.text(id)).initial(def).build();
}

//public Component asBedrock () {
    //return InputComponent.of(id, placeholder, def);
//}
public Class<String> returnType () {return String.class;}
}
