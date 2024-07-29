package view;

import java.util.HashMap;

public class CheckBox extends ImageView{

    public CheckBox(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap){
        initialBasicAttrs(attrMap);

        //  lp 相关
        mLayoutParams = layoutParams;
        mLayoutParams.setLayoutParams(attrMap);

        if(attrMap.containsKey("button")){
            setSrc(attrMap.get("button"));
            attrMap.remove("button");
        }

        AttrMap = attrMap;
    }

    @Override
    public void setSrc(String button){
        if(button.startsWith("@drawable/")){
            this.src = button;
        }
    }

}
