package test.construct;

/**
 * @author JASONJ
 * @dateTime: 2021-05-26 16:28:26
 * @description: test
 */
public class Test {
   @org.junit.Test
    public void test(){
       new Boos();
   }
}
class Boos extends Person{
    public Boos(){
        super();
    }
}
class Person{

    public Person(){
        System.out.println(c);
    }
    private int a = 1;
    private int b = 2;
    private int c = 3;

    public int getC(){
        return c;
    }
}
