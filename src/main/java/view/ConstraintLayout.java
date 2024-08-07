package view;

import android.renderscript.Script;
import android.text.Layout;
import com.google.j2objc.annotations.ObjectiveCName;
import fj.data.Array;
import soot.AntTask;
import util.MeasureSpec;
import util.Spec;

import java.util.*;

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

    double vertical_weight_sum = 0;
    double horizontal_weight_sum = 0;


    public void constructHDG(){
        HashMap<String, Integer> IdMap = constructIdIndexMap(); //建立 id -> index 的映射
        for(View child : this.Children){
            if(child.isNormal) continue;
            // children之间的 水平方向上的依赖关系
            for (int h=0; h<HorizontalRules.length; h++){
                if(child.AttrMap==null) System.out.println(this.getId());
                System.out.println(child.index);
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
//        HCO = sortChilren(horizontalRules, Children.size());
        detectHorizontalChains(horizontalRules);
        HCO = sortChildren(horizontalRules, horizontal_chains, Children.size());
        System.out.println("horizontal chain cnt = " + horizontal_chains.size());
    }

    public void constructVDG(){
        HashMap<String, Integer> IdMap = constructIdIndexMap(); //建立 id -> index 的映射
        for(View child : this.Children) {
            if(child.isNormal) continue;
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
//        VCO = sortChilren(verticalRules, Children.size());
        detectVerticalChains(verticalRules);
        VCO = sortChildren(verticalRules, vertical_chains, Children.size());
        System.out.println("vertical chain cnt = " + vertical_chains.size());
    }
    List<Chain> horizontal_chains = new ArrayList<>();
    void detectHorizontalChains(List<Rule> all_rules) {
        // 用来保存图结构的邻接表
        Map<Integer, List<Rule>> graph = new HashMap<>();

        // 构建邻接表和反向邻接表
        for (Rule rule : all_rules) {
            graph.putIfAbsent(rule.start, new ArrayList<>());
            graph.putIfAbsent(rule.target, new ArrayList<>());
            graph.get(rule.start).add(rule);
        }

        // 记录已访问的节点
        Map<Integer, Boolean> visited = new HashMap<>();
        for (int node : graph.keySet()) {
            if (visited.getOrDefault(node, false)) {
                continue;
            }
            visited.put(node, true);
            Chain chain = null;
            List<Rule> rules = graph.get(node);
            List<Rule> rules_to_remove = new ArrayList<>();
            for(Rule rule : rules) {
                if (rule.relation == START_OF || rule.relation == LEFT_OF) {
                    int next_node = rule.target;
                    int current_node = node;
                    boolean hasChain = false;
                    boolean continue_search = true;
                    Rule cur_next_shared_edge = rule;
                    while(continue_search){
                        Rule r1 = new Rule(next_node, current_node, END_OF, Rule.HORIZONTAL);
                        Rule r2 = new Rule(next_node, current_node, RIGHT_OF, Rule.HORIZONTAL);
                        Rule next_cur_shared_edge = ruleIsInList(graph.get(next_node), r1);
                        if(next_cur_shared_edge == null)
                            next_cur_shared_edge = ruleIsInList(graph.get(next_node), r2);
                        if(next_cur_shared_edge != null){
                            if(hasChain){
                                chain.views_in_chain.add(next_node);
                            }else {
                                // 找到了两个共边的组件，放入chain中
                                chain = new Chain(Chain.HORIZONTAL);
                                chain.views_in_chain.add(current_node);
                                chain.views_in_chain.add(next_node);
                                hasChain = true;
                                // 获取chain start rule:
                                chain.setStartRule(Chain.getStartRule(rules, Chain.HORIZONTAL));
                                rules_to_remove.add(chain.start_rule);
                            }
                            // 去掉不需要的rules
                            rules_to_remove.add(cur_next_shared_edge);
                            rules_to_remove.add(next_cur_shared_edge);
                            // 更新visited
                            visited.put(next_node, true);
                        }else {
                            break;
                        }
                        boolean hasNext = false;
                        for (Rule rule1 : graph.get(next_node)){
                            if(rule1.relation == START_OF || rule1.relation == LEFT_OF){
                                current_node = next_node;
                                next_node = rule1.target;
                                hasNext = true;
                                cur_next_shared_edge = rule1;
                                break;
                            }
                        }
                        if(!hasNext){
                            chain.setEndRule(Chain.getEndRule(graph.get(next_node), Chain.HORIZONTAL));
                            rules_to_remove.add(chain.end_rule);
                            continue_search = false;
                        }
                    }
                }
            }

            // 将找到的链加入到链表中
            if (chain != null) {
                // initial chain style
                for(int index : chain.views_in_chain){
                    View child_in_chain = this.Children.get(index);
                    if(child_in_chain.AttrMap.containsKey("layout_constraintHorizontal_chainStyle")){
                        chain.setChainStyle(child_in_chain.AttrMap.get("layout_constraintHorizontal_chainStyle"));
                        child_in_chain.AttrMap.remove("layout_constraintHorizontal_chainStyle");
                        break;
                    }
                }
                // initial vertical weight_sum
                for(int index : chain.views_in_chain){
                    View child_in_chain = this.Children.get(index);
                    LayoutParams lp = (LayoutParams) child_in_chain.mLayoutParams;
                    chain.weight_sum += lp.Horizontal_weight;
                }

                // initial independence
//                chain.setIndependent();
                horizontal_chains.add(chain);

                for(Rule rule : rules_to_remove){
                    all_rules.remove(rule);
                }

                int start_target = chain.start_rule.target;
                int start_relation = chain.start_rule.relation;
                Rule start_rule = new Rule(chain.chain_index, start_target, start_relation, chain.direction);

                int end_target = chain.end_rule.target;
                int end_relation = chain.end_rule.relation;
                Rule end_rule = new Rule(chain.chain_index, end_target, end_relation, chain.direction);

                all_rules.add(start_rule);
                all_rules.add(end_rule);
            }
        }
        for (Chain chain : horizontal_chains) {
            chain.showChain();
        }
    }

    List<Chain> vertical_chains = new ArrayList<>();
    void detectVerticalChains(List<Rule> all_rules) {
        // 用来保存图结构的邻接表
        Map<Integer, List<Rule>> graph = new HashMap<>();

        // 构建邻接表
        for (Rule rule : all_rules) {
            graph.putIfAbsent(rule.start, new ArrayList<>());
            graph.putIfAbsent(rule.target, new ArrayList<>());
            graph.get(rule.start).add(rule);
        }
        // 记录已访问的节点
        Map<Integer, Boolean> visited = new HashMap<>();
        List<Integer> visit_list = new ArrayList<>();
        for (int node : graph.keySet()) {
            if (visited.getOrDefault(node, false)) {
                continue;
            }
            visited.put(node, true);
            visit_list.add(node);

            Chain chain = null;
            List<Rule> rules = graph.get(node);
            List<Rule> rules_to_remove = new ArrayList<>();
            for(Rule rule : rules) {
                if (rule.relation == ABOVE) {
                    int current_node = node;
                    int next_node = rule.target;
                    boolean continue_search = true;
                    boolean hasChain = false;
                    Rule cur_next_shared_edge = rule;

                    while(continue_search){
                        // next——node 与 current——node 有共边
                        Rule r1 = new Rule(next_node, current_node, BELOW, Rule.VERTICAL);
                        Rule next_cur_shared_edge = ruleIsInList(graph.get(next_node), r1);
                        if(next_cur_shared_edge != null){
                            if(hasChain){
                                chain.views_in_chain.add(next_node);
                            }else {
                                // 找到了两个共边的组件，放入chain中
                                chain = new Chain(Chain.VERTICAL);
                                chain.views_in_chain.add(current_node);
                                chain.views_in_chain.add(next_node);
                                hasChain = true;
                                // 获取chain start rule: align_top / below
                                chain.setStartRule(Chain.getStartRule(rules, chain.direction));
                                rules_to_remove.add(chain.start_rule);
                            }
                            // 去掉不需要的rules
                            rules_to_remove.add(cur_next_shared_edge);
                            rules_to_remove.add(next_cur_shared_edge);
                            // 更新visited
                            visited.put(next_node, true);
                            System.out.println("next node in chain: " + next_node);
//                            System.out.println("visited " + next_node);
                        }else { // next——node 与 current——node 没有共边
                            break;
                        }

                        // 继续检查 next——node 是否还有下一个可能共边的组件
                        boolean hasNext = false;
                        for (Rule rule1 : graph.get(next_node)){
                            // 如果有
                            if(rule1.relation == ABOVE) {
                                current_node = next_node;
                                next_node = rule1.target;
                                hasNext = true;
                                cur_next_shared_edge = rule1;
                                break;
                            }
                        }
                        // 如果没有，那就结束
                        if(!hasNext){
                            chain.setEndRule(Chain.getEndRule(graph.get(next_node), Chain.VERTICAL));
                            rules_to_remove.add(chain.end_rule);
                            continue_search = false;
                        }
                    }
                }
            }

            // 解析链，并将找到的链加入到链表中
            if (chain != null) {
                // initial chain style
                for(int index : chain.views_in_chain){
                    View child_in_chain = this.Children.get(index);
                    if(child_in_chain.AttrMap.containsKey("layout_constraintVertical_chainStyle")){
                        chain.setChainStyle(child_in_chain.AttrMap.get("layout_constraintVertical_chainStyle"));
                        child_in_chain.AttrMap.remove("layout_constraintVertical_chainStyle");
                        break;
                    }
                }
                // initial vertical weight_sum
                for(int index : chain.views_in_chain){
                    View child_in_chain = this.Children.get(index);
                    LayoutParams lp = (LayoutParams) child_in_chain.mLayoutParams;
                    chain.weight_sum += lp.Vertical_weight;
                }

                // initial independence
//                chain.setIndependent();
                vertical_chains.add(chain);

                // 去掉不需要的rules
                for(Rule rule : rules_to_remove){
                    all_rules.remove(rule);
                }

                int start_target = chain.start_rule.target;
                int start_relation = chain.start_rule.relation;
                Rule start_rule = new Rule(chain.chain_index, start_target, start_relation, chain.direction);

                int end_target = chain.end_rule.target;
                int end_relation = chain.end_rule.relation;
                Rule end_rule = new Rule(chain.chain_index, end_target, end_relation, chain.direction);

                all_rules.add(start_rule);
                all_rules.add(end_rule);
            }


        }
//        System.out.println("visit list " + visit_list);

        for (Chain chain : vertical_chains) {
//            System.out.println(chain.views_in_chain);
            chain.showChain();
        }
    }

    static Rule ruleIsInList(List<Rule> list, Rule rule){
        for(Rule r : list){
            if(r.target == rule.target && r.start == rule.start && r.relation == rule.relation){
                return r;
            }
        }
        return null;
    }


    @Override
    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize) {
        System.out.println("start constraint measure: " + this.Id);
        System.out.println("WidthMeasureSpecSize = " + WidthMeasureSpecSize);
        System.out.println("HeightMeasureSpecSize = " + HeightMeasureSpecSize);
        if(HCO == null){
            constructHDG();
            System.out.println("HCO : " + HCO);
        }
        if(VCO == null){
            constructVDG();
            System.out.println("VCO : " + VCO);
        }

        for(View child : this.Children){
            if(!child.isNormal){
                LayoutParams lp = (LayoutParams) child.mLayoutParams;
                horizontal_weight_sum += lp.Horizontal_weight;
                vertical_weight_sum += lp.Vertical_weight;
                if(lp.width == 0 && lp.Horizontal_weight == 0){
                    lp.width = LayoutParams.WRAP_CONTENT;
                    measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                    lp.width = 0;
                }
                if(lp.height == 0 && lp.Vertical_weight == 0){
                    lp.height = LayoutParams.WRAP_CONTENT;
                    measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                    lp.height = 0;
                }
                measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
            }
        }
//
        System.out.println("HORIZONTAL LOCATION");
        int right_bound = 0;
        for(int index : HCO){
            System.out.println("start measure child " + index + "---------");
            // 处理chain
            if(index < 0){
                Chain chain = horizontal_chains.get(-index-2);
                System.out.println("chain: " + chain.views_in_chain);
                Boundary start_bound = getBounds(chain.start_rule, WidthMeasureSpecSize, HeightMeasureSpecSize);
                Boundary end_bound = getBounds(chain.end_rule, WidthMeasureSpecSize, HeightMeasureSpecSize);
                int chain_start = 0;
                int chain_end = 0;
                // 获取chain的左边界
                if(start_bound.val == Boundary.ERROR_CODE){
                    System.out.println("chain start rule got error");
                } else if (start_bound.dir == Boundary.LEFT) {
                    chain_start = start_bound.val;
                }else{
                    System.out.println("chain start rule got wrong direction");
                }
                // 获取chain的右边界
                if(end_bound.val == Boundary.ERROR_CODE){
                    System.out.println("chain end rule got error");
                } else if (end_bound.dir == Boundary.RIGHT) {
                    chain_end = end_bound.val;
                }else{
                    System.out.println("chain end rule got wrong direction");
                }

                System.out.println("chain start:" + chain_start + "; chain end: " + chain_end);

                // 没有weight
                if(chain.weight_sum == 0){
                    int total_space = chain_end - chain_start;
                    for(int index_in_chain : chain.views_in_chain){
                        View child = Children.get(index_in_chain);
                        total_space -= child.measuredWidth;
                    }
                    if(chain.chainStyle == Chain.CHAIN_SPREAD){
                        int gap_cnt = chain.views_in_chain.size() + 1;
                        int space = total_space / gap_cnt;
                        int left = chain_start + space;
                        for(int index_in_chain : chain.views_in_chain){
                            View child = Children.get(index_in_chain);
                            child.left = left;
                            child.right = left + child.measuredWidth;
                            left = child.right + space;
                        }
                    }else if(chain.chainStyle == Chain.CHAIN_PACKED){
                        int space = total_space / 2;
                        int left = chain_start + space;
                        for(int index_in_chain : chain.views_in_chain){
                            View child = Children.get(index_in_chain);
                            child.left = left;
                            child.right = left + child.measuredWidth;
                            left = child.right;
                        }
                    }
                }else{
                    int total_space = chain_end - chain_start;
                    for(int index_in_chain : chain.views_in_chain){
                        View child = Children.get(index_in_chain);
                        total_space -= child.measuredWidth;
                    }
                    int left = chain_start;
                    for(int index_in_chain : chain.views_in_chain){
                        View child = Children.get(index_in_chain);
                        LayoutParams lp = (LayoutParams) child.mLayoutParams;
                        if(child.measuredWidth == 0){
                            lp.width = (int) (total_space * lp.Horizontal_weight / chain.weight_sum);
                            measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                        }
                        child.left = left;
                        child.right = left + child.measuredWidth;
                        left = child.right;
                    }
                }

                for(int index_in_chain : chain.views_in_chain){
                    View child = Children.get(index_in_chain);
                    if(right_bound < child.right)
                        right_bound = child.right;
                }
                continue;
            }

            // 处理常规view
            System.out.println("---------------child index " + index + "-------------------");
            View child = this.getChildren().get(index);
            if(child.isNormal) continue;
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
                    if(lp.width == 0){
                        lp.width = r - l;
                        measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
//                        lp.width = 0;
                    }
                    double mid = (l + r) * 0.5;
                    child.left = (int) (mid - child.measuredWidth * 0.5);
                    child.right = child.left + child.measuredWidth;
//                    System.out.println("mid = " + mid + " left = " + child.left + " right = " + child.right + "measureWidth = " + child.measuredWidth);
                }else{
                    if(lp.width == 0){
                        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                        measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                        lp.width = 0;
                    }
                    child.left = l;
                    child.right = l + child.measuredWidth;
                }
            }else {
                if(rFlag){ // 有右侧限制
                    if(lp.width == 0){
                        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                        measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                        lp.width = 0;
                    }
                    child.right = r;
                    child.left = r - child.measuredWidth;
//                    System.out.println("有右侧限制，组件中线与之对齐 child " + child.left + " " + child.right + " " + measuredWidth);
                }else{
                    System.err.println("No Horizontal Constraints");
                    child.left = l;
                    child.right = l + child.measuredWidth;
                }
            }
            if(right_bound < child.right) right_bound = child.right;
//            System.out.println("child " + child.index + ": " + child.left + " " + child.right + "; measured width = " + child.measuredWidth);
        }
        System.out.println("VERTICAL");
        int bottom_bound = 0;
        for(int index : VCO) {
            System.out.println("start measure child " + index);
            // 处理chain
            if(index < 0){
                Chain chain = vertical_chains.get(-index-2);
                System.out.println("chain: " + chain.views_in_chain);
                Boundary start_bound = getBounds(chain.start_rule, WidthMeasureSpecSize, HeightMeasureSpecSize);
                Boundary end_bound = getBounds(chain.end_rule, WidthMeasureSpecSize, HeightMeasureSpecSize);
                int chain_top = 0;
                int chain_bot = 0;
                // 获取chain的左边界
                if(start_bound.val == Boundary.ERROR_CODE){
                    System.out.println("chain start rule got error");
                } else if (start_bound.dir == Boundary.TOP) {
                    chain_top = start_bound.val;
                }else{
                    System.out.println("chain start rule got wrong direction");
                }
                // 获取chain的右边界
                if(end_bound.val == Boundary.ERROR_CODE){
                    System.out.println("chain end rule got error");
                } else if (end_bound.dir == Boundary.BOTTOM) {
                    chain_bot = end_bound.val;
                }else{
                    System.out.println("chain end rule got wrong direction");
                }

                System.out.println("chain top:" + chain_top + "; chain bottom: " + chain_bot);

                // 没有weight
                if(chain.weight_sum == 0){
                    int total_space = chain_bot - chain_top;
                    for(int index_in_chain : chain.views_in_chain){
                        View child = Children.get(index_in_chain);
                        total_space -= child.measuredHeight;
                    }
                    if(chain.chainStyle == Chain.CHAIN_SPREAD){
                        int gap_cnt = chain.views_in_chain.size() + 1;
                        int space = total_space / gap_cnt;
                        int top = chain_top + space;
                        for(int index_in_chain : chain.views_in_chain){
                            View child = Children.get(index_in_chain);
                            child.top = top;
                            child.bottom = top + child.measuredHeight;
                            top = child.bottom + space;
                        }
                    }else if(chain.chainStyle == Chain.CHAIN_PACKED){
                        int space = total_space / 2;
                        int top = chain_top + space;
                        for(int index_in_chain : chain.views_in_chain){
                            View child = Children.get(index_in_chain);
//                            System.out.println("start measure " + child.Id);
                            child.top = top;
                            child.bottom = top + child.measuredHeight;
                            top = child.bottom;
//                            System.out.println(child.getId() + " in chain top = " + child.top + "; bottom = " + child.bottom + " line 587");
                        }
                    }
                }else{
                    int total_space = chain_bot - chain_top;
                    for(int index_in_chain : chain.views_in_chain){
                        View child = Children.get(index_in_chain);
                        total_space -= child.measuredHeight;
                    }
                    int top = chain_top;
                    for(int index_in_chain : chain.views_in_chain){
                        View child = Children.get(index_in_chain);
                        LayoutParams lp = (LayoutParams) child.mLayoutParams;
                        if(child.measuredHeight == 0){
                            lp.height = (int) (total_space * lp.Vertical_weight / chain.weight_sum);
                            measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                        }
                        child.top = top;
                        child.bottom = top + child.measuredHeight;
                        top = child.bottom;
                    }
                }

                for(int index_in_chain : chain.views_in_chain){
                    View child = Children.get(index_in_chain);
                    if(bottom_bound < child.bottom)
                        bottom_bound = child.bottom;
//                    System.out.println(child.getId() + ": child.top = " + child.top);
                }
                continue;
            }

            System.out.println("---------------child index " + index + "-------------------");
            View child = this.getChildren().get(index);
            if(child.isNormal) continue;
//            System.out.println(child.getId());
            LayoutParams lp = (LayoutParams) child.mLayoutParams;
//            int t = this.paddingTop + lp.topMargin, b = HeightMeasureSpecSize - this.paddingBottom - lp.bottomMargin;
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
                        System.out.println(rule.target);
                        System.out.println(child.getId() + " top = " + t);
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
                    if(lp.height == 0){
                        lp.height = b - t;
                        measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                        // TODO: 2024/8/4
//                        lp.height = 0;
                    }
                    double mid = (t + b) * 0.5;
                    child.top = (int) (mid - child.measuredHeight * 0.5);
                    child.bottom = child.top + child.measuredHeight;
                }else{
                    if(lp.height == 0){
                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                        // TODO: 2024/8/4
//                        lp.height = 0;
                    }
                    child.top = t + lp.topMargin;
                    System.out.println("top constraint top = " + t);
                    child.bottom = t + child.measuredHeight;
                }
            }else{
                if(lp.height == 0){
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    measureChild(child, WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
                    // TODO: 2024/8/4
//                    lp.height = 0;
                }
                if(bFlag){
                    child.bottom = b - lp.bottomMargin;
                    child.top = child.bottom - child.measuredHeight;
                }else{
                    System.out.println("No Vertical constraints");
                    child.top = t;
                    child.bottom = t + child.measuredHeight;
                }
            }
            if(bottom_bound < child.bottom) bottom_bound = child.bottom;
//            System.out.println("child " + child.index + ": " +  child.top + " " + child.bottom + " " + child.measuredHeight);
        }
