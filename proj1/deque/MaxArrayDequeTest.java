package deque;

import org.junit.Test;
import org.junit.Assert.*;

import java.util.Comparator;

import static org.junit.Assert.*;

public class MaxArrayDequeTest {

    @Test
    public void maxIntegerTest() {
        // 按数字大小对比
        Comparator<Integer> intCmp = Integer::compareTo;
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(intCmp);

        mad.addLast(1);
        mad.addLast(5);
        mad.addLast(3);

        assertEquals(Integer.valueOf(5), mad.max());
    }

    @Test
    public void maxStringTest() {
        // 按字符串长度比较
        Comparator<String> lengthComp = Comparator.comparingInt(String::length);
        /* Comparator.comparingInt(比较规则) 本质是返回一个新的 Comparator
           这个新的 Comparator 以括号的 比较规则 为准, 被传入其他方法去比较 */

        // 按字母字典序比较
        Comparator<String> alphaCmp = String::compareTo;
        MaxArrayDeque<String> mad = new MaxArrayDeque<>(lengthComp);

        mad.addLast("apple");  // 长度 5
        mad.addLast("banana"); // 长度 6
        mad.addLast("cat");    // 长度 3

        assertEquals("banana", mad.max());
        assertEquals("cat", mad.max(alphaCmp));
    }
}
