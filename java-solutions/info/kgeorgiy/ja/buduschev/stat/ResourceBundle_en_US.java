package info.kgeorgiy.ja.buduschev.stat;

import java.util.ListResourceBundle;

public class ResourceBundle_en_US extends ListResourceBundle {
    private final Object[][] CONTENTS = {
            {"analyzed_file", "Analyzed file"},
            {"summary", "Summary statistics"},
            {"count", "Count of"},
            {"unique", "uniques"},
            {"statistic_of", "Statistics of"},
            {"min_by_val", "Minimum by value of"},
            {"max_by_val", "Maximum by value of"},
            {"min_by_len", "Minimum length of"},
            {"max_by_len", "Maximum length of"},
            {"avg", "Average value of"},
            {"mnv_word", "word"},
            {"mnv_sentence", "sentence"},
            {"mnv_date", "date"},
            {"mnv_num", "number"},
            {"mnv_currency", "currency"},
            {"mxv_word", "word"},
            {"mxv_sentence", "sentence"},
            {"mxv_date", "date"},
            {"mxv_num", "number"},
            {"mxv_currency", "currency"},
            {"mnl_word", "word"},
            {"mxl_word", "word"},
            {"mnl_sentence", "sentence"},
            {"mxl_sentence", "sentence"},
            {"avg_len", "Average lenght of"},
            {"avg_num", "numbers"},
            {"avg_date", "dates"},
            {"avg_currency", "currencies"},
            {"avgl_sentence", "sentence"},
            {"avgl_word", "word"},
            {"c_sentence", "sentences"},
            {"c_word", "words"},
            {"c_num", "numbers"},
            {"c_date", "dates"},
            {"c_currency", "currencies"},
            {"s_sentence","sentence"},
            {"s_word","word"},
            {"s_num","number"},
            {"s_date","date"},
            {"s_currency","currency"},
            {"unrep", "unrepresented"}
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
