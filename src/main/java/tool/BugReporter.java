package tool;

import view.TextualView;
import view.View;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class BugReporter {
    static File BugReport;

    // bug types
    public final static int TEXT_INCOMPLETE = 1;
    public final static int VIEW_OVERLAP = 2;
    public final static int VIEW_INCOMPLETE = 3;
    public final static int UNIT_ERROR = 4;

    public static int BUG_CNT = 0;

    static boolean shouldBeEmpty = true;

    static FileWriter writer;

    static void initialReportFile(String pkgName){
//        System.out.println("initial reporter");
        if (BugReport == null) {
            BugReport = new File(pkgName + "_bug_report.txt");
        }

        if (shouldBeEmpty) {
            try {
                new FileWriter(BugReport).write("");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            shouldBeEmpty = false;
        }

        if (writer == null) {
            try {
                writer = new FileWriter(BugReport, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void writeViewBug(String pkgName, int bugType, View bugView1, View bugView2) {
        BUG_CNT++; // 记录bug数量
//        System.out.println(BUG_CNT);
        initialReportFile(pkgName);

        try{
            writer.write("-------------------BUG " + BUG_CNT + "-----------------\n");
            switch (bugType) {
                case TEXT_INCOMPLETE -> {
                    writer.write("TEXT CAN'T BE DISPLAYED COMPLETELY, DETAILED INFO:\n");
                    reportBugInfo(bugView1, writer);
                }
                case VIEW_INCOMPLETE -> {
                    writer.write("VIEW CAN'T BE DISPLAYED COMPLETELY, DETAILED INFO:\n");
                    reportBugInfo(bugView1, writer);
                }
                case VIEW_OVERLAP -> {
                    writer.write("VIEWS OVERLAP WITH EACH OTHER, DETAILED INFO:\n");
//                    System.out.println("VIEWS OVERLAP WITH EACH OTHER, DETAILED INFO:\n");
                    writer.write("bug view 1 info:\n");
                    reportBugInfo(bugView1, writer);
                    writer.write("bug view 2 info:\n");
                    reportBugInfo(bugView2, writer);
                }
            }
            writer.flush();
            writer.close();
            writer = null; // Ensure the writer is recreated for the next call
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    public static void writeUnitBug(String pkgName, View bugView, String attr) {
        BUG_CNT++; // 记录bug数量

        initialReportFile(pkgName);

        try {
            writer.write("-------------------BUG " + BUG_CNT + "-----------------\n");
            writer.write("THE UNIT USED IS NOT APPROPRIATE, DETAILED INFO:\n");
            reportUnitBugInfo(bugView, writer, attr);
            writer.flush();
            writer.close();
            writer = null; // Ensure the writer is recreated for the next call
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void reportBugInfo(View bugView, FileWriter writer) throws IOException {
        if (bugView.getXmlFileName() != null) {
            writer.write("bug view is in file: " + bugView.getXmlFileName() + '\n');
        }
        writer.write("bug view class name: " + bugView.getClassName() + '\n');
        if (bugView.getId() != null) {
            writer.write("bug view id: " + bugView.getId() + '\n');
        }
        writer.write("bug view bounds: " + Arrays.toString(bugView.getBounds()) + '\n');
        if (bugView instanceof TextualView) {
            writer.write("bug view text: " + ((TextualView) bugView).getText() + '\n');
        }
    }

    static void reportUnitBugInfo(View bugView, FileWriter writer, String attr) throws IOException {
        if (bugView.getXmlFileName() != null) {
            writer.write("bug view is in file: " + bugView.getXmlFileName() + '\n');
        }
        writer.write("bug view class name: " + bugView.getClassName() + '\n');
        if (bugView.getId() != null) {
            writer.write("bug view id: " + bugView.getId() + '\n');
        }
        writer.write("bug view bounds: " + Arrays.toString(bugView.getBounds()) + '\n');
        if (bugView instanceof TextualView) {
            writer.write("bug view text: " + ((TextualView) bugView).getText() + '\n');
        }
        writer.write("BUG UNIT ->  " + attr + '\n');
    }

    public static void writeInReport(String content){
        initialReportFile(View.packageName);
        try {
            writer.write(content + '\n');
            writer.flush();
            writer.close();
            writer = null; // Ensure the writer is recreated for the next call
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
