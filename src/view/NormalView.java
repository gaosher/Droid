package view;

import java.util.HashMap;

public class NormalView extends View{

    String Text;

    public NormalView(HashMap<String, String> attrMap){
        isNormal = true;
        initialBasicAttrs(attrMap);
    }

    @Override
    void initialBasicAttrs(HashMap<String, String> attrMap) {
        // initial bounds
        this.setBounds(attrMap.get("bounds"));
        attrMap.remove("bounds");

        // initial index
        this.setIndex(attrMap.get("index"));
        attrMap.remove("index");

        // initial className
        if (attrMap.containsKey("class")) {
            this.ClassName = attrMap.get("class");
            attrMap.remove("class");
        }

        // initial text
        if(attrMap.containsKey("text")){
            String text = attrMap.get("text");
            if(text.length() > 0) {
                this.Text = text;
            }
            attrMap.remove("text");
        }
    }

    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize) {
        System.out.println("start measure normal view: " + Bounds[0] + " " + Bounds[1] + " " + Bounds[2] + " " + Bounds[3]);
        this.measuredHeight = Bounds[3] - Bounds[1];
        this.measuredWidth = Bounds[2] - Bounds[0];
        locateView(0, 0, measuredWidth, measuredHeight);
    }
}
