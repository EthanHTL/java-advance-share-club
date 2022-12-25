package aop.aop.introduction;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * 首先 LockMixin 实现了Lockable 接口,同样继承了 可代理的引入拦截器
 */
public class LockMixin extends DelegatingIntroductionInterceptor implements Lockable {

    private boolean locked;

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean locked() {
        return this.locked;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 它的执行很简单, 判断有没有对应的方法是 set 开始的,如果有,则判断符合条件?
        if (locked() && invocation.getMethod().getName().indexOf("set") == 0) {
            throw new LockedException();
        }
        // 从而调用父类方法 (我们知道很简单,就是判断是否为引入的接口方法,如果是则调用接口上的方法,否则调用proceed(也就是判断其他通知是否在这个方法里面))
        return super.invoke(invocation);
    }

}