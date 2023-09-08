package bisq.web.util;

import bisq.common.observable.ObservableArray;
import bisq.common.observable.ObservableSet;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.event.EventListenerSupport;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class UIUtils {

    public static final String PLEASE_SELECT = "--Please Select--";
    static DateFormat df = DateFormat.getDateInstance();

    public static LocalDate toDate(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }

    public static Date fromDate(LocalDate ldate) {
        return Date.from(ldate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static <T extends HasStyle> T create(T comp, Consumer<T> c, String... classNames) {
        c.accept(comp);
        comp.addClassNames(classNames);
        return comp;
    }

    public static Icon createIconButton(VaadinIcon vaadinIcon, ComponentEventListener<ClickEvent<Icon>> listener) {
        Icon iconButton = new Icon(vaadinIcon);
        iconButton.getStyle().set("cursor", "pointer");
        iconButton.addClickListener(listener);
        return iconButton;
    }

    public static <T> T ex2null(Supplier<T> s) {
        try {
            return s.get();
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T ex2default(Supplier<T> s, T def) {
        try {
            return s.get();
        } catch (Exception e) {
            return def;
        }
    }

    public static <T> T lambda(T t, Consumer<T> c) {
        if (t != null) {
            c.accept(t);
        }
        return t;
    }

    public static String formatDate(Date date) {
        return df.format(date);
    }

    public static String defaultIfNull(Supplier<Object> s, String def) {
        try {
            return s.get().toString();
        } catch (NullPointerException e) {
            return def;
        }
    }

    public static <R, T extends Collection<R>> Stream<R> ex2EmptyStream(Supplier<T> streamSupplier) {
        try {
            return streamSupplier.get().stream();
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    public static <R, T extends Collection<R>> Stream<R> nullSafeStream(T streamable) {
        if (streamable == null) {
            return Stream.empty();
        } else {
            return streamable.stream();
        }
    }

    public static <T> Stream<T> nullSafeStream(T[] streamable) {
        if (streamable == null) {
            return Stream.empty();
        } else {
            return Arrays.stream(streamable);
        }
    }

    public static <T> Collection<T> nullSafeCollection(Collection<T> col) {
        if (col == null) {
            return Collections.EMPTY_LIST;
        } else {
            return col;
        }
    }

    /**
     * use formatString("Hello {1}, have a nice day.", nameOfPerson)
     *
     * @param template
     * @param args
     * @return
     */
    public static String formatString(String template, String... args) {
        // String[] array = (String[]) IntStream.range(1, args.length + 1).mapToObj(i -> "{" + i +
        // "}").collect(Collectors.toList()).toArray();
        if (template == null) {
            throw new NullPointerException();
        }
        String[] array = new String[ args.length ];
        for (int i = 0; i < args.length; ) {
            array[ i ] = "{" + ++i + "}";
        }
        return StringUtils.replaceEach(template, array, args);
    }

    public static String concatenate(Stream<String> sstream) {
        return sstream.sorted().collect(Collectors.joining(", "));
    }

    public static StreamResource createResourceFromURL(String name, String url) {
        return new StreamResource(name, () -> {
            try {
                return new BufferedInputStream(new URL(url).openStream());
            } catch (IOException e) {
                log.error("Error in createResourceFromURL: ", e);
            }
            return null;
        });
    }

    public static <T> ListDataProvider<T> providerFrom(Component comp, ObservableSet<T> observableSet, EventListenerSupport<Consumer<T>> detailListener) {
        ListDataProvider<T> provider = new ListDataProvider<>(observableSet);
        new AttachListener(comp, observableSet, provider::refreshAll);
        new AttachListener<T>(comp, detailListener, provider::refreshItem);
        return provider;
    }

    public static <T> ListDataProvider<T> providerFrom(Component comp, ObservableArray<T> observableArray) {
        ListDataProvider<T> provider = new ListDataProvider<>(observableArray);
        new AttachListener(comp, observableArray, provider::refreshAll);
        return provider;
    }

    public static <T> ListDataProvider<T> providerFrom(Component comp, ObservableSet<T> observableSet) {
        ListDataProvider<T> provider = new ListDataProvider<>(observableSet);
        new AttachListener(comp, observableSet, provider::refreshAll);
        return provider;
    }

    /**
     * Why doesnt Vaading write a class SerializableComparator, which is actually worth the name?
     */
    public static <T> SerializableComparator<T> toS(Comparator<T> c) {
        return (SerializableComparator<T>) c::compare;
    }

    public static <T> void sortByLabel(ComboBox<T> box) {
        box.getListDataView().setSortComparator(toS(Comparator.comparing(box.getItemLabelGenerator())));
    }

    public static <T, U extends Comparable<? super U>> SerializableComparator<T> comparing(Function<T, U> compFunction) {
        return toS(Comparator.comparing(compFunction));
    }
}

// install observer listener



