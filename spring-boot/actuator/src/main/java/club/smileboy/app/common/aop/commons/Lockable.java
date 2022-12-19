package club.smileboy.app.common.aop.commons;

/**
 * 目标对象在操作之前可以进行加锁,加锁之后操作报错
 */
public interface Lockable {

    void lock();

    void unlock();

    boolean isLock();
}
