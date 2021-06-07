package info.kgeorgiy.ja.buduschev.stat;

public abstract class AbstractStatistics<T> implements Statistic<T> {
    protected int count;
    protected T maxByValue;
    protected T minByValue;

    public AbstractStatistics() {
        count = 0;
    }

    public void add(String item) {
        throw new UnsupportedOperationException();
    };

    public int getCount() {
        return count;
    }

    public abstract int getUniqueCount();

    public T getMinByValue() {
        return minByValue;
    }

    public T getMaxByValue() {
        return maxByValue;
    }

}
