import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BlackListMatcherTest {
    private BlackListMatcher matcher = new BlackListMatcher();
    private List<String> expected = new ArrayList<>(Arrays.asList("Osama Bin Laden"));
    private List<String> variations = new ArrayList<>(Arrays.asList(
            "Osama Bin Laden",
            "Osama Laden",
            "Bin Laden, Osama",
            "Laden Osama Bin",
            "to the osama bin laden",
            "osama and bin laden",
            "Dr Osama Bin Laden"
    ));
    private List<String> partial = new ArrayList<>(Arrays.asList(
            "Osa Bin Laden",
            "Osam Lade",
            "B6n La9en, Osama",
            "dr ladenr osamar binr"
    ));
    private List<String> mustFail = new ArrayList<>(Arrays.asList(
            "Osama Jackson",
            "Elvis Laden"
    ));
    private List<String> expectedInDiffLang = new ArrayList<>(Arrays.asList("debarquer"));

    @Test
    public void TestFullWords() {
        variations.forEach(i -> assertEquals(expected,
                matcher.searchFromBlackList(i, "blacklist.txt", "noise.txt")));
    }

    @Test
    public void TestPartialMatch() {
        partial.forEach(i -> assertEquals(expected,
                matcher.searchFromBlackList(i, "blacklist.txt", "noise.txt")));
    }

    @Test
    public void TestFalsePositives() {
        mustFail.forEach(i -> assertNotEquals(expected,
                matcher.searchFromBlackList(i, "blacklist.txt", "noise.txt")));
    }

    @Test
    public void TestDiffLang() {
        assertEquals(expectedInDiffLang, matcher.searchFromBlackList("débárquér", "blacklist.txt", "noise.txt"));
    }
}