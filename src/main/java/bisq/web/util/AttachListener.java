package bisq.web.util;

import bisq.common.observable.Observable;
import bisq.common.observable.ObservableArray;
import bisq.common.observable.ObservableSet;
import bisq.common.observable.Pin;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.Command;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.event.EventListenerSupport;

import java.util.function.Consumer;

@Slf4j
public class AttachListener<T> {
    Pin pin;

    public AttachListener(Component bound2lifecycle, ObservableSet<T> observableSet, Command run) {
        Runnable changeListener = () -> bound2lifecycle.getUI().orElseThrow().access(run);
        if (bound2lifecycle.isAttached()) {
            pin = observableSet.addChangedListener(changeListener);
        }
        bound2lifecycle.addAttachListener(ev -> {
            if (pin != null) throw new RuntimeException();
            log.info("attaching " + changeListener);
            pin = observableSet.addChangedListener(changeListener);
        });
        bound2lifecycle.addDetachListener(ev -> {
            if (pin == null) throw new RuntimeException();
            log.info("detaching " + changeListener);
            pin.unbind();
            pin = null;
        });
    }

    public AttachListener(Component bound2lifecycle, ObservableArray<T> observablearray, Command run) {
        Runnable changeListener = () -> bound2lifecycle.getUI().orElseThrow().access(run);
        if (bound2lifecycle.isAttached()) {
            pin = observablearray.addChangedListener(changeListener);
        }
        bound2lifecycle.addAttachListener(ev -> {
            if (pin != null) throw new RuntimeException();
            log.info("attaching " + changeListener);
            pin = observablearray.addChangedListener(changeListener);
        });
        bound2lifecycle.addDetachListener(ev -> {
            if (pin == null) throw new RuntimeException();
            log.info("detaching " + changeListener);
            pin.unbind();
            pin = null;
        });
    }

    public AttachListener(Component bound2lifecycle, Observable<T> observable, Consumer<T> consumer) {
        Consumer<T> observer = t -> bound2lifecycle.getUI().orElseThrow().access(() -> consumer.accept(t));
        if (bound2lifecycle.isAttached()) {
            pin = observable.addObserver(observer);
        }
        bound2lifecycle.addAttachListener(ev -> pin = observable.addObserver(observer));
        bound2lifecycle.addDetachListener(ev -> pin.unbind());
    }

    public AttachListener(Component bound2lifecycle, EventListenerSupport<Consumer<T>> eventListener, Consumer<T> consumer) {
        Consumer<T> observer = t -> bound2lifecycle.getUI().orElseThrow().access(() -> consumer.accept(t));
        if (bound2lifecycle.isAttached()) {
            eventListener.addListener(observer);
        }
        bound2lifecycle.addAttachListener(ev -> eventListener.addListener(observer));
        bound2lifecycle.addDetachListener(ev -> eventListener.removeListener(observer));
    }
}
