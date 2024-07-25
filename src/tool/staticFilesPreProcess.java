package tool;

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

import util.DimenValue;

import javax.imageio.ImageIO;


public class staticFilesPreProcess {

    static File ResBase;
    static File ValuesBase;
    public static File DimensXML;
    public static File StylesXML;
    static File Drawables;
    static File MipMaps;
    static File Drawable_nodpi;
    static HashMap<String, int[]> Images = new HashMap<>();

    public static HashMap<String, String> Dimens = new HashMap<>();


    static public void initial(String resBase) {

        System.out.println("------------------Start initial static files---------------");
        String valuesBase = resBase + "\\values";
        ValuesBase = new File(valuesBase);
        if (!ValuesBase.exists()) {
            System.err.println(valuesBase + " is a invalid path");
        }

        String dimenXml = valuesBase + "\\dimens.xml";
        DimensXML = new File(dimenXml);
        if (!DimensXML.exists()) {
            System.err.println(dimenXml + " is a invalid path");
        }

        String stylesXml = valuesBase + "\\styles.xml";
        StylesXML = new File(stylesXml);
        if (!StylesXML.exists()) {
            System.err.println(stylesXml + " is a invalid path");
        }

        String drawableBase = resBase + "\\drawable-xxxhdpi";
        Drawables = new File(drawableBase);
        if (!Drawables.exists()) {
            System.err.println(drawableBase + " is a invalid path");
        }

        String mipmapBase = resBase + "\\mipmap-xxxhdpi";
        MipMaps = new File(mipmapBase);
        if(!MipMaps.exists() || !MipMaps.isDirectory()){
            System.err.println(mipmapBase + " is a invalid path");
        }

        String drawable_nodpi = resBase + "\\drawable-nodpi";
        Drawable_nodpi = new File(drawable_nodpi);
        if(!Drawable_nodpi.exists() || !Drawable_nodpi.isDirectory()){
            System.err.println(drawable_nodpi + " is a invalid path");
        }

        System.out.println("start to parse dimens.xml...");
        parseDimens();
        System.out.println("start to parse styles.xml...");
        parseStyles();
        System.out.println("start to get images' sizes...");
        initalDrawables();
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

            // 2. 递归遍历整个文档，删除包含"color"或"animation"的节点
            removeNodes(document.getRootElement());

            // 3. 保存修改后的XML文件
            XMLWriter writer = new XMLWriter(new FileWriter(new File("output_styles.xml")));
            writer.write(document);
            writer.close();

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
            Document document = reader.read(DimensXML);
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

    static void initalDrawables(){
        File[] images = Drawables.listFiles();

        if(images != null){
            for(File img : images){
                try{
                    BufferedImage image = ImageIO.read(img);
                    if(image != null){
                        int width = image.getWidth();
                        int height = image.getHeight();
                        String imgName = img.getName();
                        imgName = imgName.replace("@drawable/", "");
                        if(imgName.endsWith(".png")){
                            imgName = imgName.substring(0, imgName.length()-4);
                        }else{
                            System.err.println(imgName + " is not a png");
                        }
                        Images.put(imgName, new int[]{width, height});
                    }else{
                        System.err.println("can't read picture" + img.getName());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }else{
            System.err.println("empty or wrong drawable base: " + Drawables.getAbsolutePath());
        }

        images = MipMaps.listFiles();
        if(images != null){
            for(File img : images){
                try{
                    BufferedImage image = ImageIO.read(img);
                    if(image != null){
                        int width = image.getWidth();
                        int height = image.getHeight();
                        String imgName = img.getName();
                        if(imgName.endsWith(".png")){
                            imgName = imgName.substring(0, imgName.length()-4);
                        }else{
                            System.err.println(imgName + " is not a png");
                        }
                        Images.put(imgName, new int[]{width, height});
                    }else{
                        System.err.println("can't read picture" + img.getName());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }else{
            System.err.println("empty or wrong MipMaps base: " + MipMaps.getAbsolutePath());
        }

        images = Drawable_nodpi.listFiles();
        if(images != null){
            for(File img : images){
                try{
                    String img_name = img.getName();

                    if(img_name.endsWith(".xml")){
                        img_name = img_name.replace(".xml", "");
                        SAXReader reader = new SAXReader();
                        Document doc = reader.read(img);
                        Element image = doc.getRootElement();
                        int width = 0, height = 0;
                        if(image.attribute("width") != null){
                            width = DimenValue.parseDimenValue2Px(image.attributeValue("width"));
                        }
                        if(image.attribute("height") != null){
                            height = DimenValue.parseDimenValue2Px(image.attributeValue("height"));
                        }
                        Images.put(img_name, new int[]{width, height});
                    }else if(img_name.endsWith(".webp")){
                        img_name = img_name.replace(".webp", "");
                        // 使用ImageIO读取WEBP图片
                        BufferedImage webp = ImageIO.read(img);
                        if (webp != null) {
                            // 获取图片的宽度和高度
                            int width = webp.getWidth();
                            int height = webp.getHeight();
                            Images.put(img_name, new int[]{width, height});
                        } else {
                            System.out.println("Failed to read the WEBP image.");
                        }
                    }
                } catch (DocumentException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }else{
            System.err.println("empty or wrong drawable-nodpi base: " + Drawable_nodpi.getAbsolutePath());
        }
    }

    public static HashMap<String, int[]> getImages() {
        return Images;
    }

    public static void main(String[] args) {
        initial("C:/Accessibility/DataSet/owleyeDataset/DemocracyDroid3.7.1/apk/DemocracyDroid-3.7.1/res/values");
        parseStyles();
    }
}
