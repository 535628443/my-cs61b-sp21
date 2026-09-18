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

    public Stage() {
        this.addedFiles = new HashMap<>();
        this.removedFiles = new HashSet<>();
    }

    public void save() {
        Utils.writeObject(Repository.STAGING_FILE, this);
    }

    public static Stage load() {
        return Utils.readObject(Repository.STAGING_FILE, Stage.class);
    }

    public void clear() {
        addedFiles.clear();
        removedFiles.clear();
    }

    public boolean isEmpty() {
        return addedFiles.isEmpty() && removedFiles.isEmpty();
    }

    public void stageForAddition(String fileName, String blobId) {
        addedFiles.put(fileName, blobId);
        removedFiles.remove(fileName);
    }

    public void stageForRemoval(String fileName) {
        addedFiles.remove(fileName);
        removedFiles.add(fileName);
    }

    public Map<String, String> getAddedFiles() {
        return addedFiles;
    }

    public Set<String> getRemovedFiles() {
        return removedFiles;
    }
}
