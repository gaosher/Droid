package view;

import tool.BugReporter;
import util.DimenValue;
import util.MeasureSpec;
import util.Spec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewGroup extends View{
//    public String view_type = "view group";
    List<View> Children = new ArrayList<>();
//    public ViewGroup(int width, int height){
//        Children = new ArrayList<>();
////        this.Height = height;
////        this.Width = width;
//    }

    public ViewGroup(LayoutParams layoutParams, HashMap<String, String> attrMap){
        initialBasicAttrs(attrMap);
        mLayoutParams = layoutParams;
        mLayoutParams.setLayoutParams(attrMap);
        this.AttrMap = attrMap;
    }

    public ViewGroup() {
    }

    public void addChild(View view){
        //todo
        this.Children.add(view);
    }

    public List<View> getChildren(){
        return this.Children;
    }

    @Override
    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize){
        System.out.println("----------------start ViewGroup measurement: " + this.getId() + "-------------------");
        showMeasureParams(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
//        System.out.println("WidthMeasureSpecSize = " + WidthMeasureSpecSize);
//        System.out.println("HeightMeasureSpecSize = " + HeightMeasureSpecSize);

        int maxWidth = 0;
        int maxHeight = 0;
        for (View child : Children){
//            System.out.println("ViewGroup.measureChild WidthMeasureSpecSize = " + WidthMeasureSpecSize + "; HeightMeasureSpecSize = " + HeightMeasureSpecSize);
            measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
//            System.out.println(child.getId() + ": " + child.measuredWidth + " " + child.measuredHeight);

//            if(child.measuredHeight > maxHeight){
//                maxHeight = child.measuredHeight;
//            }
//            if(child.measuredWidth > maxWidth){
//                maxWidth = child.measuredWidth;
//            }

            if(!child.isNormal){
                LayoutParams lp = child.mLayoutParams;
                int l = this.paddingLeft + lp.leftMargin;
                int t = this.paddingTop + lp.topMargin;
//                System.out.println("viewgroup locate child: " + child.getId() + " --- " + l + " " + t + " " + (l + child.measuredWidth) + " " + (t + child.measuredHeight));
                child.locateView(l, t, l + child.measuredWidth, t + child.measuredHeight);
                if(maxWidth < child.right + lp.rightMargin) {
                    maxWidth = Math.min(child.right + lp.rightMargin, WidthMeasureSpecSize);
                }
                if(maxHeight < child.bottom + lp.topMargin){
                    maxHeight = Math.min(child.bottom + lp.topMargin, HeightMeasureSpecSize);
                }
            }else{
                child.locateView(0, 0, child.measuredWidth, child.measuredHeight);
                if(maxWidth < child.right) {
                    maxWidth = Math.min(child.right, WidthMeasureSpecSize);
                }
                if(maxHeight < child.bottom ){
                    maxHeight = Math.min(child.bottom, HeightMeasureSpecSize);
                }
            }

        }

        if(WidthMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredWidth = WidthMeasureSpecSize;
        }else {
            this.measuredWidth = maxWidth + paddingLeft + paddingRight;
        }
        if(HeightMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredHeight = HeightMeasureSpecSize;
        }else{
            this.measuredHeight = maxHeight + paddingTop + paddingBottom;
        }
        showChildCoors();
        System.out.println("view group measured width = " + this.measuredWidth);
        System.out.println("view group measured height = " + this.measuredHeight);
        System.out.println("----------------end ViewGroup measurement: " + this.getId() + "-------------------");
    }

    void showChildCoors(){
        for(View child : Children){
            System.out.println("child " + child.index + ": " + child.left +" " + child.top + " " + child.right + " " + child.bottom);
        }
    }



    /**
     * Does the hard part of measureChildren: figuring out the MeasureSpec to
     * pass to a particular child. This method figures out the right MeasureSpec
     * for one dimension (height or width) of one child view.
     *
     * The goal is to combine information from our MeasureSpec with the
     * LayoutParams of the child to get the best possible results. For example,
     * if the this view knows its size (because its MeasureSpec has a mode of
     * EXACTLY), and the child has indicated in its LayoutParams that it wants
     * to be the same size as the parent, the parent should ask the child to
     * layout given an exact size.
     *
     * 修改自Android.ViewGroup.getChildMeasureSpec
     *
     * @param specSize The requirements for this view
     * @param specMode The requirements for this view
     * @param padding The padding of this view for the current dimension and margins, if applicable
     * @param childDimension How big the child wants to be in the current dimension
     * @return a MeasureSpec integer for the child
     */
    public static Spec getChildMeasureSpec(int specMode, int specSize, int padding, int childDimension) {

        int size = Math.max(0, specSize - padding);

        int resultSize = 0;
        int resultMode = 0;

        switch (specMode) {
            // Parent has imposed an exact size on us
            case MeasureSpec.EXACTLY -> {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LinearLayout.LayoutParams.MATCH_PARENT) {
                    // Child wants to be our size. So be it.
                    resultSize = size;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LinearLayout.LayoutParams.WRAP_CONTENT) {
                    // Child wants to determine its own size. It can't be bigger than us.
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
            }

            // Parent has imposed a maximum size on us
            case MeasureSpec.AT_MOST -> {
                if (childDimension >= 0) {
                    // Child wants a specific size... so be it
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LinearLayout.LayoutParams.MATCH_PARENT) {
                    // Child wants to be our size, but our size is not fixed. Constrain child to not be bigger than us.
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                } else if (childDimension == LinearLayout.LayoutParams.WRAP_CONTENT) {
                    // Child wants to determine its own size. It can't be bigger than us.
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;

                }
            }

            // 尽量避免UNSPECIFIED的出现，可滑动的组件设置为(AT_MOST, Integer.MIN_VALUE)
            case MeasureSpec.UNSPECIFIED -> {
                if (childDimension >= 0) {
                    // Child wants a specific size... let him have it
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                }else{
                    System.err.println("UNSPECIFIED view");
                }
            }
        }

        return new Spec(resultMode, resultSize);
    }


    void measureChild(View child, int wSpecMode, int wSpecSize, int hSpecMode, int hSpecSize){
//        System.out.println("ViewGroup measureChild Params: wSpecMode = " + wSpecMode + "; wSpecSize = " + wSpecSize +
//                "; hSpecMode = " + hSpecMode + "; hSpecSize = " + hSpecSize);

        if(child.isNormal){
            child.onMeasure(wSpecMode, wSpecSize, hSpecMode, hSpecSize);
            child.locateView(0, 0, child.measuredWidth, child.measuredHeight);
            return;
        }

        LayoutParams lp = child.mLayoutParams;

        // 水平方向
        int horizontal_padding =  this.paddingLeft + this.paddingRight + lp.leftMargin + lp.rightMargin;
        Spec cWidthSpec = MeasureSpec.getChildMeasureSpec(wSpecMode, wSpecSize, horizontal_padding, lp.width);

        // 垂直方向
        int vertical_padding = this.paddingTop + this.paddingBottom + lp.topMargin + lp.bottomMargin;

        Spec cHeightSpec = MeasureSpec.getChildMeasureSpec(hSpecMode, hSpecSize, vertical_padding, lp.height);

//        System.out.println("ViewGroup child.onMeasure: " + child.getId());
//        System.out.println("view group measure child " + cWidthSpec.mode + " " + cWidthSpec.size + " " + cHeightSpec.mode + " " + cHeightSpec.size);
        child.onMeasure(cWidthSpec.mode, cWidthSpec.size, cHeightSpec.mode, cHeightSpec.size);
//        System.out.println(child.getId() + " " + child.measuredWidth + " " + child.measuredHeight);
    }

    void checkOutOfParentBounds(){
        for(View child : this.getChildren()){
            if(child instanceof NormalView || child instanceof NormalViewGroup){
//                System.out.println("skip normal view");
                continue;
            }
            boolean isReported = false;
//            System.out.println();
            // 当子组件不是可横向滑动的组件时，子组件的左边界不应该超过0，子组件的右边界不应该超过父组件的右边界
            if(child.left < 0 && !(child instanceof HorizontalView)){
                BugReporter.writeViewBug(View.packageName, BugReporter.VIEW_INCOMPLETE, child, null);
                BugReporter.writeInReport("left side out of boundary " + child.left);
                isReported = true;
            }
            if(child.right + this.left > this.right && !(child instanceof HorizontalView)){
                if(!isReported){
                    BugReporter.writeViewBug(View.packageName, BugReporter.VIEW_INCOMPLETE, child, null);
                    isReported = true;
                }
                BugReporter.writeInReport("right side out of boundary " + child.right + " " + this.left + " " + this.right);
            }
            // 当子组件不是可竖向滑动的组件时，子组件的上边界不应该小于0，子组件的下边界不应该超过父组件的下边界
            if(child.top < 0 && !(child instanceof ScrollView)){
                if(!isReported){
                    BugReporter.writeViewBug(View.packageName, BugReporter.VIEW_INCOMPLETE, child, null);
                    isReported = true;
                }
                BugReporter.writeInReport("top side out of boundary " + child.top);
            }

            if(child.bottom + this.top > this.bottom && !(child instanceof ScrollView)){
                if(!isReported){
                    BugReporter.writeViewBug(View.packageName, BugReporter.VIEW_INCOMPLETE, child, null);
                    isReported = true;
                }
                BugReporter.writeInReport("bottom side out of boundary " + child.bottom + " " + this.top + " " + this.bottom);
            }
        }
    }

    public void showAllAttrs(){
        super.showAllAttrs();
        System.out.println("Children number: " + Children.size());
    }

    public void printClassName(){
        System.out.println("ViewGroup");
    }

    @Override
    public void checkView() {
        checkOutOfParentBounds();
        int n = this.getChildren().size();
        for (int i = 0; i < n; i++) {
            View child = this.getChildren().get(i);
            child.checkView();
        }
    }

    public static class LayoutParams{
        public static final int FILL_PARENT = -1;
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;
        public int width;
        public int height;
        public int minHeight = Integer.MAX_VALUE;
        public int leftMargin;
        public int topMargin;
        public int rightMargin;
        public int bottomMargin;

        public int WidthMeasureSpecMode = 0;
        public int WidthMeasureSpecSize = 0;
        public int HeightMeasureSpecMode = 0;
        public int HeightMeasureSpecSize = 0;


        void setWidthMeasureSpec(int mode, int size){
            this.WidthMeasureSpecMode = mode;
            this.WidthMeasureSpecSize = size;
        }


        void setHeightMeasureSpec(int mode, int size){
            this.HeightMeasureSpecMode = mode;
            this.HeightMeasureSpecSize = size;
        }


        public void printClassName(){
            System.out.println("ViewGroup.LayoutParams");
        }

        public LayoutParams() {
        }

        int getDimen(String dimenStr){
            if(dimenStr.equals("wrap_content")){
                return DimenValue.WRAP_CONTENT;
            } else if (dimenStr.equals("fill_parent") || dimenStr.equals("match_parent")) {
                return DimenValue.FILL_PARENT;
            }else{
                return DimenValue.parseDimenValue2Px(dimenStr);
            }
        }

        public void setLayoutParams(HashMap<String, String> attrMap){
            // initial height
            if(!attrMap.containsKey("layout_height")) {
                System.err.println("can't find layout_height");
            }
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
            // initial minHeight
            if(attrMap.containsKey("minHeight")){
                this.minHeight = getDimen(attrMap.get("minHeight"));
                attrMap.remove("minHeight");
            }
        }

        void setMargin(String margin_type, String margin_val){
            int margin_px = DimenValue.parseDimenValue2Px(margin_val);
            switch (margin_type) {
                case "layout_margin" -> {
                    leftMargin = margin_px;
                    topMargin = margin_px;
                    rightMargin = margin_px;
                    bottomMargin = margin_px;
                }
                case "layout_marginLeft", "layout_marginStart" -> leftMargin = margin_px;
                case "layout_marginTop" -> topMargin = margin_px;
                case "layout_marginRight", "layout_marginEnd" -> rightMargin = margin_px;
                case "layout_marginBottom" -> bottomMargin = margin_px;
                default -> System.err.println("unknown margin type" + margin_type);
            }
        }

        public void show(){
            System.out.println("width: " + width);
            System.out.println("height: " + height);
            System.out.println("leftMargin: " + leftMargin);
            System.out.println("topMargin: " + topMargin);
            System.out.println("rightMargin: " + rightMargin);
            System.out.println("bottomMargin: " + bottomMargin);
            System.out.println("width measureSpec Mode: " + this.WidthMeasureSpecMode);
            System.out.println("width measureSpec Size: " + this.WidthMeasureSpecSize);
            System.out.println("height measureSpec Mode: " + this.HeightMeasureSpecMode);
            System.out.println("height measureSpec Size: " + this.HeightMeasureSpecSize);
        }
    }

}
