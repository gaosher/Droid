package view;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import tool.BugReporter;
import util.DimenValue;
import util.DisplayParams;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import util.MeasureSpec;
import tool.staticFilesPreProcess;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;


public class TextualView extends View {

    public String Text;
    int TextSize =(int) (49 * DisplayParams.DPI/DisplayParams.DEFAULT_DPI * DisplayParams.SCALING_PARAM); // 改成以px为单位，默认为14sp，受字体放大影响？
    int maxLines = -1;
    int minLines = -1;
    int textHeight = -1;
    int textWidth = -1;

    public static final int TEXTVIEW = 0;
    public static final int BUTTON = 1;
    public static final int EDITTEXT = 2;
    public static final int CHECKEDTEXTVIEW = 3;
    
    int textual_view_type = TEXTVIEW;

    public TextualView() {
    }


    public TextualView(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap, int type) {
        super(layoutParams, attrMap);
        // initial textual_view_type
        textual_view_type = type;
        
        // initial text
        String text = "text";
        if(attrMap.containsKey(text)){
//            System.out.println("containsKey text: " + attrMap.get(text));
            if(!attrMap.get(text).equals("")) {
                setText(attrMap.get(text));
            }else{
                System.out.println("No text in this Textual View");
            }
            attrMap.remove(text);
        }else System.out.println("No text in this Textual View"); // TODO: 考虑文本为空的特殊情况

        // initial text size
        String textSize = "textSize";
        if(attrMap.containsKey(textSize)){
            setTextSize(attrMap.get(textSize));
            attrMap.remove(textSize);
        }
        
        //initial maxLines
        String maxLines = "maxLines";
        if(attrMap.containsKey(maxLines)){
            setMaxLines(attrMap.get(maxLines));
            attrMap.remove(maxLines);
        }
        
        // initial minLines
        String minLines = "minLines";
        if(attrMap.containsKey(minLines)){
            setMinLines(attrMap.get(minLines));
            attrMap.remove(minLines);
        }

        // initial textAppearance
        if(attrMap.containsKey("textAppearance")){
            setTextAppearance(attrMap.get("textAppearance"));
            attrMap.remove("textAppearance");
        }

        // initial singleLine
        if(attrMap.containsKey("singleLine") && attrMap.get("singleLine").equals("true")){
            setMaxLines("1");
            attrMap.remove("singleLine");
        }

        if(type == BUTTON){
            if(paddingLeft == 0){
                paddingLeft = 16 * DisplayParams.DPI / 160;
            }
            if(paddingRight == 0){
                paddingRight = 16 * DisplayParams.DPI / 160;
            }
            if(paddingTop == 0){
                paddingTop = 10 * DisplayParams.DPI / 160;
            }
            if(paddingBottom == 0){
                paddingBottom = 10 * DisplayParams.DPI / 160;
            }
        }

        this.AttrMap = attrMap;
    }

    void setText(String text) {
        Text = text;
    }

    public void setMaxLines(String maxLines) {
        // TODO: 2024/5/19
        try{
            this.maxLines =  Integer.parseInt(maxLines);
        }catch (Exception e){
            System.err.println("can't parse minLines value : " + minLines);
        }
    }

    public void setMinLines(String minLines) {
        // TODO: 2024/5/19
        try{
            this.minLines = Integer.parseInt(minLines);
        }catch (Exception e){
            System.err.println("can't parse minLines value : " + minLines);
        }
    }

    void setTextSize(String textSize){
        if(textSize == null){
            System.err.println("null textSize value");
            return;
        }
        System.out.println(textSize);
        this.TextSize = DimenValue.parseTextSizeValue(textSize);
        System.out.println(TextSize);

        // CHECK POINT : TEXT SIZE UNIT todo 是否可以解耦到checkView函数中？
        if(TextSize < 0){
            BugReporter.writeUnitBug(View.packageName, this, "textSize");
        }
        System.out.println("end setTextSize");
    }

    void setTextAppearance(String textAppearance){
        if(textAppearance.contains("@style/")){
            textAppearance = textAppearance.replace("@style/", "");
            String textSizeDefined = getTextSizeFromStyles(textAppearance);
//            setText(textSizeDefined);
            setTextSize(textSizeDefined);
//            System.out.println(DimenValue.parseTextSizeValue(textSizeDefined));
        } else if (textAppearance.contains("?android:")) {
            System.out.println(textAppearance);
            textAppearance = textAppearance.replace("?android:", "");
            String text_size = staticFilesPreProcess.findValInJson(staticFilesPreProcess.TEXT_APPEARANCE_JSON, textAppearance);
            System.out.println(text_size);
            setTextSize(text_size);
        }
    }

