package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
Commit
├── fields
├── constructor
├── getters
├── getSha1()
├── save()
└── fromFile()
 */

/** Represents a gitlet commit object.
 *
 *  does at a high level.
 *
 *  @author Andy Yang
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date timestamp;
    private String parent;
    private String secondParent;
    private Map<String, String> trackedFiles;

    public Commit () {
        message = "initial commit";
        /* Date(0) 指的是Unix Epoch (即 00:00:00 UTC, Thursday, 1 January 1970)
         为了让全世界所有学生、所有仓库的第一个 Commit，拥有完全一模一样的 SHA-1 哈希值
         */
        timestamp = new Date(0);
        parent = null;
        secondParent = null;
        trackedFiles = new HashMap<>();
    }

    public Commit (String message,
                  Date timestamp,
                  String parent,
                  String secondParent,
                  Map<String, String> trackedFiles) {
        this.message = message;
        this.timestamp = timestamp;
        this.parent = parent;
        this.secondParent = secondParent;
        this.trackedFiles = trackedFiles;
    }

    public String getSha1() {
        /* serialize 把当前这个 Commit 对象里的所有东西
        （message、timestamp、parent、trackedFiles ...）
        整整齐齐地打包序列化成一串 byte[] 二进制字节流
         */
        return Utils.sha1(Utils.serialize(this));
    }

    public String save() {
        String id = getSha1();
        File commitFile = Utils.join(Repository.COMMITS_DIR, id);
        Utils.writeObject(commitFile, this);
        return id;
    }

    public static Commit fromFile(String id) {
        File commitFile = Utils.join(Repository.COMMITS_DIR, id);
        return Utils.readObject(commitFile, Commit.class);
    }


    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getParent() {
        return parent;
    }

    public String getSecondParent() {
        return secondParent;
    }

    public Map<String, String> getTrackedFiles() {
        return trackedFiles;
    }
}
