import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
//        System.out.println("Hello world!");
        get(3, 8, 8, new ArrayList<Integer>());
        System.out.println(res_set);

    }
    static List<List<Integer>> res_set = new ArrayList<>();

    public static void get(int w, int sum, int max, List<Integer> res){
        if (sum < 0 || w < 0) {
            return;
        }

        if(sum > max * w) {
            return;
        }

        if (sum == 0 && w == 0) {
            res_set.add(new ArrayList<>(res));
            return;
        }

        if (w == 0) {
            return; // 如果 w 到了 0 但是 sum 不是 0，直接返回
        }

        for (int i = max; i >= 0; i--) {
            res.add(i);
            get(w - 1, sum - i, i, res); // 递归调用
            res.remove(res.size() - 1); // 回溯，移除最后添加的元素
        }
    }
}