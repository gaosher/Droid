package util;

public class Spec{
    public int mode = MeasureSpec.UNSPECIFIED;
    public int size = Integer.MAX_VALUE;

    public Spec(int mode, int size){
        this.mode = mode;
        this.size = size;
    }
}