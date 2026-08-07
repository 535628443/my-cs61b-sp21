package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    // ---- 内部节点类 ----
    private class BSTNode {
        K key;
        V value;
        BSTNode left, right;

        // BSTNode 的构造方法
        BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    // ---- 实例变量 ----
    private BSTNode root;
    private int size;

    // BSTMap 的构造方法 (无参构造一颗空树)
    public BSTMap() {
        clear();
    }

    // 辅助函数
    private BSTNode get(BSTNode n, K key) {
        if (n == null) {
            return null;
        }
        int cmp = key.compareTo(n.key);
        if (cmp < 0) {
            return get(n.left, key);
        } else if (cmp > 0) {
            return get(n.right, key);
        } else {
            return n;
        }
    }

    private BSTNode put (BSTNode n, K key, V value) {
        if (n == null) {
            size++;
            return new BSTNode(key, value);
        }

        int cmp = key.compareTo(n.key);
        if (cmp < 0) {
            n.left = put (n.left, key, value);
        } else if (cmp > 0){
            n.right = put (n.right, key, value);
        } else {
            n.value = value;
        }
        return n;
    }

    private void keySet (BSTNode n, Set<K> set) {
        if (n == null) {
            return;
        }
        // 中序遍历(左->中->右)
        // 改成 先/后 序遍历 只需要下列三行换位置即可
        keySet(n.left, set);
        set.add(n.key);
        keySet(n.right, set);
    }



    // 接口实现
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        BSTNode n = get(root, key);
        if (n == null) {
            return false;
        }
        return true;
    }

    @Override
    public V get(K key) {
        BSTNode n = get(root, key);
        if (n == null) {
            return null;
        }
        return n.value;
    }
    @Override
    public int size() {
        return size;
    }
    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
    }
    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        keySet(root, set);
        return set;
    }
    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }
    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

}
