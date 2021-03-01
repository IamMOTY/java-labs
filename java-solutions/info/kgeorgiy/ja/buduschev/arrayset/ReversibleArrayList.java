package info.kgeorgiy.ja.buduschev.arrayset;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.RandomAccess;

public class ReversibleArrayList<E> extends AbstractList<E> implements RandomAccess {
    private final ArrayList<E> array;
    private final boolean isReversed;

    public ReversibleArrayList(Collection<E> array, boolean isReversed) {
        this.array = new ArrayList<>(array);
        this.isReversed = isReversed;
    }

    public ReversibleArrayList(Collection<E> array) {
        this.array = new ArrayList<>(array);
        this.isReversed = false;
    }

    public ReversibleArrayList() {
        this(new ArrayList<>());
    }

    private int index(int i) {
        return (isReversed) ? array.size() - i - 1 : i;
    }

    @Override
    public E get(int i) {
        return array.get(index(i));
    }

    public ReversibleArrayList<E> reversed() {
        return new ReversibleArrayList<>(array, !isReversed);
    }

    @Override
    public int size() {
        return array.size();
    }
}
