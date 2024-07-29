package view;

import tool.BugReporter;
import util.MeasureSpec;
import util.Spec;

import java.util.*;

public class RelativeLayout extends ViewGroup {

//    public  String view_type = "RelativeLayout";
    public static final int LEFT_OF                  = 0;
    public static final int RIGHT_OF                 = 1;
    public static final int ABOVE                    = 2;
    public static final int BELOW                    = 3;
    public static final int ALIGN_BASELINE           = 4;
    public static final int ALIGN_LEFT               = 5;
    public static final int ALIGN_TOP                = 6;
    public static final int ALIGN_RIGHT              = 7;
    public static final int ALIGN_BOTTOM             = 8;
    public static final int ALIGN_PARENT_LEFT        = 9;
    public static final int ALIGN_PARENT_TOP         = 10;
    public static final int ALIGN_PARENT_RIGHT       = 11;
    public static final int ALIGN_PARENT_BOTTOM      = 12;
    public static final int CENTER_IN_PARENT         = 13;
    public static final int CENTER_HORIZONTAL        = 14;
    public static final int CENTER_VERTICAL          = 15;
    public static final int START_OF                 = 16;
    public static final int END_OF                   = 17;
    public static final int ALIGN_START              = 18;
    public static final int ALIGN_END                = 19;
    public static final int ALIGN_PARENT_START       = 20;
    public static final int ALIGN_PARENT_END         = 21;
    private static final int VERB_COUNT              = 22;

    final static int VALUE_NOT_SET = Integer.MIN_VALUE;

    private static final int[] RULES_HORIZONTAL_PARENT = {
            ALIGN_PARENT_LEFT, ALIGN_PARENT_RIGHT, ALIGN_PARENT_START, ALIGN_PARENT_END, CENTER_HORIZONTAL
    };

    final static String [] HorizontalParentRules = {
            "layout_alignParentLeft", "layout_alignParentRight", "layout_alignParentStart", "layout_alignParentEnd", "layout_centerHorizontal"
    };

    private static final int[] RULES_VERTICAL = {
            ABOVE, BELOW, ALIGN_BASELINE, ALIGN_TOP, ALIGN_BOTTOM
    };
    final static String [] VerticalRules = {
            "layout_above", "layout_below", "layout_alignBaseline", "layout_alignTop", "layout_alignBottom"
    };

    private static final int[] RULES_HORIZONTAL = {
            LEFT_OF, RIGHT_OF, ALIGN_LEFT, ALIGN_RIGHT, START_OF, END_OF, ALIGN_START, ALIGN_END
    };

    final static String [] HorizontalRules = {
            "layout_toLeftOf", "layout_toRightOf", "layout_alignLeft", "layout_alignRight",
            "layout_toStartOf", "layout_toEndOf", "layout_alignStart", "layout_alignEnd"
    };

    private static final int[] RULES_VERTICAL_PARENT = {
            ALIGN_PARENT_TOP, ALIGN_PARENT_BOTTOM //, CENTER_VERTICAL
    };

    final static String [] VerticalParentRules = {
            "layout_alignParentTop", "layout_alignParentBottom", // "layout_centerVertical"
    };

    public RelativeLayout(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap){
//        super(layoutParams, attrMap);
        Children = new ArrayList<>();
        mLayoutParams = layoutParams;
        mLayoutParams.setLayoutParams(attrMap);
        initialBasicAttrs(attrMap);
        this.AttrMap = attrMap;
//        if(attrMap.isEmpty()){
//            System.err.println("RelativeLayout: empty attrMap");
//        }
    }

    HashMap<String, Integer> constructIdIndexMap(){
        HashMap<String, Integer> map = new HashMap<>();
        for (View child : this.Children) {
            if (child.Id != null && child.Id.length() > 0) {
                map.put(child.Id, child.index);
                System.out.println(child.Id + " "  + child.index);
                // System.out.println(child.index + " : " + child.getId());
            }
        }
        return map;
    }

