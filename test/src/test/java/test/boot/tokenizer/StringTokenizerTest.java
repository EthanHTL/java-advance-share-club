package test.boot.tokenizer;

import org.junit.Test;

import java.util.StringTokenizer;

/**
 * @author JASONJ
 * @dateTime: 2021-05-27 13:16:04
 * @description: test
 */
public class StringTokenizerTest {
    @Test
    public void test(){
        final StringTokenizer tokenzier = new StringTokenizer("dev&prod|test","()&|!",true);
        while (tokenzier.hasMoreTokens()) {
          System.out.println(tokenzier.nextToken());
      }


    }
}
