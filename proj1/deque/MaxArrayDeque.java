package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> defaultComparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        this.defaultComparator = c;
    }

    // 使用构造时传入的默认比较器寻找最大值
    public T max() {
        return max(defaultComparator);
    }

    // 使用传入的自定义比较器 c 寻找最大值
    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }

        T maxItem = get(0); // 假定第 0 个元素最大
        for (int i = 1; i < size(); i++) {
            T currentItem = get(i);
            // c.compare(x, y) 如果返回正数，说明 x 比 y 大
            if (c.compare(currentItem, maxItem) > 0) {
                maxItem = currentItem;
            }
        }
        return maxItem;
    }
}
