package gitlet;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.*;

/*
Repository
  │
  ├── public command methods
  │   ├── init()
  │   ├── add()
  │   ├── commit()
  │   ├── rm()
  │   ├── log()
  │   ├── checkout()
  │   ├── reset()
  │   └── merge()
  │
  └── private helpers
      ├── getCurrentBranch()
      ├── getHeadCommitId()
      ├── getHeadCommit()
      ├── readCommit()
      ├── resolveCommitId()
      ├── checkoutFile()
      ├── checkoutCommit()
      ├── checkUntrackedFiles()
      └── findSplitPoint()
 */

/** Represents a gitlet repository.
 *  @author Andy Yang
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    public static final File OBJECTS_DIR = Utils.join(GITLET_DIR, "objects");
    public static final File COMMITS_DIR = Utils.join(OBJECTS_DIR, "commits");
    public static final File BLOBS_DIR = Utils.join(OBJECTS_DIR, "blobs");

    public static final File REFS_DIR = Utils.join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = Utils.join(REFS_DIR, "heads");

    public static final File HEAD_FILE = Utils.join(GITLET_DIR, "HEAD");
    public static final File STAGING_FILE = Utils.join(GITLET_DIR, "staging");

    /**
     * 1. 检查 .gitlet 是否已经存在
     * 2. 创建所有目录
     * 3. 创建 initial commit
     * 4. 保存 initial commit，得到它的 hash
     * 5. 创建 master branch，并写入 initial commit hash
     * 6. HEAD 写入 "master"
     * 7. 创建并保存空 Stage
     */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();

        Commit initial = new Commit();
        String id = initial.save();
        File master = Utils.join(HEADS_DIR,"master");

        Utils.writeContents(master, id);
        Utils.writeContents(HEAD_FILE, "master");

        Stage stage = new Stage();
        stage.save();
    }

    /**
     * 获取当前 HEAD 所指向的分支名称（例如 "master"）。
     */
    private static String getCurrentBranch() {
        return Utils.readContentsAsString(HEAD_FILE);
    }

    /**
     * 获取当前分支 HEAD 提交的 SHA-1 哈希值。
     */
    private static String getHeadCommitId() {
        String branch = getCurrentBranch();
        return Utils.readContentsAsString(Utils.join(HEADS_DIR, branch));
    }

    /**
     * 获取当前 HEAD 指向的 Commit 对象
     */
    private static Commit getHeadCommit() {
        String id = getHeadCommitId();
        return Commit.fromFile(id);
    }

    /**
     *
     * @param fileName
     *
     * 找working dir 的文件 (不存在就报错)
     *
     */
    public static void add(String fileName) {
        File file = Utils.join(CWD, fileName);
        if(!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        // fileName 的 sha1
        byte[] contents = Utils.readContents(file);
        String boldId = Utils.sha1(contents);

        // HEAD 里 trackedFile 的 sha1
        Commit head = getHeadCommit();
        String trackedBoldId = head.getTrackedFiles().get(fileName);

        Stage stage = Stage.load();

        // 没有任何改动就直接清空暂存区中对应的文件
        if (boldId.equals(trackedBoldId)) {
            stage.getAddedFiles().remove(fileName);
            stage.getRemovedFiles().remove(fileName);
            stage.save();
            return;
        }

        // 如果有存在改动
        File blobFile = Utils.join(BLOBS_DIR, boldId);
        if (!blobFile.exists()) {
            Utils.writeContents(blobFile, contents);
        }

        // 更新暂存区的内容
        stage.stageForAddition(fileName, boldId);
        stage.save();
    }

    /**
     *
     * @param message
     *
     * 1. 检查 message 是否为空，检查暂存区是否有变更
     * 2. 获取当前 HEAD Commit，克隆一份当前追踪文件表副本
     * 3. 用暂存区数据更新副本（应用增加与删除）
     * 4. 创建并保存新的 Commit 对象（parent 指向原 HEAD）
     * 5. 更新当前分支文件指针指向新 Commit
     * 6. 清空并保存暂存区
     */
    public static void commit(String message) {
        if (message == null || message.trim().isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        Stage stage = Stage.load();
        if (stage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        String headId = getHeadCommitId();
        Commit head = Commit.fromFile(headId);
        // 新建一个当前 trackedFiles 的副本
        Map<String, String> newTrackedFile = new HashMap<>(head.getTrackedFiles());
        newTrackedFile.putAll(stage.getAddedFiles());
        for(String file : stage.getRemovedFiles()) {
            newTrackedFile.remove(file);
        }

        Commit newCommit = new Commit(
                message,
                // 默认传入当前时间
                new Date(),
                headId,
                null,
                newTrackedFile
        );

        String newCommitId = newCommit.save();
        String currentBranch = getCurrentBranch();
        File branchFile = Utils.join(HEADS_DIR, currentBranch);
        Utils.writeContents(branchFile, newCommitId);

        stage.clear();
        stage.save();
    }
}
