package bisq.web.util;

import bisq.i18n.Res;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Popup extends Dialog {

    Button actionButton;

    public Popup() {
        super();
        addClassName("Popup");
        setCloseOnEsc(true);
        setDraggable(true);
        setCloseOnOutsideClick(false);
        setModal(true);
    }

    public Popup warning(String warnText) {
        addClassName("PopupWarning");
        setHeaderTitle(warnText);
        return this;
    }

    public Popup cancelButton() {
        Button cancel = new Button(Res.get("cancel"));
        cancel.addClassName("cancelButton");
        cancel.addClickListener(ev -> close());
        getFooter().add(cancel);
        return this;
    }

    public Popup addActionButton(Consumer<Button> buttonConfig) {
        buttonConfig.accept(getActionButton());
        return this;
    }

    public Button getActionButton() {
        if (actionButton == null) {
            actionButton = new Button();
            actionButton.addClassName("actionButton");
            getFooter().add(actionButton);
        }
        return actionButton;
    }

    public Popup actionText(String actionText) {
        getActionButton().setText(actionText);
        return this;
    }

    public Popup onAction(Runnable action) {
        getActionButton().addClickListener(ev -> {
            action.run();
            close();
        });
        return this;
    }

    public Popup show() {
        open();
        return this;
    }
}
