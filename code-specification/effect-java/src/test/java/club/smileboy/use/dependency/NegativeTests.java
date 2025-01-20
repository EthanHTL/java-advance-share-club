package club.smileboy.use.dependency;

import org.junit.jupiter.api.Test;

/**
 * @author jasonj
 * @date 2024/11/9
 * @time 11:22
 * @description  负数使用math.abs 尽然有一些奇怪的现象..
 **/
public class NegativeTests {

    @Test
    public void test() {
        System.out.println(Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE);

        // 丢失了给定新的符号位的精度,所以等于自己.
        System.out.println(- Integer.MIN_VALUE);

        System.out.println( - (long)Integer.MIN_VALUE);

        // 正码 / 反码  / 补码
        // 负数 是在二进制中 通过反码 + 1 来存储的 ..


        // 于是如果我们在使用Abs来进行取模计算的时候,一定要考虑到Integer.min_value的影响 ..
        // (首先可能会返回负数,其次,如果 取模数为n,并且不是2的倍数,那么必定会返回一个负数),这是存在问题的..
        // 所以一旦解决返回负数的问题,后面的这个问题也可以解决 ..
    }
}
