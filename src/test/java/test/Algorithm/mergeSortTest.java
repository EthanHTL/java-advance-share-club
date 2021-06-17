package test.Algorithm;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author JASONJ
 * @dateTime: 2021-06-07 09:10:30
 * @description: 归并算法
 */
public class mergeSortTest {
    @Test
    public void test(){
        int[] values = new int[]{30,-67,-55,-35,-4,-22,61,74,-43,2,-7,-71,-57,-53,-12};
        mergeSort(values,0,values.length - 1);
        System.out.println("sorted: "+ Arrays.toString(values));

        int[] values1 = new int[]{30,-67,-55,-35,-4,-22,61,74,-43,2,-7,-71,-57,-53,-12};
        merge2Sort(values1,0,values.length);
        System.out.println("sorted: "+Arrays.toString(values1));
    }


    public static void  merge(int[] array,int left,int middle,int right){
        if(left == right){
            return;
        }
        int L1 = left;
        int L2 = middle + 1;
        int len = 0;
        int[] helper = new int[right - left + 1];
        while(L1 <= middle && L2 <= right){
            helper[len ++] = array[L1] < array[L2] ? array[L1 ++] : array[L2 ++];
        }
        // 仅仅只有一个会存在多出的清空
        while(L1 <= middle){
           helper[ len ++ ] = array[L1++];
        }
        while(L2 <= right){
            helper[ len ++ ] = array[L2++];
        }

        // 然后重新放入array
       for(int i = 0; i < helper.length ; i ++){
           array[left + i] = helper[i];
       }
    }

    public static void swap(int[] array,int x,int y){
        array[x] ^= array[y];
        array[y] ^= array[x];
        array[x] ^= array[y];
    }

    public static void mergeSort(int[] array,int left,int right){
        if(right - left < 1){
            return ;
        }
        int middle =left +  ((right - left) >> 1);
        mergeSort(array,left,middle);
        mergeSort(array,middle + 1,right);
        merge(array,left,middle,right); // 合并出来即可!
    }


    // 非递归实现

    public static void merge2Sort(int[] array,int left,int right){

        int step = 1,middle = 0,r = 0;
        // 进行步长的循环
        while(step < right){
            // 进行划分组进行合并
            while(left < right){

                if(step > (right - left )){
                    break;
                }
                middle = left + step - 1;
                r = middle + Math.min(step,right - middle - 1);
                merge(array,left,middle,r);
                left = r + 1;
            }
            // 当合并完成之后,就代表着没有可以合并的数组了!
            if(step > right / 2){
                break;
            }
            left = 0; // 每次从0开始
            step <<= 1; // 步长增加
        }
    }

    // for test
    public static void main(String[] args) {
        int testTime = 500000;
        int maxSize = 100;
        int maxValue = 100;
        System.out.println("测试开始");
        for (int i = 0; i < testTime; i++) {
            int[] arr1 = generateRandomArray(maxSize, maxValue);
            int[] arr2 = copyArray(arr1);
            Arrays.sort(arr1);
            mergeSort(arr2,0,arr2.length - 1);
            if (!isEqual(arr1, arr2)) {
                System.out.println("出错了！");
                printArray(arr1);
                printArray(arr2);
                break;
            }
        }
        System.out.println("测试结束");
    }

    // for test
    public static int[] generateRandomArray(int maxSize, int maxValue) {
        int[] arr = new int[(int) ((maxSize + 1) * Math.random())];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int) ((maxValue + 1) * Math.random()) - (int) (maxValue * Math.random());
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
