package aop.aop.introduction;

import org.springframework.aop.support.DefaultIntroductionAdvisor;

/**
 *  使用了默认的引入顾问 ..
 *
 *  这种是自给自足的 advisor ...
 */
public class LockMixinAdvisor extends DefaultIntroductionAdvisor {

    public LockMixinAdvisor() {
        super(new LockMixin(), Lockable.class);
    }
}