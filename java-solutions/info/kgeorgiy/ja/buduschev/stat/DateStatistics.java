package info.kgeorgiy.ja.buduschev.stat;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class DateStatistics extends AbstractStatistics<Date> {
    private long sumItems;
    private final Map<String, Date> itemToValue;

    public DateStatistics() {
        super();
        this.sumItems = 0L;
        this.itemToValue = new LinkedHashMap<>();
    }

    @Override
    public int getUniqueCount() {
        return itemToValue.size();
    }

    public void add(final String item, final Date date) {
        sumItems += date.getTime();
        count++;
        itemToValue.putIfAbsent(item, date);
        maxByValue = maxByValue == null || maxByValue.getTime() < date.getTime() ? date : maxByValue;
        minByValue = minByValue == null || minByValue.getTime() > date.getTime() ? date : minByValue;
    }

    public Date getAvgValue() {
        return count == 0 ? null : Date.from(Instant.ofEpochMilli(sumItems / count));
    }
}
