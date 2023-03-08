package test.Algorithm;


import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author JASONJ
 * @dateTime: 2021-06-03 21:49:57
 * @description: 直接插入排序
 */
public class directInsertSort {

//    n^2
    @Test
    public void test(){

        int[] array = new int[] {5,6,2,1,7,8,0,9,5,6,2,1,7,8,0,9};

        directInsertSort(array);

        System.out.println("sorted: "+ Arrays.toString(array));

    }

    public void directInsertSort(int[] array){

        for (int i = 1; i < array.length ; i++){
            for(int j = i ; j > 0 &&  array[j] < array[j - 1] ; j --){
                array[j] += array[j - 1];
                array[j - 1] = array[j] - array[j - 1];
                array[j] -= array[j - 1];
            }
        }
    }
}