    final static int PARENT_INDEX = -1;

    List<Integer> HCO; // child的水平创建顺序
    List<Integer> VCO; // child的垂直创建顺序


    /**
     * 构造垂直方向的组件依赖图 vertical dependency graph
     */
    public void constructHDG(){
        HashMap<String, Integer> IdMap = constructIdIndexMap(); //建立 id -> index 的映射
        for(View child : this.Children){
            // children之间的 水平方向上的依赖关系
            for(int h=0; h<HorizontalRules.length; h++){
                if(child.AttrMap == null){
                    System.err.println(child.getId());
                }
                if(child.AttrMap.containsKey(HorizontalRules[h])){
                    // 没有对id进行任何修改操作，@id/view_id
                    String target_id = child.AttrMap.get(HorizontalRules[h]);
                    int target_view_index = -2;
                    if(IdMap.containsKey(target_id)){
                        target_view_index = IdMap.get(target_id);
                    }else{
                        // 如果不存在该id，输出该问题
                        System.err.println("can't find " + target_id + " in " + this.AttrMap.get("isMerged"));
                        continue;
                    }
                    int relation = RULES_HORIZONTAL[h];
                    Rule rule = new Rule(child.index, target_view_index, relation, Rule.HORIZONTAL);
                    horizontalRules.add(rule);
                }
            }

            // child与parent之间的 水平方向上的依赖关系
            for (int p_h = 0; p_h < HorizontalParentRules.length; p_h++) {
                if(child.AttrMap.containsKey(HorizontalParentRules[p_h]) && // 如，layout_alignParentStart = “true”
                        child.AttrMap.get(HorizontalParentRules[p_h]).equals("true")){
                    int relation = RULES_HORIZONTAL_PARENT[p_h];
                    Rule rule = new Rule(child.index, PARENT_INDEX, relation, Rule.HORIZONTAL);
                    horizontalRules.add(rule);
                }
            }

        }
        HCO = sortChilren(horizontalRules, Children.size());
    }

    List<Rule> horizontalRules = new ArrayList<>();
    List<Rule> verticalRules = new ArrayList<>();


    /**
     * 构造水平方向组件依赖图 horizontal dependency graph
     */
    public void constructVDG(){
        HashMap<String, Integer> IdMap = constructIdIndexMap(); //建立 id -> index 的映射
        for(View child : this.Children){
            // children之间的 垂直方向上的依赖关系
            for(int v=0; v < VerticalRules.length; v++){
                if(child.AttrMap.containsKey(VerticalRules[v])){
                    String target_id = child.AttrMap.get(VerticalRules[v]);
                    int target_view_index = -2;
                    if(IdMap.containsKey(target_id)){
                        target_view_index = IdMap.get(target_id);
                    }else{
                        // 找不到被依赖的组件id时，报错；加入结果表单
                        System.err.println("can't find " + target_id + " in " + this.xmlFileName);
                        continue;
                    }
                    int relation = RULES_VERTICAL[v];
//                    Edge edge = new Edge(target_view_index, relation);
//                    edge_list.add(edge);
                    Rule rule = new Rule(child.index, target_view_index, relation, Rule.VERTICAL);
                    verticalRules.add(rule);
                }
            }

            // child与parent之间的 垂直方向上的依赖关系
            for (int p_h = 0; p_h < VerticalParentRules.length; p_h++) {
                if(child.AttrMap.containsKey(VerticalParentRules[p_h]) && // layout_alignParentTop = "true"
                        child.AttrMap.get(VerticalParentRules[p_h]).equals("true")){
                    int relation = RULES_VERTICAL_PARENT[p_h];

                    Rule rule = new Rule(child.index, PARENT_INDEX, relation, Rule.VERTICAL);
                    verticalRules.add(rule);
                }
            }
        }
        VCO = sortChilren(verticalRules, Children.size());
    }

