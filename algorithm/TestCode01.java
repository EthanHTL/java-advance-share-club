package class01;

import java.util.Arrays;

public class TestCode01 {

    public static void main(String[] args) {
        int maxSize = 100;
        int maxValue = 100;
        int testTime = 5_0000;
        boolean succeed = true;
        SortUtil util = new SortUtil();
        for (int i = 0; i < testTime; i++) {
            int[] arr = generateRandomArray(maxSize, maxValue);
            int[] arr1 = copyArray(arr);
            int[] arr2 = copyArray(arr);
            // util.selectionSort(arr1); // 选择排序
            // util.bubbleSort(arr1);   // 冒泡排序
            // util.insertionSort(arr1); // 插入排序
            // util.hillSort1(arr1); // 希尔排序
            util.heapSort(arr1); // 归并排序
            comparator(arr2);
            if (!isEqual(arr1, arr2)) {
                succeed = false;
                printArray(arr);
                printArray(arr1);
                printArray(arr2);
                break;
            }
        }
        System.out.println(succeed ? "Nice!" : "Fucking fucked!");

        // int[] arr = generateRandomArray(maxSize, maxValue);
        int[] arr = new int[]{-76,75,57,35,69,-69,-36,-41,42,-7,78,45,6,-45,21,49,30,-55,-46,-5,94,-40,80,59,38,54,36,8,5,77,-38,45,45};
        long startTime  = System.nanoTime();
        util.heapSort(arr);
        long endTime = System.nanoTime();
        printArray(arr);
        System.out.println("time:"+(endTime-startTime));
    }

    /**
     * 总结：
     *   插入排序（24300，28900） 冒泡排序（34100，31200）
     *   选择排序（23600，24700，26600） 希尔排序（17500，15600）
     *   归并排序（3800，4400）
     *   归并 > 希尔 > 选择 > 插入 > 冒泡
     *
     */


    // for test
    public static void comparator(int[] arr) {
        Arrays.sort(arr);
    }

    public static int[] generateRandomArray(int maxSize, int maxValue) {
        // Math.random()   [0,1)
        // Math.random() * N  [0,N)
        // (int)(Math.random() * N)  [0, N-1]
        int[] arr = new int[(int) (Math.random() * (maxSize + 1))];
        for (int i = 0; i < arr.length; i++) {
            // [-? , +?]
            arr[i] =(int) ((maxValue + 1) * Math.random()) - (int) (maxValue * Math.random());
        }

        return arr;
    }
    // for test
    public static int[] copyArray(int[] arr) {
        if (arr == null) {
            return null;
        }
        int[] res = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            res[i] = arr[i];
        }
        return res;
    }
    // for test
    public static boolean isEqual(int[] arr1, int[] arr2) {
        if ((arr1 == null && arr2 != null) || (arr1 != null && arr2 == null)) {
            return false;
        }
        if (arr1 == null && arr2 == null) {
            return true;
        }
        if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    // for test
    public static void printArray(int[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
    }

}
