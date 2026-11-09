package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Andy Yang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateNumArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validateInitialized();
                validateNumArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateInitialized();
                validateNumArgs(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                validateInitialized();
                validateNumArgs(args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                validateInitialized();
                validateNumArgs(args, 1);
                Repository.log();
                break;
            case "global-log":
                validateInitialized();
                validateNumArgs(args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateInitialized();
                validateNumArgs(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                validateInitialized();
                validateNumArgs(args, 1);
                Repository.status();
                break;
            case "checkout":
                validateInitialized();
                if (args.length == 3 && args[1].equals("--")) {
                    // 形如: checkout -- [file name]
                    Repository.checkout(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    // 形如: checkout [commit id] -- [file name]
                    Repository.checkout(args[1], args[3]);
                } else if (args.length == 2) {
                    // 形如: checkout [branch name]
                    Repository.checkoutBranch(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "branch":
                validateInitialized();
                validateNumArgs(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                validateInitialized();
                validateNumArgs(args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                validateInitialized();
                validateNumArgs(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                validateInitialized();
                validateNumArgs(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    private static void validateInitialized() {
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static void validateNumArgs(String[] args, int i) {
        if (args.length != i) {
            System.out.println("Incorrect operands.");
            // 直接退出程序
            System.exit(0);
        }
    }
}
