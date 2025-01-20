package club.smileboy.use.dependency;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

/**
 * @author jasonj
 * @date 2024/11/9
 * @time 11:55
 * @description
 **/
public class IntegerTests {
    @Test
    public void test() {
        System.out.println(Integer.valueOf(42) == Integer.valueOf(42));
        System.out.println(Integer.valueOf(42) < Integer.valueOf(42));
        System.out.println(Integer.valueOf(42) > Integer.valueOf(42));

        Comparator<Integer> comparator = (x,y) -> x < y ? -1 : (x == y ? 0 : 1);
        System.out.println(comparator.compare(Integer.valueOf(42), Integer.valueOf(42)));

        // 当然 newInteger(42) 肯定不等于 newInteger(42),只有Integer.valueOf 提供了对象缓存 ..
        System.out.println();
    }
}
