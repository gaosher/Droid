package view;

import util.MeasureSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LinearLayout extends ViewGroup{


    final static boolean HORIZONTAL = false;
    final static boolean VERTICAL = true;
    boolean Orientation = HORIZONTAL; // 方向默认Horizontal
    double WeightSum = 0;
    HashMap<Integer, Double> WeightMap = new HashMap<>(); // child.index --> child.weight
    public LinearLayout(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap) {
        this.Children = new ArrayList<>();
        mLayoutParams = layoutParams;
        mLayoutParams.setLayoutParams(attrMap);
        initialBasicAttrs(attrMap);

        // initial orientation
        if(attrMap.containsKey("orientation")){
            if(attrMap.get("orientation").equals("vertical")){
                this.Orientation = true;
            }
            attrMap.remove("orientation");
        }

        this.AttrMap = attrMap;
    }

    public void printClassName(){
        System.out.println("LinearLayout");
    }

    void getWeightSum(){
        for(View child : this.Children){
            if(child.AttrMap.containsKey("layout_weight")){
                String weight_str = child.AttrMap.get("layout_weight");
                double weight = Double.parseDouble(weight_str);
                WeightMap.put(child.index, weight);
                this.WeightSum += weight;
            }
        }
    }



    // TODO: 2024/6/13
    @Override
    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize){
        // TODO: 2024/6/13
        if(this.Orientation == HORIZONTAL){ // 水平布局
            measureHorizontal();
        }else{ // 垂直布局
            measureVertical(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
        }

    }

    void measureVertical(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize){
        // 本LinearLayout的measureSpec信息

        int WidestChildWidth = 0;

        if(HeightMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredHeight = HeightMeasureSpecSize;
            int left_height = HeightMeasureSpecSize;
            for(View child : this.getChildren()){

                LayoutParams lp = (LayoutParams) child.mLayoutParams;
                this.WeightSum += lp.Weight;

                // measureChild
                measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, left_height);
//                System.out.println("child measured height 1 = " + child.measuredHeight);

                // left_height 迭代
                left_height -= (child.measuredHeight + lp.topMargin + lp.bottomMargin);

                // 记录子组件最大的宽度
                WidestChildWidth = Math.max(WidestChildWidth, child.measuredWidth);
            }

            System.out.println("left_height = " + left_height);

            if(left_height < 0){
                // todo linearLayout高度无法容纳child，输出到报告中
                System.out.println("----------------------BUG REPORT---------------------");
                System.out.println("LinearLayout高度不足");
                System.out.println("File Name: " + this.xmlFileName);
                this.showAllAttrs();
                System.out.println("----------------------BUG REPORT END---------------------");
            }

            for(View child : this.getChildren()){
                LayoutParams lp = (LayoutParams) child.mLayoutParams;
//                System.out.println("lp.weight = " + lp.Weight + "; weightSum = " + this.WeightSum);
                if(lp.Weight == 0) continue;
                // todo 这里存在分配剩余高度的二次测量，是否需要重新调用measureChild
                int child_height = child.measuredHeight + (int) (left_height * (lp.Weight / this.WeightSum));
                child.onMeasure(MeasureSpec.EXACTLY, child.measuredWidth, MeasureSpec.EXACTLY, child_height);
            }
        }

        else if(HeightMeasureSpecMode == MeasureSpec.AT_MOST){
            // System.out.println("LinearLayout measureVertical");
            int spare_height = HeightMeasureSpecSize;
            List<View> zeroHeightChildren = new ArrayList<>();

            double heightUsed = this.paddingTop + this.paddingBottom; // 已经被占用的高度，自身的padding，children的margin，不需要二次分配的children高度

            for(View child : this.getChildren()){
                LayoutParams lp = (LayoutParams) child.mLayoutParams;
                this.WeightSum += lp.Weight;

                boolean isZeroHeightChild = false;

                if(lp.height == 0 && lp.Weight > 0){
                    lp.height = LayoutParams.WRAP_CONTENT;
                    zeroHeightChildren.add(child);
                    isZeroHeightChild = true;
                }

                // measureChild
                measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, spare_height);
                System.out.println("1st measuredHeight = " + child.measuredHeight);

                // spare_height 迭代, 表示用不到的高度
                spare_height -= (child.measuredHeight + lp.topMargin + lp.bottomMargin);

                // 记录子组件最大的宽度
                WidestChildWidth = Math.max(WidestChildWidth, child.measuredWidth);

                heightUsed += lp.topMargin + lp.bottomMargin;
//                System.out.println("heightUsed = " + heightUsed);

                if( !isZeroHeightChild ) {
                    heightUsed += child.measuredHeight;
                }
            }

            if(spare_height < 0){
                // todo linearLayout高度无法容纳child，输出到报告中
                System.out.println("----------------------BUG REPORT---------------------");
                System.out.println("LinearLayout高度不足");
                System.out.println("File Name: " + this.xmlFileName);
                this.showAllAttrs();
                System.out.println("----------------------BUG REPORT END---------------------");
            }
//            System.out.println("spare_height = " + spare_height);

            // 设置LinearLayout的高度
            this.measuredHeight = HeightMeasureSpecSize - spare_height;
            System.out.println("ll.measuredHeight = " + measuredHeight);

            // 对于weight的二次测量
            if(!zeroHeightChildren.isEmpty()){
                // 根据需要根据weight重新分配到child上的总高度
//                System.out.println("heightUsed = " + heightUsed);
                double heightSharedByWeight = this.measuredHeight - heightUsed;
                System.out.println("heightSharedByWeight = " +heightSharedByWeight);
                for(View child : this.getChildren()){
                    // 误差1px
                    LayoutParams lp = (LayoutParams) child.mLayoutParams;
                    if(lp.Weight == 0) continue;
                    int child_height = (int) (heightSharedByWeight * lp.Weight / this.WeightSum);
                    System.out.println("child.verticalPadding" + (child.paddingBottom + child.paddingTop));
                    child.onMeasure(MeasureSpec.EXACTLY, child.measuredWidth, MeasureSpec.EXACTLY, child_height);
                }
            }



        }

        // 设置LinearLayout的宽度
        if(WidthMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredWidth = WidthMeasureSpecSize;
        }else{
            this.measuredWidth = WidestChildWidth;
        }

        // child 定位
        int useHeight = 0;
        for(View child : this.getChildren()){
            // 确定child位置
            LayoutParams lp = (LayoutParams) child.mLayoutParams;
            int left = this.paddingLeft + lp.leftMargin;
            int right = left + child.measuredWidth;
            int top = useHeight + this.paddingTop + lp.topMargin;
            int bottom = top + child.measuredHeight;
            locateChild(child, left, right, top, bottom);
            useHeight += bottom + lp.bottomMargin;
        }


        // 输出展示
        System.out.println("----------Linear Layout-----------");
        System.out.println("LinearLayout: measuredHeight = " + this.measuredHeight + "px, measuredWidth = " +
                this.measuredWidth + "px.");
        for (int i = 0; i < this.getChildren().size(); i++) {
            View child = this.getChildren().get(i);
            System.out.println("child " + i + ": measuredHeight = " + child.measuredHeight + "px, measuredWidth = " +
                    child.measuredWidth + "px.");
            System.out.println("left: " + child.left + ", top: " + child.top + ", right: " + child.right + ", bottom: " + child.bottom);
        }
        System.out.println("-------------Linear Layout End-----------");
    }

    void measureHorizontal(){

    }

    void locateChild(View child, int left, int right, int top, int bottom){
//        System.out.println("locateChild");
        child.left = left;
        child.right = right;
        child.top = top;
        child.bottom = bottom;
    }



    public static class LayoutParams extends ViewGroup.LayoutParams{ // public static
        public double Weight = 0;
        public LayoutParams(){
        }

        public void setLayoutParams(HashMap<String, String> attrMap){
            // initial height
            this.height = getDimen(attrMap.get("layout_height")); //px
            attrMap.remove("layout_height");
            // initial width
            this.width = getDimen(attrMap.get("layout_width")); // px
            attrMap.remove("layout_width");
            // initial margin
            for (String margin : Margin_Attr) {
                if (attrMap.containsKey(margin)) {
                    setMargin(margin, attrMap.get(margin));
                    attrMap.remove(margin);
                }
            }

            // set weight
            if(attrMap.containsKey("layout_weight")){
                try{
                    Weight = Double.parseDouble(attrMap.get("layout_weight"));
                }catch (Exception e){
                    System.err.println("can't parse layout weight: " + attrMap.get("layout_weight"));
                }
            }
        }
    }

}
