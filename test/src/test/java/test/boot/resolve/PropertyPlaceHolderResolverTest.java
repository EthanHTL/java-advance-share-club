package test.boot.resolve;

import org.junit.jupiter.api.Test;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import java.util.Properties;

/**
 * @author JASONJ
 * @dateTime: 2021-05-27 10:53:34
 * @description: test
 */
public class PropertyPlaceHolderResolverTest {
    @Test
    public void test(){
        final PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("{", "}", ":", true);
        String test = "12rlkfsdkfl{name:{default}}sfalsdkflsdf";
        final Properties properties = new Properties();
        properties.put("default","kk");
        System.out.println(propertyPlaceholderHelper.replacePlaceholders(test, properties));
        // 所以这种根本无法解析!
        System.out.println(propertyPlaceholderHelper.replacePlaceholders("123123{{default}",properties));
    }

    @Test
    public void test1(){
        String test = "12rlkfsdkfl{name:{default}}sfalsdkflsdf";
        final int i = test.indexOf("{");
        System.out.println(StringUtils.substringMatch(test, i, "}"));
    }
}
