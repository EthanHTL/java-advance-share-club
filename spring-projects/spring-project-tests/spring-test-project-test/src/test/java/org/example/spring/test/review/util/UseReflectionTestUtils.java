package org.example.spring.test.review.util;

import org.example.spring.test.review.util.it.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author jasonj
 * @date 2023/10/29
 * @time 20:05
 * @description
 **/
public class UseReflectionTestUtils {

    @Test
    public void test() {

        User user = new User() {
            @Override
            public String getName() {
                return "mock-name";
            }

            @Override
            public String getPassword() {
                return "mock-password";
            }
        };

        Assert.assertThrows(IllegalArgumentException.class,() -> {
            Object name = ReflectionTestUtils.getField(
                    user, "name"
            );
        });



        Object name1 = ReflectionTestUtils.invokeGetterMethod(
                user, "name"
        );

        Assert.assertSame("mock-name",name1);


        Object method = ReflectionTestUtils.invokeMethod(
                user, "getName"
        );

        Assert.assertSame("mock-name",method);
    }
}
