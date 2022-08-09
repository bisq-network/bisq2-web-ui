package bisq.web.base;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.function.Consumer;
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

    public static Dialog errorDialog(String msg) {
        return errorDialog("An Error occurred at " + Instant.now(), msg);
    }

    public static Dialog errorDialog(String title, String msg) {
        Button closeButton = new Button("Close");
        Dialog note = dialog(title, msg, closeButton);
        closeButton.addClickListener(ev -> note.close());
        return note;
    }

    public static Dialog dialog(String title, String msg, Button... buttons) {
        Dialog note = new Dialog();// msg, 0, Position.MIDDLE);
        note.setCloseOnEsc(true);
        note.setDraggable(true);
        note.setCloseOnOutsideClick(false);
        note.setModal(true);

        Div titleText = new Div();
        titleText.add(title);
        titleText.getStyle().set("color", "red");
        note.add(titleText);
        note.add(new Hr());
        note.add(new Text(msg));
        HorizontalLayout bbar = new HorizontalLayout(buttons);
        bbar.setJustifyContentMode(JustifyContentMode.END);
        bbar.setWidthFull();
        bbar.getStyle().set("margin-top", "20px");
        note.add(bbar);
        note.open();
        return note;

    }

    public static Dialog confirmDialog(String title, String msg, Runnable confirmAction) {
        Button closeButton = new Button("Cancel");
        Button ok = new Button("Ok");
        Dialog note = dialog(title, msg, ok, closeButton);
        closeButton.addClickListener(ev -> note.close());

        ok.addClickListener(ev -> {
            confirmAction.run();
            note.close();
        });
        return note;
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
}
