package info.kgeorgiy.ja.buduschev.stat;

import java.util.ListResourceBundle;

public class ResourceBundle_ru_RU extends ListResourceBundle {
    private final Object[][] CONTENTS = {
            {"analyzed_file", "Анализируемый файл"},
            {"summary", "Сводная статистика"},
            {"count", "Число"},
            {"unique", "различных"},
            {"statistic_of", "Статистика по"},
            {"min_by_val", "Минимальное"},
            {"max_by_val", "Максимальное"},
            {"min_by_len", "Минимальная длина"},
            {"max_by_len", "Максимальная длина"},
            {"avg", "Среднее"},
            {"mnv_word", "слово"},
            {"mnv_sentence", "предложение"},
            {"mnv_date", "дата"},
            {"mnv_num", "число"},
            {"mnv_currency", "сумма"},
            {"mxv_word", "слово"},
            {"mxv_sentence", "предложение"},
            {"mxv_date", "дата"},
            {"mxv_num", "число"},
            {"mxv_currency", "сумма"},
            {"mnl_word", "слова"},
            {"mxl_word", "слова"},
            {"mnl_sentence", "предложения"},
            {"mxl_sentence", "предложения"},
            {"avg_len", "Средняя длина"},
            {"avgl_sentence", "предложения"},
            {"avgl_word", "слова"},
            {"c_sentence", "предложений"},
            {"c_word", "слов"},
            {"c_num", "чисел"},
            {"c_date", "дат"},
            {"c_currency", "сумм"},
            {"s_sentence","предложениям"},
            {"s_word","словам"},
            {"s_num","числам"},
            {"s_date","датам"},
            {"s_currency","суммам"},
            {"avg_num", "чисел"},
            {"avg_date", "дат"},
            {"avg_currency", "сумм"},
            {"unrep", "не представленно"}
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