    static String getTextSizeFromStyles(String styleName){
        String textSize = null;
        try {
            SAXReader reader = new SAXReader();
            Document style_doc = reader.read(staticFilesPreProcess.StylesXML);
            Element style_root = style_doc.getRootElement();
            List<Element> styles = style_root.elements();
            System.out.println("textAppearance: " + styleName);
            for(Element style : styles){
                if(style.attributeValue("name").equals(styleName)){
//                    System.out.println("1");
                    List<Element> items = style.elements();
                    if(items != null){
                        for (Element item : items){
                            if(item.attributeValue("name").equals("android:textSize")){
                                textSize = item.getText();
                                System.out.println("getTextSizeFromStyles: " + textSize);
                                return textSize;
                            }
                        }
                    }
                    String parentStyle = style.attributeValue("parent");
                    if (parentStyle != null && parentStyle.contains("@style/")){
                        return getTextSizeFromStyles(parentStyle.replace("@style/", ""));
                    }
                    break;
                }
            }
            return textSize;
        }catch (DocumentException e) {
//            System.err.println("DocumentException");
            throw new RuntimeException(e);
        }
    }


    /**
     * 将包含换行符的字符串分割成字符串数组。
     * @param input 包含换行符的字符串
     * @return 分割后的字符串数组
     */
    public static String[] splitStringByNewline(String input) {
        if (input == null || input.isEmpty()) {
            return new String[0]; // 返回空数组
        }
        return input.split("\\R");
    }

    static int getTextLines(String text, int textSize, int width, int type){
        System.out.println("width = " + width);
        if(text == null || text.length() == 0) return 0; // text为空直接返回

        if (textSize == Integer.MIN_VALUE) {
            System.err.println("can't parse textSize of text: " + text);
            return 0;
        }

        if(width <= 0){
            System.err.println("TextView: " + text + " has 0 width");
            return -1;
        }

        if(textSize < 0) textSize *= -1;

        double space_width = 0;// 单个字母的宽度 px
        if(type == BUTTON){
            space_width = textSize * DisplayParams.CAPITAL_TEXT_WIDTH_PARAM ;
        }else{
            space_width = textSize * DisplayParams.TEXT_WIDTH_PARAM ;
        }
//        space_width = Math.abs(space_width);

        int lines = 1;
//        System.out.println("width = " + width);
        int max_letters = (int) (width / space_width); // 一行能放下的最多的字母
//         System.out.println("max letter in a line : " + max_letters);
        String[] words = text.split(" ");

        int [] letterCnt = new int[words.length];

//        System.out.println(words.length);
        for(int i=0; i<words.length; i++){
            letterCnt[i] = words[i].length();
            // System.out.println(letterCnt[i]);
        }

        int left_letter_cnt = max_letters;
        for(int num : letterCnt){
//            System.out.println("num = " + num + "; max_letters = " + max_letters);
            if (left_letter_cnt <= 0){
                lines ++;
                left_letter_cnt = max_letters;
            }

            if(num > left_letter_cnt){ // 剩余空间无法容纳这个单词
                if(num > max_letters){ // 整行都无法容纳
                    // 使用上一行的剩余空间
                    int left_letters = num - left_letter_cnt; // 使用上一行的剩余空间后还剩下的字母数量
                    if(left_letters % max_letters < (max_letters - left_letter_cnt)){
//                        System.out.println(1);
                        lines += Math.ceil( (double) left_letters / max_letters);
                        left_letter_cnt =  max_letters - left_letters % max_letters;
                    }else{
                        System.out.println(2);
                        lines += Math.ceil( (double) num / max_letters);
                        left_letter_cnt = max_letters - num % max_letters;
                    }
                }else{ // 整行可以容纳，直接换到下一行
                    left_letter_cnt = max_letters - num;
//                    System.out.println(3);
                    lines ++;
                }
            } else { // 剩余空间可以容纳这个单词
                left_letter_cnt -= num;
                if(left_letter_cnt != 0) left_letter_cnt--; // 单词后的空格
            }
        }
        System.out.println("left letter cnt = " + left_letter_cnt);
//        System.out.println("lines = " + lines);
        return lines;
    }


