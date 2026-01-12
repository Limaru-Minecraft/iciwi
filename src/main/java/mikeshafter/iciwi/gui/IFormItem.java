package mikeshafter.iciwi.gui;

//import org.geysermc.cumulus.component.Component;
import io.papermc.paper.registry.data.dialog.input.DialogInput;

public interface IFormItem {
String getId();
DialogInput asJava();
//Component asBedrock();
Class<?> returnType();
}