    static List<Integer> sortChilren(List<Rule> rules, int childrenCNT){
        List<Integer> res = new ArrayList<>();
        HashMap<Integer, Integer> inDegree = new HashMap<>();
        System.out.println(childrenCNT);
        for (int i = 0; i<childrenCNT; i++) {
            inDegree.put(i, 0);
        }
        for(Rule rule : rules){
            if(rule.target != PARENT_INDEX){
                System.out.println(rule.start + " " + rule.target + " " + rule.relation);
                inDegree.put(rule.target, inDegree.get(rule.target) + 1);
            }
        }
        Queue<Integer> queue = new LinkedList<>();
        for (int node : inDegree.keySet()) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }
        while (!queue.isEmpty()) {
            int currentNode = queue.poll();
            res.add(0, currentNode); // 从列表头插入数据（省略逆序步骤）

            // Reduce the in-degree of each neighbor by 1
            for(Rule rule : rules){
                if(rule.start == currentNode && rule.target != PARENT_INDEX){
                    inDegree.put(rule.target, inDegree.get(rule.target) - 1);
                    if(inDegree.get(rule.target) == 0){
                        queue.add(rule.target);
                    }
                }
            }
        }
        if (res.size() != childrenCNT ) { // -1：为了减少PARENT
            System.err.println(res.size() + " " + childrenCNT);
            System.err.println(res + "; " + childrenCNT);
            throw new IllegalArgumentException("The graph has a cycle, so topological sorting is not possible.");
        }
        return res;
    }

