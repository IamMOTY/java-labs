package info.kgeorgiy.ja.buduschev.stat;

import java.text.Collator;
import java.util.Map;
import java.util.TreeMap;

public class LexicalStatistics extends AbstractStatistics<String> {
    private final Collator collator;
    private double sumItems;
    private final Map<String, Integer> itemsToCount;
    private String maxByLength = "";
    private String minByLength = "";

    public LexicalStatistics(final Collator collator) {
        super();
        maxByValue = "";
        minByValue = "";
        this.collator = collator;
        this.sumItems = 0;
        this.itemsToCount = new TreeMap<>(collator);
    }

    public String getMaxByLength() {
        return maxByLength;
    }

    public String getMinByLength() {
        return minByLength;
    }

    @Override
    public void add(String item) {
        sumItems += item.length();
        count++;
        itemsToCount.computeIfPresent(item, (k, v) -> v + 1);
        itemsToCount.putIfAbsent(item, 1);
        minByValue = minByValue.isEmpty() || collator.compare(minByValue, item) > 0 ? item : minByValue;
        maxByValue = collator.compare(maxByValue, item) < 0 ? item : maxByValue;
        maxByLength = maxByLength.length() < item.length() ? item : maxByLength;
        minByLength = minByLength.isEmpty() || minByLength.length() > item.length() ? item : minByLength;
    }

    @Override
    public int getUniqueCount() {
        return itemsToCount.size();
    }

    public Double getAvgValue() {
        return sumItems / count;
    }
}
