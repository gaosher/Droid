package view;


import util.MeasureSpec;

import javax.imageio.ImageIO;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import tool.staticFilesPreProcess;
public class ImageView extends View{

    // ImageView ImageButton

    static HashMap<String, int[]> Drawables = staticFilesPreProcess.getImages();
//    static String drawablesPath = "C:\\Accessibility\\DataSet\\owleyeDataset\\DemocracyDroid3.7.1\\apk\\DemocracyDroid-3.7.1\\res\\drawable-xxxhdpi";

    public ImageView() {
    }

    String src;
    int scaleType = FIT_CENTER;
    int w = 0;
    int h = 0;

    public void setSrc(String scr) {
        this.src = scr;
        // TODO: 2024/5/20 需要解析一下是不是drawable，获取一下图片原始宽高
    }

    public void setScaleType(String scaleType){
        switch (scaleType){
            case "center" -> this.scaleType = CENTER;
            case "centerCrop" -> this.scaleType = CENTER_CROP;
            case "centerInside" -> this.scaleType = CENTER_INSIDE;
            case "fitCenter" -> this.scaleType = FIT_CENTER;
            case "fitEnd" -> this.scaleType = FIT_END;
            case "fitStart" -> this.scaleType = FIT_START;
            case "fitXY" -> this.scaleType = FIT_XY;
            case "matrix" -> this.scaleType = MATRIX;
            default -> System.err.println("can't parse scaleType value: " + scaleType);
        }
    }

    void getImageSizeFromSrc(){
        if(Drawables.isEmpty()){
            System.err.println("Empty drawable/mipmap base");
            return;
        }
        if(Drawables.containsKey(this.src)){
            this.w = Drawables.get(this.src)[0];
            this.h = Drawables.get(this.src)[1];
        }else{
            System.err.println("can't find " + this.src);
        }
    }

    public ImageView(ViewGroup.LayoutParams layoutParams, HashMap<String, String> attrMap){

        initialBasicAttrs(attrMap);

        //  lp 相关
        mLayoutParams = layoutParams;
        mLayoutParams.setLayoutParams(attrMap);

        // src
        String img_src;
        if(attrMap.containsKey("src")){
            img_src = attrMap.get("src").replace("@drawable/", "").replace("@mipmap/", "");
            setSrc(img_src);
            attrMap.remove("src");
        } else if (attrMap.containsKey("srcCompat")) {
            img_src = attrMap.get("srcCompat").replace("@drawable/", "").replace("@mipmap/", "");
            setSrc(img_src);
            attrMap.remove("srcCompat");
        } else {
            System.out.println("can't find attribute src");
        }

        // scale type
        if(attrMap.containsKey("scaleType")){
            setScaleType(attrMap.get("scaleType"));
            attrMap.remove("scaleType");
        }

        this.AttrMap = attrMap;
    }