//    static int relativeLayoutMeasureCNT = 0;

    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize) {
//        System.out.println("relativeLayoutMeasureCNT = " + ++relativeLayoutMeasureCNT);
        System.out.println("------------start relativeLayout measure: " + this.Id + "------------");
//        System.out.println("WidthMeasureSpecSize = " + WidthMeasureSpecSize);
//        System.out.println("HeightMeasureSpecSize = " + HeightMeasureSpecSize);

        constructHDG();
        System.out.println("HCO : " + HCO);
        constructVDG();
        System.out.println("VCO : " + VCO);

        int right = 0;
        int bottom = 0;
        // 水平方向
        System.out.println("HORIZONTAL");
        for(int index : HCO){
//            System.out.println("HORIZONTAL");
            System.out.println("---------------child index " + index + "-------------------");
            View child = this.getChildren().get(index);
            LayoutParams lp = (LayoutParams) child.mLayoutParams;
//            List<Rule> rules = new ArrayList<>(); // child index 相关的水品规则
            boolean lFlag = false;
            boolean rFlag = false;

            int wpadding = this.paddingLeft + this.paddingRight + lp.leftMargin + lp.rightMargin;
            Spec wSpec = MeasureSpec.getChildMeasureSpec(WidthMeasureSpecMode, WidthMeasureSpecSize, wpadding, lp.width);
//            if(lp.width == LayoutParams.MATCH_PARENT){
//                horizontalRules.add(new Rule(index, PARENT_INDEX, ALIGN_PARENT_END, Rule.HORIZONTAL));
//            }
            int wSpecMode = wSpec.mode;
            int wSpecSize = wSpec.size;

//            System.out.println("wSpecMode = " + wSpecMode + ", wSpecSize = " + wSpecSize);

            int hpadding = this.paddingTop + this.paddingBottom + lp.topMargin + lp.bottomMargin;
            Spec hSpec = MeasureSpec.getChildMeasureSpec(HeightMeasureSpecMode, HeightMeasureSpecSize, hpadding, lp.height);
//            if(lp.height == LayoutParams.MATCH_PARENT){
//                verticalRules.add(new Rule(index, PARENT_INDEX, ALIGN_PARENT_BOTTOM, Rule.VERTICAL));
//            }
            int hSpecMode = hSpec.mode;
            int hSpecSize = hSpec.size;

            for(Rule rule : horizontalRules){
                if(rule.start == index){
//                    rules.add(rule);
                    Boundary bound = getBounds(rule, WidthMeasureSpecSize, HeightMeasureSpecSize);
                    // TODO: 2024/7/8 右端点 align_parent_right > align_right > to_left_of 优先级处理
                    if(bound.val == Boundary.ERROR_CODE){
//                        System.out.println("target child index = " + rule.target + " dependency = " + rule.relation);
                        System.err.println("at 1 invalid dependency in RelativeLayout " + this.Id + ": child index = " + child.index + ", target child index = " + rule.target + ", dependency = " + rule.relation);
                        continue;
                    }
                    if(bound.dir == Boundary.LEFT){
                        lp.bLeft = bound.val;
//                        System.out.println("lp.bLeft: " + lp.bLeft);
                        lFlag = true;
                    } else if (bound.dir == Boundary.RIGHT) {
                        lp.bRight = bound.val;
//                        System.out.println("lp.bRight = " + lp.bRight);
                        rFlag = true;
                    }else{ // 不是水平方向
                        System.err.println("Wrong direction");
                    }
                }
            }


            if(lFlag){ // 左边界有限制
                if(rFlag){ // 右边界有限制
                    wSpecMode = MeasureSpec.EXACTLY;
                    wSpecSize = lp.bRight - lp.bLeft - lp.leftMargin - lp.rightMargin;
//                    System.out.println("wSpecSize: " + wSpecSize);
                }else{ // 右边界没有限制
                    if(wSpecMode == MeasureSpec.AT_MOST || lp.width == LayoutParams.MATCH_PARENT){
                        wSpecSize = WidthMeasureSpecSize - lp.bLeft - lp.leftMargin - lp.rightMargin - this.paddingRight;
//                        System.out.println("lp.bLeft: " + lp.bLeft + ", wSpecSize: " + wSpecSize);
                    }
                }
            }else{ // 左边界没有限制
                if(rFlag){ // 右边界有限制
                    if(wSpecMode == MeasureSpec.AT_MOST || lp.width == LayoutParams.MATCH_PARENT){
                        wSpecSize = lp.bRight - lp.rightMargin - lp.leftMargin - this.paddingLeft;
//                        System.out.println("lp.bRight = " + lp.bRight);
//                        System.out.println("test wSpecSize: " + wSpecSize);
                    }
//                    else{
//                        lp.bLeft = lp.bRight - wSpecSize;
//                    }
                }
            }
            lp.wSpecMode = wSpecMode;
            lp.wSpecSize = wSpecSize;

            // measure 1
            measureChild(child, wSpecMode, wSpecSize, hSpecMode, hSpecSize);
//            System.out.println("child.onMeasure lp.wSpecSize = " + lp.wSpecSize);

            if(lFlag){ // 左边界有限制
                child.left = lp.bLeft + lp.leftMargin;
                if(rFlag){ // 右边界有限制
                    child.right = lp.bRight - lp.rightMargin;
                }else{ // 右边界没有限制
                    child.right = child.left + child.measuredWidth;
                }
            }else{ // 左边界没有限制
                if(rFlag){ // 右边界有限制
                    child.right = lp.bRight - lp.rightMargin;
                    child.left = child.right - child.measuredWidth;
                }else{
                    child.left = this.paddingLeft + lp.leftMargin;
                    child.right = child.left + child.measuredWidth;
                }
            }

            child.top = this.paddingTop + lp.topMargin;
            child.bottom = child.top + child.measuredHeight;
//            System.out.println("measuredWidth: " + child.measuredWidth);
        }


        System.out.println("VERTICAL");
        for(int index : VCO){
//            System.out.println("VERTICAL");
            System.out.println("---------------child index " + index + "-------------------");
            View child = this.getChildren().get(index);
            System.out.println(child.getId());
            LayoutParams lp = (LayoutParams) child.mLayoutParams;

            boolean tFlag = false;
            boolean bFlag = false;
            for(Rule rule : verticalRules){
                if(rule.start == index){
//                    rules.add(rule);
                    Boundary bound = getBounds(rule, WidthMeasureSpecSize, HeightMeasureSpecSize);
                    if(bound.val == Boundary.ERROR_CODE){
                        System.err.println(rule.target + " " + rule.start + " "  + rule.relation);
                        System.err.println("invalid dependency in RelativeLayout : " + this.Id);
                        continue;
                    }
                    if(bound.dir == Boundary.TOP){
                        lp.bTop = bound.val;
                        tFlag = true;
                    } else if (bound.dir == Boundary.BOTTOM) {
                        lp.bBottom = bound.val;
                        bFlag = true;
                    }else{ // 不是垂直方向
                        System.err.println("Wrong direction");
                    }
                }
            }

            int padding = this.paddingTop + this.paddingBottom + lp.topMargin + lp.bottomMargin;
            Spec hSpec = MeasureSpec.getChildMeasureSpec(HeightMeasureSpecMode, HeightMeasureSpecSize, padding, lp.height);
            int hSpecMode = hSpec.mode;
            int hSpecSize = hSpec.size;
            
            if(tFlag){ // 确定上边界
                if(bFlag){
                    hSpecMode = MeasureSpec.EXACTLY;
                    hSpecSize = lp.bBottom - lp.bottomMargin - lp.bTop - lp.topMargin;
                }else{
                    if(hSpecMode == MeasureSpec.AT_MOST || lp.height == LayoutParams.MATCH_PARENT){
                        hSpecSize = HeightMeasureSpecSize - lp.bTop - lp.topMargin - lp.bottomMargin - this.paddingBottom;
                    }

                }
            }else{
                if(bFlag){ // 下边界有限制
                    if(hSpecMode == MeasureSpec.AT_MOST || lp.height == LayoutParams.MATCH_PARENT){
                        hSpecSize = lp.bBottom - lp.bottomMargin - lp.topMargin - this.paddingTop;
                    }
//                    else{
//                        lp.bLeft = lp.bRight - hSpecSize;
//                    }
                }
            }

            lp.hSpecMode = hSpecMode;
            lp.hSpecSize = hSpecSize;

            child.onMeasure(lp.wSpecMode, lp.wSpecSize, lp.hSpecMode, lp.hSpecSize);
//            System.out.println("index: " + index + ", Measure 2, " + lp.wSpecSize + ", " + lp.hSpecSize);

            if(tFlag){ // 确定上边界
                child.top = lp.bTop + lp.topMargin;
//                System.out.println("fixed Top" + child.top);
                if(bFlag){
                    child.bottom = lp.bBottom - lp.bottomMargin;
                }else{
                    child.bottom = child.top + child.measuredHeight;
                }
            }else{
                if(bFlag){ // 右边界有限制
                    child.bottom = lp.bBottom - lp.bottomMargin;
                    child.top = child.bottom - child.measuredHeight;
                }else{
                    child.top = this.paddingTop + lp.topMargin;
                    child.bottom = child.top + child.measuredHeight;
                }
            }

//            System.out.println("child " + index + ": measured width = " + child.measuredWidth + ", measured height = " + child.measuredHeight);
//            System.out.println("left: " + child.left + "; top: " + child.top + "; right: " + child.right + "; bottom: " + child.bottom);

            if(right < child.right) right = child.right;
            if(bottom < child.bottom) bottom = child.bottom;
        }

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

        System.out.println("relative layout measured width = " + measuredWidth + "; measured height = " + measuredHeight);
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
                case ALIGN_PARENT_LEFT, ALIGN_PARENT_START -> {
//                    System.out.println("ALIGN_PARENT_START");
                    val = this.paddingLeft;
                    dir = Boundary.LEFT;
                }
                case ALIGN_PARENT_TOP -> {
                    val = this.paddingTop;
                    dir = Boundary.TOP;
                }

                case ALIGN_PARENT_RIGHT, ALIGN_PARENT_END -> {
                    val = WidthMeasureSpecSize - this.paddingRight;
                    dir = Boundary.RIGHT;
                }
                case ALIGN_PARENT_BOTTOM -> {
                    val = HeightMeasureSpecSize - this.paddingBottom;
                    dir = Boundary.BOTTOM;
                }
            }
        }else{ // 这里已经处理过Margin
            View target_view = this.getChildren().get(target);
            LayoutParams lp = (LayoutParams) target_view.mLayoutParams; // todo 类型是否需要改变
            switch (rel){
                case ALIGN_LEFT -> {
                    val = target_view.left;
//                    val = lp.bLeft;
                    dir = Boundary.LEFT;
                }
                case LEFT_OF, START_OF ->{
                    val = target_view.left + lp.leftMargin;
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
                    val = target_view.top + lp.topMargin;
                    dir = Boundary.BOTTOM;
                }

                case ALIGN_RIGHT -> {
                    val = target_view.right;
//                    val = lp.bRight;
                    dir = Boundary.RIGHT;
                }
                case RIGHT_OF, END_OF -> {
                    val = target_view.right + lp.rightMargin;
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
                    val = target_view.bottom + lp.bottomMargin;
                    dir = Boundary.TOP;
                }
            }
        }
        return new Boundary(dir, val);
    }



    @Override
    void measureChild(View child, int wSpecMode, int wSpecSize, int hSpecMode, int hSpecSize) {
        super.measureChild(child, wSpecMode, wSpecSize, hSpecMode, hSpecSize);
    }

    public void printClassName(){
        System.out.println("RelativeLayout");
    }

