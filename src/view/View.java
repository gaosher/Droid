package view;


import util.DimenVaule;
import util.MeasureSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class View {

    public static String packageName = "text.app"; // todo 需要在入口初始化
    public String view_type = "view";

    int measuredWidth = 0;
    int measuredHeight = 0;

    int index;
    String ResourceId;
    String Id;
    String ClassName;
    int [] Bounds = new int[4];
    static final String [] Margin_Attr = {
            "layout_margin", "layout_marginLeft", "layout_marginRight", "layout_marginTop", "layout_marginBottom"
    };
    int [] Margin = new int [4]; //左右上下的maigin
    static final String [] Padding_Attr = {
            "padding", "paddingLeft", "paddingRight", "paddingTop", "paddingBottom"
    };
    //int [] Padding = new int [4]; // 上下左右的padding
    int paddingLeft = 0;
    int paddingRight = 0;
    int paddingTop = 0;
    int paddingBottom = 0;

    HashMap<String, String> AttrMap;

    View Parent = null;

    public ViewGroup.LayoutParams mLayoutParams;

    public String xmlFileName;

    public View(){
    }

    public void setmLayoutParams(ViewGroup.LayoutParams mLayoutParams) {
        this.mLayoutParams = mLayoutParams;
    }


    public View(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap){

        mLayoutParams = layoutParams;
        mLayoutParams.setLayoutParams(attrMap);


        initialBasicAttrs(attrMap);
    }

    void initialBasicAttrs(HashMap<String, String> attrMap){
        // initial bounds
        this.setBounds(attrMap.get("bounds"));
        attrMap.remove("bounds");
        // initial resource-id
        if(attrMap.containsKey("resource-id")){
            this.setResourceId(attrMap.get("resource-id"));
            attrMap.remove("resource-id");
        }
        // initial id
        if(attrMap.containsKey("id")){
            this.setId(attrMap.get("id"));
            attrMap.remove("id");
        }
        // initial index
        this.setIndex(attrMap.get("index"));
        attrMap.remove("index");

        // initial padding
        for (String pad : Padding_Attr){
            if (attrMap.containsKey(pad)) {
                setPadding(pad, attrMap.get(pad));
                attrMap.remove(pad);
            }
        }

        // initial className
        if(attrMap.containsKey("class")){
            this.ClassName = attrMap.get("class");
            attrMap.remove("class");
        }

        // initial xml File name
        if(attrMap.containsKey("isMerged")){
            this.xmlFileName = attrMap.get("isMerged");
            attrMap.remove("isMerged");
        }
    }


    void setIndex(String index_str){
        this.index = Integer.parseInt(index_str);
    }

    void setResourceId(String resourceId){
        this.ResourceId = resourceId;
    }

    void setId(String Id){
        //System.out.println("setID" + Id);
        Id = Id.replace("@+id", "@id");
        this.Id = Id;
    }

    /**
     * 提取字符串中的4个int，存入Bounds中
     * @param str_bounds bounds字符串
     */
    void setBounds(String str_bounds){
        Pattern pattern = Pattern.compile("\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]");
        Matcher matcher = pattern.matcher(str_bounds);
        if (matcher.find()) {
            for (int i = 0; i < 4; i++) {
                this.Bounds[i] = Integer.parseInt(matcher.group(i+1));
                //System.out.println(this.Bounds[i]);
            }
        } else {
            System.err.println("No match found in " + str_bounds);
        }
    }


    /**
     * 解析Padding相关属性
     * @param padding_type 属性名
     * @param padding_val 属性值
     */
    void setPadding(String padding_type, String padding_val){
        int padding_px = DimenVaule.parseDimenValue2Px(padding_val);
        switch (padding_type) {
            case "padding" -> {
                this.paddingLeft = padding_px;
                this.paddingTop = padding_px;
                this.paddingBottom = padding_px;
                this.paddingRight = padding_px;
            }
            case "paddingLeft" -> paddingLeft = padding_px;
            case "paddingTop" -> paddingTop = padding_px;
            case "paddingRight" -> paddingRight = padding_px;
            case "paddingBottom" -> paddingBottom = padding_px;
            default -> System.err.println("unknown padding type" + padding_type);
        }
    }

    public void setParent(View parent){
        this.Parent = parent;
    }

    public View getParent(){
        return this.Parent;
    }

    public String getId(){
        return this.Id;
    }
    public int[] getBounds(){
        return this.Bounds;
    }
    public String getClassName(){
        return this.ClassName;
    }

    public String getXmlFileName() {
        return xmlFileName;
    }


    public void showAllAttrs(){
//        System.out.println("Width: " + this.Width);
//        System.out.println("Height: " + this.Height);
        System.out.println("Id: " + this.Id);

        System.out.print("Bounds: ");
        for (int i = 0; i < 4; i++) {
            System.out.print(Bounds[i] + " ");
        }
        System.out.println();

        System.out.print("Margin: ");
        for (int i = 0; i < 4; i++) {
            System.out.print(Margin[i] + " ");
        }
        System.out.println();

        System.out.println("Padding left: "+ this.paddingLeft);
        System.out.println("Padding top: "+ this.paddingTop);
        System.out.println("Padding right: "+ this.paddingRight);
        System.out.println("Padding Bottom: "+ this.paddingBottom);
        System.out.println();
    }



    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize){
        if(WidthMeasureSpecMode == MeasureSpec.AT_MOST){
            // todo 考虑是否将这里处理为默认设置下的宽高
        } else if (WidthMeasureSpecMode == MeasureSpec.EXACTLY) {
            this.measuredWidth = WidthMeasureSpecSize;
        }

        if(HeightMeasureSpecMode == MeasureSpec.AT_MOST){

        } else if (HeightMeasureSpecMode == MeasureSpec.EXACTLY) {
            this.measuredHeight = HeightMeasureSpecSize;
        }
    }

    int left = Integer.MIN_VALUE;
    int right = Integer.MAX_VALUE;
    int top = Integer.MIN_VALUE;
    int bottom = Integer.MAX_VALUE;
    void setCoords(int left, int top, int right, int bottom){
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    void onLayout(){

    }

    public void printClassName() {
        System.out.println("View");
    }

    public void checkView() throws IOException {

    }



}
