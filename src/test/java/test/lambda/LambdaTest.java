package test.lambda;


import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author JASONJ
 * @dateTime: 2021-05-26 13:57:01
 * @description: lambda Test
 */
public class LambdaTest {

    @Test
    public void test(){
        final List<Supplier<String>> collect = Stream.of("1", "2", "3", "4").map(ele -> ((Supplier<String>) ele::toString)).collect(Collectors.toList());
        collect.forEach(ele -> {
            System.out.println(ele.get());
        });
    }
}
