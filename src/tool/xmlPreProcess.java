package tool;

import java.util.Arrays;
import java.util.HashSet;

public class xmlPreProcess {

    static String [] uselessAttrs = {"package", "checkable", "checked", "clickable", "enabled", "focusable", "focused",
            "long-clickable", "password", "selected", "displayed", "content-desc"
    };
    static HashSet<String> uselessAttrsSet = new HashSet<>(Arrays.asList(uselessAttrs));//xml dump中无用的属性集合


    private class Id{
        String xmlFileName;
        String viewId;
    }
}
