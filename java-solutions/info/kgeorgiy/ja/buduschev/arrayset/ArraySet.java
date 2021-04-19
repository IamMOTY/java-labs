package info.kgeorgiy.ja.buduschev.arrayset;


import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements java.util.NavigableSet<E> {
    private final ReversibleArrayList<E> array;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(new ReversibleArrayList<>(), null);
    }

    public ArraySet(Collection<? extends E> array, Comparator<? super E> comparator) {
        TreeSet<E> tree = new TreeSet<>(comparator);
        tree.addAll(array);
        this.array = new ReversibleArrayList<>(tree);
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends E> array) {
        this(array, null);
    }

    private ArraySet(ReversibleArrayList<E> array, Comparator<E> comparator) {
        this.array = array;
        this.comparator = comparator;
    }

    private ArraySet(Comparator<? super E> comparator) {
        this(new ReversibleArrayList<>(), comparator);
    }

    private int binarySearch(E e) {
        return Collections.binarySearch(array, e, comparator);
    }

    private int combSearch(E e, boolean inclusive, int firstShift, int secondShift) {
        final int position = binarySearch(e);
        if (position < 0) {
            return -position + firstShift;
        }
        if (inclusive) {
            return position;
        }
        return position + secondShift;
    }

    /**
     * @param e         key value for search
     * @param inclusive flag for inclusive the e value to search
     * @return position of element x, if inclusive is true x <= otherwise, x < e.
     * if element can't be founded, it returns array size.
     */
    private int getIndexOfUpper(E e, boolean inclusive) {
        return combSearch(e, inclusive, -1, 1);
    }

    /**
     * @param e         key value for search
     * @param inclusive flag for inclusive the e value to search
     * @return position of element x, if inclusive is true x <= e otherwise, x < e.
     * if element can't be founded, it returns -1.
     */
    private int getIndexOfLower(E e, boolean inclusive) {
        return combSearch(e, inclusive, -2, -1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        final int position = binarySearch((E) o);
        return position >= 0 && position < size();
    }

    @Override
    public E lower(E e) {
        final int position = getIndexOfLower(e, false);
        return (position < 0) ? null : get(position);
    }

    @Override
    public E floor(E e) {
        final int position = getIndexOfLower(e, true);
        return (position < 0) ? null : get(position);
    }

    @Override
    public E ceiling(E e) {
        final int position = getIndexOfUpper(e, true);
        return (position == size()) ? null : get(position);
    }

    @Override
    public E higher(E e) {
        final int position = getIndexOfUpper(e, false);
        return (position == array.size()) ? null : array.get(position);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(array).iterator();
    }

    @Override
    public ArraySet<E> descendingSet() {
        return new ArraySet<>(array.reversed(), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return this.descendingSet().iterator();
    }

    @SuppressWarnings("unchecked")
    private int compare(E var1, E var2) {
        if (comparator != null) {
            return comparator.compare(var1, var2);
        }
        return ((Comparable<E>) var1).compareTo(var2);
    }

    @Override
    public ArraySet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (contains(fromElement) && contains(toElement) && compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("FromKey > ToKey");
        }
        final int left = getIndexOfUpper(fromElement, fromInclusive);
        final int right = getIndexOfLower(toElement, toInclusive);
        if (left > right) {
            if (compare(fromElement, toElement) <= 0) {
                return new ArraySet<>(comparator);
            }
            throw new IllegalArgumentException("FromKey > ToKey");
        }
        return new ArraySet<>(array.subList(left, right + 1), comparator);
    }

    @Override
    public ArraySet<E> headSet(E toElement, boolean inclusive) {
        final int position = getIndexOfLower(toElement, inclusive);
        return (size() == 0 || position == -1) ? new ArraySet<>(comparator) : subSet(first(), true, toElement, inclusive);
    }

    @Override
    public ArraySet<E> tailSet(E fromElement, boolean inclusive) {
        final int position = getIndexOfUpper(fromElement, inclusive);
        return (size() == 0 || position == size()) ? new ArraySet<>(comparator) : subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E fromElement) {
        return headSet(fromElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E toElement) {
        return tailSet(toElement, true);
    }

    @Override
    public E first() {
        return get(0);
    }

    @Override
    public E last() {
        return get(size() - 1);
    }

    private E get(int i) {
        if (array.isEmpty()) {
            throw new NoSuchElementException();
        }
        return array.get(i);
    }

    @Override
    public int size() {
        return array.size();
    }
}