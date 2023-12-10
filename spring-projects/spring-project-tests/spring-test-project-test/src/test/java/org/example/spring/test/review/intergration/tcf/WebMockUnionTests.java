package org.example.spring.test.review.intergration.tcf;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * @author jasonj
 * @date 2023/12/10
 * @time 22:36
 * @description web mocks 连同测试
 **/
@SpringJUnitWebConfig
public class WebMockUnionTests {

    @Autowired
    WebApplicationContext wac; // cached

    @Autowired
    MockServletContext servletContext; // cached

    @Autowired
    MockHttpSession session;

    @Autowired
    MockHttpServletRequest request;

    @Autowired
    MockHttpServletResponse response;

    @Autowired
    ServletWebRequest webRequest;


    @Test
    public void test() {
        Assert.assertNotNull(wac);
        Assert.assertNotNull(servletContext);
        Assert.assertNotNull(session);
        Assert.assertNotNull(request);
        Assert.assertNotNull(response);
        Assert.assertNotNull(webRequest);
    }
}
