package view;

import util.MeasureSpec;
import util.Spec;

import java.util.HashMap;

public class ScrollView extends ViewGroup {


    int child_width = 0;
    int child_height = 0;

    public ScrollView(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap){
        super(layoutParams, attrMap);
    }

    @Override
    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize) {
        System.out.println("-----------------start ScrollView measurement: " + this.Id + "------------");
//        System.out.println("HeightMeasureSpecMode = " + HeightMeasureSpecMode);
//        System.out.println("WidthMeasureSpecSize = " + WidthMeasureSpecSize);
//        System.out.println("HeightMeasureSpecSize = " + HeightMeasureSpecSize);

        if(HeightMeasureSpecMode == MeasureSpec.AT_MOST){
            HeightMeasureSpecSize = Integer.MAX_VALUE;
        }

        if(this.Children.size() > 1){
            System.err.println("ScrollView Has More Than 1 Child! ");
        }

        for(View child : this.Children){
            LayoutParams child_lp = child.mLayoutParams;

            int width_padding = this.paddingLeft + this.paddingRight + child_lp.leftMargin + child_lp.rightMargin;
            Spec wSpec = getChildMeasureSpec(WidthMeasureSpecMode, WidthMeasureSpecSize, width_padding, child_lp.width);

            int height_padding = this.paddingTop + this.paddingBottom + child_lp.topMargin + child_lp.bottomMargin;
            Spec hSpec = getChildMeasureSpec(HeightMeasureSpecMode, HeightMeasureSpecSize, height_padding, child_lp.height);

            child.onMeasure(wSpec.mode, wSpec.size, hSpec.mode, hSpec.size);

            this.child_width = child.measuredWidth;
            this.measuredHeight = child.measuredHeight;

//            LayoutParams lp = child.mLayoutParams;
            int l = this.paddingLeft + child_lp.leftMargin;
            int t = this.paddingTop + child_lp.topMargin;
            child.locateView(l, t, l + child.measuredWidth, t + child.measuredHeight);
        }

        if(WidthMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredWidth = WidthMeasureSpecSize;
        }else {
            this.measuredWidth = (int) Math.min(WidthMeasureSpecSize, child_width);
        }

        if(HeightMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredHeight = HeightMeasureSpecSize;
        }else{
            this.measuredHeight = child_height;
        }

        showChildCoors();
        System.out.println("Scroll View measured width = " + this.measuredWidth);
        System.out.println("Scroll View measured height = " + this.measuredHeight);
        System.out.println("----------------end Scroll View measurement: " + this.getId() + "-------------------");
    }
}