//
//
        if(HeightMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredHeight = HeightMeasureSpecSize;
        }else{
            this.measuredHeight = Math.min(HeightMeasureSpecSize, bottom_bound + this.paddingBottom);
        }
        if(WidthMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredWidth = WidthMeasureSpecSize;
        }else {
            this.measuredWidth = Math.min(WidthMeasureSpecSize, right_bound + this.paddingRight);
        }

        System.out.println("constraint layout measured width = " + measuredWidth + "; measured height = " + measuredHeight);
    }

    void measureChild(View child, int wSpecMode, int wSpecSize, int hSpecMode, int hSpecSize){

        if(child.isNormal){
            child.onMeasure(wSpecMode, wSpecSize, hSpecMode, hSpecSize);
            child.locateView(0, 0, child.measuredWidth, child.measuredHeight);
            return;
        }
        ViewGroup.LayoutParams lp = child.mLayoutParams;

        // 水平方向
//        int horizontal_padding =  this.paddingLeft + this.paddingRight + lp.leftMargin + lp.rightMargin;
        Spec cWidthSpec = MeasureSpec.getChildMeasureSpec(wSpecMode, wSpecSize, 0, lp.width);

        // 垂直方向
//        int vertical_padding = this.paddingTop + this.paddingBottom + lp.topMargin + lp.bottomMargin;
        Spec cHeightSpec = MeasureSpec.getChildMeasureSpec(hSpecMode, hSpecSize, 0, lp.height);

        child.onMeasure(cWidthSpec.mode, cWidthSpec.size, cHeightSpec.mode, cHeightSpec.size);
    }

    static List<Integer> sortChildren(List<Rule> rules, List<Chain> chains, int childrenCNT){

        // 创建一个Set来存储给定的列表元素
        Set<Integer> set = new HashSet<>();
        List<Integer> view_not_in_chain = new ArrayList<>();
        HashMap<Integer, Integer> view_chain_map = new HashMap<>();
        for(Chain chain : chains){
            // 将chain的index设置为负数
            view_not_in_chain.add(chain.chain_index);
            set.addAll(chain.views_in_chain);
            for(int view_index : chain.views_in_chain){
                view_chain_map.put(view_index, chain.chain_index);
            }
        }

        // 遍历从children的index
        for (int i = 0; i < childrenCNT; i++) {
            // 如果Set中不包含当前数字，添加到结果列表中
            if (!set.contains(i)) {
                view_not_in_chain.add(i);
            }
        }
        System.out.println("view_not_in_chain: " + view_not_in_chain);

        List<Integer> res = new ArrayList<>();
//        for(Chain chain : chains){
//            if(chain.independent){
//                res.addAll(chain.views_in_chain);
//            }
//        }
        HashMap<Integer, Integer> inDegree = new HashMap<>();
//        System.out.println(childrenCNT);
        for (int i : view_not_in_chain) {
            inDegree.put(i, 0);
        }

        for(Rule rule : rules){
            if(rule.target != PARENT_INDEX){
                int target = rule.target;
                if(view_chain_map.containsKey(target)) target = view_chain_map.get(target);
                System.out.println(rule.start + " " + target + " " + rule.relation);
                inDegree.put(target, inDegree.get(target) + 1);
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
                    int target = rule.target;
                    if(view_chain_map.containsKey(target)) target = view_chain_map.get(target);
                    inDegree.put(target, inDegree.get(target) - 1);
                    if(inDegree.get(target) == 0){
                        queue.add(target);
                    }
                }
            }
        }

        return res;
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
                    val = target_view.left;
                    dir = Boundary.LEFT;
                }
                case LEFT_OF, START_OF ->{
                    val = target_view.left;
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
        return new Boundary(dir, val);
    }

    public static class LayoutParams extends RelativeLayout.LayoutParams{
        double Horizontal_bias = 0.5;
        double Vertical_bias = 0.5;
        double Vertical_weight = 0;
        double Horizontal_weight = 0;
        @Override
        public void setLayoutParams(HashMap<String, String> attrMap){
            super.setLayoutParams(attrMap);
            if(attrMap.containsKey("layout_constraintHorizontal_bias")){
                this.Horizontal_bias = Double.parseDouble(attrMap.get("layout_constraintHorizontal_bias"));
                attrMap.remove("layout_constraintHorizontal_bias");
            }
            if(attrMap.containsKey("layout_constraintVertical_bias")){
                this.Vertical_bias = Double.parseDouble(attrMap.get("layout_constraintVertical_bias"));
                attrMap.remove("layout_constraintVertical_bias");
            }
            if(attrMap.containsKey("layout_constraintVertical_weight")){
                this.Vertical_weight = Double.parseDouble(attrMap.get("layout_constraintVertical_weight"));
                attrMap.remove("layout_constraintVertical_weight");
            }
            if(attrMap.containsKey("layout_constraintHorizontal_weight")){
                this.Horizontal_weight = Double.parseDouble(attrMap.get("layout_constraintHorizontal_weight"));
                attrMap.remove("layout_constraintHorizontal_weight");
            }
        }

    }

    static class Chain {
        static final boolean HORIZONTAL = true;
        static final boolean VERTICAL = false;
        static final int CHAIN_SPREAD = 1;
        static final int CHAIN_PACKED = 2;
        static int chain_cnt_horizontal = 0;
        static int chain_cnt_vertical = 0;
        int chain_index  = -2;// index 从-2开始，与PARENT——INDEX区分
        int chainStyle = CHAIN_SPREAD;
        boolean direction = HORIZONTAL;
        List<Integer> views_in_chain = new ArrayList<>();
        double weight_sum = 0;
        Rule start_rule = null;
        Rule end_rule = null;
        public Chain(boolean direction){
            this.direction = direction;
            if(direction == HORIZONTAL){
                chain_cnt_horizontal ++;
                chain_index = -chain_cnt_horizontal -1;
            }else{
                chain_cnt_vertical++;
                chain_index = -chain_cnt_vertical -1;
            }
        }

        void setStartRule(Rule rule){
            this.start_rule = rule;
        }

        void setEndRule(Rule rule){
            this.end_rule = rule;
        }

        public void setChainStyle(String chainStyle){
            if(chainStyle.equals("packed")){
                this.chainStyle = CHAIN_PACKED;
            } else if (chainStyle.equals("spread")) {
                this.chainStyle = CHAIN_SPREAD;
            }else{
                System.err.println("unknown chainStyle");
            }
        }


        public void showChain(){
            System.out.println("----show chain----");
            System.out.println(views_in_chain);
            System.out.println(start_rule.start + " " + start_rule.target + " " + start_rule.relation);
            System.out.println(end_rule.start + " " + end_rule.target + " " + end_rule.relation);
            System.out.println("weight sum = " + weight_sum);
        }

        static Rule getStartRule(List<Rule> rules, boolean direction){
            int start = rules.get(0).start;
            System.out.println("get Start Rule");
            if(direction == HORIZONTAL){
                for(Rule rule : rules){
                    if(rule.relation == ALIGN_START || rule.relation == ALIGN_LEFT
                            || rule.relation == END_OF || rule.relation == RIGHT_OF)
                        return rule;
                }
                return new Rule(start, -1, ALIGN_START, HORIZONTAL);
            }else{

                for(Rule rule : rules){
                    if(rule.relation == ALIGN_TOP || rule.relation == BELOW)
                        return rule;
                }
                return new Rule(start, -1, ALIGN_TOP, HORIZONTAL);
            }
        }

        static Rule getEndRule(List<Rule> rules, boolean direction){
            int start = rules.get(0).start;
            System.out.println("get End Rule");
            if(direction == HORIZONTAL){
                for(Rule rule : rules){
                    if(rule.relation == ALIGN_END || rule.relation == ALIGN_RIGHT
                            || rule.relation == START_OF || rule.relation == LEFT_OF)
                        return rule;
                }
                return new Rule(start, PARENT_INDEX, ALIGN_END, HORIZONTAL);
            }else{
                for(Rule rule : rules){
                    if(rule.relation == ALIGN_BOTTOM || rule.relation == ABOVE)
                        return rule;
                }
                return new Rule(start, PARENT_INDEX, ALIGN_BOTTOM, HORIZONTAL);
            }
        }
    }

}
