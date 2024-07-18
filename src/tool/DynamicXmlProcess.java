package tool;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class DynamicXmlProcess {
    static String package_name;

    static HashMap<Element, String> DynamicIdMap = new HashMap<>(); // dynamic_element -> res_id
    static HashMap<String, List<Element>> StaticIdMap = new HashMap<>(); // ID -> static_element_list
    static HashMap<String, List<File>> IdMap = new HashMap<>(); // id -> xml_file_name_list
    static HashMap<File, List<String>> FileIdMap = new HashMap<>(); // xml_file_name -> static_id_list


    static String [] uselessAttrs = {"package", "checkable", "checked", "clickable", "enabled", "focusable", "focused",
            "long-clickable", "password", "selected", "displayed", "content-desc"
    };
    static HashSet<String> uselessAttrsSet = new HashSet<>(Arrays.asList(uselessAttrs));//xml dump中无用的属性集合

    /**
     * 在动态xml中搜索"resource-id"为id参数的元素
     * @param root xml doc的根节点
     * @param id "resource-id"属性值
     * @return
     */
    public static Element findElementInDynamic(Element root, String id){
        if(root.attribute("resource-id") != null && root.attributeValue("resource-id").equals(id)){
            return root;
        }
        Element res = null;
        List<Element> children = root.elements();
        for (Element child : children) {
            res = findElementInDynamic(child, id);
            if(res != null) return res;
        }
        return null;
    }

    /**
     * 为动态xml生成一个Element-(resource-id) HashMap
     * @param root xml doc的根节点
     */
    public static void buildDynamicMap(Element root){
        if(root.attribute("resource-id") != null){
            DynamicIdMap.put(root, root.attributeValue("resource-id"));
        }
        List<Element> children = root.elements();
        for (Element child : children) {
            buildDynamicMap(child);
        }
    }


    /**
     * 删去动态xml dump中无用的属性
     * @param doc
     */
    static void deleteUselessAttributesInDynamicXml(Document doc){
        Element root = doc.getRootElement();
        deleteUselessAttributesInElem(root);
    }

    /**
     * 删去元素中的无用属性，并且初始化包名
     * @param e
     */
    static void deleteUselessAttributesInElem(Element e){
        List<Attribute> toBeDel = new ArrayList<>();
        for(Iterator<Attribute> it = e.attributeIterator(); it.hasNext();) {
            Attribute a = it.next();
            if(package_name == null && a.getName().equals("package")){
                package_name = a.getValue();
                toBeDel.add(a);
                System.out.println("package_name: " + package_name);
            }

            if(a.getName().equals("scrollable") && a.getValue().equals("false")) {
                toBeDel.add(a);
                continue;
            }
            if(uselessAttrsSet.contains(a.getName())){
                toBeDel.add(a);
            }
        }
        for(Attribute attr : toBeDel){
            e.remove(attr);
        }
        List<Element> children = e.elements();
        for(Element child : children){
            deleteUselessAttributesInElem(child);
        }
    }

    /**
     * 构建静态xml的 id--Element--File 映射
     * @param layoutBase res/layout的文件路径
     */
    static void buildStaticIDMap(File layoutBase){
        if (layoutBase.isDirectory() && layoutBase.exists()) {
            File[] files = layoutBase.listFiles();  // 获取文件夹中所有文件数组
            if (files != null) {
                for (File xml : files) {
                    if (xml.isFile()) {
                        getOneXmlIds(xml);
                    }
                }
            }
        } else {
            System.err.println(layoutBase.getAbsolutePath() + "并不是一个存在的文件夹");
        }
    }

    static void getOneXmlIds(File xml){
        try {
            // 文件读取
            SAXReader reader = new SAXReader();
            Document document = reader.read(xml);

            //Root element
            Element root = document.getRootElement();

            // 使用栈来保存待处理的元素
            Stack<Element> elementStack = new Stack<>();
            elementStack.push(root);

            List<String> ids = new ArrayList<>();
            // root id 处理
            String root_id = root.attributeValue("id");
            if(root_id != null) {
                List<File> file_list;
                if(IdMap.containsKey(root_id)){
                    file_list = IdMap.get(root_id);
                }else {
                    file_list = new ArrayList<>();
                }
                file_list.add(xml);
                IdMap.put(root_id, file_list);

                List<Element> ele_list;
                if(StaticIdMap.containsKey(root_id)){
                    ele_list = StaticIdMap.get(root_id);
                }else {
                    ele_list = new ArrayList<>();
                }
                ele_list.add(root);
                StaticIdMap.put(root_id, ele_list);

                ids.add(root_id);
            }

            // 迭代遍历所有元素
            while (!elementStack.isEmpty()) {
                Element currentElement = elementStack.pop();

                if(currentElement.getName().equals("include")) {
                    System.out.println("include tag");
                    System.out.println(currentElement.attributeValue("id"));
                    continue;
                }

                String id = currentElement.attributeValue("id");
                if(id != null){
                    List<File> file_list;
                    if(IdMap.containsKey(id)){
                        file_list = IdMap.get(id);
                    }else {
                        file_list = new ArrayList<>();
                    }
                    file_list.add(xml);
                    IdMap.put(id, file_list);

                    List<Element> ele_list;
                    if(StaticIdMap.containsKey(id)){
                        ele_list = StaticIdMap.get(id);
                    }else {
                        ele_list = new ArrayList<>();
                    }
                    ele_list.add(currentElement);
                    StaticIdMap.put(id, ele_list);
                    ids.add(id);
                }
                //System.out.println("Element Name: " + currentElement.getName()); // 输出元素名称
                // 将当前元素的所有子元素压入栈中
                for (Element childElement : currentElement.elements()) {
                    if (childElement != null) {
//                        Element childElement = obj;
                        elementStack.push(childElement);
                    }
                }
            }
            FileIdMap.put(xml, ids);
        } catch (DocumentException e){
            e.printStackTrace();
        }
    }

    /**
     * 合并layout中的静态xml和dump的动态xml
     * @return
     */
    public static void mergeProcess(){

        for(Map.Entry<Element, String> d_entry : DynamicIdMap.entrySet()){
            Element de = d_entry.getKey();
            String d_id = d_entry.getValue();
            if(! d_id.contains(package_name)) {
                continue;
            }
            String s_id = d_id.replace(package_name + ":" , "@");
            if(StaticIdMap.containsKey(s_id)){
                List<Element> se_list = StaticIdMap.get(s_id);
                Element se;
                int rightIndex = 0;
                if(se_list.size() == 1){
                    se = se_list.get(0);
                }else{
                    rightIndex = chooseRightElement(s_id, se_list, de);
                    se = se_list.get(rightIndex);
                }
                File static_xml = IdMap.get(s_id).get(rightIndex);
                mergeElements(de, se, static_xml);
            }else{
                System.out.println("can't find " + s_id + " in static xml");
            }
        }
    }


    /**
     * 有多个id相同的静态element的情况下，选择出正确的与动态element对应的静态element
     * 以静态文件中的id和动态xml中的res-id重合数量为指标，选择数量最大的那个静态文件进行merge
     * @param id 以id为锚点
     * @param se_list id相同的静态element_list
     * @param de 动态element
     * @return 返回se_list的index
     */
    static int chooseRightElement(String id, List<Element> se_list, Element de){
        if(se_list == null) return -1;

        System.out.println(id);
        int maxSameIds = 0;
        int rightIndex = 0;
        for(int i=0; i<se_list.size(); i++){
            Element se = se_list.get(i);
            Element s_root = se;
            Element d_root = de;

            while(s_root.getParent() != null && de.getParent() != null){
                s_root = s_root.getParent();
                d_root = d_root.getParent();
            }

            File s_xml = IdMap.get(id).get(i);
            System.out.println("file name: " + s_xml);
            int sameIdCnt = countCommonStrings(FileIdMap.get(s_xml), getAllIdsUnderRootEle(d_root));

            // 能覆盖最多动态res-id的静态layout文件
            if(sameIdCnt > maxSameIds){
                rightIndex = i;
                maxSameIds = sameIdCnt;
            }
        }
        return rightIndex;
    }

    /**
     * 以root为根节点的树中的所有id
     * @param root
     * @return id_list
     */
    static List<String> getAllIdsUnderRootEle(Element root){
        if(root == null){
            System.out.println("null root");
            return null;
        }
        List<String> res = new ArrayList<>();
        if(DynamicIdMap.containsKey(root)){
            String id = DynamicIdMap.get(root);
            id = id.replace(package_name + ":", "@");
            res.add(id);
        }

        for(Element child : root.elements()){
            res.addAll(getAllIdsUnderRootEle(child));
        }
        return res;
    }

    // 返回连个字符串列表中相同的字符串个数
    public static int countCommonStrings(List<String> list1, List<String> list2) {
        if(list1 == null || list2 == null) return -1;
        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);

        // Find the intersection of two sets
        set1.retainAll(set2);

        return set1.size();
    }



    public static void mergeElements(Element de, Element se, File static_file){
        if(se == null || de == null) {
            return;
        }

        if(de.attribute("isMerged") != null){
            return;
        }

        if (de.attributeValue("isMerged") != null) {
            return;
        }

        if(!de.getName().contains(se.getName())) System.err.println("WARNING : merge " + de.getName() + " and " + se.getName());

        for(Iterator<Attribute> it = se.attributeIterator(); it.hasNext();) {
            Attribute a = it.next();
            if(a.getName().equals("text")) continue;
            de.addAttribute(a.getName(), a.getValue());
        }
        de.addAttribute("type", se.getName());
        de.addAttribute("isMerged", static_file.getName());
        mergeElements(de.getParent(), se.getParent(), static_file);
    }


    /**
     * 总流程函数
     * @param dynamic_xml 动态xml
     * @param LayoutBase 静态layout xml base
     * @param out_file 输出文件
     */
    static void mergeXmls(File dynamic_xml, File LayoutBase, String out_file){
//        getIds(LayoutBase);
        buildStaticIDMap(LayoutBase);

        try{
            SAXReader reader = new SAXReader();
            Document Dynamic_doc = reader.read(dynamic_xml);

            deleteUselessAttributesInDynamicXml(Dynamic_doc);

            Element Dynamic_root = Dynamic_doc.getRootElement();

            // 搜索整个页面的根节点
            Element Dynamic_target = findElementInDynamic(Dynamic_root, "android:id/content");

            Document DocMerged = DocumentHelper.createDocument();

            if (Dynamic_target != null) {
                Dynamic_root  = Dynamic_target.createCopy();
            }

            DocMerged.add(Dynamic_root);

            buildDynamicMap(Dynamic_root);

            mergeProcess();

            // 创建 XMLWriter
            FileWriter fileWriter = new FileWriter(out_file);
            XMLWriter writer = new XMLWriter(fileWriter);
            // 将 Document 对象写入文件
            writer.write(DocMerged);
            // 关闭 writer
            writer.close();

        } catch (DocumentException | IOException e) {
            throw new RuntimeException(e);
        }
    }




    //@id/simple_lat_text
    public static void main(String[] args){
        File dynamic_xml = new File("C:\\Users\\gaoshu\\Desktop\\textExamples\\linphone.xml");
        File layout_base = new File("C:\\Accessibility\\DataSet\\owleyeDataset\\linphone4.2.3\\linphone-android-debug-4.2.3\\res\\layout");

//        File dynamic_xml = new File("C:\\Users\\gaoshu\\Desktop\\textExamples\\democracy.xml");
//        File layout_base = new File("C:\\Accessibility\\DataSet\\owleyeDataset\\DemocracyDroid3.7.1\\apk\\DemocracyDroid-3.7.1\\res\\layout");
        String activity_name = "MainActivity";
        String out_path = "linphoneMergeTest.xml";
        mergeXmls(dynamic_xml, layout_base, out_path);
    }


}
