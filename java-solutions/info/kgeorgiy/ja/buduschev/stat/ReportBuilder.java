package info.kgeorgiy.ja.buduschev.stat;

import java.nio.file.Path;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class ReportBuilder {
    private final FullStatistic fullStatistic;
    private final ResourceBundle bundle;
    private final Locale locale;
    private final Path file;
    private final NumberFormat numberFormat;
    private final DateFormat dateFormat;
    private final NumberFormat currencyFormat;
    private final Map<String, LexicalStatistics> lexicalMap;
    private final Map<String, NumStatistics> numMap;
    private final String report;


    public ReportBuilder(final FullStatistic fullStatistic, final Path file, final Locale locale) {
        this.fullStatistic = fullStatistic;
        this.file = file;
        this.locale = locale;
        numberFormat = NumberFormat.getNumberInstance(locale);
        dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        currencyFormat = NumberFormat.getCurrencyInstance(locale);
        bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.buduschev.stat.ResourceBundle", locale);
        lexicalMap = Map.of(
                "word", fullStatistic.getWords(),
                "sentence", fullStatistic.getSentences());
        numMap = Map.of(
                "num", fullStatistic.getNums(),
                "currency", fullStatistic.getCurrencies());
        report = String.join("",
                header(),
                summary(),
                lexical("sentence"),
                lexical("word"),
                numerical("num", numberFormat),
                numerical("currency", currencyFormat),
                date());
    }

    public String getReport() {
        return report;
    }

    private String header() {
        return format("%s \"%s\"%n", bundle.getString("analyzed_file"), file.toString());
    }

    private String summary() {
        return format(
                "%s%n" +
                        "\t%s %s: %s.%n" +
                        "\t%s %s: %s.%n" +
                        "\t%s %s: %s.%n" +
                        "\t%s %s: %s.%n" +
                        "\t%s %s: %s.%n",
                bundle.getString("summary"),
                bundle.getString("count"),
                bundle.getString("c_sentence"),
                numberFormat.format(fullStatistic.getSentences().getCount()),
                bundle.getString("count"),
                bundle.getString("c_word"),
                numberFormat.format(fullStatistic.getWords().getCount()),
                bundle.getString("count"),
                bundle.getString("c_num"),
                numberFormat.format(fullStatistic.getNums().getCount()),
                bundle.getString("count"),
                bundle.getString("c_currency"),
                numberFormat.format(fullStatistic.getCurrencies().getCount()),
                bundle.getString("count"),
                bundle.getString("c_date"),
                numberFormat.format(fullStatistic.getDates().getCount())
        );
    }

    private String lexical(final String key) {
        return format(
                "%s %s%n" +
                        "\t%s %s: %s (%s %s).%n" +
                        "\t%s %s: \"%s\".%n" +
                        "\t%s %s: \"%s\".%n" +
                        "\t%s %s: %s (\"%s\").%n" +
                        "\t%s %s: %s (\"%s\").%n" +
                        "\t%s %s: %s.%n",
                bundle.getString("statistic_of"),
                bundle.getString("s_" + key),
                bundle.getString("count"),
                bundle.getString("c_" + key),
                numberFormat.format(lexicalMap.get(key).getCount()),
                numberFormat.format(lexicalMap.get(key).getUniqueCount()),
                bundle.getString("unique"),
                bundle.getString("min_by_val"),
                bundle.getString("mnv_" + key),
                lexicalMap.get(key).getMinByValue(),
                bundle.getString("max_by_val"),
                bundle.getString("mxv_" + key),
                lexicalMap.get(key).getMaxByValue(),
                bundle.getString("min_by_len"),
                bundle.getString("mnl_" + key),
                numberFormat.format(lexicalMap.get(key).getMinByLength().length()),
                lexicalMap.get(key).getMinByLength(),
                bundle.getString("max_by_len"),
                bundle.getString("mxl_" + key),
                numberFormat.format(lexicalMap.get(key).getMaxByLength().length()),
                lexicalMap.get(key).getMaxByLength(),
                bundle.getString("avg_len"),
                bundle.getString("avgl_" + key),
                numberFormat.format(lexicalMap.get(key).getAvgValue())
        );
    }

    private String numerical(final String key, final NumberFormat formatter) {
        final NumStatistics nums = numMap.get(key);
        return format(
                "%s %s%n" +
                        "\t%s %s: %s (%s %s).%n" +
                        "\t%s %s: %s.%n" +
                        "\t%s %s: %s.%n" +
                        "\t%s %s: %s.%n",
                bundle.getString("statistic_of"),
                bundle.getString("s_" + key),
                bundle.getString("count"),
                bundle.getString("c_" + key),
                numberFormat.format(nums.getCount()),
                numberFormat.format(nums.getUniqueCount()),
                bundle.getString("unique"),
                bundle.getString("min_by_val"),
                bundle.getString("mnv_" + key),
                getValue(nums.getMinByValue(), formatter),
                bundle.getString("max_by_val"),
                bundle.getString("mxv_" + key),
                getValue(nums.getMaxByValue(), formatter),
                bundle.getString("avg"),
                bundle.getString("avg_" + key),
                getValue(nums.getAvgValue(), formatter)
        );
    }

    private String getValue(final Object obj, final Format formatter) {
        return obj == null ? bundle.getString("unrep") : formatter.format(obj);
    }

    private String date() {
        final DateStatistics dates = fullStatistic.getDates();
        return format(
                "%s %s%n" +
                        "\t%s %s: %s (%s %s).%n" +
                        "\t%s %s: %s.%n" +
                        "\t%s %s: %s.%n" +
                        "\t%s %s: %s.%n",
                bundle.getString("statistic_of"),
                bundle.getString("s_date"),
                bundle.getString("count"),
                bundle.getString("c_date"),
                numberFormat.format(dates.getCount()),
                numberFormat.format(dates.getUniqueCount()),
                bundle.getString("unique"),
                bundle.getString("min_by_val"),
                bundle.getString("mnv_date"),
                getValue(dates.getMinByValue(), dateFormat),
                bundle.getString("max_by_val"),
                bundle.getString("mxv_date"),
                getValue(dates.getMaxByValue(), dateFormat),
                bundle.getString("avg"),
                bundle.getString("avg_date"),
                getValue(dates.getAvgValue(), dateFormat)
        );
    }

    private String format(String template, Object... args) {
        return String.format(locale, template, args);
    }
}
