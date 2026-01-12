package mikeshafter.iciwi.gui;

import java.util.ArrayList;
import java.util.HashMap;
//import org.geysermc.cumulus.response.CustomFormResponse;
import io.papermc.paper.dialog.DialogResponseView;

public class GuiContext {
    private HashMap<String, String> stringItems = new HashMap<>();
    private HashMap<String, Float> floatItems = new HashMap<>();
    private HashMap<String, Boolean> booleanItems = new HashMap<>();

    public static GuiContext fromJava (ArrayList<IFormItem> form, DialogResponseView response) {
        GuiContext guiContext = new GuiContext();
        for (IFormItem formItem : form) {
            Class<?> returnType = formItem.returnType();
            String id = formItem.getId();
            if (returnType == String.class) {
                guiContext.stringItems.put(id, response.getText(id));
            }
            else if (returnType == Float.class) {
                guiContext.floatItems.put(id, response.getFloat(id));
            }
            else if (returnType == Boolean.class) {
                guiContext.booleanItems.put(id, response.getBoolean(id));
            }
        }
        return guiContext;
    }
    //public static GuiContext fromBedrock (ArrayList<IFormItem> form, CustomFormResponse response) {
        //GuiContext guiContext = new GuiContext();
        //for (int i = 0; i < form.size(); i++) {
            //String id = form.get(i).getId();
            //if (form.get(i) instanceof FDropdown dropdown) {
                //int j = response.asDropdown(i);
                //guiContext.stringItems.put(id, dropdown.getOptions().get(j));
            //}
            //else if (form.get(i) instanceof FStrSlider slider) {
                //int j = response.asStepSlider(i);
                //guiContext.stringItems.put(id, slider.getOptions().get(j));
            //}
            //else if (form.get(i) instanceof FFloatRange) {
                //float j = response.asSlider(i);
                //guiContext.floatItems.put(id, j);
            //}
            //else if (form.get(i) instanceof FInput) {
                //String j = response.asInput(i);
                //guiContext.stringItems.put(id, j);
            //}
            //else if (form.get(i) instanceof FToggle) {
                //boolean j = response.asToggle(i);
                //guiContext.booleanItems.put(id, j);
            //}
        //}
        //return guiContext;
    //}
    public String getString (String label) {
        return stringItems.get(label);
    }
    public float getFloat (String label) {
        return floatItems.get(label);
    }
    public boolean getBoolean (String label) {
        return booleanItems.get(label);
    }
}
