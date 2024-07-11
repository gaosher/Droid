package view;
import util.BugReporter;
import util.DimenVaule;
import util.DisplayParams;

import java.util.HashMap;

import util.MeasureSpec;

public class TextualView extends View {

    String Text;
    int TextSize = 49; // 改成以px为单位，默认为14sp，受字体放大影响？
    int maxLines = -1;
    int minLines = -1;

    int textHeight = -1;

    public TextualView() {
    }


    public TextualView(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap) {

        mLayoutParams = layoutParams;
        layoutParams.setLayoutParams(attrMap);

        initialBasicAttrs(attrMap);
        super.view_type = "Textual view";
        // initial text
        String text = "text";
        if(attrMap.containsKey(text)){
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
        // TODO:
        this.TextSize = DimenVaule.parseTextSizeValue(textSize);

        // CHECK POINT : TEXT SIZE UNIT
        if(TextSize < 0){
            BugReporter.writeUnitBug(View.packageName, BugReporter.UNIT_ERROR, this, "textSize", textSize);
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

    static int getTextLines(String text, int textSize, int width){

        // TODO: 2024/6/21 是否与显示设置相关？
        double space_width = textSize * DisplayParams.TEXT_WIDTH_PARAM ; // 单个字母的宽度 px

        if(textSize > 0){ // sp 为单位
            space_width *= DisplayParams.SCALING_PARAM;
        } else if (textSize == Integer.MIN_VALUE) {
            System.err.println("can't parse textSize of text: " + text);
            return 0;
        }else { // 非sp为单位
            space_width = -space_width;
        }

        if(text == null || text.length() == 0) return 0; // text为空直接返回

        int lines = 1;

        int max_letters = (int) (width / space_width); // 一行能放下的最多的字母
        // System.out.println("max letter in a line : " + max_letters);
        String[] words = text.split(" ");

        int [] letterCnt = new int[words.length];

        for(int i=0; i<words.length; i++){
            letterCnt[i] = words[i].length();
            // System.out.println(letterCnt[i]);
        }

        int left_letter_cnt = max_letters;
        for(int num : letterCnt){
            if (left_letter_cnt <= 0){
                lines ++;
                left_letter_cnt = max_letters;
            }

            if(num > left_letter_cnt){ // 剩余空间无法容纳这个单词
                if(num > max_letters){ // 整行都无法容纳
                    // 使用上一行的剩余空间
                    int left_letters = num - left_letter_cnt; // 使用上一行的剩余空间后还剩下的字母数量
                    if(left_letters % max_letters < (max_letters - left_letter_cnt)){
                        lines += Math.ceil( (double) left_letters / max_letters);
                        left_letter_cnt =  max_letters - left_letters % max_letters;
                    }else{
                        lines += Math.ceil( (double) num / max_letters);
                        left_letter_cnt = max_letters - num % max_letters;
                    }
                }else{ // 整行可以容纳，直接换到下一行
                    left_letter_cnt = max_letters - num;
                    lines ++;
                }
            } else { // 剩余空间可以容纳这个单词
                left_letter_cnt -= num;
                if(left_letter_cnt != 0) left_letter_cnt--; // 单词后的空格
            }
        }
//        System.out.println("lines = " + lines);
        return lines;
    }


    static int onMeasureCNT = 0;
    void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize){
//        System.out.println("TextualView.onMeasure Params: WidthMeasureSpecSize = " + WidthMeasureSpecSize +
//                "; HeightMeasureSpecSize = " + HeightMeasureSpecMode);
        System.out.println("Textual View onMeasure CNT = " + ++onMeasureCNT);
        int lineSum = 0;
        int letters_cnt = 0;

        // 确定文字宽度，需要去掉水平padding所占的空间
        int textWidth = WidthMeasureSpecSize - (this.paddingLeft + this.paddingRight);

        for(String str : splitStringByNewline(this.Text)){
            // 记录字母最多的句子
            if(str.length() > letters_cnt) letters_cnt = str.length();
            lineSum += getTextLines(str, this.TextSize, textWidth);
        }

        // 确定组件宽度
        if(WidthMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredWidth = WidthMeasureSpecSize;
        } else if (WidthMeasureSpecMode == MeasureSpec.AT_MOST) {
            // 如果每行文字都很短的话，应该是使用最宽的文字
            int space_width = (int) (this.TextSize * DisplayParams.TEXT_WIDTH_PARAM); // 单字符的宽度
            this.measuredWidth = Math.min(letters_cnt * space_width + paddingLeft + paddingRight, WidthMeasureSpecSize);
        }else{
            System.err.println("unspecified width");
        }

        // 确定文本高度
        if(this.maxLines > 0){
            lineSum = Math.min(lineSum, this.maxLines);
        }
        int textHeight = (int) Math.round(calTextHeight(lineSum, this.TextSize));
        this.textHeight = textHeight;

        System.out.println("textSize = " + this.TextSize + "; textHeight = " + textHeight);

        if(HeightMeasureSpecMode == MeasureSpec.EXACTLY){
            this.measuredHeight = HeightMeasureSpecSize;
        } else if (HeightMeasureSpecMode == MeasureSpec.AT_MOST) {
            if((textHeight + paddingTop + paddingBottom) < HeightMeasureSpecSize) {
                this.measuredHeight = textHeight + paddingTop + paddingBottom; // todo Padding?
            }else{
                this.measuredHeight = HeightMeasureSpecSize;
            }
        }
    }

    /**
     * 计算文本所需的行高
     * @param lines 文本行数
     * @param textSize 字体大小
     * @return 文本所需的行高
     */
    static double calTextHeight(int lines, double textSize){
        System.out.println("lines = " + lines);

        if(textSize == Integer.MIN_VALUE){
            // err
            return -1;
        }

        int singleLineHeight = 0; // 单行文本高度

        singleLineHeight = (int) Math.round(textSize * DisplayParams.TEXT_HEIGHT_PARAM);// 字体高度参数
        System.out.println("singleLineHeight = " + singleLineHeight);

        double res = 0;
        if(lines == 1){
            System.out.println("single Line");
            res = singleLineHeight;
        } else{
            // TODO: 2024/6/24 这个公式还需要更多的拟合，参数是否需要调整位置
            res = singleLineHeight + (lines - 1) * textSize * 4.1 / 3.5;
        }
        if (textSize > 0) {
            res *= DisplayParams.SCALING_PARAM;
        }else{
            res *= -1;
        }
//        System.out.println("text Line = " + lines + ", text Height = " + res);
        return res;
    }


    @Override
    public void checkView(){
        // CHECK POINT 文字高度受限
        if(this.textHeight > this.measuredHeight - this.paddingTop - this.paddingBottom){
            BugReporter.writeViewBug(View.packageName, BugReporter.TEXT_INCOMPLETE, this, null);
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

    }
}
