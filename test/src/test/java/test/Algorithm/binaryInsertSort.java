package test.Algorithm;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author JASONJ
 * @dateTime: 2021-06-03 21:54:47
 * @description: 二分直接插入法
 */
public class binaryInsertSort {

    // n^2
    @Test
    public void test(){
        int[] array = new int[] {5,6,2,1,7,8,0,9,5,6,2,1,7,8,0,9};
        binaryInsertSort(array);
        System.out.println("sorted: "+ Arrays.toString(array));
    }

    public static void binaryInsertSort(int[] array){
        int left = 0, right = 0, middle = (left + right) / 2,temp = -1;
        for(int i = 0 ; i < array.length; i++){
            temp = array[i];
            // 一般情况下 left=right才能够接出最优解,否则就是middle
            while(left <= right ){
                if(array[middle] == array[i]){
                    break;
                }
                if(array[middle] > array[i]){
                    right = middle - 1;
                }
                else if(array[middle] < array[i]){
                    left = middle + 1;
                }
                middle = (left + right) / 2;
            }

            int start = middle; // 假设中间出来了!
            if(left > right){
                start = left;
            }

            for(int x = i; x > start;  x -- ){
                array[x] += array[x - 1];
                array[x -1] = array[x] - array[x - 1];
                array[x] -= array[x - 1];
            }
            array[start] = temp;
            left = 0 ;
            right = i;
        }
    }
}
