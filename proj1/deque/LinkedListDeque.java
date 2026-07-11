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

    public T getRecursive(int index) {
        if (index < 1 || index > size) {
            return null;
        } else {
            return getRecursiveHelper(sentinel.next, index-1);
        }
    }

    private T getRecursiveHelper (Node<T> p, int index) {
        if (index == 0) {
            return p.item;
        }else {
            return getRecursiveHelper(p.next, index-1);
        }
    }
}
