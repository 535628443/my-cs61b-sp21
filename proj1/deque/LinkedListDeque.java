package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private static class Node<T> {
        T item;
        Node<T> prev;
        Node<T> next;

        Node(T item, Node<T> prev, Node<T> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    private Node<T> sentinel;
    private int size;

    // 无参构造方法：初始化一个空的双向链表
    public LinkedListDeque() {
        this.sentinel = new Node<>(null, null, null);
        this.sentinel.next = sentinel; // 闭环：next指向自己
        this.sentinel.prev = sentinel; // 闭环：prev指向自己
        this.size = 0;
    }

    @Override
    public void addFirst(T item) {
        // TODO: 实现添加元素到队首
        Node<T> p = new Node<>(item, sentinel, sentinel.next);
        sentinel.next.prev = p;
        sentinel.next = p;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        // TODO: 实现添加元素到队尾
        Node<T> p = new Node<>(item, sentinel.prev, sentinel);
        sentinel.prev.next = p;
        sentinel.prev = p;
        size += 1;
    }

    @Override
    public int size() {
        // TODO: 返回队列中的元素个数
        return size;
    }

    @Override
    public void printDeque() {
        // TODO: 打印队列中的元素
        Node<T> p = sentinel.next; // 从真正的第一个元素开始
        while (p != sentinel) {    // 遇到 sentinel 说明绕完一圈了
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println(); // 打印完毕后换行，让输出更美观
    }

    @Override
    public T removeFirst() {
        // TODO: 移除并返回队首元素
        if (size < 1) {
            return null;
        }
        T x = sentinel.next.item;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel; // 新的队首向左指回 sentinel
        size -= 1;
        return x;
    }

    @Override
    public T removeLast() {
        // TODO: 移除并返回队尾元素
        if (size < 1) {
            return null;
        }
        T x = sentinel.prev.item;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel; // 新的队尾向右指回 sentinel
        size -= 1;
        return x;
    }

    @Override
    public T get(int index) {
        // TODO: 获取指定索引位置的元素
        if (index < 0 || index >= size) {
            return null;
        }
        Node<T> p = sentinel.next;
        for(int i = 0; i < index; i++) {
            p = p.next;
        }
        return p.item;
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        } else {
            return getRecursiveHelper(sentinel.next, index);
        }
    }

    private T getRecursiveHelper (Node<T> p, int index) {
        if (index == 0) {
            return p.item;
        }else {
            return getRecursiveHelper(p.next, index-1);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        // 检查 o 是否实现了 Deque 接口
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<?> other = (Deque<?>) o;
        if (this.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            // 比较对应位置的元素是否相等
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node<T> p;

        LinkedListDequeIterator() {
            p = sentinel.next; // 从第一个真实元素开始
        }

        @Override
        public boolean hasNext() {
            return p != sentinel; // 当指针绕回到 sentinel 时，迭代结束
        }

        @Override
        public T next() {
            T item = p.item;
            p = p.next; // 指针后移
            return item;
        }
    }
}
