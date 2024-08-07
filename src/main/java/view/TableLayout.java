package view;

import java.util.HashMap;

public class TableLayout extends LinearLayout{
    public TableLayout(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap) {
        super(layoutParams, attrMap);
        this.Orientation = VERTICAL;
    }
}
