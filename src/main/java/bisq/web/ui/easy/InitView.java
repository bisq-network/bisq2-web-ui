package bisq.web.ui.easy;

import bisq.web.base.BisqContext;
import bisq.web.base.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "init", layout = MainLayout.class)
@CssImport("./styles/shared-styles.css")
public class InitView extends VerticalLayout {

    public InitView() {
        Button rel = new Button("Reload P2P Network");
        add(rel);
        rel.addClickListener(ev -> reload());
    }

    private void reload() {
        BisqContext.get().startP2PNetwork("--appName=bisq2_seed1", "--data-dir=c:\\work\\bisq2\\web-ui\\ui");
    }
}
