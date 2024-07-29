package view;

import util.MeasureSpec;

import java.util.ArrayList;
import java.util.HashMap;

public class NormalViewGroup extends ViewGroup{


    public NormalViewGroup(HashMap<String, String> attrMap) {
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

    boolean allNormal(){
        for(View child : getChildren()){
            if(! (child instanceof NormalView)){
                return false;
            }
        }
        return true;
    }

    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize) {
        this.measuredWidth = Bounds[2] - Bounds[0];
        this.measuredHeight = Bounds[3] - Bounds[1];
        if(allNormal()){
            Children = new ArrayList<>();
        }
        int wSpecMode = MeasureSpec.EXACTLY;
        int hSpecMode = MeasureSpec.EXACTLY;

        if(WidthMeasureSpecMode != MeasureSpec.EXACTLY){
            wSpecMode = MeasureSpec.AT_MOST;
        }
        if(HeightMeasureSpecMode != MeasureSpec.EXACTLY){
            hSpecMode = MeasureSpec.AT_MOST;
        }

//        System.out.println("start measure normal view group: " + Bounds[0] + " " + Bounds[1] + " " + Bounds[2] + " " + Bounds[3]);
        for(View child : Children){
            child.onMeasure(wSpecMode, measuredWidth, hSpecMode, measuredHeight);
            if(!child.isNormal){
                LayoutParams lp = child.mLayoutParams;
                int l = this.paddingLeft + lp.leftMargin;
                int t = this.paddingTop + lp.topMargin;
//                System.out.println("locate view: " + child.getId() + " --- " + l + " " + t + " " + (l + child.measuredWidth) + " " + (t + child.measuredHeight));
                child.locateView(l, t, l + child.measuredWidth, t + child.measuredHeight);
            }

        }
//        System.out.println("locate Normal View Group");
        this.locateView(0, 0, measuredWidth, measuredHeight);
    }

    @Override
    public void checkView(){
        for(View child : getChildren()){
            child.checkView();
        }
    }

}
