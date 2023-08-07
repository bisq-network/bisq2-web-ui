package bisq.web.util;

import bisq.common.observable.ObservableArray;
import bisq.common.observable.ObservableSet;
import bisq.common.observable.Pin;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class ObservableList<T> extends CopyOnWriteArraySet<T> {
    protected transient List<Runnable> listeners;

    public ObservableList() {
    }

    public ObservableList(Collection<T> values) {
        addAll(values);
    }

    public ObservableList(ObservableSet<T> values) {
        addAll(values);
        values.addChangedListener(() -> {
            super.clear();
            super.addAll(values.stream().collect(Collectors.toList()));
            fire();
        });
    }

    public ObservableList(ObservableArray<T> values) {
        addAll(values);
        values.addChangedListener(() -> {
            super.clear();
            super.addAll(values.stream().collect(Collectors.toList()));
            fire();
        });
    }

    protected List<Runnable> getListeners() {
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
        }
        return listeners;
    }

    public Pin addListener(Runnable handler) {
        getListeners().add(handler);
        return () -> getListeners().remove(handler);
    }

    @Override
    public boolean add(T element) {
        boolean result = super.add(element);
        if (result) {
            fire();
        }
        return result;
    }

    public boolean addAll(Collection<? extends T> values) {
        boolean result = super.addAll(values);
        if (result) {
            fire();
        }
        return result;
    }

    @Override
    public boolean remove(Object element) {
        boolean result = super.remove(element);
        if (result) {
            fire();
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> values) {
        boolean result = super.removeAll(values);
        if (result) {
            fire();
        }
        return result;
    }

    @Override
    public void clear() {
        super.clear();
        fire();
    }

    public void fire() {
        getListeners().forEach(Runnable::run);

    }
}
