package bisq.web.base;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Theme(themeClass = Lumo.class, variant = Lumo.DARK)
public class MainLayout extends VerticalLayout implements RouterLayout, AppShellConfigurator {

    public MainLayout() {
        this.setSpacing(false);
        this.setPadding(false);
        this.setMargin(false);
        this.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
        addClassName("MainLayout");
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.addClassName("topBar");
        add(topBar, createmenue());
    }

    public MenuBar createmenue() {
        MenuBar menu = new MenuBar();
        entry(menu, "Dashboard", TBD.class);
        SubMenu tradeApps = menu.addItem("Trade Apps").getSubMenu();
        entry(tradeApps, "Bisq Easy", TBD.class);
        entry(tradeApps, "Liquid Swaps", TBD.class);
        entry(tradeApps, "Bisq Multisig", TBD.class);


        return menu;
    }

    public void showRouterLayoutContent(HasElement content) {
        this.getElement().appendChild(new Element[]{content.getElement()});
    }


    public HasMenuItems entry(HasMenuItems bar, String name, Class<? extends Component> navTarget) {
        bar.addItem(name, e -> {
            UI.getCurrent().navigate(navTarget);
        });
        return bar;
    }

}
