package test.boot.compare;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * @author JASONJ
 * @dateTime: 2021-05-27 11:37:16
 * @description: compare
 */
public class Compare {

    // 集合比较策略,就是遍历判断是否包含对象！
    @Test
    public void test(){
        final LinkedHashSet<String> aDefault = new LinkedHashSet<>(Collections.singleton("default"));
        System.out.println(aDefault.equals(Collections.singleton("default")));
    }
}
