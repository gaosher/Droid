package view;

import com.google.j2objc.annotations.ObjectiveCName;
import util.MeasureSpec;
import util.Spec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConstraintLayout extends RelativeLayout{

    public ConstraintLayout(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap) {
        super(layoutParams, attrMap);
    }

    private static final String [] HorizontalRules = {
            "layout_constraintStart_toStartOf" , "layout_constraintStart_toEndOf", "layout_constraintEnd_toStartOf", "layout_constraintEnd_toEndOf"
    };
    private static final int[] RULES_HORIZONTAL = {
//            LEFT_OF, RIGHT_OF, ALIGN_LEFT, ALIGN_RIGHT, START_OF, END_OF, ALIGN_START, ALIGN_END,
            ALIGN_START, END_OF, START_OF, ALIGN_END
    };


    private static final String [] VerticalRules = {
            "layout_constraintTop_toTopOf" , "layout_constraintTop_toBottomOf", "layout_constraintBottom_toTopOf", "layout_constraintBottom_toBottomOf"
    };
    private static final int[] RULES_VERTICAL = {
//            LEFT_OF, RIGHT_OF, ALIGN_LEFT, ALIGN_RIGHT, START_OF, END_OF, ALIGN_START, ALIGN_END,
            ALIGN_TOP, BELOW, ABOVE, ALIGN_BOTTOM
    };


    public void constructHDG(){
        HashMap<String, Integer> IdMap = constructIdIndexMap(); //建立 id -> index 的映射
        for(View child : this.Children){
            // children之间的 水平方向上的依赖关系
            for (int h=0; h<HorizontalRules.length; h++){
                if(child.AttrMap.containsKey(HorizontalRules[h])){
                    String target_id = child.AttrMap.get(HorizontalRules[h]);
                    int target_view_index = -2;
                    if(target_id.equals("parent")){// 参照物是父组件
                        target_view_index = PARENT_INDEX;
                    }else{
                        if(IdMap.containsKey(target_id)){
                            target_view_index = IdMap.get(target_id);
                        }else{
                            // 如果不存在该id，输出该问题
                            System.err.println("can't find " + target_id + " in " + this.AttrMap.get("isMerged"));
                        }
                    }
                    int relation = RULES_HORIZONTAL[h];
                    Rule rule = new Rule(child.index, target_view_index, relation, Rule.HORIZONTAL);
                    horizontalRules.add(rule);
                }
            }
        }
        HCO = sortChilren(horizontalRules, Children.size());
    }

    public void constructVDG(){
        HashMap<String, Integer> IdMap = constructIdIndexMap(); //建立 id -> index 的映射
        for(View child : this.Children) {
            // children之间的 垂直方向上的依赖关系
            for (int v = 0; v < VerticalRules.length; v++) {
                if (child.AttrMap.containsKey(VerticalRules[v])) {
                    String target_id = child.AttrMap.get(VerticalRules[v]);
                    int target_view_index = -2;
                    if (target_id.equals("parent")) {
                        target_view_index = PARENT_INDEX;
                    } else {
                        if (IdMap.containsKey(target_id)) {
                            target_view_index = IdMap.get(target_id);
                        } else {
                            // 找不到被依赖的组件id时，报错；加入结果表单
                            System.err.println("can't find " + target_id + " in " + this.AttrMap.get("isMerged"));
                        }
                    }
                    int relation = RULES_VERTICAL[v];
                    Rule rule = new Rule(child.index, target_view_index, relation, Rule.VERTICAL);
                    verticalRules.add(rule);
                }
            }
        }
        VCO = sortChilren(verticalRules, Children.size());
    }

    @Override
    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize) {
//        System.out.println("start constraint measure: " + this.Id);
//        System.out.println("WidthMeasureSpecSize = " + WidthMeasureSpecSize);
//        System.out.println("HeightMeasureSpecSize = " + HeightMeasureSpecSize);
        constructHDG();
        System.out.println("HCO : " + HCO);
        constructVDG();
        System.out.println("VCO : " + VCO);


        for(View child : this.Children){
            measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
//            System.out.println("measureChild: " + child.measuredWidth + " " + child.measuredHeight);
        }
        for(int index : HCO){
            System.out.println("HORIZONTAL LOCATION");
            System.out.println("---------------child index " + index + "-------------------");
            View child = this.getChildren().get(index);
            LayoutParams lp = (LayoutParams) child.mLayoutParams;
            int l = this.paddingLeft + lp.leftMargin, r = WidthMeasureSpecSize - this.paddingRight - lp.rightMargin;
            boolean lFlag = false;
            boolean rFlag = false;
            for(Rule rule : horizontalRules){
                if(rule.start == index){
//                    rules.add(rule);
                    Boundary bound = getBounds(rule, WidthMeasureSpecSize, HeightMeasureSpecSize);
                    if(bound.val == Boundary.ERROR_CODE){
                        System.out.println("target child index = " + rule.target + " dependency = " + rule.relation);
                        System.err.println("at 1 invalid dependency in RelativeLayout " + this.Id + ": child index = " + child.index + ", target child index = " + rule.target + ", dependency = " + rule.relation);
                        continue;
                    }
                    if(bound.dir == Boundary.LEFT){
                        l = bound.val + lp.leftMargin;
//                        System.out.println("lp.bLeft: " + lp.bLeft);
                        lFlag = true;
                    } else if (bound.dir == Boundary.RIGHT) {
                        r = bound.val - lp.rightMargin;
//                        System.out.println("r = " + r);
//                        System.out.println("lp.bRight = " + lp.bRight);
                        rFlag = true;
                    }else{ // 不是水平方向
                        System.err.println("Wrong direction");
                    }
                }
            }

            if(lFlag){ // 有左侧的限制
                if(rFlag){ // 有右侧限制，组件中线与之对齐
//                    System.out.println("l = " + l + "; r = " + r);
                    double mid = (l + r) * 0.5;
                    child.left = (int) (mid - child.measuredWidth * 0.5);
                    child.right = child.left + child.measuredWidth;
//                    System.out.println("mid = " + mid + " left = " + child.left + " right = " + child.right + "measureWidth = " + child.measuredWidth);
                }else{
                    child.left = l;
                    child.right = l + child.measuredWidth;
                }
            }else {
                if(rFlag){ // 有右侧限制
                    child.right = r;
                    child.left = r - child.measuredWidth;
//                    System.out.println("有右侧限制，组件中线与之对齐 child " + child.left + " " + child.right + " " + measuredWidth);
                }else{
                    System.err.println("No Horizontal Constraints");
                    child.left = l;
                    child.right = l + child.measuredWidth;
                }
            }

//            System.out.println("child " + child.index + ": " + child.left + " " + child.right + "; measured width = " + child.measuredWidth);
        }

        for(int index : VCO) {
            System.out.println("VERTICAL");
            System.out.println("---------------child index " + index + "-------------------");
            View child = this.getChildren().get(index);
//            System.out.println(child.getId());
            LayoutParams lp = (LayoutParams) child.mLayoutParams;
            int t = this.paddingTop + lp.topMargin, b = HeightMeasureSpecSize - this.paddingBottom - lp.bottomMargin;
            boolean tFlag = false;
            boolean bFlag = false;
            for (Rule rule : verticalRules) {
                if (rule.start == index) {
//                    rules.add(rule);
                    Boundary bound = getBounds(rule, WidthMeasureSpecSize, HeightMeasureSpecSize);
                    if (bound.val == Boundary.ERROR_CODE) {
                        System.err.println(rule.target + " " + rule.start + " " + rule.relation);
                        System.err.println("invalid dependency in RelativeLayout : " + this.Id);
                        continue;
                    }
                    if (bound.dir == Boundary.TOP) {
                        t = bound.val + lp.topMargin;
                        tFlag = true;
                    } else if (bound.dir == Boundary.BOTTOM) {
                        b = bound.val - lp.bottomMargin;
                        bFlag = true;
                    } else { // 不是垂直方向
                        System.err.println("Wrong direction");
                    }
                }
            }

            if(tFlag){
                if(bFlag){
                    double mid = (t + b) * 0.5;
                    child.top = (int) (mid - child.measuredHeight * 0.5);
                    child.bottom = child.top + child.measuredHeight;
                }else{
                    child.top = t;
                    child.bottom = t + child.measuredHeight;
                }
            }else{
                if(bFlag){
                    child.bottom = b;
                    child.top = b - child.measuredHeight;
                }else{
                    System.out.println("No Vertical constraints");
                    child.top = t;
                    child.bottom = t + child.measuredHeight;
                }
            }
//            System.out.println("child " + child.index + ": " +  child.top + " " + child.bottom + " " + child.measuredHeight);
        }


//        for(View child : this.Children){
//            System.out.println("child " + child.index + ": " + child.left + " " + child.top + " " + child.right + " " + child.bottom);
//        }


        if(HeightMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredHeight = HeightMeasureSpecSize;
        }else{
            this.measuredHeight = Math.min(HeightMeasureSpecSize, bottom + this.paddingBottom);
        }
        if(WidthMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredWidth = WidthMeasureSpecSize;
        }else {
            this.measuredWidth = Math.min(WidthMeasureSpecSize, right + this.paddingRight);
        }
//
//        System.out.println("relative layout measured width = " + measuredWidth + "; measured height = " + measuredHeight);
    }

    /**
     * @param rule
     * @return 返回边界，值
     */
    Boundary getBounds(Rule rule,  int WidthMeasureSpecSize,  int HeightMeasureSpecSize){
        int target = rule.target;
        int rel = rule.relation;
        int val = Boundary.ERROR_CODE;
        int dir = 0;
        // todo 考虑padding/margin
        if(target == -1){
            switch (rel){
                case ALIGN_START -> {
//                    System.out.println("ALIGN_PARENT_START");
                    val = this.paddingLeft;
                    dir = Boundary.LEFT;
                }
                case ALIGN_END -> {
                    val = WidthMeasureSpecSize - this.paddingRight;
                    dir = Boundary.RIGHT;
                }

                case LEFT_OF, START_OF -> {
                    val = 0;
                    dir = Boundary.RIGHT;
                }
                case RIGHT_OF, END_OF -> {
                    val = WidthMeasureSpecSize;
                    dir = Boundary.LEFT;
                }

                case ALIGN_TOP -> {
                    val = this.paddingTop;
                    dir = Boundary.TOP;
                }
                case ALIGN_BOTTOM -> {
                    val = HeightMeasureSpecSize - this.paddingBottom;
                    dir = Boundary.BOTTOM;
                }
                case ABOVE -> {
                    val = 0;
                    dir = Boundary.BOTTOM;
                }
                case BELOW -> {
                    val = HeightMeasureSpecSize;
                    dir = Boundary.TOP;
                }
            }
        }else{ // 这里已经处理过Margin
            View target_view = this.getChildren().get(target);
            LayoutParams lp = (LayoutParams) target_view.mLayoutParams; // todo 类型是否需要改变
            switch (rel){
                case ALIGN_LEFT, ALIGN_START -> {
//                    System.out.println("align left");
                    val = target_view.left;
//                    val = lp.bLeft;
                    dir = Boundary.LEFT;
                }
                case LEFT_OF, START_OF ->{
                    val = target_view.left;
//                    System.out.println("LEFT_OF");
//                    System.out.println("to_left_of " + val);
//                    val = lp.bLeft + lp.leftMargin;
                    dir = Boundary.RIGHT;
                }
                case ALIGN_TOP -> {
                    val = target_view.top;
                    dir = Boundary.TOP;
                }
                case ABOVE-> {
                    val = target_view.top;
                    dir = Boundary.BOTTOM;
                }

                case ALIGN_RIGHT, ALIGN_END -> {
//                    System.out.println("align right");
                    val = target_view.right;
//                    val = lp.bRight;
                    dir = Boundary.RIGHT;
                }
                case RIGHT_OF, END_OF -> {
                    val = target_view.right;
//                    System.out.println("END_OF");
//                    System.out.println("to_right_of " + val);
//                    val = lp.bRight + lp.rightMargin;
                    dir = Boundary.LEFT;
                }
                case ALIGN_BOTTOM-> {
                    val = target_view.bottom;
                    dir = Boundary.BOTTOM;
                }
                case BELOW -> {
                    val = target_view.bottom;
                    dir = Boundary.TOP;
                }
            }
        }
//        System.out.println("val = " + val);
        return new Boundary(dir, val);
    }



    public static class LayoutParams extends RelativeLayout.LayoutParams{
        double Horizontal_bias = 0.5;
        double Vertical_bias = 0.5;
        @Override
        public void setLayoutParams(HashMap<String, String> attrMap){
            super.setLayoutParams(attrMap);
            if(attrMap.containsKey("layout_constraintHorizontal_bias")){
                this.Horizontal_bias = Double.parseDouble(attrMap.get("layout_constraintHorizontal_bias"));
                attrMap.remove("layout_constraintHorizontal_bias");
            }
            if(attrMap.containsKey("layout_constraintVertical_bias")){
                this.Horizontal_bias = Double.parseDouble(attrMap.get("layout_constraintVertical_bias"));
                attrMap.remove("layout_constraintVertical_bias");
            }
        }

    }
}
