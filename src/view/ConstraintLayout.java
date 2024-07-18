package view;

import com.google.j2objc.annotations.ObjectiveCName;

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
}
