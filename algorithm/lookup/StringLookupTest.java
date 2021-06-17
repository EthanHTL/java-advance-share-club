package lookup;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author JASONJ
 * @dateTime: 2021-05-18 22:49:52
 * @description: lookup test
 */
public class StringLookupTest {
    // 这里学习 Brute-Force 模式匹配算法
    // 例如 存在串"s0s1...sn-1" 字串 "t0t1....tm-1"
    // 需要在串中匹配字串,实际是0<i<n-m的有效范围,父串 si<si<si+m-1,字串的范围 t0<ti<tm-1
    public static void main(String[] args) {
//        System.out.println(bruteForce("12323423423", "232", 0));
        String s = "123125454412312";
        final Instant now = Instant.now();
        System.out.println(kmp(s,"544"));
        final Instant now1 = Instant.now();
        System.out.println(TimeUnit.NANOSECONDS.toMicros(now1.getNano() - now.getNano()));
        final Instant now2 = Instant.now();
        System.out.println(s.indexOf("544"));
        final Instant now3 = Instant.now();
        System.out.println(TimeUnit.NANOSECONDS.toMicros(now3.getNano() - now2.getNano()));
    }

    /**
     * 根据字串获取从主串中出现的第一次位置 [强制匹配]
     * @param primaryStr
     * @param subStr
     * @return
     */
    public static int bruteForce(String primaryStr,String subStr,int pos){
        if(pos < 0){
            throw new RuntimeException("非法参数!");
        }
        int spos = 0;
        while (pos < primaryStr.length() && spos < subStr.length()){
            if(primaryStr.charAt(pos) == subStr.charAt(spos)){
                pos ++ ;
                spos ++ ;
            }else{
                pos = pos - spos + 1;
            }
        }

        if(spos >= subStr.length()){
            return pos - subStr.length();
        }
        return 0;
    }

    // kmp模式匹配算法
    public static int kmp(String s,String t){
        int n = 0 , m = 0;
        int[] directions = new int[t.length()];
        next(directions,t);
        while( n < s.length() && m < t.length()){
            if( m == -1 || s.charAt(n) == t.charAt(m)){
                // 偏移
                n ++ ;
                m ++ ;
            }else{
                m = directions[m];
            }
        }
        if( m >= t.length()) {
            return n - t.length();
        }
        return -1;
    }
    // 这其实就是在进行规律定义
    public static void next(int[] directions,String t){
        int k = -1, j = 0;
        directions[j] = k; // 先定义一个首选! directions[0] = -1;
        while( j < t.length() - 1 ){
            if( k == -1 || t.charAt(j) == t.charAt(k)){
                k ++ ;
                j ++ ;
                if( t.charAt(j) == t.charAt(k) ){
                    directions[j] = directions[k];
                }
                else
                    directions[j] = k;
            }
            // 则需要进行回退  
            else{
                k = directions[k];
            }
        }
    }
}
