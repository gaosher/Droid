package tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.*;

import polyglot.visit.DataFlow;
import util.DimenValue;

import javax.imageio.ImageIO;


public class staticFilesPreProcess {

    static File ResBase;
    static File ValuesBase;
    static File Value_xhdpiBase;
    public static List<File> DimensXMLs;
//    public static File Dimens_xhpidXML;
    public static File StylesXML;

    static HashMap<String, int[]> Images = new HashMap<>();

    public static HashMap<String, String> Dimens = new HashMap<>();


    static public void initial(String resBase) {
        if(DimensXMLs == null) DimensXMLs = new ArrayList<>();

        System.out.println("------------------Start initial static files---------------");
        String valuesBase = resBase + "\\values";
        ValuesBase = new File(valuesBase);
        if (!ValuesBase.exists()) {
            System.err.println(valuesBase + " is a invalid path");
        }

        String values_xhdpiBase = resBase + "\\values-xhdpi";
        Value_xhdpiBase = new File(values_xhdpiBase);
        if (!Value_xhdpiBase.exists()) {
            System.err.println(valuesBase + " is a invalid path");
        }

        String dimenXml = valuesBase + "\\dimens.xml";
        File Dimens = new File(dimenXml);
        if (!Dimens.exists()) {
            System.err.println(dimenXml + " is a invalid path");
        }else{
            DimensXMLs.add(Dimens);
        }

        String dimen_xhdpiXml = values_xhdpiBase + "\\dimens.xml";
        File Dimens_xhdpi = new File(dimen_xhdpiXml);
        if (!Dimens_xhdpi.exists()) {
            System.err.println(dimen_xhdpiXml + " is a invalid path");
        }else{
            DimensXMLs.add(Dimens_xhdpi);
        }


        String stylesXml = valuesBase + "\\styles.xml";
        StylesXML = new File(stylesXml);
        if (!StylesXML.exists()) {
            System.err.println(stylesXml + " is a invalid path");
        }

        System.out.println("start to parse dimens.xml...");
        parseDimens();
        System.out.println("start to parse styles.xml...");
        parseStyles();
        System.out.println("start to get images' sizes...");
        initialDrawables(resBase);
    }

    static void parseStyles() {
        deleteUselessItems(StylesXML);
    }


