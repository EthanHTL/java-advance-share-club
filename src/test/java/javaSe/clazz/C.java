package javaSe.clazz;

import org.junit.Test;

/**
 * @author JASONJ
 * @dateTime: 2021-05-27 20:26:08
 * @description: test c
 */
public class C {
    static {
        System.out.println("初始化");
    }

    @Test
    public static void main(String[] args) {
        class C<T>{

        }

        System.out.println(Arrays.toString(C.class.getTypeParameters()));
    }
}
