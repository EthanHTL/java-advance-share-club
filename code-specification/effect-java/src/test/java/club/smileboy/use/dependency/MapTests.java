package club.smileboy.use.dependency;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jasonj
 * @date 2024/11/9
 * @time 15:49
 * @description
 **/
public class MapTests {

    @Test
    public void test() {

        new LinkedHashMap<>(){{
            put("2","2");
            put("1","1");
            put("10","10");
            put("9","9");

            put("3","3");
            put("4","4");
            put("5","5");
        }}.keySet().forEach(System.out::println);


        System.out.println("-------------------------------");
        new HashMap<>(){{
            put("2","2");
            put("1","1");
            put("10","10");
            put("9","9");

            put("3","3");
            put("4","4");
            put("5","5");
        }}.keySet().forEach(System.out::println);

        Class<? extends Map> subclass = HashMap.class.asSubclass(Map.class);
        System.out.println(subclass);


    }
}
