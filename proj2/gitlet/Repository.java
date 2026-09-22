package gitlet;

import java.io.File;
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
     * 获取当前 HEAD 指向的 Commit 对象。
     */
    private static Commit getHeadCommit() {
        String id = getHeadCommitId();;
        return Commit.fromFile(id);
    }


}
