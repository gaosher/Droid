package tool;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import util.DisplayParams;
import util.MeasureSpec;
import view.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class XmlParser {
    static final  int VIEW = 0;
    static final  int VIEW_GROUP = 1;
    static final  int LINEAR_LAYOUT = 2;
    static final  int RELATIVE_LAYOUT = 3;
    static final  int CONSTRAINT_LAYOUT = 4;
    static final int RECYCLER_VIEW = 5;
    static final int HORIZONTAL_SCROLL_VIEW = 6;
    static final int SCROLL_VIEW = 7;
    static final int TEXTUAL_VIEW = 8;
    static final int IMAGE_VIEW = 9;

    static final int NORMAL_VIEW = -1;
    static final int NORMAL_VIEW_GROUP = -2;



    static final String [] ViewGroups = {
            "FrameLayout", "LinearLayout", "ConstrainedLayout", "RelativeLayout", "ViewGroup"
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

            // 构建ViewTree
            View view_root = parseElement(root, null, rootLP);

            if (view_root != null) {
                int root_height = view_root.getBounds()[3] - view_root.getBounds()[1];
                root_height = getRootHeight(root_height);
                System.out.println("root_height = " + root_height);
                System.out.println("------------------------START MEASURE----------------------");
                view_root.onMeasure(MeasureSpec.EXACTLY, DisplayParams.SCREEN_WIDTH, MeasureSpec.EXACTLY, root_height);
                view_root.checkView();
            }

//            if (view_root != null) {
//                printXML(view_root);
//            }

        } catch (DocumentException e) {
            System.err.println("fail to read" + xmlFile.getAbsolutePath());
            throw new RuntimeException(e);
        }
    }

    static View parseElement(Element ele, View parent, ViewGroup.LayoutParams layoutParams){
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
        if(attrMap.containsKey("isMerged")){
            if(ele_name.contains("RecyclerView")) ViewType = RECYCLER_VIEW;
            else if(ele_name.contains("HorizontalScrollView")) ViewType = HORIZONTAL_SCROLL_VIEW;
            else if(ele_name.contains("LinearLayout")) ViewType = LINEAR_LAYOUT;
            else if(ele_name.contains("RelativeLayout")) ViewType = RELATIVE_LAYOUT;
            else if(ele_name.contains("ConstraintLayout")) ViewType = CONSTRAINT_LAYOUT;
            else if(ele_name.contains("ScrollView")) ViewType = SCROLL_VIEW;
//            else if(ele_name.contains("ScrollView")) ViewType = SCROLL_VIEW;
            else if (TextualViewSet.contains(ele_name) || (attrMap.get("text") != null && attrMap.get("text").length()>0)){ // 存在文字属性的就可以算是
                ViewType = TEXTUAL_VIEW;
            } else if (ImageViewSet.contains(ele_name)) {
                ViewType = IMAGE_VIEW;
            }else{
                System.out.println("Undefined View Type: " + ele_name);
                List<Element> children = ele.elements();
                if(children == null || children.size() == 0){
                    ViewType = VIEW;
                }else{
                    System.out.println(ele.attributeValue("id") + " ViewType = VIEW_GROUP");
                    ViewType = VIEW_GROUP;
                }
            }
        }else{
            List<Element> children = ele.elements();
            if(children == null || children.size() == 0){
                ViewType = NORMAL_VIEW;
            }else{
                ViewType = NORMAL_VIEW_GROUP;
            }
        }

        List<Element> children = ele.elements();

        switch (ViewType) {
            case NORMAL_VIEW -> { // 未合并的组件
                NormalView normalView = new NormalView(attrMap);
                normalView.setParent(parent);
                System.out.println("parse a normal view");
                return normalView;
            }
            case NORMAL_VIEW_GROUP -> {
                NormalViewGroup normalViewGroup = new NormalViewGroup(attrMap);
                for (Element child : children) {
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(); // add
//                    System.out.println("parseChildren");
                    View child_view = parseElement(child, normalViewGroup, lp);
//                    System.out.println("parseChildren end");
                    normalViewGroup.addChild(child_view);
                }
                normalViewGroup.setParent(parent);
                System.out.println("parse a normalViewGroup");
                return normalViewGroup;
            }

//            ViewGroup vg;
//            List<Element> children = ele.elements();
            case RELATIVE_LAYOUT -> {
//                ViewGroup vg;
//                List<Element> children = ele.elements(); // 获取子elements
//                if(!attrMap.containsKey("layout_height")) System.out.println("lost layout_height");
                RelativeLayout vg = new RelativeLayout(layoutParams, attrMap);
                vg.setParent(parent);
                for (Element child : children) {
                    ViewGroup.LayoutParams rlayoutParams = new RelativeLayout.LayoutParams(); // add
                    View child_view = parseElement(child, vg, rlayoutParams);
                    vg.addChild(child_view);
                }
//                System.out.println("return RelativeLayout");
                return vg;
            }
            case LINEAR_LAYOUT -> {
//                ViewGroup vg;
//                List<Element> children = ele.elements(); // 获取子elements
                LinearLayout vg = new LinearLayout(layoutParams, attrMap);
                vg.setParent(parent);
                for (Element child : children) {
                    ViewGroup.LayoutParams lLayoutParams = new LinearLayout.LayoutParams();
                    View child_view = parseElement(child, vg, lLayoutParams);
                    vg.addChild(child_view);
                }
//                System.out.println("return LinearLayout");
                return vg;
            }
            case CONSTRAINT_LAYOUT -> {
                ConstraintLayout constraintLayout = new ConstraintLayout(layoutParams, attrMap);
                constraintLayout.setParent(parent);
                for (Element child : children){
                    ViewGroup.LayoutParams cLayoutParams = new RelativeLayout.LayoutParams();
                    View child_view = parseElement(child, constraintLayout, cLayoutParams);
                    constraintLayout.addChild(child_view);
                }
                return constraintLayout;
            }
            case VIEW_GROUP -> {
//                ViewGroup vg;
//                List<Element> children = ele.elements(); // 获取子elements
                System.out.println("new ViewGroup");
                ViewGroup vg = new ViewGroup(layoutParams, attrMap);
                vg.setParent(parent);
                for (Element child : children) {
                    ViewGroup.LayoutParams dLayoutParams = new ViewGroup.LayoutParams();
                    View child_view = parseElement(child, vg, dLayoutParams);
                    vg.addChild(child_view);
                    System.out.println("add child to ViewGroup : " + child_view.getId());
                }
//                System.out.println("return ViewGroup");
                System.out.println("ViewGroup childrenCNT = " + vg.getChildren().size());
                return vg;
            }

            case TEXTUAL_VIEW -> { // textual views
                TextualView textualView = new TextualView(layoutParams, attrMap);
                textualView.setParent(parent);
                System.out.println("parsed textualView text: " + textualView.Text);
                return textualView;
            }
            case IMAGE_VIEW -> { // image views
                View imageView = new ImageView(layoutParams, attrMap);
                imageView.setParent(parent);
//                System.out.println("return imageView");
                return imageView;
            }

            case RECYCLER_VIEW -> {
                RecyclerView recyclerView = new RecyclerView(layoutParams, attrMap);
                // 因为是重复的，只检查第一个child
                Element child = ele.elements().get(0);
                View child_view = parseElement(child, recyclerView,new ViewGroup.LayoutParams());
                recyclerView.addChild(child_view);
//                System.out.println("return recyclerView");
                return recyclerView;
            }

            case HORIZONTAL_SCROLL_VIEW -> {
                HorizontalView horizontalView = new HorizontalView(layoutParams, attrMap);
                horizontalView.setParent(parent);
                for (Element child : children) {
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams();
                    View child_view = parseElement(child, horizontalView, lp);
                    horizontalView.addChild(child_view);
                }
//                System.out.println("return horizontalView");
                return horizontalView;
            }

            case SCROLL_VIEW -> {
                ScrollView scrollView = new ScrollView(layoutParams, attrMap);
                scrollView.setParent(parent);
                for (Element child : children) {
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams();
                    View child_view = parseElement(child, scrollView, lp);
                    scrollView.addChild(child_view);
                }
                return  scrollView;
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
     * @param height
     */
    static int getRootHeight(int height){
        int dpi = DisplayParams.DPI;
        int default_dpi = DisplayParams.DEFAULT_DPI;
        int screen_height = DisplayParams.SCREEN_HEIGHT;
        int root_height = screen_height - (screen_height - height) * dpi / default_dpi;
        return root_height;
    }

    public static void main(String[] args){
        String filename = "mergedXmlBase\\linphoneMergeTest.xml";
        readXml(new File(filename));
    }
}
