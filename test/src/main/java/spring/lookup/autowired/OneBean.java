package spring.lookup.autowired;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author JASONJ
 * @dateTime: 2021-05-02 11:35:13
 * @description: one bean
 */
@Component
public class OneBean implements InitializingBean {
    private TwoBean twoBean;

    @Autowired
    public void setTwoBean(TwoBean twoBean){
        this.twoBean = twoBean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("one Bean occur bean properties set,but ocuur before init ");
    }
}
