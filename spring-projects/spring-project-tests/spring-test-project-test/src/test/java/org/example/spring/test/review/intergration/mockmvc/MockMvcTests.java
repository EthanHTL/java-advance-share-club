package org.example.spring.test.review.intergration.mockmvc;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * @author jasonj
 * @date 2023/12/14
 * @time 00:02
 * @description
 **/
public class MockMvcTests {

    @Test
    public void test() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup()
                .defaultRequest(get("/"))
                .build();
        mockMvc.perform(get("/hotels?thing={thing}", "somewhere"));
        mockMvc.perform(get("/app/main/hotels/{id}").contextPath("/app").servletPath("/main"));
    }
}
