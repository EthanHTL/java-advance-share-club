package aop.aop.introduction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个类的写法类似于 DelegatePerTargetObjectIntroductionInterceptor ..
 */
public class LockableSupport implements Lockable {

    private Lockable target;

    private final Map<Object,Lockable> beans = new ConcurrentHashMap<>();


    @Override
    public void lock() {
        target.lock();
    }

    @Override
    public void unlock() {
        target.unlock();
    }

    @Override
    public boolean locked() {
        return target.locked();
    }

    public Lockable checkIfPossible(Object target) {
        if (beans.containsKey(target)) {
            this.target = beans.get(target);
        }
        else {
            // 否则增加一个记录 ...
            beans.put(target, new Lockable() {
                private volatile boolean lockFlag = false;

                @Override
                public void lock() {
                    this.lockFlag = true;
                }

                @Override
                public void unlock() {
                    this.lockFlag = false;
                }

                @Override
                public boolean locked() {
                    return lockFlag;
                }
            });

            this.target = beans.get(target);
        }
        return this;
    }
}