    // TODO: 2024/7/15 styles.xml
    public static void deleteUselessItems(File inputFile) {//styles.xml
        try {
            // 1. 读取XML文件
//            File inputFile = new File("C:/Accessibility/DataSet/owleyeDataset/DemocracyDroid3.7.1/apk/DemocracyDroid-3.7.1/res/values/styles.xml");
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputFile);
            Element root = document.getRootElement();

            // 2. 递归遍历整个文档，删除包含"color"或"animation"的节点
//            removeNodes(document.getRootElement());
            List<Element> styles = root.elements();
            for(Element style : styles){
                List<Element> items = style.elements();
                if(items == null) continue;
                List<Element> to_be_delete = new ArrayList<>();
                for (Element item : items){
                    String val = item.getText();
                    if(val.contains("@color") || val.contains("@anim") || val.contains("@null")){
                        to_be_delete.add(item);
                    }
                }
                for(Element item : to_be_delete){
                    items.remove(item);
                }
            }

            // 3. 保存修改后的XML文件
            File newStyles = new File(ValuesBase.getAbsolutePath() + "\\new_styles.xml");
            XMLWriter writer = new XMLWriter(new FileWriter(newStyles));
            writer.write(document);
            writer.close();
            StylesXML = newStyles;
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeNodes(Element element) {
        Iterator<Node> iterator = element.nodeIterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node instanceof Element childElement) {
                String childText = childElement.asXML();
                if (childText.contains("#") || childText.contains("color") || childText.contains("Color") || childText.contains("@anim") || childText.contains("animation")
                        || childText.contains("@null")) {
                    iterator.remove();
                } else {
                    // 递归处理子节点
                    removeNodes(childElement);
                }
            }
        }
    }


    static void parseDimens() { //dimens.xml
        try {
            SAXReader reader = new SAXReader();
            for(File dimensXml : DimensXMLs){
                Document document = reader.read(dimensXml);
                Element root = document.getRootElement();
                List<Element> dimens = root.elements();
                HashMap<String, String> items = new HashMap<>();
                for(Element dimen : dimens) {
                    String name = dimen.attributeValue("name");
                    String dimenVal = dimen.getText();
                    if(dimen.getName().equals("dimen")){ // <dimen>
                        Dimens.put(name, dimenVal);
                    }else { // <item>
                        if(dimenVal.contains("@dimen/")){
                            dimenVal = dimenVal.replace("@dimen/", "");
                            items.put(name, dimenVal);
                        }else{
                            Dimens.put(name, dimenVal);
                        }
                    }
                }

                // 处理<item>
                for (HashMap.Entry<String, String> e : items.entrySet()) {
                    String name = e.getKey();
                    String item_val = e.getValue();
                    if (Dimens.containsKey(item_val)) {
                        Dimens.put(name, Dimens.get(item_val));
                    } else {
                        System.err.println("can't find " + item_val + " in dimens.xml");
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    static double parsePercentageDimen(String dimenValue) {
        int len = dimenValue.length();
        if (dimenValue.endsWith("%") && len > 1) {
            return Double.parseDouble(dimenValue.substring(0, len - 1)) / 100;
        } else if (dimenValue.charAt(len - 1) >= '0' && dimenValue.charAt(len - 1) <= '9') {
            return Double.parseDouble(dimenValue);
        } else {
            System.err.println("can't parse" + dimenValue);
            return Double.MAX_VALUE;
        }
    }

    static void initialDrawables(String resBase){
        File Drawables_XXXH;
        File Drawables;
        File MipMaps;
        File Drawable_nodpi;
        List<File> all_image_bases = new ArrayList<>();

        String drawable_xxxhBase = resBase + "\\drawable-xxxhdpi";
        Drawables_XXXH = new File(drawable_xxxhBase);
        if (!Drawables_XXXH.exists()) {
            System.err.println(drawable_xxxhBase + " is a invalid path");
        }else all_image_bases.add(Drawables_XXXH);

        String drawableBase = resBase + "\\drawable";
        Drawables = new File(drawableBase);
        if (!Drawables.exists()) {
            System.err.println(drawableBase + " is a invalid path");
        }else all_image_bases.add(Drawables);

        String mipmapBase = resBase + "\\mipmap-xxxhdpi";
        MipMaps = new File(mipmapBase);
        if(!MipMaps.exists() || !MipMaps.isDirectory()){
            System.err.println(mipmapBase + " is a invalid path");
        }else all_image_bases.add(MipMaps);

        String drawable_nodpi = resBase + "\\drawable-nodpi";
        Drawable_nodpi = new File(drawable_nodpi);
        if(!Drawable_nodpi.exists() || !Drawable_nodpi.isDirectory()){
            System.err.println(drawable_nodpi + " is a invalid path");
        }else all_image_bases.add(Drawable_nodpi);

        for(File base : all_image_bases){
            File[] images = base.listFiles();
            if (images != null) {
                for(File img : images){
                    if(img.getName().endsWith(".xml")){
                        getPicSizeFromXml(img, false);
                    }else{
                        getPicSizeFromImage(img);
                    }
                }
            }
        }

        System.out.println("processing waiting list...");
        for(File img : wait_list){

            if(img.getName().endsWith(".xml")){
                getPicSizeFromXml(img, true);
            }else{
                getPicSizeFromImage(img);
            }
        }
    }

    static List<File> wait_list = new ArrayList<>();
    static void getPicSizeFromXml(File xml, boolean isWaitList){
        try{
            String img_name = xml.getName().replace(".xml", "");
            SAXReader reader = new SAXReader();
            Document doc = reader.read(xml);
            Element image = doc.getRootElement();
            int width = 0, height = 0;
            boolean isValid = false;
            if(image.getName().equals("vector") || image.getName().equals("shape")){
                if(image.attribute("width") != null){
                    isValid = true;
                    width = DimenValue.parseDimenValue2Px(image.attributeValue("width"));
                }
                if(image.attribute("height") != null){
                    isValid = true;
                    height = DimenValue.parseDimenValue2Px(image.attributeValue("height"));
                }
                if(isValid) {
                    Images.put(img_name, new int[]{width, height});
//                    System.out.println(img_name + ": " + width + " " + height);
                }
            }else if(image.getName().equals("selector") || image.getName().equals("layer-list")){
                List<Element> items = image.elements();
                if(items == null) return;
                for(Element item : items){
                    String drawable = item.attributeValue("drawable");
                    if(drawable == null) continue;
                    drawable = drawable.replace("@drawable/", "");
                    if(Images.containsKey(drawable)){
                        Images.put(img_name, Images.get(drawable));
                    }else {
                        if(isWaitList){
                            System.out.println("can't find " + drawable + " in Images");
                            return;
                        }
//                        System.out.println("add" + xml + " to waiting list");
                        wait_list.add(xml);
                    }
                    return;
                }
            }
            else{
                System.out.println(xml.getName() + ": " + image.getName());
            }

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    static void getPicSizeFromImage(File image){
        try{
            String img_name = image.getName();
            if(img_name.endsWith(".png")){
                img_name = img_name.replace(".png", "");
                BufferedImage img = ImageIO.read(image);
                int width = img.getWidth();
                int height = img.getHeight();
                Images.put(img_name, new int[]{width, height});
            }
            else if(img_name.endsWith(".webp")){
                img_name = img_name.replace(".webp", "");
                // 使用ImageIO读取WEBP图片
                BufferedImage webp = ImageIO.read(image);
                if (webp != null) {
                    // 获取图片的宽度和高度
                    int width = webp.getWidth();
                    int height = webp.getHeight();
                    Images.put(img_name, new int[]{width, height});
                } else {
                    System.out.println("Failed to read the WEBP image.");
                }
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, int[]> getImages() {
        return Images;
    }


    public static final int TEXT_APPEARANCE_JSON = 1;
    public static final int DIMEN_JSON = 2;
    public static final int STYLE_JSON = 3;
    public static String findValInJson(int JSON, String key){
        ObjectMapper objectMapper = new ObjectMapper();
        String value = null;
        try {
            // 读取 JSON 文件
            JsonNode rootNode = null;
            switch (JSON){
                case TEXT_APPEARANCE_JSON -> rootNode = objectMapper.readTree(new File("JSON\\textAppearance.json"));
                case DIMEN_JSON -> rootNode = objectMapper.readTree(new File("JSON\\dimens.json"));
                case STYLE_JSON -> rootNode = objectMapper.readTree(new File("JSON\\styles.json"));
            }

            if(rootNode == null) return null;

            // 搜索特定的键值对
            value = searchJson(rootNode, key);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(value == null){
            System.err.println("no such key as " + key);
        }
        return value;
    }

    private static String searchJson(JsonNode rootNode, String searchKey) {
        if (rootNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (key.equals(searchKey)) {
                    return value.asText();
                }
            }
        }

        return null;
    }

    public static void main(String[] args) {
        initial("C:\\Accessibility\\DataSet\\owleyeDataset\\mtg-familiar3.6.4\\mtgfam_3.6.4\\res");
//        System.out.println(Arrays.toString(Images.get("btn_check_c")));
//        parseStyles();
//        System.out.println(findValInJson(TEXT_APPEARANCE_JSON, "textAppearanceMedium"));
    }
}
