package test.Algorithm;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author JASONJ
 * @dateTime: 2021-06-03 22:27:58
 * @description: 希尔排序
 */
public class HillSort {

    @Test
    public void test(){
        int[] array = new int[] {-66,-43,5,6,2,1,7,8,0,9,5,6,2,1,7,8,0,9};
        String value = "-62 -54 -66 -53 -53 -51 -48 -43 -33 -32 -31 -25 -24 -18 -16 -15 -14 -13 -13 -11 -10 -9 -3 -3 0 3 4 4 4 6 7 7 9 9 10 14 14 16 17 20 28 30 31 37 56 60 60 61 62 64 65 68 73 85 87";
        final String[] s = value.split(" ");
        final List<Integer> collect = Stream.of(s).map(Integer::valueOf).collect(Collectors.toList());
        final Integer[] integers = collect.toArray(Integer[]::new);
        int[] values = new int[]{30,-67,-55,-35,-4,-22,61,74,-43,2,-7,-71,-57,-53,-12};
        shell_sort(values);
        System.out.println("Sorted: "+ Arrays.toString(values));
        Arrays.sort(values);
        System.out.println("Sorted: "+ Arrays.toString(values));
    }
    @Test
    public void test1(){
       int n = 6;
        System.out.println(7 ^ 9);
        System.out.println(14 ^ 9);
    }


    public static void shell_sort(int[] arr) {
        for (int i = arr.length / 2 ; i > 0; i /= 2) {
            for (int j = i; j < arr.length; j++) {
                int temp = arr[j];
                int index = j;
                for (int z = j - i; z >= 0 && arr[z] > temp; z -= i) {
                    arr[z + i] = arr[z];
                    index = z;
                }
                arr[index] = temp;
            }
        }
    }
}
