package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Andy Yang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                validateNumArgs(args, 2);
                Repository.add(args[1]);
                break;
                // TODO: FILL THE REST IN
        }
    }

    private static void validateNumArgs(String[] args, int i) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            // 直接退出程序
            System.exit(0);
        }
    }
}
