package tool;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import java.io.File;
import java.util.*;

import util.DimenValue;

import javax.swing.text.Style;

public class staticFilesPreProcess {

    static File ValuesBase;
    static File DimensXML;
    static File StylesXML;
    public static HashMap<String, Double> Dimens = new HashMap<>();


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
            Iterator<Element> dimens = root.elementIterator();
            HashMap<String, String> DimenValues = new HashMap<>();
            while (dimens.hasNext()) {
                Element dimen = dimens.next();

                String name = dimen.attributeValue("name");
                String dimenVal = dimen.getText();

                // <item type="dimen" name="notification_media_narrow_margin">@dimen/notification_content_margin_start</item>
                if (!dimen.getName().equals("dimen")) {
                    if (dimen.getText().contains("@dimen/")){
                        DimenValues.put(dimen.attributeValue("name"), dimen.getText());
                        continue;
                    }else{
                        name = dimen.attributeValue("name");
                        dimenVal = dimen.getText();
                    }
                }

                double val;
                if (dimenVal.endsWith("%") ||
                        (dimenVal.charAt(dimenVal.length() - 1) >= '0' && dimenVal.charAt(dimenVal.length() - 1) <= '9')) { // 处理%
                    val = parsePercentageDimen(dimenVal);
                } else { // 处理普通dimen，单位px
                    val = DimenValue.parseDimenValue2Px(dimenVal);
                }
                Dimens.put(name, val);
            }

            for (Map.Entry e : DimenValues.entrySet()) {
                String name = (String) e.getKey();
                String dimenVal = (String) e.getValue();
                System.out.println(name + " " + dimenVal);
                dimenVal = dimenVal.replace("@dimen/", "");
                if (Dimens.containsKey(dimenVal)) {
                    Dimens.put(name, Dimens.get(dimenVal));
                } else {
                    System.err.println("can't find " + dimenVal + " in dimens.xml");
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

    static public void initial(String valuesBase) {
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

        parseDimens();
        parseStyles();
    }

    public static void main(String[] args) {
        initial("C:/Accessibility/DataSet/owleyeDataset/DemocracyDroid3.7.1/apk/DemocracyDroid-3.7.1/res/values");
        parseStyles();
    }
}
