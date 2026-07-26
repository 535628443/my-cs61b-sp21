package flik;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlikTest {

    @Test
    public void testFlik() {
        assertTrue(Flik.isSameNumber(127,127));
        assertTrue(Flik.isSameNumber(128,128));
        assertTrue(Flik.isSameNumber(500,500));
    }
}

