package bisq.web.base;

import bisq.web.ui.admin.IntroView;
import bisq.web.util.Popup;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class ServiceListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(
                initEvent -> initEvent.getSession().setErrorHandler(this::error));
    }

    public void error(ErrorEvent errorEvent) {
        log.error("Unhandled Exception", errorEvent.getThrowable());
        if (UI.getCurrent() != null) {
            UI.getCurrent().access(() -> {
                String msg = String.format("Time %1$s Cause %2$s",
                        LocalDateTime.now(), errorEvent.getThrowable().getMessage());
                new Popup().error("Unhandled Exception")
                        .msgText(msg)
                        .onAction(this::recover)
                        .show();
            });
        }
    }

    public void recover() {
        UI.getCurrent().navigate(IntroView.class);
    }
}