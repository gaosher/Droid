package view;

import java.util.HashMap;

public class CheckBox extends View{

    public CheckBox(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap){
        super(layoutParams, attrMap);

    }

    void setButton(String button){
        if(button.startsWith("@drawable/")){
            button = button.replace("@drawable/", "");

        }
    }

}
