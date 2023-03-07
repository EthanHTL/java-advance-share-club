package club.smileboy.app.common.aop;

@AopExecute
public interface AopTargetBean {


     void printf();

     @ApcExecute1
     void toto();
}
