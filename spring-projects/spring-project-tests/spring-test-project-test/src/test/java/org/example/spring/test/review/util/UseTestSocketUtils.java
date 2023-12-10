package org.example.spring.test.review.util;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.TestSocketUtils;

/**
 * @author jasonj
 * @date 2023/10/29
 * @time 20:11
 * @description
 **/
public class UseTestSocketUtils {

    @Test
    public void findAnyAvailablePort() {
        int availableTcpPort = TestSocketUtils.findAvailableTcpPort();
        Assertions.assertThat(availableTcpPort)
                .isGreaterThan(-1);
    }
}
