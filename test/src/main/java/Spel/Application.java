package Spel;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.math.RoundingMode;

/**
 * @author JASONJ
 * @dateTime: 2021-05-29 21:46:15
 * @description: application
 */
public class Application {

    static class Test{
        public Integer year;
        public Integer[] array;
        public Test(Integer year){
            this.year = year;
        }
        public Test(Integer[] array){
            this.array = array;
        }
    }
    //可以明白,SPEL表达式没有根spring耦合;
    public static void main(String[] args) {
        // 解析属性引用
        final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        final Expression expression = spelExpressionParser.parseExpression("year + 1900");
        System.out.println(expression.getValue(new Test(230)));

        // 解析数组,集合差不多  map同样的方式
        // 例如 xxx.parseExpression("officers['president'].placeOfBirth.city")
        System.out.println(spelExpressionParser.parseExpression("array[0]").getValue(new Test(new Integer[]{1, 2})));

        // 行内表达式
        System.out.println(spelExpressionParser.parseExpression("{1,2,3,4}").getValue());
        // 二维数组或者集合
        System.out.println(spelExpressionParser.parseExpression("{{'a','b'},{'x','y'}}").getValue());
    }
}
