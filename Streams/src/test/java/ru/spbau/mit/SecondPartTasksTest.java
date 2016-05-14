package ru.spbau.mit;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.spbau.mit.SecondPartTasks.*;

public class SecondPartTasksTest {
    private static final String[] quoteFiles = {"./src/test/resources/quote1.txt", "./src/test/resources/quote2.txt"};
    private static final String[] quotesAns = {"IntelliJ IDEA editor is a powerful tool for", "The editor is tab-based.", "There is also"};

    private static final double EPS = 0.001;

    @Test
    public void testFindQuotes() {
        List<String> quotes = findQuotes(Arrays.asList(quoteFiles), " is ");
        assertEquals(3, quotes.size());
        quotes.sort(Comparator.<String>naturalOrder());
        for (int i = 0; i < quotes.size(); i++) {
            assertEquals(quotes.get(i), quotesAns[i]);
        }
    }

    @Test
    public void testPiDividedBy4() {
        assertEquals(Math.PI / 4.0, piDividedBy4(), EPS);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> testMap = new TreeMap<>();
        testMap.put("1", Arrays.asList("aaaa", "aaaa"));
        testMap.put("2", Arrays.asList("bbbbbb"));
        testMap.put("3", Arrays.asList("c", "c", "cc"));
        assertEquals("1", findPrinter(testMap));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> testMap1 = new TreeMap<>();
        Map<String, Integer> testMap2 = new TreeMap<>();
        testMap1.put("banana", 100);
        testMap1.put("beer", 1000);
        testMap2.put("banana", 100);
        testMap2.put("absinthe", 10);
        Map result = calculateGlobalOrder(Arrays.asList(testMap1, testMap2));
        assertEquals(result.get("banana"), 200);
        assertEquals(result.get("beer"), 1000);
        assertEquals(result.get("absinthe"), 10);
    }
}