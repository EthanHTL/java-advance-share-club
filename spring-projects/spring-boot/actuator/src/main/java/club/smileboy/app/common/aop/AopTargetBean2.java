package club.smileboy.app.common.aop;

@AopExecute
public class AopTargetBean2 implements AopTargetBean {
    @Override
    public void printf() {
        System.out.println("aop target bean 2");
    }

    @ApcExecute1
    @Override
    public void toto() {
        System.out.println("toto bean2");
    }
}
