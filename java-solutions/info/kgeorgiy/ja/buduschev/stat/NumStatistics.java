package info.kgeorgiy.ja.buduschev.stat;

import java.util.LinkedHashMap;
import java.util.Map;

public class NumStatistics extends AbstractStatistics<Number> {
    private Double sumItems;
    private final Map<Double, Double> itemToValue;

    public NumStatistics() {
        super();
        this.sumItems = 0.0;
        this.itemToValue = new LinkedHashMap<>();
    }

    public void add(final Number number) {
        double value = number.doubleValue();
        sumItems += number.doubleValue();
        count++;
        itemToValue.putIfAbsent(value, value);
        maxByValue = maxByValue == null || maxByValue.longValue() < value ? number : maxByValue;
        minByValue = minByValue == null || minByValue.longValue() > value ? number : minByValue;
    }


    @Override
    public int getUniqueCount() {
        return itemToValue.size();
    }


    public Double getAvgValue() {
        return count != 0 ? sumItems / count : 0;
    }
}
