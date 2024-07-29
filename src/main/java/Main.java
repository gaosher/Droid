import tool.DynamicXmlProcess;
import tool.XmlParser;
import tool.staticFilesPreProcess;

import java.io.File;

public class Main {
    public static void main(String[] args) {
//        File resBase = new File("")
        long startTime = System.currentTimeMillis();
        // 文件路径
//        String resBase = "C:\\Accessibility\\DataSet\\owleyeDataset\\DemocracyDroid3.7.1\\apk\\DemocracyDroid-3.7.1\\res";
//        File dynamic_xml = new File("C:\\Users\\gaoshu\\Desktop\\textExamples\\NoDefects.xml");
//        File layout_base = new File(resBase + "\\layout");
//        String out_path = "mergedXmlBase\\NoDefectsMergeTest.xml";

        String resBase = "C:\\Accessibility\\DataSet\\owleyeDataset\\linphone4.2.3\\linphone-android-debug-4.2.3\\res";
        File dynamic_xml = new File("C:\\Users\\gaoshu\\Desktop\\textExamples\\linphone\\linphone_call.xml");
        File layout_base = new File(resBase + "\\layout");
        String out_path = "mergedXmlBase\\" + dynamic_xml.getName().replace(".xml", "") + "Merged.xml";


        // 合并
        DynamicXmlProcess.mergeXmls(dynamic_xml, layout_base, out_path);
        // 静态预处理
        staticFilesPreProcess.initial(resBase);
        // 分析
        XmlParser.readXml(new File(out_path));

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("程序运行时间： " + elapsedTime + " 毫秒");
    }
}