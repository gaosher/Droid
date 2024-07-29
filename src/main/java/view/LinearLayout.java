package view;

import util.MeasureSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LinearLayout extends ViewGroup {


    final static boolean HORIZONTAL = false;
    final static boolean VERTICAL = true;
    boolean Orientation = HORIZONTAL; // 方向默认Horizontal
    double WeightSum = 0;
    HashMap<Integer, Double> WeightMap = new HashMap<>(); // child.index --> child.weight

    public LinearLayout(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap) {
        this.Children = new ArrayList<>();
        initialBasicAttrs(attrMap);

        mLayoutParams = layoutParams;
        mLayoutParams.setLayoutParams(attrMap);

        // initial orientation
        if (attrMap.containsKey("orientation")) {
            if (attrMap.get("orientation").equals("vertical")) {
                this.Orientation = VERTICAL;
            }
            attrMap.remove("orientation");
        }
//        System.out.println("orientation: " + this.Orientation);


        this.AttrMap = attrMap;
    }

    public void printClassName() {
        System.out.println("LinearLayout");
    }

    void setWeightSum() {
        double weight_sum = 0;
        for (View child : this.Children) {
            if (child.isNormal) {
                continue;
            }
            LayoutParams lp = (LayoutParams) child.mLayoutParams;
            weight_sum += lp.Weight;
        }
        this.WeightSum = weight_sum;
    }


    // TODO: 2024/6/13
    @Override
    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize) {
        // TODO: 2024/6/13
//        if(getChildren().size() == 1){
//            super.onMeasure(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode,HeightMeasureSpecSize);
//            return;
//        }
        setWeightSum();

        System.out.println("-------- start LinearLayout measurement " + this.getId() + " ----------");
        showMeasureParams(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
        if (this.Orientation == HORIZONTAL) { // 水平布局
            System.out.println("start LinearLayout horizontal measurement");
            measureHorizontal(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
        } else { // 垂直布局
            System.out.println("start LinearLayout vertical measurement");
            measureVertical(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
        }
        System.out.println("measured width = " + measuredWidth);
        System.out.println("measured height = " + measuredHeight);
        System.out.println("end LinearLayout measurement id: " + this.getId());
    }

    void measureVertical(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize) {
        int WidestChildWidth = 0;
        if (HeightMeasureSpecMode == MeasureSpec.EXACTLY) {
//            System.out.println("measureVertical height mode = MeasureSpec.EXACTLY");
            this.measuredHeight = HeightMeasureSpecSize;
            int left_height = HeightMeasureSpecSize;
            for (View child : this.getChildren()) {
                if (child.isNormal) {
                    child.onMeasure(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                    left_height -= child.measuredHeight;
                    continue;
                }
                LayoutParams lp = (LayoutParams) child.mLayoutParams;
                this.WeightSum += lp.Weight;

                measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, left_height);

                // left_height 迭代
                left_height -= (child.measuredHeight + lp.topMargin + lp.bottomMargin);

                // 记录子组件最大的宽度
                WidestChildWidth = Math.max(WidestChildWidth, child.measuredWidth);
            }

            for (View child : this.getChildren()) {
                if (child.isNormal) {
//                    child.onMeasure(MeasureSpec.EXACTLY, 0, MeasureSpec.EXACTLY, 0);
                    continue;
                }
                LayoutParams lp = (LayoutParams) child.mLayoutParams;
                if (lp.Weight == 0) continue;
                int child_height = child.measuredHeight + (int) (left_height * (lp.Weight / this.WeightSum));

                child.onMeasure(MeasureSpec.EXACTLY, child.measuredWidth, MeasureSpec.EXACTLY, child_height);
            }
        }
        else if (HeightMeasureSpecMode == MeasureSpec.AT_MOST) {
            // System.out.println("LinearLayout measureVertical");
//            int spare_height = HeightMeasureSpecSize - paddingTop - paddingBottom;
            this.measuredHeight = paddingTop + paddingBottom;
//            System.out.println(getId() + ": measuredHeight = " + measuredHeight);
            List<View> zeroHeightChildren = new ArrayList<>();

            double heightUsed = this.paddingTop + this.paddingBottom; // 已经被占用的高度，自身的padding，children的margin，不需要二次分配的children高度

            for (View child : this.getChildren()) {
                // TODO: 2024/7/17
                if (child.isNormal) {
                    child.onMeasure(MeasureSpec.EXACTLY, 0, MeasureSpec.EXACTLY, 0);
                    this.measuredHeight += child.measuredHeight;
                    heightUsed += child.measuredHeight;
                    continue;
                }
//                System.out.println("linearlayout measureVertical: " + child.getId());

                LayoutParams lp = (LayoutParams) child.mLayoutParams;
//                this.WeightSum += lp.Weight;

                boolean isZeroHeightChild = false;

                if (lp.height == 0 && lp.Weight > 0) {
                    lp.height = LayoutParams.WRAP_CONTENT;
                    zeroHeightChildren.add(child);
                    isZeroHeightChild = true;
                }

                measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize - this.measuredHeight);
//                System.out.println("1st measuredHeight = " + child.measuredHeight);

                // spare_height 迭代, 表示用不到的高度
                this.measuredHeight += (child.measuredHeight + lp.topMargin + lp.bottomMargin);
                System.out.println(getId() + ": measuredHeight = " + measuredHeight);

                // 记录子组件最大的宽度
                WidestChildWidth = Math.max(WidestChildWidth, child.measuredWidth);

                heightUsed += lp.topMargin + lp.bottomMargin;
//                System.out.println("heightUsed = " + heightUsed);

                if (!isZeroHeightChild) {
                    heightUsed += child.measuredHeight;
                }
            }


            // 设置LinearLayout的高度
//            this.measuredHeight = HeightMeasureSpecSize - spare_height;
            System.out.println(getId() + " ll.measuredHeight = " + measuredHeight);

            // 对于weight的二次测量
            if (!zeroHeightChildren.isEmpty()) {
                // 根据需要根据weight重新分配到child上的总高度
//                System.out.println("heightUsed = " + heightUsed);
                double heightSharedByWeight = this.measuredHeight - heightUsed;
//                System.out.println("heightSharedByWeight = " +heightSharedByWeight);
                for (View child : this.getChildren()) {
                    // TODO: 2024/7/18 normalView 处理 
                    if (child.isNormal) {
//                        child.onMeasure(MeasureSpec.EXACTLY, 0, MeasureSpec.EXACTLY, 0);
                        continue;
                    }
                    // 误差1px
                    LayoutParams lp = (LayoutParams) child.mLayoutParams;
                    if (lp.Weight == 0) continue;
                    int child_height = (int) (heightSharedByWeight * lp.Weight / this.WeightSum);
//                    System.out.println("child.verticalPadding" + (child.paddingBottom + child.paddingTop));
                    child.onMeasure(MeasureSpec.EXACTLY, child.measuredWidth, MeasureSpec.EXACTLY, child_height);
                }
            }

        }

        // 设置LinearLayout的宽度
        if (WidthMeasureSpecMode == MeasureSpec.EXACTLY) {
            this.measuredWidth = WidthMeasureSpecSize;
        } else {
            this.measuredWidth = WidestChildWidth;
        }

        // child 定位
//        int useHeight = 0;
        int top = this.paddingTop;
        for (View child : this.getChildren()) {
            // TODO: 2024/7/18
            if (child.isNormal) {
                locateChild(child, paddingLeft, paddingLeft + child.measuredWidth, top, top + child.measuredHeight);
                top = top + child.measuredHeight;
                System.out.println(getId() + " child locations : child " + child.index + ": " + child.left + " " + child.top + " " + child.right + " " + child.bottom);
//
                continue;
            }
            // 确定child位置
            LayoutParams lp = (LayoutParams) child.mLayoutParams;
            int left = this.paddingLeft + lp.leftMargin;
            int right = left + child.measuredWidth;
            top += lp.topMargin;
            int bottom = top + child.measuredHeight;
//            System.out.println("linear layout locate child: " + left + " " + top + " " + right + " " + bottom);
            child.locateView(left, top, right, bottom);
            top = bottom;

            // 输出children的坐标
            System.out.println(getId() + " child locations : child " + child.index + ": " + child.left + " " + child.top + " " + child.right + " " + child.bottom);
        }


        // 输出展示
//        System.out.println("----------Linear Layout-----------");
        System.out.println(getId() + ": LinearLayout: measuredHeight = " + this.measuredHeight + "px, measuredWidth = " +
                this.measuredWidth + "px.");
//        System.out.println("children cnt = " + this.getChildren().size());
//        for (int i = 0; i < this.getChildren().size(); i++) {
//            View child = this.getChildren().get(i);
////            System.out.println(child.getId());
//            System.out.println("child " + i + ": measuredHeight = " + child.measuredHeight + "px, measuredWidth = " +
//                    child.measuredWidth + "px.");
//            System.out.println("left: " + child.left + ", top: " + child.top + ", right: " + child.right + ", bottom: " + child.bottom);
//        }
//        System.out.println("-------------Linear Layout End-----------");
    }

    void measureHorizontal(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize) {
        int maxChildHeight = 0;
        if (WidthMeasureSpecMode == MeasureSpec.EXACTLY) {
            System.out.println("measureHorizontal width mode = MeasureSpec.EXACTLY");
            this.measuredWidth = WidthMeasureSpecSize;
            int left_width = WidthMeasureSpecSize - this.paddingLeft - this.paddingRight; // TODO: 2024/7/18 -horizontalPadding?
            for (View child : this.getChildren()) {
                if (child.isNormal) {// TODO: 2024/7/18
                    child.onMeasure(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                    continue;
                }
                LayoutParams lp = (LayoutParams) child.mLayoutParams;

                if (lp.width == 0) {
                    System.out.println("skip 0 width measure");
                    continue;
                }

                measureChild(child, WidthMeasureSpecMode, left_width, HeightMeasureSpecMode, HeightMeasureSpecSize);

                // left_width 迭代
                left_width -= (child.measuredWidth + lp.leftMargin + lp.rightMargin);

                // 记录子组件最大的高度
                maxChildHeight = Math.max(maxChildHeight, child.measuredHeight);
            }


            if (left_width < 0) {
                // todo linearLayout高度无法容纳child，输出到报告中
//                System.out.println("----------------------BUG REPORT---------------------");
//                System.out.println("LinearLayout宽度不足");
//                System.out.println("File Name: " + this.xmlFileName);
//                this.showAllAttrs();
//                System.out.println("----------------------BUG REPORT END---------------------");
            }

            for (View child : this.getChildren()) {
                if (child.isNormal) continue; // todo
                LayoutParams lp = (LayoutParams) child.mLayoutParams;
                if (lp.Weight == 0) continue;
                int child_width = child.measuredWidth + (int) (left_width * (lp.Weight / this.WeightSum));
                System.out.println("left_width = " + left_width);
                System.out.println("WeightSum = " + WeightSum);
                System.out.println("child_width = " + child_width);
                if (lp.width == 0) {
                    lp.width = child_width;
                    measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                } else {
                    child.onMeasure(MeasureSpec.EXACTLY, child_width, MeasureSpec.EXACTLY, child.measuredHeight);
                }

            }
        } else if (WidthMeasureSpecMode == MeasureSpec.AT_MOST) {
            // System.out.println("LinearLayout measureVertical");
            int spare_width = WidthMeasureSpecSize;
            List<View> zeroWidthChildren = new ArrayList<>();

            double widthUsed = this.paddingTop + this.paddingBottom; // 已经被占用的高度，自身的padding，children的margin，不需要二次分配的children高度

            for (View child : this.getChildren()) {
                // TODO: 2024/7/17
                if (child.isNormal) {
                    child.onMeasure(MeasureSpec.EXACTLY, 0, MeasureSpec.EXACTLY, 0);
                    spare_width -= child.measuredWidth;
                    maxChildHeight = Math.max(maxChildHeight, child.measuredHeight);
//                    maxChildHeight = Math.min(maxChildHeight, HeightMeasureSpecSize);
//                    widthUsed += child.measuredWidth;
                    continue;
                }
//                System.out.println("linearlayout measureHorizontal: " + child.getId());

                LayoutParams lp = (LayoutParams) child.mLayoutParams;

                boolean isZeroWidthChild = false;

                if (lp.width == 0 && lp.Weight > 0) {
                    lp.width = LayoutParams.WRAP_CONTENT;
                    zeroWidthChildren.add(child);
                    isZeroWidthChild = true;
                }

                measureChild(child, WidthMeasureSpecMode, spare_width, HeightMeasureSpecMode, HeightMeasureSpecSize);
//                System.out.println("1st measuredWidth = " + child.measuredWidth);

                // spare_width 迭代, 表示用不到的宽度
                spare_width -= (child.measuredWidth + lp.leftMargin + lp.rightMargin);

                // 记录子组件最大的宽度
                maxChildHeight = Math.max(maxChildHeight, child.measuredWidth);

                widthUsed += lp.leftMargin + lp.rightMargin;
//                System.out.println("heightUsed = " + heightUsed);

                if (!isZeroWidthChild) {
                    widthUsed += child.measuredWidth;
                }
            }

            if (spare_width < 0) {
                // todo linearLayout高度无法容纳child，输出到报告中
//                System.out.println("----------------------BUG REPORT---------------------");
//                System.out.println("LinearLayout宽度不足");
//                System.out.println("File Name: " + this.xmlFileName);
//                this.showAllAttrs();
//                System.out.println("----------------------BUG REPORT END---------------------");
            }
//            System.out.println("spare_height = " + spare_height);

            // 设置LinearLayout的高度
            this.measuredWidth = WidthMeasureSpecSize - spare_width;
            System.out.println("ll.measuredWidth = " + measuredWidth);

            // 对于weight的二次测量
            if (!zeroWidthChildren.isEmpty()) {
                // 根据需要根据weight重新分配到child上的总高度
//                System.out.println("heightUsed = " + heightUsed);
                double widthSharedByWeight = this.measuredWidth - widthUsed;
//                System.out.println("widthSharedByWeight = " + widthSharedByWeight);
                for (View child : this.getChildren()) {
//                     误差1px
                    LayoutParams lp = (LayoutParams) child.mLayoutParams;
//                    if(lp.Weight == 0) continue;
                    int child_width = (int) (widthSharedByWeight * lp.Weight / this.WeightSum);
//                    System.out.println("child.verticalPadding" + (child.paddingBottom + child.paddingTop));
                    child.onMeasure(MeasureSpec.EXACTLY, child_width, MeasureSpec.EXACTLY, child.measuredHeight);
                }
            }

        }

        // 设置LinearLayout的宽度
        if (HeightMeasureSpecMode == MeasureSpec.EXACTLY) {
            this.measuredHeight = HeightMeasureSpecSize;
        } else {
            this.measuredHeight = maxChildHeight;
        }

        // child 定位
        int useWidth = 0;
        int left = paddingLeft;
        int top = paddingTop;
        for (View child : this.getChildren()) {
            // TODO: 2024/7/18
            if (child.isNormal) {
                child.locateView(left, top, left+child.measuredWidth, top + child.measuredHeight);
                left += child.measuredWidth;
                System.out.println(getId() + " child locations : child " + child.index + ": " + child.left + " " + child.top + " " + child.right + " " + child.bottom);
                continue;
            }
            // 确定child位置
            LayoutParams lp = (LayoutParams) child.mLayoutParams;
            left += lp.leftMargin;
            int right = left + child.measuredWidth;
            int bottom = top + child.measuredHeight;
            child.locateView(left, top, right, bottom);
//            System.out.println("linear layout locate child: " + left + " " + top + " " + right + " " + bottom);
            left = right;
            System.out.println(getId() + " child locations : child " + child.index + ": " + child.left + " " + child.top + " " + child.right + " " + child.bottom);
        }

//        // 输出展示
//        System.out.println("----------Linear Layout-----------");
//        System.out.println("LinearLayout: measuredHeight = " + this.measuredHeight + "px, measuredWidth = " +
//                this.measuredWidth + "px.");
//        System.out.println("children cnt = " + this.getChildren().size());
//        for (int i = 0; i < this.getChildren().size(); i++) {
//            View child = this.getChildren().get(i);
////            System.out.println(child.getId());
//            System.out.println("child " + i + ": measuredHeight = " + child.measuredHeight + "px, measuredWidth = " +
//                    child.measuredWidth + "px.");
//            System.out.println("left: " + child.left + ", top: " + child.top + ", right: " + child.right + ", bottom: " + child.bottom);
//        }
//        System.out.println("-------------Linear Layout End-----------");
    }

    void locateChild(View child, int left, int right, int top, int bottom) {
//        System.out.println("locateChild");
        child.left = left;
        child.right = right;
        child.top = top;
        child.bottom = bottom;
    }


    public static class LayoutParams extends ViewGroup.LayoutParams { // public static
        public double Weight = 0;

        public LayoutParams() {
        }

        @Override
        public void setLayoutParams(HashMap<String, String> attrMap) {
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
            if (attrMap.containsKey("layout_weight")) {
                try {
                    Weight = Double.parseDouble(attrMap.get("layout_weight"));
                } catch (Exception e) {
                    System.err.println("can't parse layout weight: " + attrMap.get("layout_weight"));
                }
            }
        }
    }

}
