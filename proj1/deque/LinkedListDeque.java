package deque;

public class LinkedListDeque<T> implements Deque<T>{
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
        this.size = 0;
    }

    // 带参构造方法：初始化时就放入一个元素
    public LinkedListDeque(T x) {
        this.sentinel = new Node<>(null, null, null);
        this.sentinel.next = new Node<>(x, this.sentinel, null);
        this.size = 1;
    }

    @Override
    public void addFirst(T item) {
        // TODO: 实现添加元素到队首
        sentinel.next = new Node<>(item, sentinel, sentinel.next);
        size += 1;
    }

    @Override
    public void addLast(T item) {
        // TODO: 实现添加元素到队尾
        Node<T> p = sentinel;
        while (p.next != null) {
            p = p.next;
        }
        p.next = new Node<>(item, p, null);
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
        Node<T> p = sentinel;
        while (p.next != null) {
            p = p.next;
            System.out.print(p.item + " ");
        }
    }

    @Override
    public T removeFirst() {
        // TODO: 移除并返回队首元素
        if (sentinel.next != null) {
            T x = sentinel.next.item;
            sentinel.next = sentinel.next.next;
            size -= 1;
            return x;
        } else {
            return null;
        }
    }

    @Override
    public T removeLast() {
        // TODO: 移除并返回队尾元素
        Node<T> p = sentinel;
        if (sentinel.next != null) {
            while (p.next != null){
                p = p.next;
            }
            p.prev.next = null;
            size -= 1; // 别忘了更新 size！
            return p.item; // 直接返回 p.item，不需要定义 x
        } else {
            return null;
        }
    }

    @Override
    public T get(int index) {
        // TODO: 获取指定索引位置的元素
        Node<T> p = sentinel;
        for(int i = 0; i < index; i++) {
            if (p.next != null) {
                p = p.next;
            } else {
                return null;
            }
        }
        return p.item;
    }
}
