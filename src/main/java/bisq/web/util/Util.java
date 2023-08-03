package bisq.web.util;

import bisq.common.observable.ObservableArray;
import bisq.common.observable.ObservableSet;
import bisq.web.base.BisqContext;
import com.vaadin.flow.data.provider.ListDataProvider;

public class Util {

    public static <T> ListDataProvider<T> observable2ListProvider(ObservableArray<T> observableSet) {
        ListDataProvider<T> provider = new ListDataProvider<>(observableSet);
        observableSet.addChangedListener(BisqContext.get().runInUIThread(provider::refreshAll));
        return provider;
    }

    public static <T> ListDataProvider<T> observable2ListProvider(ObservableSet<T> observableSet) {
        ListDataProvider<T> provider = new ListDataProvider<>(observableSet);
        observableSet.addChangedListener(BisqContext.get().runInUIThread(provider::refreshAll));
        return provider;
    }

}
