package util;

public class MeasureSpec {


    // UNSPECIFIED的模式设置：0
    public static final int UNSPECIFIED = 0;

    // EXACTLY的模式设置：1
    public static final int EXACTLY = 1;

    // AT_MOST的模式设置：2
    public static final int AT_MOST = 2;


    /**
     * makeMeasureSpec（）方法
     * 作用：根据提供的size和mode得到一个详细的测量结果吗，即measureSpec
     **/
    public static Spec makeMeasureSpec(int size, int mode) {
        return new Spec(mode, size);
    }

    /**
     * getMode（）方法
     * 作用：通过measureSpec获得测量模式（mode）
     **/
    public static int getMode(int[] measureSpec) {
        return measureSpec[0];
    }
    /**
     * getSize方法
     * 作用：通过measureSpec获得测量大小size
     **/
    public static int getSize(int[] measureSpec) {
        return measureSpec[1];
    }


    /**
     * @param padding view当前尺寸的的内边距和外边距(padding,margin)
     * @param childDimension 子视图的布局参数（宽/高）
     * @return child的MeasureSpec
     * 源码分析：getChildMeasureSpec()
     * 作用：根据父视图的MeasureSpec & 布局参数LayoutParams，计算单个子View的MeasureSpec
     * 注：子view的大小由父view的MeasureSpec值 和 子view的LayoutParams属性 共同决定
     */
    public static Spec getChildMeasureSpec(int measureSpecMode, int measureSpecSize,  int padding, int childDimension) {
//        System.out.println("getChildMeasureSpec measureSpecSize = " + measureSpecSize);
        // 通过父view计算出的子view = 父大小-边距（父要求的大小，但子view不一定用这个值）
        int size = Math.max(0, measureSpecSize - padding);
//        System.out.println("getChildMeasureSpec size = " + size);

        // 子view想要的实际大小和模式（需要计算）
        int resultSize = 0;
        int resultMode = 0;

        // 通过父view的MeasureSpec和子view的LayoutParams确定子view的大小
        // 当父view的模式为EXACITY时，父view强加给子view确切的值
        // 一般是父view设置为match_parent或者固定值的ViewGroup
        switch (measureSpecMode) {
            case MeasureSpec.EXACTLY -> {
                // 当子view的 LayoutParams > 0 ，即有确切的值
                if (childDimension >= 0) {
                    //子view大小为子自身所赋的值，模式大小为EXACTLY
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;

                } else if (childDimension == DimenValue.FILL_PARENT) { // 当子view的LayoutParams为MATCH_PARENT时(-1)
                    //子view大小为父view大小，模式为EXACTLY
//                    System.out.println("getChildMeasureSpec FILL_PARENT");
                    resultSize = size;
                    System.out.println(size);
                    resultMode = MeasureSpec.EXACTLY;

                } else if (childDimension == DimenValue.WRAP_CONTENT) { // 当子view的LayoutParams为WRAP_CONTENT时(-2)
                    // 子view决定自己的大小，但最大不能超过父view，模式为AT_MOST
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
            }

            // 当父view的模式为AT_MOST时，父view强加给子view一个最大的值。（一般是父view设置为wrap_content）
            case MeasureSpec.AT_MOST -> {
                // 道理同上
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == DimenValue.FILL_PARENT) {
                    // 当父View = wrap_content, 子View = fill_parent时，可以看到子View的模式为AT_MOST，简单理解为wrap_content
                    // 在as中也得到了验证
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                } else if (childDimension == DimenValue.WRAP_CONTENT) {
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
            }
        }

        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }

}
