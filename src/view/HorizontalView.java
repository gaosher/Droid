package view;

import java.util.HashMap;

public class HorizontalView extends ViewGroup{

    public HorizontalView(LayoutParams layoutParams, HashMap<String, String> attrMap) {
        super(layoutParams, attrMap);
    }

    @Override
    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize){
        System.out.println("start HorizontalView measurement");
        System.out.println("WidthMeasureSpecSize = " + WidthMeasureSpecSize);
        System.out.println("HeightMeasureSpecSize = " + HeightMeasureSpecSize);
        super.onMeasure(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
        System.out.println("HorizontalView: " + measuredHeight);
    }
}
