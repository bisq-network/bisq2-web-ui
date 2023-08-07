package bisq.web.util;

import bisq.common.observable.ObservableSet;
import bisq.common.observable.Pin;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ObservableSetDelegate<T> extends ObservableSet<T> {
    public ObservableSetDelegate<T> delegate(ObservableSet<T> d) {
        delegate = d;
        return this;
    }

    protected List<Runnable> listenerList;

    @Override
    public <L> Pin addChangedListener(Runnable handler) {
        if (listenerList == null) {
            listenerList = new CopyOnWriteArrayList<>();
        }
        listenerList.add(handler);
        Pin pin = delegate.addChangedListener(handler);
        return () -> {
            pin.unbind();
            listenerList.remove(handler);
        };
    }

    public void fire() {
        listenerList.forEach(Runnable::run);
    }

    @Override
    public <L> Pin addObservableListMapper(Collection<L> collection, Function<T, L> mapFunction, Consumer<Runnable> executor) {
        return delegate.addObservableListMapper(collection, mapFunction, executor);
    }

    @Override
    public boolean add(T element) {
        return delegate.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends T> values) {
        return delegate.addAll(values);
    }

    @Override
    public boolean remove(Object element) {
        return delegate.remove(element);
    }

    @Override
    public boolean removeAll(Collection<?> values) {
        return delegate.removeAll(values);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return delegate.removeIf(filter);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        delegate.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public <T1> T1[] toArray(IntFunction<T1[]> generator) {
        return delegate.toArray(generator);
    }

    @Override
    public Stream<T> stream() {
        return delegate.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return delegate.parallelStream();
    }

    public static <E> Set<E> of() {
        return Set.of();
    }

    public static <E> Set<E> of(E e1) {
        return Set.of(e1);
    }

    public static <E> Set<E> of(E e1, E e2) {
        return Set.of(e1, e2);
    }

    public static <E> Set<E> of(E e1, E e2, E e3) {
        return Set.of(e1, e2, e3);
    }

    public static <E> Set<E> of(E e1, E e2, E e3, E e4) {
        return Set.of(e1, e2, e3, e4);
    }

    public static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5) {
        return Set.of(e1, e2, e3, e4, e5);
    }

    public static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6) {
        return Set.of(e1, e2, e3, e4, e5, e6);
    }

    public static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
        return Set.of(e1, e2, e3, e4, e5, e6, e7);
    }

    public static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
        return Set.of(e1, e2, e3, e4, e5, e6, e7, e8);
    }

    public static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
        return Set.of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
    }

    public static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
        return Set.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
    }

    @SafeVarargs
    public static <E> Set<E> of(E... elements) {
        return Set.of(elements);
    }

    public static <E> Set<E> copyOf(Collection<? extends E> coll) {
        return Set.copyOf(coll);
    }

    protected ObservableSet<T> delegate;

}
