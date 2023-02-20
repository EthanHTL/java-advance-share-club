package test.Algorithm;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author JASONJ
 * @dateTime: 2021-06-03 22:24:59
 * @description: 冒泡排序
 */
public class bubbleSort {

    @Test
    public void test(){
        int[] array = new int[] {5,6,2,1,7,8,0,9,5,6,2,1,7,8,0,9};
        bubbleSort(array);
        System.out.println("sorted: "+ Arrays.toString(array));

    }

    public static void bubbleSort(int[] array){
        for (int i = array.length - 1; i > 0 ; i --){
            for(int j = 0; j < i ; j ++){
                if(array[j] > array[i]){
                    array[j] += array[i];
                    array[i] = array[j] - array[i];
                    array[j] -= array[i];
                }
            }
        }
    }
}
