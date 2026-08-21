package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import student.StudentArrayDeque;
import edu.princeton.cs.introcs.StdRandom;

public class TestArrayDequeEC {

    @Test
    public void randomizedTest() {
        StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads1 = new ArrayDequeSolution<>();
        String message = "";

        int N = 5000;
        for (int i=0; i < N; i++){
            int randVal = StdRandom.uniform(0,100);

            // 0: addFirst, 1: addLast, 2: removeFirst, 3: removeLast
            int op = StdRandom.uniform(0,4);
            if (op == 0) {
                message += "addFirst(" + randVal + ")\n";
                sad1.addFirst(randVal);
                ads1.addFirst(randVal);

            } else if (op == 1) {
                message += "addLast(" + randVal + ")\n";
                sad1.addFirst(randVal);
                ads1.addFirst(randVal);

            } else if (op == 2) {
                if (ads1.size() > 0 ) {
                    message += "removeFirst()\n";
                    Integer act = sad1.removeFirst();
                    Integer exp = ads1.removeFirst();
                    assertEquals(message, exp, act);
                }

            } else if (op == 3) {
                if (ads1.size() > 0 ) {
                    message += "removeLast()\n";
                    Integer act = sad1.removeLast();
                    Integer exp = ads1.removeLast();
                    assertEquals(message, exp, act);
                }
            }
        }

    }
}
