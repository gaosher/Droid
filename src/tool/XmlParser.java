package tool;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import util.DisplayParams;
import util.MeasureSpec;
import view.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class XmlParser {

    static final String [] ViewGroups = {
            "FrameLayout", "LinearLayout", "ConstrainedLayout", "RelativeLayout"
    };
    static final String [] TextualViews = {
            "TextView", "Button", "EditText"
    };
    static final String [] ImageViews = {
            "ImageView", "ImageButton"
    };

    static  final String [] InfiniteWidthViews = { // 拥有无限宽度的View
            "HorizontalScrollView"
    };

    static final String [] InfiniteHeightViews = {
            "ScrollView", "RecyclerView", "ListView"
    };

    static final String [] IgnoredViews = {
            "FloatingButton"
    };
    static HashSet<String> ViewGroupSet = new HashSet<>(List.of(ViewGroups));
    static HashSet<String> TextualViewSet = new HashSet<>(List.of(TextualViews));
    static HashSet<String> ImageViewSet = new HashSet<>(List.of(ImageViews));

    static SAXReader reader = new SAXReader();

    static void readXml(File xmlFile){
        try {
            Document doc = reader.read(xmlFile);
            Element root = doc.getRootElement();

            // todo 需要初始化包名 View.pkgName


            // todo 这里还需要设置一下根组件的宽高
            ViewGroup.LayoutParams rootLP = new ViewGroup.LayoutParams();
            View view_root = parseElement(root, null, rootLP);

//            if (view_root != null) {
//                printXML(view_root);
//            }

        } catch (DocumentException e) {
            System.err.println("fail to read" + xmlFile.getAbsolutePath());
            throw new RuntimeException(e);
        }
    }

    static View parseElement(Element ele, View parent, ViewGroup.LayoutParams layoutParams ){
        /* View的类型*/
        String ele_name = ele.getName();
        ele_name = ele_name.replace("android.widget.", ""); // TODO: 可能会有名称不匹配的问题

        // List<Attribute> attributes --> HashMap<String, String> attrMap
        List<Attribute> attributes = ele.attributes();
        HashMap<String, String> attrMap = new HashMap<>();
        for (Attribute a : attributes) {
            attrMap.put(a.getName(), a.getValue());
        }

        int ViewType = 0;
        if(ViewGroupSet.contains(ele_name)){
            ViewType = 1;
        }else if (TextualViewSet.contains(ele_name) || (attrMap.get("text") != null && attrMap.get("text").length()>0)){ // 存在文字属性的就可以算是
            ViewType = 2;
        } else if (ImageViewSet.contains(ele_name)) {
            ViewType = 3;
        }else{
            System.out.println("Undefined View Type: " + ele_name);
            return null;
        }
        switch (ViewType) {
            case 1 -> { // ViewGroups
                //todo 细分每一种 ViewGroup
                ViewGroup vg;
                List<Element> children = ele.elements(); // 获取子elements
                switch (ele_name){
                    case "RelativeLayout" -> {
                        vg = new RelativeLayout(layoutParams, attrMap);
                        vg.setParent(parent);
                        for (Element child : children) {
                            ViewGroup.LayoutParams rlayoutParams = new RelativeLayout.LayoutParams(); // add
                            View child_view = parseElement(child, vg, rlayoutParams);
                            vg.addChild(child_view);
                        }
//                        ((RelativeLayout) vg).constructDependencyGraph();
                        ((RelativeLayout) vg).onMeasure(MeasureSpec.EXACTLY, 1388, MeasureSpec.AT_MOST, 2560);
                    }
                    case "LinearLayout" -> {
                        vg = new LinearLayout(layoutParams, attrMap);
                        vg.setParent(parent);
                        for (Element child : children) {
                            ViewGroup.LayoutParams lLayoutParams = new LinearLayout.LayoutParams();
                            View child_view = parseElement(child, vg, lLayoutParams);
                            vg.addChild(child_view);
                        }
//                        ((LinearLayout) vg).onMeasure(MeasureSpec.EXACTLY, 1440, MeasureSpec.AT_MOST, 2560);
                    }
                    default -> {
                        //todo 细分每一种 ViewGroup
                        vg = new ViewGroup(attrMap);
                        vg.setParent(parent);
                        for (Element child : children) {
                            ViewGroup.LayoutParams dLayoutParams = new ViewGroup.LayoutParams();
                            View child_view = parseElement(child, vg, dLayoutParams);
                            vg.addChild(child_view);
                        }
                    }
                }
                return vg;
            }
            case 2 -> { // textual views
                View textualView = new TextualView(layoutParams, attrMap);
                textualView.setParent(parent);
                return textualView;
            }
            case 3 -> { // image views
                View imageView = new ImageView(layoutParams, attrMap);
                imageView.setParent(parent);
                return imageView;
            }
            default -> {
                System.err.println("Undefined View Type: " + ele_name);
                return null;
            }
        }
    }

    static void printXML(View view){
        System.out.println("------------------------------------------");
        view.printClassName();
        if(view instanceof ViewGroup) {
            view.showAllAttrs();
            view.mLayoutParams.show();
            for(View child : ((ViewGroup) view).getChildren()){
                printXML(child);
            }
        }else{
            view.showAllAttrs();
            view.mLayoutParams.show();
        }
    }


    /**
     * 根据默认设置下的组件高度，计算设置修改后的根组件大小
     * @param lp
     * @param height
     */
    void setRootLp(ViewGroup.LayoutParams lp, int height){
        lp.width = DisplayParams.SCREEN_WIDTH;
        int dpi = DisplayParams.DPI;
        int default_dpi = DisplayParams.DEFAULT_DPI;
        int screen_height = DisplayParams.SCREEN_HEIGHT;

        int root_height = screen_height - (screen_height - height) * dpi / default_dpi;
        lp.height = root_height;
    }



    public static void main(String[] args){
        String filename = "src\\RelativeLayout_example.xml";
        readXml(new File(filename));
    }
}
