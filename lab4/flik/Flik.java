package flik;

import java.util.Objects;

/** An Integer tester created by Flik Enterprises.
 * @author Josh Hug
 * */
public class Flik {
    /** @param a Value 1
     *  @param b Value 2
     *  @return Whether a and b are the same */
    /* 方法 1
    public static boolean isSameNumber(Integer a, Integer b) {
        return Objects.equals(a, b);
        // return a == b (题目给的)
        Integer 类型是对象 == 只能对比存入的地址
        int 类型是整数 才能使用 == 判断大小

        对于 Integer 对象创建时:
        Java 会创建 -128 ~ 127 在一个地址范围内(正好占据一个byte)
        i,j 从 0 到 127 都对比的是同一个地址
        但是 128 超出了，Java 单独为 i,j 分别又创建了一个容纳 128 的地址
        所以当对比 128 的时候，i,j 的地址已经完全不同了
    } */

    // 方法 2
    public static boolean isSameNumber(int a, int b) {
        return (a == b);
    }
}
