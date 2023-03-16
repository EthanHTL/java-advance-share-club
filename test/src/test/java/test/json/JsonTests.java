package test.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.runtime.ObjectMethods;
import java.util.List;

public class JsonTests {
    @Test
    public void test() throws JsonProcessingException {
        String value = "1,2,3,4";

        String s = new ObjectMapper().writeValueAsString(List.of(1, 2, 3, 4, 5));
        System.out.println(s);
//        System.out.println(new ObjectMapper().convertValue(s, List.class));

    }
}
