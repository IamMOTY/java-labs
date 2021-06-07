package info.kgeorgiy.ja.buduschev.stat;


import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


import static org.junit.Assert.*;

public class StTests {
    private static final Path ROOT = Path.of("info","kgeorgiy","ja","buduschev","stat");
    private static Map<String, Locale> locals;
    private static Map<String, Path> files;

    @BeforeClass
    public static void beforeClass() {
        locals = new LinkedHashMap<>();
        locals.put("en", Locale.forLanguageTag("en-US"));
        locals.put("ru", Locale.forLanguageTag("ru-RU"));

        files = new LinkedHashMap<>();
        files.put("en", ROOT.resolve("en.txt"));
        files.put("ru", ROOT.resolve("ru.txt"));
    }

    @Test
    public void english() {
        FullStatistic fullStatistic = getStatistics("en");
        if (fullStatistic == null) {
            fail();
            return;
        }
        assertEquals(22, fullStatistic.getSentences().getCount());
        assertEquals(387, fullStatistic.getWords().getCount());
        assertEquals(16, fullStatistic.getNums().getCount());
        assertEquals(2, fullStatistic.getCurrencies().getCount());
        assertEquals(0, fullStatistic.getDates().getCount());

    }

    @Test
    public void russian() {
        FullStatistic fullStatistic = getStatistics("ru");
        if (fullStatistic == null) {
            fail();
            return;
        }
        assertEquals(124, fullStatistic.getSentences().getCount());
        assertEquals(1481, fullStatistic.getWords().getCount());
        assertEquals(3, fullStatistic.getNums().getCount());
        assertEquals(0, fullStatistic.getCurrencies().getCount());
        assertEquals(0, fullStatistic.getDates().getCount());
    }

    private static FullStatistic getStatistics(String key) {
        FullStatistic statistics;
        try {
            statistics = new FullStatistic(Main.readAll(files.get(key)), locals.get(key));
            return statistics;
        } catch (IOException e) {
            fail();
        }
        return null;
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        Result result = junit.run(StTests.class);
        System.out.println(result.getFailureCount());
        System.exit(result.wasSuccessful() ? 1 : 0);
    }
}
