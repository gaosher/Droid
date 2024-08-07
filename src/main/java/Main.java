import tool.BugReporter;
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

//        String resBase = "C:\\Accessibility\\DataSet\\owleyeDataset\\linphone4.2.3\\linphone-android-debug-4.2.3\\res";
//        File dynamic_xml = new File("C:\\Users\\gaoshu\\Desktop\\textExamples\\linphone\\linphone_call.xml");
//        File layout_base = new File(resBase + "\\layout");
//        String out_path = "mergedXmlBase\\" + dynamic_xml.getName().replace(".xml", "") + "Merged.xml";


//        String resBase = "C:\\Accessibility\\DataSet\\dVerminDataset\\antimine12.4.2\\app-foss-debug\\res";
//        File dynamic_xml = new File("C:/Accessibility/DataSet/dVerminDataset/antimine12.4.2/antimate.xml");

//        String out_path = "C:/Accessibility/DataSet/dVerminDataset/badreads0.1.6/badBreadMerged.xml";
//        String resBase = "C:\\Accessibility\\DataSet\\dVerminDataset\\badreads0.1.6\\app-release\\res";

//        String out_path = "C:/Accessibility/DataSet/dVerminDataset/Easer-0.8.2/easer_welcomeMerged.xml";
//        String resBase = "C:\\Accessibility\\DataSet\\dVerminDataset\\Easer-0.8.2\\ryey.easer\\res";


        // constraint circle problems
//        String out_path = "C:\\Accessibility\\DataSet\\dVerminDataset\\EVMap0.8.3\\evMapMerged.xml";
        // TabLayout tabrow 缺少layout_height GridLayout
//        String out_path = "C:/Accessibility/DataSet/dVerminDataset/gpslogger112/gpsloggerpMerged.xml";

//        String out_path = "C:/Accessibility/DataSet/dVerminDataset/antimine12.4.2/antimateMerged.xml";
//        String out_path = "mergedXmlBase\\easer_welcomeMerged.xml";


//        File dynamic_xml = new File("C:/Accessibility/DataSet/dVerminDataset/tickmate1.4.13/tickmate.xml");
//        String resBase = "C:\\Accessibility\\DataSet\\dVerminDataset\\tickmate1.4.13\\de.smasi.tickmate_48\\res";
//        String out_path = "C:\\Accessibility\\DataSet\\dVerminDataset\\tickmate1.4.13\\tickmateMerged.xml";

//        File dynamic_xml = new File("C:/Accessibility/DataSet/dVerminDataset/weeklyBudget/weeklyBudget.mainActivity.xml");
//        String resBase = "C:\\Accessibility\\DataSet\\dVerminDataset\\weeklyBudget\\com.cohenchris.weeklybudget\\res";
//        String out_path = "C:\\Accessibility\\DataSet\\dVerminDataset\\weeklyBudget\\weeklyBudget.mainActivity.xml";

//        File dynamic_xml = new File("C:/Accessibility/DataSet/dVerminDataset/PDFConverter/pdfconverter.xml");
//        String resBase = "C:\\Accessibility\\DataSet\\dVerminDataset\\PDFConverter\\swati4star.createpdf\\res";
//        String out_path = "C:\\Accessibility\\DataSet\\dVerminDataset\\PDFConverter\\pdfconverterMerged.xml";

//        File dynamic_xml = new File("C:/Accessibility/DataSet/dVerminDataset/APKEditer/ApkEditer.xml");
//        String resBase = "C:\\Accessibility\\DataSet\\dVerminDataset\\APKEditer\\app-full-release\\res";
//        String out_path = "C:\\Accessibility\\DataSet\\dVerminDataset\\APKEditer\\ApkEditerMerged.xml";

        File dynamic_xml = new File("C:/Accessibility/DataSet/dVerminDataset/weeklyBudget/weeklybudget.budget.xml");
        String resBase = "C:\\Accessibility\\DataSet\\dVerminDataset\\weeklyBudget\\com.cohenchris.weeklybudget\\res";
        String out_path = "C:/Accessibility/DataSet/dVerminDataset/weeklyBudget/weeklybudget.budgetMerged.xml";


        File layout_base = new File(resBase + "\\layout");
        // 合并
        DynamicXmlProcess.mergeXmls(dynamic_xml, layout_base, out_path);
        // 静态预处理
        staticFilesPreProcess.initial(resBase);
        // 分析
        XmlParser.readXml(new File(out_path));
        System.out.println("detect " + BugReporter.BUG_CNT + " bug in total");

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("程序运行时间： " + elapsedTime + " 毫秒");
    }
}