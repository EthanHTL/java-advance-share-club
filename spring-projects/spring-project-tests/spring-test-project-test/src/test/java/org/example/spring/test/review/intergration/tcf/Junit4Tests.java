package org.example.spring.test.review.intergration.tcf;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

/**
 * @author jasonj
 * @date 2023/11/2
 * @time 22:07
 * @description Junit 4 测试
 **/

@RunWith(SpringJUnit4ClassRunner.class)
public class Junit4Tests {

    // 包含 Rules

    private SpringClassRule classRule = new SpringClassRule();

    private SpringMethodRule methodRule = new SpringMethodRule();
}
