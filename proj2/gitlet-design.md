# Gitlet Design Document

**Name**: Andy Yang

## Classes and Data Structures

### `Main`
Gitlet 的入口, 解析命令行参数, 检查操作数语法, 委托命令给 Repository

### `Repository`

#### Fields

* `public static final File CWD`
* `public static final File GITLET_DIR`
* `public static final File OBJECTS_DIR`
* `public static final File BLOBS_DIR`
* `public static final File COMMITS_DIR`
* `public static final File REFS_DIR`
* `public static final File HEADS_DIR`
* `public static final File HEAD_FILE`
* `public static final File STAGE_FILE`


### `Commit`

#### Fields

* `private String message`
* `private Date timestamp`
* `private String parent` : 指向上一个 commit hash 
* `private Map<String, String> trackedFiles` : 文件名 -> blob hash
* `private String secondParent` : 默认是 null, merge 后指向另一个分支的 commit hash


### `Stage`

#### Fields

* `private Map<String, String> addedFile`
* `private Set<String> removedFile`


## Algorithms

### `init`

1. 检查 `.gitlet` 是否已经存在
2. 创建目录结构
3. 创建 initial commit
4. 保存 initial commit 的 hash, 并存入 commits 目录
5. 创建 `master`, 指向 initial commit
6. 设置 `HEAD = master`
7. 创建空 Stage

### `add`

1. 从当前工作目录下读取文件
2. 对文件计算 blob hash
3. 和 HEAD Commit 中 trackedFiles hash 进行比对
4. 比较两个 blob hash
    - 相同：不加入 Stage
    - 不同：在 Stage 的 `addedFiles` 中记录 `文件名 → blob hash`
5. 如果对应 blob 已经存在，则直接复用；不存在则将文件内容存入 blobs 目录
6. 如果该文件之前存在于 `removedFiles` 中，将其移除
7. 保存 Stage

### `commit`

1. 读取 Stage，如果 Stage 为空则终止
2. 读取当前 HEAD Commit
3. 复制 HEAD Commit 的 `trackedFiles`
4. 用 `Stage.addedFiles` 添加或覆盖副本中的对应文件
5. 根据 `Stage.removedFiles` 删除副本中的对应文件
6. 使用更新后的 `trackedFiles` 创建新的 Commit
    - `parent` 指向原 HEAD Commit 的 hash
    - 保存 message 和 timestamp
7. 计算新 Commit 的 hash，并保存到 commits 目录
8. 将 HEAD 当前所在 branch 的指针更新为新 Commit 的 hash
9. 清空并保存 Stage

### `rm`

1. 读取当前 Stage 和 HEAD Commit
2. 判断文件是否：
    - 已经在 `Stage.addedFiles` 中
    - 或者被 HEAD Commit 的 `trackedFiles` 追踪
3. 如果两者都不是，则终止
4. 如果文件在 `addedFiles` 中，将其从 `addedFiles` 移除
5. 如果文件被 HEAD Commit 追踪：
    - 将文件名加入 `removedFiles`
    - 如果工作目录中存在该文件，则删除该文件
6. 保存 Stage

### `log`: 只能打印一条 branch 上的 commits 记录

1. 根据 `HEAD` 找到当前 branch
2. 根据当前 branch 找到 HEAD Commit 的 hash
3. 从 HEAD Commit 开始读取 Commit
4. 打印当前 Commit 的 hash、message 和 timestamp
5. 根据当前 Commit 的 `parent` hash 读取上一个 Commit
6. 重复上述过程，直到 `parent == null`

### `global-log`: 打印出所有 branch 上的 commits 记录

1. 获取 `COMMITS_DIR` 下所有 Commit 文件
2. 逐个读取并反序列化 Commit
3. 打印每个 Commit 的 hash、message 和 timestamp
4. 不要求按照 parent 顺序输出

### `status`: 比较 HEAD Commit, Stage 和 Working Directory

1. 读取 `HEAD`，得到当前 branch
2. 输出所有 branch，当前 branch 前加 *
3. 读取 Stage
4. 输出 `Stage.addedFiles` 中的所有文件名
5. 输出 `Stage.removedFiles` 中的所有文件名
6. 比较 Working Directory、HEAD Commit 和 Stage，找出：
    - 修改但未 staged 的文件
    - 被删除但未 staged removal 的文件
7. 找出 Working Directory 中的 untracked files

