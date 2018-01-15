package project;

import org.junit.Test;
import project.client.exceptions.WrongResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static project.Options.*;

public class OptionsTest {

    @Test
    public void makeMessageTest() {
        String expected = Options.JOIN + ";name";
        assertEquals(expected, makeMessage(Options.JOIN, "name"));
    }

    @Test
    public void makeMessageTest1() {
        String expected = Options.SENDSEQ + ";a;b;c;d;e";
        Stream s = Stream.of("a", "b", "c", "d", "e");
        assertEquals(expected, makeMessage(SENDSEQ, s));
    }

    @Test
    public void makeMessageTest2() {
        String expected = Options.SENDSEQ + ";a;b;c";
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        assertEquals(expected, makeMessage(Options.SENDSEQ, list));
    }

    @Test
    public void getOptionTest() {
        String expected = Options.JOIN;
        assertEquals(expected, getOption(makeMessage(Options.JOIN, "name")));
    }

    @Test
    public void getFirstArgTest() {
        String expected = "name";
        try {
            assertEquals(expected, getFirstArg(makeMessage(Options.JOIN, "name")));
        } catch (WrongResponseException e) {
            fail();
        }

        try {
            getFirstArg(makeMessage(Options.HOWMANY));
            fail();
        } catch (WrongResponseException e) {
            //test passed
        }
    }

    @Test
    public void getSecondArgTest() {
        String expected = "surname";
        try {
            assertEquals(expected, getSecondArg(makeMessage(Options.HOWMANY, "name", "surname")));
        } catch (WrongResponseException e) {
            fail();
        }

        try {
            getSecondArg(makeMessage(Options.JOIN, "name"));
            fail();
        } catch (WrongResponseException e) {
            //test passed
        }

    }

    @Test
    public void isOptionCorrectTest() {
        assertTrue(isOptionCorrect(Options.JOIN));
        assertFalse(isOptionCorrect("JOINING"));
    }

    @Test
    public void isArgCorrectTest() {
        assertTrue(isArgCorrect("simplename"));
        assertFalse(isArgCorrect("simple;name"));
    }

    @Test
    public void correctArgTest() {
        String expected = "simplename";
        assertEquals(expected, correctArg("simple;name"));
    }
}