package view;

import java.util.HashMap;

public class RecyclerView extends ViewGroup{

    public RecyclerView(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap){
        super(layoutParams, attrMap);
    }

    @Override
    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize){
//        System.out.println("start Recycler View measurement");
//        System.out.println("WidthMeasureSpecSize = " + WidthMeasureSpecSize);
//        System.out.println("HeightMeasureSpecSize = " + HeightMeasureSpecSize);
        super.onMeasure(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
    }
}
