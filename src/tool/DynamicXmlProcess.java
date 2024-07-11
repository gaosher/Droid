package tool;

import heros.fieldsens.AccessPath;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class DynamicXmlProcess {

    static HashMap<Element, String> DynamicIdMap = new HashMap<>();
    static HashMap<String, Element> StaticIdMap = new HashMap<>();


    static String [] uselessAttrs = {"package", "checkable", "checked", "clickable", "enabled", "focusable", "focused",
            "long-clickable", "password", "selected", "displayed", "content-desc"
    };
    static HashSet<String> uselessAttrsSet = new HashSet<>(Arrays.asList(uselessAttrs));//xml dump中无用的属性集合

    /**
     * 删去动态xml dump中无用的属性
     * @param doc
     */
    static void deleteUselessAttributesInDynamicXml(Document doc){
        Element root = doc.getRootElement();
        deleteUselessAttributesInElem(root);
    }

    /**
     * 删去元素中的无用属性
     * @param e
     */
    static void deleteUselessAttributesInElem(Element e){
        List<Attribute> toBeDel = new ArrayList<>();
        for(Iterator<Attribute> it = e.attributeIterator(); it.hasNext();) {
            Attribute a = it.next();
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
     * 合并layout中的静态xml和dump的动态xml
     * @param Dynamic_doc
     * @param static_xml
     * @param pkg_name app包名
     * @param out_file 输出文件
     * @return
     */
    public static Document mergeXml(Document Dynamic_doc, File static_xml, String pkg_name, String out_file){
        try {
            // 读取 XML 文件
            SAXReader reader = new SAXReader();
            //Document Dynamic_doc = reader.read(dynamic_xml);
            Document Static_doc = reader.read(static_xml);
            // 获取根节点
            Element Dynamic_root = Dynamic_doc.getRootElement();
            Element Static_root = Static_doc.getRootElement();

            buildDynamicMap(Dynamic_root);
            buildStaticMap(Static_root);

            for(Map.Entry<Element, String> d_entry : DynamicIdMap.entrySet()){
                Element de = d_entry.getKey();
                String d_id = d_entry.getValue();
                if(! d_id.contains(pkg_name)) {
                    continue;
                }
                String s_id = d_id.replace(pkg_name+":", "@");
                if(StaticIdMap.containsKey(s_id)){
                    Element se = StaticIdMap.get(s_id);
                    mergeElements(de, se, static_xml.getName());
                    //ViewMap.put(de, se);
                }else{
                    System.out.println("can't find " + s_id + " in static xml");
                }
            }
            return Dynamic_doc;

        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

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
     * 为静态xml生成一个Element-(androd:id) HashMap
     * @param root 静态xml doc的根节点
     */
    public static void buildStaticMap(Element root){
        if(root.attribute("id") != null){
            StaticIdMap.put(root.attributeValue("id"), root);
        }
        List<Element> children = root.elements();
        for (Element child : children) {
            buildStaticMap(child);
        }
    }

    public static void showIdMap(HashMap<Element, String> map){
        for(Map.Entry<Element, String> entry : map.entrySet()){
            System.out.println(entry.getKey().getName() + " " + entry.getValue());
        }
    }

//    public static Element findElementInStatic(Element root, String id){
//        //id = "@id/simple_lat_text";
//        if(root.attribute("id") != null && root.attributeValue("id").equals(id)){
//            return root;
//        }
//        Element res = null;
//        List<Element> children = root.elements();
//        for (Element child : children) {
//            res = findElementInStatic(child, id);
//            if(res != null) return res;
//        }
//        return res;
//    }

    public static void mergeElements(Element de, Element se, String static_file_name){
        if(se == null || de == null) {
            //System.out.println("parent is null");
            return;
        }
        if (de.attributeValue("isMerged") !=null) {
            return;
            //mergeElements(de.getParent(), se.getParent());
        }
        if(!de.getName().contains(se.getName())) System.out.println("warning");
        System.out.println("merge " + de.getName() + " and " + se.getName());

        for(Iterator<Attribute> it = se.attributeIterator(); it.hasNext();) {
            Attribute a = it.next();
            if(a.getName().equals("text")) continue;
            de.addAttribute(a.getName(), a.getValue());
        }
        de.addAttribute("type", se.getName());
        de.addAttribute("isMerged", static_file_name);
        mergeElements(de.getParent(), se.getParent(), static_file_name);
    }

    static HashMap<String, String> IdMap = new HashMap<>();
    static void getIds(File BasePath){
        if (BasePath.isDirectory() && BasePath.exists()) {
            File[] files = BasePath.listFiles();  // 获取文件夹中所有文件数组
            if (files != null) {
                for (File xml : files) {
                    if (xml.isFile()) {
                        //System.out.println("文件: " + xml.getName());
                        getOneXmlIds(xml);
                    }
                }
            }
        } else {
            System.out.println("指定的路径并不是一个存在的文件夹");
        }
    }

    static void getOneXmlIds(File xml){
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(xml);
            //Root element
            Element root = document.getRootElement();
            // 使用栈来保存待处理的元素
            Stack<Element> elementStack = new Stack<>();
            elementStack.push(root);
            String root_id = root.attributeValue("id");
            if(root_id != null) IdMap.put(root_id, xml.getName());

            // 迭代遍历所有元素
            while (!elementStack.isEmpty()) {
                Element currentElement = elementStack.pop();
                //System.out.println("Element Name: " + currentElement.getName()); // 输出元素名称
                // 将当前元素的所有子元素压入栈中
                for (Object obj : currentElement.elements()) {
                    if (obj instanceof Element) {
                        Element childElement = (Element) obj;
                        elementStack.push(childElement);
                        String id = childElement.attributeValue("id");
                        if(id != null){
                            IdMap.put(id, xml.getName());
                        }
                    }
                }
            }
        } catch (DocumentException e){
            e.printStackTrace();
        }
    }


    static Set<File> getStaticXmls(Document Doc, File LayoutBase, String pkg_name){
        Element root = Doc.getRootElement();
        Set<File> xml_files = new HashSet<>();
        getStaticXml(root, pkg_name);
        for(String name : xml_names){
            // 检查文件夹是否存在
            if (LayoutBase.exists() && LayoutBase.isDirectory()) {
                // 获取文件夹中的所有文件
                File[] files = LayoutBase.listFiles();
                // 遍历所有文件
                if (files != null) {
                    for (File file : files) {
                        if(file.getName().equals(name)){
                            System.out.println(file.getName());
                            xml_files.add(file);
                        }
                    }
                }
            } else {
                System.out.println("Folder does not exist or is not a directory.");
            }
        }
        return xml_files;
    }

    static Set<String> xml_names = new HashSet<>();
    static void getStaticXml(Element root,  String pkg_name){

        if(root.attribute("resource-id") != null){
            String resource_id = root.attributeValue("resource-id");
            //System.out.println("resource_id: " + resource_id);
            if(resource_id.contains(pkg_name)){
                String static_id = resource_id.replace(pkg_name+":", "@");
                //System.out.println("static_id: " + static_id);
                String file_name = IdMap.get(static_id);
                if(file_name != null){
                    //System.out.println("file name: " + file_name);
                    xml_names.add(file_name);
                }else{
                    System.out.println("cannot find" + static_id + "in static layouts");
                }
            }
        }
        List<Element> children = root.elements();
        if(children.size() == 0) return;
        for (Element child : children) {
            getStaticXml(child, pkg_name);
        }
    }

    static void mergeXmls(File dynamic_xml, File LayoutBase, String pkg_name, String out_file){
        getIds(LayoutBase);
        try{
            SAXReader reader = new SAXReader();
            Document Dynamic_doc = reader.read(dynamic_xml);
            deleteUselessAttributesInDynamicXml(Dynamic_doc);
            Element Dynamic_root = Dynamic_doc.getRootElement();
            Element Dynamic_target = findElementInDynamic(Dynamic_root, "android:id/content");
            Document DocMerged = DocumentHelper.createDocument();
            Dynamic_root  = Dynamic_target.createCopy();
            DocMerged.add(Dynamic_root);

            Set<File> XmlsToBeMerged = getStaticXmls(DocMerged, LayoutBase, pkg_name);
            for(File xml : XmlsToBeMerged){
                if (DocMerged != null) {
                    DocMerged = mergeXml(DocMerged, xml, pkg_name, out_file);
                }
            }

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
        File dynamic_xml = new File("C:\\Users\\gaoshu\\Desktop\\textExamples\\democracy.xml");
        File layout_base = new File("C:\\Accessibility\\DataSet\\owleyeDataset\\DemocracyDroid3.7.1\\apk\\DemocracyDroid-3.7.1\\res\\layout");
        String pkg_name = "com.workingagenda.democracydroid";
        String activity_name = "MainActivity";
        String out_path = "C:\\Users\\gaoshu\\Desktop\\textExamples\\" + "merged_" + pkg_name + "." + activity_name+ ".xml";
        mergeXmls(dynamic_xml, layout_base, pkg_name, out_path);
    }


}
