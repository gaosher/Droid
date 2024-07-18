package view;

import util.MeasureSpec;

import java.util.ArrayList;
import java.util.HashMap;

public class NormalViewGroup extends ViewGroup{


    public NormalViewGroup( HashMap<String, String> attrMap) {
        isNormal = true;
        initialBasicAttrs(attrMap);
    }

    @Override
    void initialBasicAttrs(HashMap<String, String> attrMap){
        // initial bounds
        this.setBounds(attrMap.get("bounds"));
        attrMap.remove("bounds");

        // initial index
        this.setIndex(attrMap.get("index"));
        attrMap.remove("index");

        // initial className
        if(attrMap.containsKey("class")){
            this.ClassName = attrMap.get("class");
            attrMap.remove("class");
        }
    }

    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize) {
        int wSpecMode = MeasureSpec.EXACTLY;
        int hSpecMode = MeasureSpec.EXACTLY;
        if(WidthMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredWidth = WidthMeasureSpecSize;
        }else{
            this.measuredWidth = Bounds[2] - Bounds[0];
            wSpecMode = MeasureSpec.AT_MOST;
        }

        if(HeightMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredHeight = HeightMeasureSpecSize;
        }else {
            this.measuredHeight = Bounds[3] - Bounds[1];
            hSpecMode = MeasureSpec.AT_MOST;
        }
//        System.out.println("start measure normal view group: " + Bounds[0] + " " + Bounds[1] + " " + Bounds[2] + " " + Bounds[3]);
        for(View child : Children){
            child.onMeasure(wSpecMode, measuredWidth, hSpecMode, measuredHeight);
        }
    }

}
