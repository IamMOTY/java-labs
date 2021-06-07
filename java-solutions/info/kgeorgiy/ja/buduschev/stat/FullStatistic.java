package info.kgeorgiy.ja.buduschev.stat;

import java.text.*;
import java.util.*;
import java.util.function.Consumer;

public class FullStatistic {
    private final String text;
    private final Locale locale;
    private final LexicalStatistics words;
    private final LexicalStatistics sentences;
    private final DateStatistics dates;
    private final NumStatistics nums;
    private final NumStatistics currencies;
    private final Collator collator;


    public Locale getLocale() {
        return locale;
    }

    public LexicalStatistics getWords() {
        return words;
    }

    public LexicalStatistics getSentences() {
        return sentences;
    }

    public DateStatistics getDates() {
        return dates;
    }

    public NumStatistics getNums() {
        return nums;
    }

    public NumStatistics getCurrencies() {
        return currencies;
    }


    public FullStatistic(String text, Locale locale) {
        this.text = text;
        this.locale = locale;
        this.collator = Collator.getInstance(locale);
        collator.setStrength(Collator.PRIMARY);
        words = getLexicalStats(BreakIterator.getWordInstance(locale));
        sentences = getLexicalStats(BreakIterator.getSentenceInstance(locale));
        dates = getStatsDate();
        nums = new NumStatistics();
        currencies = new NumStatistics();
        fillNumsStat(nums, currencies);
    }

    private void fillNumsStat(final NumStatistics nums, final NumStatistics currencies) {
        BreakIterator boundary = BreakIterator.getWordInstance(locale);
        NumberFormat numFormat = NumberFormat.getNumberInstance(locale);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        boundary.setText(text);
        iterateBy(boundary, word -> {
            Number number;
            try {
                number = currencyFormat.parse(word);
                currencies.add(number);
            } catch (ParseException e) {
            }
            try {
                number = numFormat.parse(word);
                nums.add(number);
            } catch (ParseException ignored) {
            }
        }, false);
    }

    private DateStatistics getStatsDate() {
        BreakIterator it = BreakIterator.getWordInstance(locale);
        DateStatistics dateStats = new DateStatistics();
        it.setText(text);
        Set<DateFormat> formats = new LinkedHashSet<>(List.of(DateFormat.getDateInstance(DateFormat.SHORT, locale),
                DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
                DateFormat.getDateInstance(DateFormat.LONG, locale),
                DateFormat.getDateInstance(DateFormat.FULL, locale)));

        iterateBy(it, word -> {
            Date date;
            for (DateFormat format : formats) {
                try {
                    date = format.parse(word);
                    dateStats.add(word, date);
                    return;
                } catch (ParseException ignored) {
                }
            }
        }, false);
        return dateStats;
    }

    private LexicalStatistics getLexicalStats(BreakIterator boundary) {
        LexicalStatistics words = new LexicalStatistics(collator);
        boundary.setText(text);
        iterateBy(boundary, words::add, true);
        return words;
    }

    private void iterateBy(final BreakIterator it, final Consumer<? super String> consumer, final boolean checkLetters) {
        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            String term = text.substring(start, end);
            boolean flag = false;
            if (checkLetters) {
                for (int i = 0; !flag && i < term.length(); i++) {
                    flag = Character.isLetter(term.charAt(i));
                }
            } else {
                flag = true;
            }
            if (!term.isBlank() && flag) {
                consumer.accept(text.substring(start, end));
            }
        }
    }
}