    @Override
    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize){
//        System.out.println("TextualView.onMeasure Params: WidthMeasureSpecSize = " + WidthMeasureSpecSize +
//                "; HeightMeasureSpecSize = " + HeightMeasureSpecMode);
        System.out.println("---------start TextualView measurement: " + this.Text + "------------");
        showMeasureParams(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);

        int lineSum = 0;
        int letters_cnt = 0;

        // 确定文字宽度，需要去掉水平padding所占的空间
        int textWidth = WidthMeasureSpecSize - (this.paddingLeft + this.paddingRight);

        if(textual_view_type == BUTTON){
            if(AttrMap.containsKey("icon")){
                String icon = AttrMap.get("icon");
                if(icon.startsWith("@drawable/")){
                    icon = icon.replace("@drawable/", "");
                    int icon_width = 0, icon_height = 0;
                    int[] size = staticFilesPreProcess.getImages().get(icon);
                    if(size != null){
                        System.out.println("find icon in button!");
                        icon_width = size[0];
                        textWidth -= icon_width;
                        icon_height = size[1];
                        this.minHeight = icon_height;
                    }
                }
            }
        }

        for(String str : splitStringByNewline(this.Text)){
            // 记录字母最多的句子
            if(str.length() > letters_cnt) letters_cnt = str.length();
            int lines = getTextLines(str, this.TextSize, textWidth, textual_view_type);
            if(lines == -1){
                System.out.println("0 width error");
                showMeasureParams(WidthMeasureSpecMode, WidthMeasureSpecSize, HeightMeasureSpecMode, HeightMeasureSpecSize);
            }
            lineSum += getTextLines(str, this.TextSize, textWidth, textual_view_type);
        }

        // 确定组件宽度
        if(WidthMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredWidth = WidthMeasureSpecSize;
        } else if (WidthMeasureSpecMode == MeasureSpec.AT_MOST) {
            // 如果每行文字都很短的话，应该是使用最宽的文字
            int space_width = (int) (this.TextSize * DisplayParams.TEXT_WIDTH_PARAM); // 单字符的宽度
            space_width = Math.abs(space_width);
            this.textWidth = letters_cnt * space_width;
            this.measuredWidth = Math.min(textWidth + paddingLeft + paddingRight, WidthMeasureSpecSize);
        }else{
            System.err.println("unspecified width");
        }

        // 确定文本高度
        if(this.maxLines > 0){
            lineSum = Math.min(lineSum, this.maxLines);
            if(this.maxLines == 1){
                System.out.println();
            }
        }

        int textHeight;
        if(lineSum == 0){
            textHeight = 0;
        }else{
            textHeight = (int) Math.round(calTextHeight(lineSum, this.TextSize));
        }
        if(textual_view_type == EDITTEXT){
            textHeight += (73 * DisplayParams.DPI / DisplayParams.DEFAULT_DPI);
        }
        this.textHeight = textHeight;

//        System.out.println("textSize = " + this.TextSize + "; textHeight = " + textHeight);
        
        if(HeightMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredHeight = HeightMeasureSpecSize;
        } else if (HeightMeasureSpecMode == MeasureSpec.AT_MOST) {
            this.measuredHeight = Math.min((textHeight + paddingTop + paddingBottom), HeightMeasureSpecSize); // todo Padding?
        }

        // 测量细节输出
        System.out.println("text lines = " + lineSum);
        System.out.println("text size = " + this.TextSize);
        System.out.println("text height = " + textHeight);
        System.out.println("textView width = " + this.measuredWidth);
        System.out.println("textView height = " + this.measuredHeight);
        System.out.println("---------end TextualView measurement: " + this.Text + "------------");
    }

    /**
     * 计算文本所需的行高
     * @param lines 文本行数
     * @param textSize 字体大小
     * @return 文本所需的行高
     */
    static double calTextHeight(int lines, double textSize){
//        System.out.println("lines = " + lines);

        if(textSize == Integer.MIN_VALUE){
            // err
            return -1;
        }

        int singleLineHeight = 0; // 单行文本高度

        singleLineHeight = (int) Math.round(textSize * DisplayParams.TEXT_HEIGHT_PARAM);// 字体高度参数
//        System.out.println("singleLineHeight = " + singleLineHeight);

        double res = 0;
        if(lines == 1){
//            System.out.println("single Line");
            res = singleLineHeight;
        } else{
            // TODO: 2024/6/24 这个公式还需要更多的拟合，参数是否需要调整位置
            res = singleLineHeight + (lines - 1) * textSize * 4.1 / 3.5;
        }
        if (textSize < 0) {
            res *= -1;
        }
//        System.out.println("text Line = " + lines + ", text Height = " + res);
        return res;
    }


    @Override
    public void checkView(){
//        System.out.println("text: " + this.Text + " textual view measuredHeight = " + this.measuredHeight + "; textHeight = " + this.textHeight);
        // CHECK POINT 文字高度受限
        System.out.println("check Text view" + this.getText() + ": " + this.textHeight + " " + (this.measuredHeight - this.paddingTop - this.paddingBottom));
        if(this.textHeight > this.measuredHeight - this.paddingTop - this.paddingBottom){
            BugReporter.writeViewBug(View.packageName, BugReporter.TEXT_INCOMPLETE, this, null);
            BugReporter.writeInReport("text height = " + this.textHeight + "; view height = " + this.measuredHeight);
        }
    }

    public void showAllAttrs(){
        super.showAllAttrs();
        System.out.println("Text: " + this.Text);
        System.out.println("TextSize: " + this.TextSize);
        System.out.println("MaxLines: " + this.maxLines);
        System.out.println("MinLines: " + this.minLines);
    }

    public void printClassName(){
        System.out.println("Textual View");
    }

    public String getText() {
        return Text;
    }

    public static void main(String[] args){
//        int lines = getTextLines("ASSISTANT", 110, 1079);
//        System.out.println("lines = " + lines);

//        getTextSizeFromStyles("TextAppearance.AppCompat.Large");
//        staticFilesPreProcess.initial("C:/Accessibility/DataSet/owleyeDataset/transistor3.2.4/transistor-app-release-3.2.4/res");
//        setTextAppearance("@style/TextAppearance.AppCompat.Large");
        int lines = getTextLines("OpenTracks - OSM Dashboard", 91, 1258, TEXTVIEW);
        System.out.println(lines);
    }
}