### `checkout`

   - `gitlet checkout -- [filename]`: 把 tracedFiles 的对应文件移动到当前 working dir
     1. 找到当前 HEAD Commit
     2. 在 HEAD Commit 的 `trackedFiles` 中查找指定文件
     3. 如果文件不存在于该 Commit 中，则终止
     4. 取得该文件对应的 blob hash
     5. 从 `BLOBS_DIR/<blob hash>` 读取文件内容
     6. 用该内容覆盖 Working Directory 中的同名文件

     <br>
     
   - `gitlet checkout <commit id> -- [filename]`: 指定对应版本的对应文件移动到 working dir
     1. 根据 commit id 找到对应 Commit
     2. 如果 Commit 不存在，则终止
     3. 在该 Commit 的 `trackedFiles` 中查找指定文件
     4. 如果文件不存在，则终止
     5. 找到对应 blob hash
     6. 从 blobs 目录读取文件内容
     7. 覆盖 Working Directory 中的同名文件

     <br>

   - `gitlet checkout [branch name]`
     1. 检查目标 branch 是否存在
     2. 如果目标 branch 就是当前 branch，则终止
     3. 读取目标 branch 指向的 Commit
     4. 检查是否存在会被覆盖的 untracked file
     5. 根据目标 Commit 的 `trackedFiles`：
        - 从 blobs 中读取对应文件内容
        - 写入 Working Directory
     6. 删除当前 Commit 正在追踪、但目标 Commit 不追踪的文件
     7. 清空 Stage
     8. 将 `HEAD` 更新为目标 branch

### `branch`

   - `gitlet branch [branch name]`: 创建一个新branch
     1. 检查目标 branch 是否已经存在，存在则终止
     2. 找到当前 HEAD Commit 的 hash
     3. 在 `HEADS_DIR` 中创建以 branch name 命名的文件
     4. 将当前 HEAD Commit 的 hash 写入该 branch 文件

   - `gitlet rm-branch [branch name]`
     1. 检查目标 branch 是否存在，不存在则终止
     2. 如果目标 branch 是当前 HEAD branch，则终止
     3. 删除 `HEADS_DIR/<branch name>`

### `reset`

   - `gitlet rest [commit id]`: 让 HEAD 移动到指定分支
     1. 根据 commit id 找到目标 Commit
     2. 如果 Commit 不存在，则终止
     3. 读取当前 HEAD Commit
     4. 检查是否存在会被目标 Commit 覆盖的 untracked file
     5. 根据目标 Commit 的 `trackedFiles`：
         - 从 blobs 中读取对应文件内容
         - 创建或覆盖 Working Directory 中的文件
     6. 删除当前 HEAD Commit 正在追踪、但目标 Commit 不追踪的文件
     7. 清空并保存 Stage
     8. 将 HEAD 当前所在 branch 的 pointer 更新为目标 Commit 的 hash

### `merge`

   - `gitlet merge [branch name]`
     1. 检查 Stage、branch 和自身 merge 等错误情况
     2. 读取 Current Commit 和 Given Commit
     3. 检查会被覆盖的 untracked files
     4. 找到 Current 和 Given 的 split point
     
             <br>
     
                - S = Split Point
                - C = Current Branch 当前分支
                - G = Given Branch 要合并进来的分支
    
                ```
                      对同一个文件比较 S / C / G
                                │
                                ▼
                             C == G ?
                            /       \
                          是         否
                          │          │
                     两边结果一样      │
                     什么都不做        ▼
                                  C == S ?
                                 /      \
                               是        否
                               │         │
                        Current 没改      ▼
                        采用 Given       G == S ?
                                        /      \
                                      是        否
                                      │         │
                               Given 没改       两边都改了
                               保留 Current     且结果不同
                                                 │
                                                 ▼
                                              CONFLICT
                ```
     5. 保存新 Commit
     6. 更新当前 branch pointer
     7. 清空 Stage


## Helper Method
   - `private static String getCurrentBranch()`
   - `private static String getHeadCommitId()`
   - `private static Commit getHeadCommit()`

### Commit helper
   - `private static Commit readCommit(String commitId)`
   - `public String save()`: 把 commit obj 存到磁盘并返回对应 hash
   - `private static String resolveCommitId(String prefix)`

### stage helper
   - `void stageForAddition(String fileName, String blobId)`
   - `void stageForRemoval(String fileName)`
   - `Map<String, String> getAddedFiles()`
   - `Set<String> getRemovedFiles()`
   - `void clear()`
   - `boolean isEmpty()`
   - `void save()`
   - `static Stage load()`

### checkout helper
   - `private static void checkoutFile(Commit commit, String fileName)`
   - `private static void checkoutCommit(Commit current, Commit target)`

### merge helper
   - `private static boolean hasUntrackedConflict(Commit target)`


## Persistence

```
CWD/
 └── .gitlet/
        ├── HEAD
        ├── staging
        │
        ├── refs/
        │     └── heads/
        │            ├── master
        │            └── dev
        │
        │
        └── objects/
                ├── commits/
                │       ├── <commit-sha1>
                │       └── ...
                │
                └── blobs/
                      ├── <blob-sha1>
                      └── ...
```
