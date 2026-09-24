package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
  Stage
    │
    ├── 数据
    │    ├── addedFiles
    │    └── removedFiles
    │
    ├── 磁盘操作
    │     ├── load()
    │     └── save()
    │
    └── 状态操作
           ├── clear()
           ├── isEmpty()
           ├── stageForAddition()
           └── stageForRemoval()
 */

public class Stage implements Serializable {
    private Map<String, String> addedFiles;
    private Set<String> removedFiles;

    /** 初始化空的暂存区 */
    public Stage() {
        this.addedFiles = new HashMap<>();
        this.removedFiles = new HashSet<>();
    }

    /** 将当前暂存区持久化保存到磁盘（STAGING_FILE） */
    public void save() {
        Utils.writeObject(Repository.STAGING_FILE, this);
    }

    /** 从磁盘反序列化读取并返回暂存区对象 */
    public static Stage load() {
        return Utils.readObject(Repository.STAGING_FILE, Stage.class);
    }

    /** 清空暂存区中的所有变更（待添加与待删除） */
    public void clear() {
        addedFiles.clear();
        removedFiles.clear();
    }

    /** 判断暂存区是否为空（既无待添加也无待删除变更） */
    public boolean isEmpty() {
        return addedFiles.isEmpty() && removedFiles.isEmpty();
    }

    /** 将文件暂存为待添加；若此前被标记为待删除，则取消删除标记 */
    public void stageForAddition(String fileName, String blobId) {
        addedFiles.put(fileName, blobId);
        removedFiles.remove(fileName);
    }

    /** 将文件暂存为待删除；若此前已暂存待添加，则取消添加暂存 */
    public void stageForRemoval(String fileName) {
        addedFiles.remove(fileName);
        removedFiles.add(fileName);
    }

    /** 获取待添加的文件映射表（文件名 -> Blob SHA-1） */
    public Map<String, String> getAddedFiles() {
        return addedFiles;
    }

    /** 获取待删除的文件名集合 */
    public Set<String> getRemovedFiles() {
        return removedFiles;
    }
}
