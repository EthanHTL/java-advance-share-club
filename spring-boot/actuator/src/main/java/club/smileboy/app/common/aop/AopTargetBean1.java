package club.smileboy.app.common.aop;

@AopExecute
public class AopTargetBean1 implements AopTargetBean {

    @AopExecute
    public void printf() {
        System.out.println("my name is aop target bean !!!");
    }


    @ApcExecute1
    @Override
    public void toto() {
        System.out.println("toto");
    }
}
