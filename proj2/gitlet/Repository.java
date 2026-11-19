package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/*
Repository
  │
  ├── public command methods
  │   ├── init()
  │   ├── add()
  │   ├── commit()
  │   ├── rm()
  │   ├── log()
  │   ├── globalLog()
  │   ├── find()
  │   ├── status()
  │   ├── checkout()
  │   ├── checkoutBranch()
  │   ├── branch()
  │   ├── rmBranch()
  │   ├── reset()
  │   └── merge()
  │
  └── private helpers
      ├── getCurrentBranch()
      ├── getHeadCommitId()
      ├── getHeadCommit()
      ├── printCommit()
      ├── resolveCommitId()
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

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    public static final File OBJECTS_DIR = Utils.join(GITLET_DIR, "objects");
    public static final File COMMITS_DIR = Utils.join(OBJECTS_DIR, "commits");
    public static final File BLOBS_DIR = Utils.join(OBJECTS_DIR, "blobs");

    public static final File REFS_DIR = Utils.join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = Utils.join(REFS_DIR, "heads");

    public static final File HEAD_FILE = Utils.join(GITLET_DIR, "HEAD");
    public static final File STAGING_FILE = Utils.join(GITLET_DIR, "staging");
    public static final File REMOTES_DIR = Utils.join(GITLET_DIR, "remotes");

    /**
     * 初始化 Gitlet 版本控制系统，创建初始提交与 master 分支
     */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
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
        File master = Utils.join(HEADS_DIR, "master");

        Utils.writeContents(master, id);
        Utils.writeContents(HEAD_FILE, "master");

        Stage stage = new Stage();
        stage.save();
    }

    /**
     * 将工作区文件添加到暂存区（生成 Blob 并暂存）
     */
    public static void add(String fileName) {
        File file = Utils.join(CWD, fileName);
        if (!file.exists()) {
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
     * 保存当前暂存区的文件快照，创建新 Commit 并推进当前分支
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
        for (String file : stage.getRemovedFiles()) {
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

    /**
     * 取消暂存文件；若已被当前 Commit 追踪则标记删除并从工作区移除
     */
    public static void rm(String fileName) {
        // 处理暂存区的文件
        Stage stage = Stage.load();
        boolean staged = stage.getAddedFiles().containsKey(fileName);

        // 处理被追踪的文件
        Commit head = getHeadCommit();
        boolean tracked = head.getTrackedFiles().containsKey(fileName);

        if (!staged && !tracked) {
            System.out.println("No reason to remove the file.");
            return;
        }

        // file 只 add 但是还没 commit 的情况(在暂存区, 但没被 tracked)
        if (staged) {
            stage.getAddedFiles().remove(fileName);
        }

        if (tracked) {
            stage.stageForRemoval(fileName);
            File file = Utils.join(CWD, fileName);
            Utils.restrictedDelete(file);
        }

        stage.save();
    }

    /**
     * 从当前 HEAD 开始沿父节点链回溯并打印提交历史
     */
    public static void log() {
        String commitId = getHeadCommitId();

        // 循环到 init commit 就退出
        while (commitId != null) {
            Commit commit = Commit.fromFile(commitId);
            printCommit(commit, commitId);
            commitId = commit.getParent();
        }
    }

    /**
     * 打印历史上创建过的所有 Commit，顺序不限
     */
    public static void globalLog() {
        List<String> commitIds = Utils.plainFilenamesIn(COMMITS_DIR);

        for (String commitId : commitIds) {
            Commit commit = Commit.fromFile(commitId);
            printCommit(commit, commitId);
        }
    }

    /**
     * 查找并打印所有具有指定提交信息的 Commit ID
     */
    public static void find(String message) {
        List<String> commitIds = Utils.plainFilenamesIn(COMMITS_DIR);
        boolean found = false;

        for (String commitId : commitIds) {
            Commit commit = Commit.fromFile(commitId);

            if (commit.getMessage().equals(message)) {
                found = true;
                System.out.println(commitId);
            }
        }

        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * 显示当前分支、暂存文件、标记删除文件、未暂存修改及未追踪文件
     */
    public static void status() {
        String currentBranch = getCurrentBranch();
        Stage stage = Stage.load();
        Commit head = getHeadCommit();

        printBranches(currentBranch);
        printStagedFiles(stage);
        printRemovedFiles(stage);
        printModificationsNotStaged(head, stage);
        printUntrackedFiles(head, stage);
    }

    private static void printBranches(String currentBranch) {
        List<String> branches = Utils.plainFilenamesIn(HEADS_DIR);
        System.out.println("=== Branches ===");
        if (branches != null) {
            Collections.sort(branches);
            for (String branch : branches) {
                if (branch.equals(currentBranch)) {
                    System.out.println("*" + branch);
                } else {
                    System.out.println(branch);
                }
            }
        }
        System.out.println();
    }

    private static void printStagedFiles(Stage stage) {
        System.out.println("=== Staged Files ===");
        List<String> stagedFiles = new ArrayList<>(stage.getAddedFiles().keySet());
        Collections.sort(stagedFiles);
        for (String fileName : stagedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    private static void printRemovedFiles(Stage stage) {
        System.out.println("=== Removed Files ===");
        List<String> removedFiles = new ArrayList<>(stage.getRemovedFiles());
        Collections.sort(removedFiles);
        for (String fileName : removedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    private static void printModificationsNotStaged(Commit head, Stage stage) {
        System.out.println("=== Modifications Not Staged For Commit ===");
        Map<String, String> modifiedNotStaged = new HashMap<>();

        for (String fileName : head.getTrackedFiles().keySet()) {
            File file = Utils.join(CWD, fileName);
            String headHash = head.getTrackedFiles().get(fileName);

            if (file.exists()) {
                String fileHash = Utils.sha1(Utils.readContents(file));
                if (!fileHash.equals(headHash) && !stage.getAddedFiles().containsKey(fileName)) {
                    modifiedNotStaged.put(fileName, "(modified)");
                }
            } else {
                if (!stage.getRemovedFiles().contains(fileName)) {
                    modifiedNotStaged.put(fileName, "(deleted)");
                }
            }
        }

        for (String fileName : stage.getAddedFiles().keySet()) {
            File file = Utils.join(CWD, fileName);
            String stageHash = stage.getAddedFiles().get(fileName);

            if (file.exists()) {
                String fileHash = Utils.sha1(Utils.readContents(file));
                if (!fileHash.equals(stageHash)) {
                    modifiedNotStaged.put(fileName, "(modified)");
                }
            } else {
                modifiedNotStaged.put(fileName, "(deleted)");
            }
        }

        List<String> sortedModified = new ArrayList<>(modifiedNotStaged.keySet());
        Collections.sort(sortedModified);
        for (String fileName : sortedModified) {
            System.out.println(fileName + " " + modifiedNotStaged.get(fileName));
        }
        System.out.println();
    }

    private static void printUntrackedFiles(Commit head, Stage stage) {
        System.out.println("=== Untracked Files ===");
        List<String> workingFiles = Utils.plainFilenamesIn(CWD);
        List<String> untrackedFiles = new ArrayList<>();

        if (workingFiles != null) {
            for (String fileName : workingFiles) {
                boolean isStaged = stage.getAddedFiles().containsKey(fileName);
                boolean isTracked = head.getTrackedFiles().containsKey(fileName)
                        && !stage.getRemovedFiles().contains(fileName);
                if (!isStaged && !isTracked) {
                    untrackedFiles.add(fileName);
                }
            }
        }

        Collections.sort(untrackedFiles);
        for (String fileName : untrackedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /**
     * checkout 的第一种形式：checkout -- [file name]
     * 从当前 HEAD 提交中检出指定文件，覆盖工作区中的同名文件。新版本文件不会被暂存（unstaged）。
     *
     * @param fileName 要检出的文件名
     */
    public static void checkout(String fileName) {
        // 直接复用 checkout(commitId, fileName)，传入当前分支 HEAD 的 Commit ID
        checkout(getHeadCommitId(), fileName);
    }

    /**
     * checkout 的第二种形式：checkout [commit id] -- [file name]
     * 从指定 commit 中检出指定文件，覆盖工作区中的同名文件。新版本文件不会被暂存（unstaged）。
     *
     * @param commitId commit 哈希值（支持短 commit ID）
     * @param fileName 要检出的文件名
     */
    public static void checkout(String commitId, String fileName) {
        // 1. 解析 commit ID（支持短 ID）
        String fullCommitId = resolveCommitId(commitId);
        if (fullCommitId == null) {
            System.out.println("No commit with that id exists.");
            return;
        }

        // 2. 检查传入的 commit 是否最终了 传入的 file 给 tracked
        Commit commit = Commit.fromFile(fullCommitId);
        String blobId = commit.getTrackedFiles().get(fileName);
        // 没被 tracked 的情况
        if (blobId == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        File blobFile = Utils.join(BLOBS_DIR, blobId);
        byte[] contents = Utils.readContents(blobFile);
        File targetFile = Utils.join(CWD, fileName);
        Utils.writeContents(targetFile, contents);
    }

    /**
     * checkout 的第三种形式：checkout [branch name]
     * 检出指定分支 HEAD 提交处的所有文件，恢复工作区，并将当前分支切换为该分支。
     *
     * @param branchName 要检出的目标分支名称
     */
    public static void checkoutBranch(String branchName) {
        File branchFile = Utils.join(HEADS_DIR, branchName);
        // 1. 检查存在性
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            return;
        }

        // 检查是否到了当前分支
        if (branchName.equals(getCurrentBranch())) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        String targetId = Utils.readContentsAsString(branchFile);
        Commit targetCommit = Commit.fromFile(targetId);
        Commit currentHead = getHeadCommit();

        // 未追踪文件冲突检查
        if (checkUntrackedFiles(targetCommit)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            return;
        }

        // 目标分支的所有 条目(entry) 都写到工作区
        for (Map.Entry<String, String> entry : targetCommit.getTrackedFiles().entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            File blobFile = Utils.join(BLOBS_DIR, blobId);
            // 如果 CWD/fileName 不存在, writeContents 将会自动创建
            Utils.writeContents(Utils.join(CWD, fileName), Utils.readContents(blobFile));
        }

        // 删除工具区有, 但目标分支没有的文件
        for (String fileName : currentHead.getTrackedFiles().keySet()) {
            if (!targetCommit.getTrackedFiles().containsKey(fileName)) {
                Utils.restrictedDelete(Utils.join(CWD, fileName));
            }
        }

        // 清空缓存区
        Stage stage = Stage.load();
        stage.clear();
        stage.save();

        // 修改当前 HEAD
        Utils.writeContents(HEAD_FILE, branchName);
    }

    /**
     * 创建一个具有给定名称的新分支，并将其指向当前的 HEAD 提交。
     * 注意：该命令仅创建分支指针，不会自动切换到新分支。
     *
     * @param branchName 新分支名称
     */
    public static void branch(String branchName) {
        // 检查存在性
        File branchFile = Utils.join(HEADS_DIR, branchName);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }

        // 新的分支 从当前 HEAD 的基础上继续开发
        String currentHead = getHeadCommitId();
        Utils.writeContents(branchFile, currentHead);
    }

    /**
     * 删除具有给定名称的分支指针（仅删除指针文件，不删除历史 Commit）。
     *
     * @param branchName 要删除的分支名称
     */
    public static void rmBranch(String branchName) {
        // 检查存在性
        File branchFile = Utils.join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        // 检查待删除分支是否为当前分支
        String currentBranch = getCurrentBranch();
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branchFile.delete();
    }

    /**
     * 将当前工作区和 HEAD 指针重置为指定 commit 的快照状态。
     *
     * @param commitId 目标 commit 的哈希值（支持短 ID）
     */
    public static void reset(String commitId) {
        // resolve 成全 hash (传入的参数可能是缩短过的)
        String targetCommitId = resolveCommitId(commitId);
        if (targetCommitId == null) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit targetCommit = Commit.fromFile(targetCommitId);
        Commit currentHead = getHeadCommit();

        // 检查是否有未被追踪的文件
        if (checkUntrackedFiles(targetCommit)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            return;
        }

        // commit 快照的内容依次写进工作区
        for (Map.Entry<String, String> entry : targetCommit.getTrackedFiles().entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            File blobFile = Utils.join(BLOBS_DIR, blobId);
            Utils.writeContents(Utils.join(CWD, fileName), Utils.readContents(blobFile));
        }

        // 清除工作区有, 但目标 commit 快照中没有的文件
        for (String fileName : currentHead.getTrackedFiles().keySet()) {
            if (!targetCommit.getTrackedFiles().containsKey(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }

        // 清空暂存区
        Stage stage = Stage.load();
        stage.clear();
        stage.save();

        // 刷新 Head
        String currentBranch = getCurrentBranch();
        File newBranchFile = Utils.join(HEADS_DIR, currentBranch);
        Utils.writeContents(newBranchFile, targetCommitId);
    }

    /**
     * 将目标分支合并到当前分支中。
     *
     * @param branchName 要合并进来的目标分支名称
     */
    public static void merge(String branchName) {
        Stage stage = Stage.load();
        File branchFile = Utils.join(HEADS_DIR, branchName);
        String currentBranch = getCurrentBranch();

        if (!validateMergePreconditions(branchName, stage, branchFile, currentBranch)) {
            return;
        }

        String currentCommitId = getHeadCommitId();
        String otherCommitId = Utils.readContentsAsString(branchFile);
        String splitCommitId = findSplitPoint(currentCommitId, otherCommitId);

        Commit currentCommit = Commit.fromFile(currentCommitId);
        Commit otherCommit = Commit.fromFile(otherCommitId);
        Commit splitCommit = Commit.fromFile(splitCommitId);

        if (splitCommitId.equals(otherCommitId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitCommitId.equals(currentCommitId)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }


        // 防止合并后没有被 tracked 的文件会被覆盖
        if (checkUntrackedFiles(otherCommit)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            return;
        }

        // 搜集三方的所有的文件(去重)
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(splitCommit.getTrackedFiles().keySet());
        allFiles.addAll(currentCommit.getTrackedFiles().keySet());
        allFiles.addAll(otherCommit.getTrackedFiles().keySet());

        boolean conflictOccurred = processMergeFiles(allFiles, splitCommit,
                currentCommit, otherCommit, stage);

        createMergeCommit(branchName, currentBranch, currentCommitId,
                otherCommitId, stage);

        // 如果发生过冲突，打印提示
        if (conflictOccurred) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static boolean validateMergePreconditions(String branchName, Stage stage,
                                                      File branchFile, String currentBranch) {
        if (!stage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return false;
        }
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return false;
        }
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return false;
        }
        return true;
    }

    private static boolean processMergeFiles(Set<String> allFiles, Commit splitCommit,
                                             Commit currentCommit, Commit otherCommit,
                                             Stage stage) {
        boolean conflictOccurred = false;

        for (String fileName : allFiles) {
            String s = splitCommit.getTrackedFiles().get(fileName);
            String c = currentCommit.getTrackedFiles().get(fileName);
            String g = otherCommit.getTrackedFiles().get(fileName);

            File targetFile = Utils.join(CWD, fileName);

            // 1. 当前和目标相同
            if (Objects.equals(c, g)) {
                continue;
            } else if (Objects.equals(g, s)) {
                // 2. 当前改动, 目标不变
                continue;
            } else if (Objects.equals(c, s)) {
                // 3. 目标改动, 当前不变
                if (g != null) {
                    File blobFile = Utils.join(BLOBS_DIR, g);
                    byte[] contents = Utils.readContents(blobFile);
                    Utils.writeContents(targetFile, contents);
                    stage.stageForAddition(fileName, g);
                } else {
                    Utils.restrictedDelete(targetFile);
                    stage.stageForRemoval(fileName);
                }
            } else {
                // 4. 当前和目标均改动
                conflictOccurred = true;
                handleMergeConflict(fileName, c, g, targetFile, stage);
            }
        }
        return conflictOccurred;
    }

    private static void handleMergeConflict(String fileName, String c, String g,
                                            File targetFile, Stage stage) {
        String cContent = "";
        if (c != null) {
            File cBlob = Utils.join(BLOBS_DIR, c);
            cContent = Utils.readContentsAsString(cBlob);
        }

        String gContent = "";
        if (g != null) {
            File gBlob = Utils.join(BLOBS_DIR, g);
            gContent = Utils.readContentsAsString(gBlob);
        }

        String conflictText = "<<<<<<< HEAD\n"
                + cContent
                + "=======\n"
                + gContent
                + ">>>>>>>\n";

        Utils.writeContents(targetFile, conflictText);

        String blobId = Utils.sha1(conflictText);
        File blobFile = Utils.join(BLOBS_DIR, blobId);
        if (!blobFile.exists()) {
            Utils.writeContents(blobFile, conflictText);
        }
        stage.stageForAddition(fileName, blobId);
    }

    private static void createMergeCommit(String branchName, String currentBranch,
                                          String currentCommitId, String otherCommitId,
                                          Stage stage) {
        String message = "Merged " + branchName + " into " + currentBranch + ".";
        Commit head = getHeadCommit();
        Map<String, String> newTrackedFiles = new HashMap<>(head.getTrackedFiles());

        newTrackedFiles.putAll(stage.getAddedFiles());

        for (String file : stage.getRemovedFiles()) {
            newTrackedFiles.remove(file);
        }

        Commit mergeCommit = new Commit(message, new Date(),
                currentCommitId, otherCommitId, newTrackedFiles);
        String mergeCommitId = mergeCommit.save();

        File branchFileCurrent = Utils.join(HEADS_DIR, currentBranch);
        Utils.writeContents(branchFileCurrent, mergeCommitId);
        stage.clear();
        stage.save();
    }

    /**
     * 添加远程仓库配置
     */
    public static void addRemote(String remoteName, String remotePath) {
        // 确保目录存在
        if (!REMOTES_DIR.exists()) {
            REMOTES_DIR.mkdirs();
        }

        // 定位 remote 对应文件
        File remoteFile = Utils.join(REMOTES_DIR, remoteName);

        // 如果已经添加过同名 remote，报错并返回
        if (remoteFile.exists()) {
            System.out.println("A remote with that name already exists.");
            return;
        }

        // 跨平台兼容：将 '/' 统一替换为当前操作系统的路径分隔符（Windows 为 '\'，Mac 为 '/'）
        String normalizedPath = remotePath.replace('/', File.separatorChar);

        // 路径写入文件
        Utils.writeContents(remoteFile, normalizedPath);
    }

    /**
     * 删除远程仓库配置
     */
    public static void rmRemote(String remoteName) {
        File remoteFile = Utils.join(REMOTES_DIR, remoteName);

        if (!remoteFile.exists()) {
            System.out.println("A remote with that name does not exist.");
            return;
        }

        remoteFile.delete();
    }

    /**
     * 推送当前分支提交至远程仓库
     */
    public static void push(String remoteName, String remoteBranchName) {
        // 检查远程目录是否存在
        String remotePath = getRemotePath(remoteName);
        if (remotePath == null || !(new File(remotePath)).exists()) {
            System.out.println("Remote directory not found.");
            return;
        }

        // 检查快进条件 (remoteHead 必须是 localHead 的祖先)
        File remoteDir = new File(remotePath);
        File remoteBranchFile = Utils.join(remoteDir, "refs", "heads", remoteBranchName);
        String localHeadId = getHeadCommitId();


        if (remoteBranchFile.exists()) {
            String remoteHeadId = Utils.readContentsAsString(remoteBranchFile);
            if (!isHistoryAncestor(localHeadId, remoteHeadId)) {
                System.out.println("Please pull down remote changes before pushing.");
                return;
            }
        }

        // 3. 将本地比远端多出的 commits 和 blobs 推送到远端
        copyCommitsAndBlobsToRemote(remoteDir, localHeadId);

        // 4. 更新远端的分支指针
        File parentDir = remoteBranchFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        Utils.writeContents(remoteBranchFile, localHeadId);
    }

    /**
     * 从远程仓库拉取分支
     */
    public static void fetch(String remoteName, String remoteBranchName) {
        // 1. 前置校验：校验远程目录和分支，并获取远端 HEAD commit id
        String remoteHeadId = checkAndGetRemoteHead(remoteName, remoteBranchName);
        if (remoteHeadId == null) {
            return;
        }

        // 2. 拷贝历史：把远端的所有 commits 和 blobs 同步到本地
        File remoteDir = new File(getRemotePath(remoteName));
        copyCommitsAndBlobsFromRemote(remoteDir, remoteHeadId);

        // 3. 更新本地跟踪分支指针（如 refs/heads/R1/master）
        updateTrackingBranch(remoteName, remoteBranchName, remoteHeadId);
    }


    /**
     * 拉取并合并远程分支
     */
    public static void pull(String remoteName, String remoteBranchName) {
        String remoteHeadId = checkAndGetRemoteHead(remoteName, remoteBranchName);
        if (remoteHeadId == null) {
            return;
        }

        fetch(remoteName, remoteBranchName);
        merge(remoteName + "/" + remoteBranchName);
    }

    // ==================== Helper Methods ====================

    /**
     * 获取当前 HEAD 所指向的分支名称（例如 "master"）
     */
    private static String getCurrentBranch() {
        return Utils.readContentsAsString(HEAD_FILE);
    }

    /**
     * 获取当前分支 HEAD 提交的 SHA-1 哈希值
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
     * 按标准格式打印单个 Commit 的信息
     */
    private static void printCommit(Commit commit, String commitId) {
        System.out.println("===");
        System.out.println("commit " + commitId);

        if (commit.getSecondParent() != null) {
            String firstParent = commit.getParent().substring(0, 7);
            String secondParent = commit.getSecondParent().substring(0, 7);
            System.out.println("Merge: " + firstParent + " " + secondParent);
        }

        SimpleDateFormat formatter = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z",
                Locale.ENGLISH
        );
        System.out.println("Date: " + formatter.format(commit.getTimestamp()));

        System.out.println(commit.getMessage());
        System.out.println();
    }

    /**
     * 解析 commitId，支持完整的 40 位 SHA-1 及前缀匹配短 ID。
     *
     * @param commitId 传入的 commit ID（完整或前缀）
     * @return 完整的 40 位 SHA-1 ID；若未找到匹配则返回 null
     */
    private static String resolveCommitId(String commitId) {
        if (commitId == null) {
            return null;
        }

        // 1. 如果 commitId 长度为 40 且 COMMITS_DIR 下存在该文件，直接返回 commitId
        if (commitId.length() == 40) {
            File commitFile = Utils.join(COMMITS_DIR, commitId);
            return commitFile.exists() ? commitId : null;
        }

        // 2. 如果是短 ID，遍历 commits 目录下所有文件名寻找前缀匹配
        List<String> commitIds = Utils.plainFilenamesIn(COMMITS_DIR);
        if (commitIds == null) {
            return null;
        }
        for (String id : commitIds) {
            if (id.startsWith(commitId)) {
                return id;
            }
        }
        return null;
    }

    /**
     * 检查工作区是否存在未追踪文件会被目标 Commit 覆盖或删除的情况。
     *
     * @param targetCommit 目标 commit 对象
     * @return 若存在未追踪文件冲突返回 true，否则返回 false
     */
    private static boolean checkUntrackedFiles(Commit targetCommit) {
        List<String> workingFiles = Utils.plainFilenamesIn(CWD);
        if (workingFiles == null) {
            return false;
        }
        Commit currentHead = getHeadCommit();
        Stage stage = Stage.load();

        for (String fileName : workingFiles) {
            boolean isTracked = currentHead.getTrackedFiles().containsKey(fileName);
            boolean isStaged = stage.getAddedFiles().containsKey(fileName);

            if (!isStaged && !isTracked) {
                if (targetCommit.getTrackedFiles().containsKey(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 寻找当前分支提交与目标分支提交的最近公共祖先（Split Point）。
     *
     * @param currentCommitId 当前分支 HEAD commit ID
     * @param targetCommitId  目标分支 HEAD commit ID
     * @return 最近公共祖先的 Commit ID
     */
    private static String findSplitPoint(String currentCommitId, String targetCommitId) {
        Set<String> currAncestors = new HashSet<>();
        Queue<String> currQueue = new ArrayDeque<>();

        currAncestors.add(currentCommitId);
        currQueue.add(currentCommitId);

        // 把当前分支的全部 commit 都装进一个 set 里
        while (!currQueue.isEmpty()) {
            String currId = currQueue.poll();
            Commit curr = Commit.fromFile(currId);

            if (curr.getParent() != null && !currAncestors.contains(curr.getParent())) {
                currQueue.add(curr.getParent());
                currAncestors.add(curr.getParent());
            }
            if (curr.getSecondParent() != null && !currAncestors.contains(curr.getSecondParent())) {
                currQueue.add(curr.getSecondParent());
                currAncestors.add(curr.getSecondParent());
            }
        }

        Set<String> visited = new HashSet<>();
        Queue<String> targetQueue = new ArrayDeque<>();

        targetQueue.add(targetCommitId);
        visited.add(targetCommitId);

        while (!targetQueue.isEmpty()) {
            String targetId = targetQueue.poll();

            if (currAncestors.contains(targetId)) {
                return targetId;
            }

            Commit target = Commit.fromFile(targetId);

            // visited 为了菱形结构导致的两个元素重复入队
            /*
                   A
                  / \
                 B   C
                  \ /
                   D  (target)
             */
            if (target.getParent() != null && !visited.contains(target.getParent())) {
                visited.add(target.getParent());
                targetQueue.add(target.getParent()); // <- 记得放进队列！
            }
            if (target.getSecondParent() != null && !visited.contains(target.getSecondParent())) {
                visited.add(target.getSecondParent());
                targetQueue.add(target.getSecondParent()); // <- 记得放进队列！
            }

        }
        return null;
    }

    /**
     * 获取指定远程仓库的目录路径（若不存在则返回 null）
     */
    private static String getRemotePath(String remoteName) {
        File remoteFile = Utils.join(REMOTES_DIR, remoteName);
        if (!remoteFile.exists()) {
            return null;
        }
        return Utils.readContentsAsString(remoteFile);
    }

    /**
     * 校验远程仓库及分支，并获取远端 HEAD 提交 ID。
     */
    private static String checkAndGetRemoteHead(String remoteName, String branchName) {
        String remotePath = getRemotePath(remoteName);
        if (remotePath == null || !(new File(remotePath)).exists()) {
            System.out.println("Remote directory not found.");
            return null;
        }
        File remoteBranchFile = Utils.join(new File(remotePath), "refs", "heads", branchName);
        if (!remoteBranchFile.exists()) {
            System.out.println("That remote does not have that branch.");
            return null;
        }
        return Utils.readContentsAsString(remoteBranchFile);
    }

    /**
     * 从远程仓库拉取指定提交及其历史上的 commits 和 blobs。
     */
    private static void copyCommitsAndBlobsFromRemote(File remoteDir, String startCommitId) {
        File remoteCommitsDir = Utils.join(remoteDir, "objects", "commits");
        File remoteBlobsDir = Utils.join(remoteDir, "objects", "blobs");

        Queue<String> queue = new ArrayDeque<>();
        queue.add(startCommitId);

        // 通过 BFS 遍历整个系统上的 commits 和 blobs 以确保 remote 和 local 同步
        while (!queue.isEmpty()) {
            String commitId = queue.poll();
            File localCommitFile = Utils.join(COMMITS_DIR, commitId);

            // 如果本地已经有这个 commit 了，说明该 commit 及其祖先都已经在本地了，跳过
            if (localCommitFile.exists()) {
                continue;
            }

            // 复制 commit 到本地
            File remoteCommitFile = Utils.join(remoteCommitsDir, commitId);
            Utils.writeContents(localCommitFile, Utils.readContents(remoteCommitFile));
            Commit commit = Utils.readObject(localCommitFile, Commit.class);

            // 复制 commit 里的所有 blob 到本地
            for (String blobId : commit.getTrackedFiles().values()) {
                File localBlob = Utils.join(BLOBS_DIR, blobId);
                File remoteBlob = Utils.join(remoteBlobsDir, blobId);
                if (!localBlob.exists() && remoteBlob.exists()) {
                    Utils.writeContents(localBlob, Utils.readContents(remoteBlob));
                }
            }

            // 把当前 commit 的父节点加入队列
            if (commit.getParent() != null) {
                queue.add(commit.getParent());
            }
            if (commit.getSecondParent() != null) {
                queue.add(commit.getSecondParent());
            }
        }
    }

    /**
     * 更新或创建本地跟踪分支指针。
     */
    private static void updateTrackingBranch(String remoteName, String branchName,
                                             String commitId) {
        File branchFile = Utils.join(HEADS_DIR, remoteName, branchName);
        File parentDir = branchFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        Utils.writeContents(branchFile, commitId);
    }

    /**
     * 将本地从指定 commit 开始的历史 commits 和 blobs 拷贝到远程仓库。
     */
    private static void copyCommitsAndBlobsToRemote(File remoteDir, String startCommitId) {
        File remoteCommitsDir = Utils.join(remoteDir, "objects", "commits");
        File remoteBlobsDir = Utils.join(remoteDir, "objects", "blobs");

        Queue<String> queue = new ArrayDeque<>();
        queue.add(startCommitId);

        while (!queue.isEmpty()) {
            String commitId = queue.poll();
            File remoteCommitFile = Utils.join(remoteCommitsDir, commitId);

            if (remoteCommitFile.exists()) {
                continue;
            }

            // 同步 commit
            File localCommitFile = Utils.join(COMMITS_DIR, commitId);
            Utils.writeContents(remoteCommitFile, Utils.readContents(localCommitFile));
            Commit commit = Commit.fromFile(commitId);

            // 同步该 commit 下的所有 blob
            for (String blobId : commit.getTrackedFiles().values()) {
                File localBlob = Utils.join(BLOBS_DIR, blobId);
                File remoteBlob = Utils.join(remoteBlobsDir, blobId);

                if (!remoteBlob.exists() && localBlob.exists()) {
                    Utils.writeContents(remoteBlob, Utils.readContents(localBlob));
                }
            }

            // 把父提交加入队列继续同步
            if (commit.getParent() != null) {
                queue.add(commit.getParent());
            }
            if (commit.getSecondParent() != null) {
                queue.add(commit.getSecondParent());
            }
        }
    }

    /**
     * 检查 targetCommitId 是否是 startCommitId 的祖先（沿着 parent 链可达）。
     */
    private static boolean isHistoryAncestor(String startCommitId, String targetCommitId) {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(startCommitId);
        visited.add(startCommitId);

        while (!queue.isEmpty()) {
            String commitId = queue.poll();
            if (commitId.equals(targetCommitId)) {
                return true;
            }
            Commit commit = Commit.fromFile(commitId);
            if (commit.getParent() != null && !visited.contains(commit.getParent())) {
                visited.add(commit.getParent());
                queue.add(commit.getParent());
            }
            if (commit.getSecondParent() != null && !visited.contains(commit.getSecondParent())) {
                visited.add(commit.getSecondParent());
                queue.add(commit.getSecondParent());
            }
        }
        return false;
    }
}



// ==================== Temporal Testing ====================
/*
    public static void main(String[] args) {
        init();
        add("wug.txt");
        commit("added wug");
    }
 */
