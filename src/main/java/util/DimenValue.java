package util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import tool.staticFilesPreProcess;

public class DimenValue {

    public static final int FILL_PARENT = -1;
    public static final int WRAP_CONTENT = -2;
//    final static  int DPI = 560;
    final static double fontScale = 1.0;
    private static final String[] DIMENSION_UNIT_STRS = new String[] {
            "px", "dip", "sp", "pt", "in", "mm"
    };

    static HashMap<String, Double> Dimens = new HashMap<>(); //全都用px计算
    static HashMap<String, Double> DoubleDimens = new HashMap<>(); // 一些无单位的值，百分比等
    static HashMap<String, Integer> Drawables = new HashMap<>();
    static HashMap<String, Integer> Integers = new HashMap<>();
    // TODO: 2024/5/20
    static HashMap<String, String> Styles = new HashMap<>();


//    /**
//     * 解析dimens.xml
//     * 还有%结尾情况
//     * @param dimenPath 路径
//     */
//    static void parseDimen(String dimenPath){//dimens.xml
//        try {
//            SAXReader reader = new SAXReader();
//            Document document = reader.read(new File(dimenPath));
//            Element root = document.getRootElement();
//            Iterator<Element> dimens = root.elementIterator();
//            HashMap<String, String> DimenValues = new HashMap<>();
//            while(dimens.hasNext()){
//                Element dimen = dimens.next();
//                String name = dimen.attributeValue("name");
//                String dimenVal = dimen.getText();
//                if(dimenVal.endsWith("%") ||
//                        (dimenVal.charAt(dimenVal.length()-1) >= '0' && dimenVal.charAt(dimenVal.length()-1) <= '9')  ){ // 处理%
//                    double val = parsePercentageDimen(dimenVal);
//                    Dimens.put(name, val);
//                } else if (dimenVal.startsWith("@dimen/")) { // 处理@dimen
//                    if(Dimens.containsKey(dimenVal)){
//                        Dimens.put(name, Dimens.get(dimenVal));
//                    }else if(DoubleDimens.containsKey(dimenVal)){
//                        DoubleDimens.put(name, DoubleDimens.get(dimenVal));
//                    }else{
//                        DimenValues.put(name, dimenVal);
//                    }
//                } else{ // 处理普通dimen，单位px
//                    double val = parseDimenValue2Px(dimenVal);
//                    Dimens.put(name, val);
//                }
//            }
//
//            for (Map.Entry<String, String> e : DimenValues.entrySet()){
//                String name = e.getKey();
//                String dimenVal = e.getValue();
//                dimenVal = dimenVal.replace("@dimen/", "");
//                if(Dimens.containsKey(dimenVal)){
//                    Dimens.put(name, Dimens.get(dimenVal));
//                }else if(DoubleDimens.containsKey(dimenVal)){
//                    DoubleDimens.put(name, DoubleDimens.get(dimenVal));
//                }else{
//                    System.err.println("can't find " + dimenVal + " in dimens.xml");
//                }
//            }
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }
//    }

    static void parseInteger(String IntegerPath){
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(IntegerPath));
            Element root = document.getRootElement();
            Iterator<Element> Ints = root.elementIterator();
            while(Ints.hasNext()){
                Element Int = Ints.next();
                String name = Int.attributeValue("name");
                Integers.put(name, Integer.parseInt(Int.getText()));
            }
        }catch (DocumentException e){
            e.printStackTrace();
        }
    }


    public static int parseDimenValue2Px(String dimenValue){
        int px = 0;
        double val = 0;
        int len = dimenValue.length();

        if(dimenValue.equals("fill_parent") || dimenValue.equals("match_parent")) return FILL_PARENT;
        if(dimenValue.equals("wrap_content")) return WRAP_CONTENT;

        if(dimenValue.startsWith("@dimen/")){
            // TODO: 2024/5/17
            dimenValue = dimenValue.replace("@dimen/", "");
            return parseDimenValue2Px(staticFilesPreProcess.Dimens.get(dimenValue));
        }

        if(dimenValue.startsWith("?")){
            dimenValue = dimenValue.replace("?", "");
            String valInJson = staticFilesPreProcess.findValInJson(staticFilesPreProcess.DIMEN_JSON, dimenValue);
            if (valInJson != null) {
                return parseDimenValue2Px(valInJson);
            }else{
                System.err.println("Unknown unit type: " + "?" + dimenValue);
            }
        }

        if(dimenValue.endsWith("dip")){
            String double_str = dimenValue.substring(0, len - 3);
            val = Double.parseDouble(double_str);
            //System.out.println(val);
            return dip2px(val);
        } else if (dimenValue.endsWith("dp")) {
            String double_str = dimenValue.substring(0, len - 2);
            val = Double.parseDouble(double_str);
            return dip2px(val);
        } else if(dimenValue.endsWith("px")){
            String double_str = dimenValue.substring(0, len - 2);
            val = Double.parseDouble(double_str);
            return (int) Math.round(val);
        } else if (dimenValue.endsWith("sp")) {
            String double_str = dimenValue.substring(0, len - 2);
            val = Double.parseDouble(double_str);
//            System.err.println("Warning" + dimenValue + "use sp as unit");
            return sp2px(val);
        } else{
            System.err.println("Unknown unit type: " + dimenValue);
        }
        return px;
    }

    static double parsePercentageDimen(String dimenValue){
        int len = dimenValue.length();
        if(dimenValue.endsWith("%") && len > 1){
            return Double.parseDouble(dimenValue.substring(0, len-1)) / 100;
        }else if(dimenValue.charAt(len-1) >= '0' && dimenValue.charAt(len-1) <= '9'){
            return Double.parseDouble(dimenValue);
        }else {
            System.err.println("can't parse" + dimenValue);
            return Double.MAX_VALUE;
        }
    }
    
    static double parseDimenValueInDimens(String dimenValue){
        // TODO: 2024/5/20
        int len = dimenValue.length();
        if(dimenValue.startsWith("@dimen/")){
            String dimenName = dimenValue.substring(7, len);
            if(Dimens.containsKey(dimenName)){
                return Dimens.get(dimenName);
            }else if (DoubleDimens.containsKey(dimenName)){
                return Integer.MIN_VALUE;
            }else{
                System.err.println("can't find" + dimenName + "in current hashmaps");
                return Integer.MIN_VALUE + 1;
            }
        }else{
            System.err.println("can't parse" + dimenValue);
            return Integer.MIN_VALUE + 2;
        }
    }

    static double parseDimenValueInPercentage(String dimenValue){
        String dimenName = dimenValue.replace("@dimen/", "");
        if (DoubleDimens.containsKey(dimenName)){
            return DoubleDimens.get(dimenName);
        }else{
            return -1;
        }
    }

    public static int parseTextSizeValue(String textSizeValue){ // 返回px
        int len = textSizeValue.length();
        double val = Double.MIN_VALUE;
        int res = Integer.MIN_VALUE;
        if(textSizeValue.startsWith("@dimen/")){
            textSizeValue = textSizeValue.replace("@dimen/", "");
//            System.out.println("find in dimens");
            return parseTextSizeValue(staticFilesPreProcess.Dimens.get(textSizeValue));
        }

        if (textSizeValue.endsWith("sp")) {
            String double_str = textSizeValue.substring(0, len - 2);
            val = Double.parseDouble(double_str);
//            System.out.println("find ends with sp");
            res = sp2px(val);
        }else {
//            System.err.println("Warning" + textSizeValue + "not use sp as unit, may cause Unresponsive issues");
            if(textSizeValue.endsWith("dip")){
                String double_str = textSizeValue.substring(0, len - 3);
                val = Double.parseDouble(double_str);
            } else if (textSizeValue.endsWith("dp")) {
                String double_str = textSizeValue.substring(0, len - 2);
                val = Double.parseDouble(double_str);
            }
            // 非sp为单位的字体大小值解析为附属
            res = -dip2px(val);
//            System.err.println(res);
        }
        return res;
    }

    static int dip2px(double dip){
        return (int) (dip * DisplayParams.DPI / 160);
    }
    static int sp2px(double sp){
        return (int) (sp * DisplayParams.DPI * DisplayParams.SCALING_PARAM / 160);
    }

    public static void main(String[] args){
//        parseDimen("C:\\Accessibility\\DataSet\\owleyeDataset\\DemocracyDroid3.7.1\\apk\\DemocracyDroid-3.7.1\\res\\values\\dimens.xml");
//        showHashMaps();
        //parseInteger("C:\\Accessibility\\DataSet\\owleyeDataset\\DemocracyDroid3.7.1\\apk\\DemocracyDroid-3.7.1\\res\\values\\integers.xml");
        //showHashMaps();
    }
}
