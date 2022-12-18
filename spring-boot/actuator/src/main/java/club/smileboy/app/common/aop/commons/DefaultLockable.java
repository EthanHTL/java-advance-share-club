package club.smileboy.app.common.aop.commons;

public class DefaultLockable implements Lockable {

    private boolean lockFlag;
    @Override
    public void lock() {
        lockFlag = true;
    }

    @Override
    public void unlock() {
        lockFlag = false;
    }

    @Override
    public boolean isLock() {
        return lockFlag;
    }
}
