package bisq.web.util;

import bisq.i18n.Res;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;

import java.util.function.Consumer;

/**
 * Popup for user Notification
 * user for warnings, errors, Infos
 */
public class Popup extends Dialog {

    protected Button cancelButton;
    Button actionButton;

    public Popup() {
        super();
        addClassName("Popup");
        setCloseOnEsc(true);
        setDraggable(true);
        setCloseOnOutsideClick(false);
        setModal(true);
    }

    public Popup information(String infoText) {
        setHeaderTitle(infoText);
        return this;
    }

    public Popup warning(String warnText) {
        addClassName("PopupWarning");
        setHeaderTitle(warnText);
        return this;
    }

    public Popup msgText(String msg) {
        this.add(UIUtils.lambda(new Div(), d -> d.setText(msg)));
        return this;
    }

    public Popup error(String errText) {
        addClassName("PopupError");
        setHeaderTitle(errText);
        return this;
    }

    public Popup cancelButton() {
        cancelButton = new Button(Res.get("cancel"));
        cancelButton.addClassName("cancelButton");
        cancelButton.addClickListener(ev -> close());
        getFooter().add(cancelButton);
        return this;
    }

    public Popup closeButtonText(String closeText) {
        cancelButton();
        cancelButton.setText(closeText);
        return this;
    }

    public Popup addActionButton(Consumer<Button> buttonConfig) {
        buttonConfig.accept(getActionButton());
        return this;
    }

    public Button getActionButton() {
        if (actionButton == null) {
            actionButton = new Button("Ok");
            actionButton.addClassName("actionButton");
            getFooter().add(actionButton);
        }
        return actionButton;
    }

    public Popup actionButtonText(String actionText) {
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

    public Popup dontShowAgainId(String dontShowAgainId) {
        //TODO implement dontShowAgainId
        return this;
    }
}
