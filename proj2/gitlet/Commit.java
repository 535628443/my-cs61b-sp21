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


    public Commit() {
        message = "initial commit";
        /* Date(0) 指的是 Unix Epoch (即 00:00:00 UTC, Thursday, 1 January 1970)
         * 为了让全世界所有学生、所有仓库的第一个 Commit，拥有完全一模一样的 SHA-1 哈希值
         */
        timestamp = new Date(0);
        parent = null;
        secondParent = null;
        trackedFiles = new HashMap<>();
    }

    public Commit(String message,
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

    /**
     * 计算并返回当前 Commit 对象的 SHA-1 哈希值。
     * 将整个 Commit 对象序列化为字节流后计算哈希，作为该 Commit 的唯一标识符（ID）。
     *
     * @return 40 位的 SHA-1 哈希字符串
     */
    public String getSha1() {
        /* serialize 把当前这个 Commit 对象里的所有东西
         * （message、timestamp、parent、trackedFiles ...）
         * 整整齐齐地打包序列化成一串 byte[] 二进制字节流
         */
        return Utils.sha1(Utils.serialize(this));
    }

    /**
     * 将当前 Commit 对象持久化保存到磁盘（.gitlet/objects/commits/ 目录下）。
     * 保存的文件名即为其 SHA-1 哈希值。
     *
     * @return 保存的 Commit 的 SHA-1 哈希值
     */
    public String save() {
        String id = getSha1();
        File commitFile = Utils.join(Repository.COMMITS_DIR, id);
        Utils.writeObject(commitFile, this);
        return id;
    }

    /**
     * 根据指定的 Commit ID（SHA-1 哈希值），从磁盘读取并反序列化出 Commit 对象。
     *
     * @param id Commit 的 SHA-1 哈希值
     * @return 反序列化还原出的 Commit 对象
     */
    public static Commit fromFile(String id) {
        File commitFile = Utils.join(Repository.COMMITS_DIR, id);
        return Utils.readObject(commitFile, Commit.class);
    }

    /**
     * 获取当前 Commit 的提交日志信息。
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取当前 Commit 的生成时间戳。
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * 获取第一个父 Commit 的 SHA-1 哈希值。
     */
    public String getParent() {
        return parent;
    }

    /**
     * 获取第二个父 Commit 的 SHA-1 哈希值（仅 merge 产生的提交存在，平时为 null）。
     */
    public String getSecondParent() {
        return secondParent;
    }

    /**
     * 获取当前 Commit 追踪的所有文件映射表（文件名 -> Blob SHA-1）。
     */
    public Map<String, String> getTrackedFiles() {
        return trackedFiles;
    }
}