    @Override
    public void onMeasure(int WidthMeasureSpecMode, int WidthMeasureSpecSize, int HeightMeasureSpecMode, int HeightMeasureSpecSize){
        System.out.println("start onMeasure Image View " + this.Id);
        if(this.src == null || this.src.equals("")){
//            System.out.println("null");
            if(HeightMeasureSpecMode == MeasureSpec.EXACTLY && WidthMeasureSpecMode == MeasureSpec.EXACTLY){
                this.measuredWidth = WidthMeasureSpecSize;
                this.measuredHeight = HeightMeasureSpecSize;
            }
            // ImageView 没有面积 todo 考虑沿用bounds？
            else if (HeightMeasureSpecMode == MeasureSpec.EXACTLY && WidthMeasureSpecMode == MeasureSpec.AT_MOST) {
                this.measuredHeight = HeightMeasureSpecSize;
                this.measuredWidth = 0;
                System.err.println("this Image View might be invalid");
            } else if (WidthMeasureSpecMode == MeasureSpec.EXACTLY && HeightMeasureSpecMode == MeasureSpec.AT_MOST) {
                this.measuredWidth = WidthMeasureSpecSize;
                this.measuredHeight = 0;
                System.err.println("this Image View might be invalid");
            } else if (WidthMeasureSpecMode == MeasureSpec.AT_MOST && HeightMeasureSpecMode == MeasureSpec.AT_MOST) {
                this.measuredWidth = 0;
                this.measuredHeight = 0;
                System.err.println("this Image View might be invalid");
            }else{
                System.err.println("this Image View's MeasureSpec have bugs");
            }
        }else{
//            System.out.println("src: " + this.src);
            // 获取src源图片的宽高
//            String image_name = this.src.replace("@drawable/", "") + ".png";
//            System.out.println(image_name);
            int image_width = 0;
            int image_height = 0;
            String image_name = this.src.replace("@drawable/", "").replace("@mipmap/", "");
            if(Drawables.containsKey(image_name)){
                int [] imageSize = Drawables.get(image_name);
                image_width = imageSize[0];
                image_height = imageSize[1];
//                System.out.println("img width = " + image_width + "; img height = " + image_height);
            }else{
                System.err.println(this.src + "not in Drawables");
//                for(Map.Entry e : ImageView.Drawables.entrySet()){
//                    System.out.println(e.getKey());
//                }
            }
            image_width += (paddingLeft + paddingRight);
            image_height += (paddingTop + paddingBottom);

            // todo 可能还有图片显示不全的情况，可以考虑输出
            if(HeightMeasureSpecMode == MeasureSpec.EXACTLY && WidthMeasureSpecMode == MeasureSpec.EXACTLY){
                this.measuredWidth = WidthMeasureSpecSize;
                this.measuredHeight = HeightMeasureSpecSize;
            } else if (HeightMeasureSpecMode == MeasureSpec.EXACTLY && WidthMeasureSpecMode == MeasureSpec.AT_MOST) {
                this.measuredHeight = HeightMeasureSpecSize;
                this.measuredWidth = Math.min(WidthMeasureSpecSize, image_width);
//                System.out.println("HeightMeasureSpecMode == MeasureSpec.EXACTLY && WidthMeasureSpecMode == MeasureSpec.AT_MOST");
//                System.out.println(WidthMeasureSpecSize + " " + image_width);
            } else if (WidthMeasureSpecMode == MeasureSpec.EXACTLY && HeightMeasureSpecMode == MeasureSpec.AT_MOST) {
                this.measuredWidth = WidthMeasureSpecSize;
                this.measuredHeight = Math.min(HeightMeasureSpecSize, image_height);
            } else if (WidthMeasureSpecMode == MeasureSpec.AT_MOST && HeightMeasureSpecMode == MeasureSpec.AT_MOST) {
                this.measuredWidth = image_width;
                this.measuredHeight = image_height;
            }else{ //
                System.err.println("this Image View's MeasureSpec have bugs");
            }
        }
        System.out.println("ImageView: measured width = " + this.measuredWidth + ", measured height = " + this.measuredHeight);
    }

    public void printClassName(){
        System.out.println("Image View");
    }

    public void showAllAttrs(){
        super.showAllAttrs();
        System.out.println("src: " + this.src);
        System.out.println("scale type: " + this.scaleType);
    }


    // scale type ints
    final static int CENTER = 0; // 使得图像居中显示，不进行任何缩放。当图片比控件大时，图片会有部分被裁剪掉，显示不全；
    final static int CENTER_CROP = 1; // 保持图像的宽高比，进行缩放图像，直到图像的宽和高都等于或大于控件的宽高，然后居中显示；
    final static int CENTER_INSIDE = 2; // 当图像的宽高大于控件的宽高时，保持图像的宽高比，进行缩放图像，直到图像的宽和高都等于或小于控件的宽高，然后居中显示
    final static int FIT_CENTER = 3; // 默认, 保持图像的宽高比，进行缩放图像，直到图像的宽和高都等于或小于控件的宽高，然后居中显示
    final static int FIT_END = 4; // 保持图像的宽高比，进行缩放图像，直到图像的宽和高都等于或小于控件的宽高，然后居右侧或底部显示
    final static int FIT_START = 5; // 保持图像的宽高比，进行缩放图像，直到图像的宽和高都等于或小于控件的宽高，然后居左侧或顶部显示
    final static int FIT_XY = 6; // 可以不用保持图像的宽高比，从控件的左上角分别对图片的宽和高进行缩放，使得图片的宽高等于控件的宽高
    final static int MATRIX = 7; // 不改变原图的宽高，不进行任何缩放，从控件的左上角开始绘制原图，原图超过控件的部分作裁剪处理。

}
