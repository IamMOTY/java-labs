package info.kgeorgiy.ja.buduschev.arrayset;

import java.util.*;

public class ReversibleArrayList<E> extends AbstractList<E> implements RandomAccess {
    private final ArrayList<E> array;
    private final boolean isReversed;

    public ReversibleArrayList(Collection<? extends E> array, boolean isReversed) {
        this.array = new ArrayList<>(array);
        this.isReversed = isReversed;
    }

    public ReversibleArrayList(Collection<? extends E> array) {
        this.array = new ArrayList<>(array);
        this.isReversed = false;
    }

    public ReversibleArrayList(ArrayList<E> array, boolean isReversed) {
        this.array = array;
        this.isReversed = isReversed;
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