//    static int overlapcheckCnt = 0;
    void overLappingCheck(View child1, View child2){
//        System.out.println("overlapcheckCnt = " + overlapcheckCnt++);
        // default settings
        int [] bounds1 = child1.getBounds();
        int [] bounds2 = child2.getBounds();

        // content bounds
        int cl1 = bounds1[0] + child1.paddingLeft;
        int ct1 = bounds1[1] + child1.paddingTop;
        int cr1 = bounds1[2] - child1.paddingRight;
        int cb1 = bounds1[3] - child1.paddingBottom;
        int cl2 = bounds2[0] + child2.paddingLeft;
        int ct2 = bounds2[1] + child2.paddingTop;
        int cr2 = bounds2[2] - child2.paddingRight;
        int cb2 = bounds2[3] - child2.paddingBottom;

        boolean default_view_overlap = isOverlapping(bounds1[0], bounds1[1], bounds1[2], bounds1[3], bounds2[0], bounds2[1], bounds2[2], bounds2[3]);
        boolean default_content_overlap = isOverlapping(cl1, ct1, cr1, cb1, cl2, ct2, cr2, cb2);

//        if(default_content_overlap) return;

        // scaling settings
        int l1 = child1.left;
        int r1 = child1.right;
        int t1 = child1.top;
        int b1 = child1.bottom;

        int l2 = child2.left;
        int r2 = child2.right;
        int t2 = child2.top;
        int b2 = child2.bottom;

        int scl1 = child1.left + child1.paddingLeft;
        int scr1 = child1.right - child1.paddingRight;
        int sct1 = child1.top + child1.paddingTop;
        int scb1 = child1.bottom - child1.paddingBottom;

        int scl2 = child2.left + child2.paddingLeft;
        int scr2 = child2.right - child2.paddingRight;
        int sct2 = child2.top + child2.paddingTop;
        int scb2 = child2.bottom - child2.paddingBottom;

        boolean view_overlap = isOverlapping(l1, t1, r1, b1, l2, t2, r2, b2);
        boolean content_overlap = isOverlapping(scl1, sct1, scr1, scb1, scl2, sct2, scr2, scb2);

        if(!default_view_overlap && view_overlap){
            BugReporter.writeViewBug(View.packageName, BugReporter.VIEW_OVERLAP, child1, child2);
            return;
        }

        if(!default_content_overlap && content_overlap){
            System.out.println(scl1 + " " + sct1 + " " + scr1 + " " + scb1);
            System.out.println(scl2 + " " + sct2 + " " + scr2 + " " + scb2);
            BugReporter.writeViewBug(View.packageName, BugReporter.VIEW_OVERLAP, child1, child2);
            return;
        }

        if((child1 instanceof ImageView || child1 instanceof TextualView) &&
                (child2 instanceof ImageView || child2 instanceof TextualView)){
            if(view_overlap) BugReporter.writeViewBug(View.packageName, BugReporter.VIEW_OVERLAP, child1, child2);
        }


    }


    private static boolean isOverlapping(int left1, int top1 , int right1, int bottom1, int left2, int top2, int right2, int bottom2) {

//        System.out.printf("%d, %d, %d, %d; %d, %d, %d, %d\t", left1, top1, right1, bottom1, left2, top2, right2, bottom2);

        // 如果一个矩形在另一个矩形的左边
        if (left1 >= right2 || left2 >= right1) {
//            System.out.println(false);
            return false;
        }
        // 如果一个矩形在另一个矩形的下边
        if (bottom1 <= top2 || bottom2 <= top1) {
//            System.out.println(false);
            return false;
        }
//        System.out.println(true);
        return true;
    }

