package deque;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Iterator;

public class ArrayDequeTest {

    @Test
    /** 测试基本的添加、删除、isEmpty 和 size */
    public void basicAddRemoveTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        assertTrue(ad.isEmpty());
        assertEquals(0, ad.size());

        ad.addFirst(10); // [10]
        ad.addLast(20);  // [10, 20]
        ad.addFirst(5);   // [5, 10, 20]

        assertEquals(3, ad.size());
        assertFalse(ad.isEmpty());

        assertEquals(Integer.valueOf(5), ad.removeFirst()); // [10, 20]
        assertEquals(Integer.valueOf(20), ad.removeLast());  // [10]
        assertEquals(1, ad.size());
    }

    @Test
    /** 测试自动扩容和自动缩容 */
    public void resizeTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        // 初始容量 8，添加 100 个元素触发多次扩容
        for (int i = 0; i < 100; i++) {
            ad.addLast(i);
        }
        assertEquals(100, ad.size());

        // 验证 get 能正确获取值
        for (int i = 0; i < 100; i++) {
            assertEquals(Integer.valueOf(i), ad.get(i));
        }

        // 删除 90 个元素，触发多次缩容
        for (int i = 0; i < 90; i++) {
            assertEquals(Integer.valueOf(i), ad.removeFirst());
        }
        assertEquals(10, ad.size());
    }

    @Test
    /** 测试越界获取是否返回 null */
    public void getOutOfBoundsTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ad.addLast(1);
        ad.addLast(2);

        assertNull(ad.get(-1));
        assertNull(ad.get(2));
        assertNull(ad.get(100));
    }

    @Test
    /** 测试迭代器 */
    public void iteratorTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ad.addLast(1);
        ad.addLast(2);
        ad.addLast(3);

        int count = 0;
        int expected = 1;
        for (int x : ad) {
            assertEquals(expected, x);
            expected++;
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    /** 测试 equals 方法（包括与 LinkedListDeque 比较） */
    public void equalsTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        ArrayDeque<Integer> ad2 = new ArrayDeque<>();
        LinkedListDeque<Integer> lld = new LinkedListDeque<>();

        ad1.addLast(1); ad1.addLast(2);
        ad2.addLast(1); ad2.addLast(2);
        lld.addLast(1); lld.addLast(2);

        assertTrue(ad1.equals(ad2));
        assertTrue(ad1.equals(lld)); // 跨类型比较应该为 true

        ad2.addLast(3);
        assertFalse(ad1.equals(ad2));
    }
}
