package test.spring.map;

import org.junit.Test;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author JASONJ
 * @dateTime: 2021-05-27 15:34:54
 * @description: test
 */
public class MapTest {
    @Test
    public void test(){
        final HashMap<Object, Object> map = new HashMap<>();
        map.put(new String("1"),"1213");

        System.out.println(map.get(new String("1")));

        map.put(new Person("123"),"123");

        System.out.println(map.get(new Person("123")));

        System.out.println(new Person("123").hashCode() + " " +new Person("123").hashCode());
    }

    class Person{
        private String value;

        public Person(){

        }
        public Person(String value){
            this.value = value;
        }
        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return Objects.equals(value, person.value);
        }
    }
}