//    static int checkCnt = 0;
    @Override
    public void checkView() {
//        System.out.println("checkCnt = " + checkCnt ++);
        super.checkView();
        int n = this.getChildren().size();
        for (int i = 0; i < n-1; i++) {
            View child1 = this.Children.get(i);
            if(child1 instanceof NormalView){
                continue;
            }
            for (int j = i+1; j < n; j++) {
                View child2 = this.Children.get(j);
                if(child2 instanceof NormalView){
                    continue;
                }
//                System.out.println(i + " " + j);
                overLappingCheck(child1, child2);
            }
        }
    }


    static class Boundary{
        final static int LEFT = 1;
        final static int TOP = 2;
        final static int RIGHT = 3;
        final static int BOTTOM = 4;

        final static int ERROR_CODE = Integer.MAX_VALUE - 1;
        final static int PARENT_BOUND = Integer.MAX_VALUE;

        int dir = 0;
        int val = Integer.MAX_VALUE;

        Boundary(int dir, int val){
            this.dir = dir;
            this.val = val;
        }

        public int getDir() {
            return dir;
        }

        public double getVal() {
            return val;
        }

    }

    static class Rule{
        int start;
        int target;
        int relation;
        boolean direction = HORIZONTAL;
        static boolean HORIZONTAL = true;
        static boolean VERTICAL = false;

        public Rule(int start, int target, int relation, boolean dir) {
            this.start = start;
            this.target = target;
            this.relation = relation;
            this.direction = dir;
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams{ // todo 是否还需要这个类？

        public void printClassName(){
            System.out.println("RelativeLayout.LayoutParams");
        }

        int bLeft = Integer.MIN_VALUE;
        int bTop = Integer.MIN_VALUE;
        int bRight = Integer.MAX_VALUE;
        int bBottom = Integer.MAX_VALUE;

        int wSpecSize = Integer.MIN_VALUE;
        int wSpecMode = MeasureSpec.UNSPECIFIED;
        int hSpecSize = Integer.MIN_VALUE;
        int hSpecMode = MeasureSpec.UNSPECIFIED;

    }

    public static void main(String [] args){
//        List<Rule> rules = new ArrayList<>();
//        Rule r1 = new Rule(0, 1, LEFT_OF, true);
//        rules.add(r1);
//
//        Rule r2 = new Rule(1, 2, LEFT_OF, true);
//        rules.add(r2);
//
//        Rule r3 = new Rule(2, -1, LEFT_OF, true);
//        rules.add(r3);
//
//        List<Integer> res = sortChilren(rules, 3);
//        System.out.println(res);

        System.out.println(isOverlapping(0, 84, 1440, 280, 224, 84, 1172, 457));
//        System.out.println(isOverlapping(483, 14, 1278, 272, 455, 147, 1278, 241));
    }

}
