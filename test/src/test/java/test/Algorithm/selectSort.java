package test.Algorithm;


import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author JASONJ
 * @dateTime: 2021-06-03 22:14:17
 * @description: 选择插入排序
 */
public class selectSort {
    @Test
    public void test(){
        int[] array = new int[] {5,6,2,1,7,8,0,9,5,6,2,1,7,8,0,9};
        selectSort(array);
        System.out.println("Sorted: "+ Arrays.toString(array));
    }

    // 只选择排序
    public static void selectSort(int[] array){
        int index = -1;
        for (int i = array.length - 1; i > 0 ; i--){
             index = i;
            for(int j = 0; j < i ; j ++ ){
                if(array[j] > array[index]){
                    index = j;
                }
            }
            if(index != i){ // 根自己不相等的时候,才需要swap
                array[i] += array[index];
                array[index] = array[i] - array[index];
                array[i] -= array[index];
            }
        }
    }
}
