package bisq.web.ui.admin;

import bisq.user.identity.UserIdentity;
import bisq.web.base.MainLayout;
import bisq.web.bo.ProfileBean;
import bisq.web.ui.easy.BisqEasyView;
import bisq.web.util.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Route(value = "intro", layout = MainLayout.class)
@RouteAlias("")
@CssImport("./styles/shared-styles.css")
@CssImport("./styles/BisqEasyView.css")
@Slf4j
public class IntroView extends VerticalLayout {
    protected Binder<ProfileBean> userBinder = new Binder<>();

    public IntroView() {
        UIUtils.create(new H1("Welcome to Bisq2"), this::add, "welcomemsg");
        Div startDiv = UIUtils.create(new Div(), this::add);
        Button startButton = UIUtils.create(new Button("Start"), startDiv::add, "startButton");
        startButton.addClickListener(event -> start());

        VerticalLayout profileContainer = UIUtils.create(new VerticalLayout(), this::add, "profileContainer");
        UIUtils.create(new H2("Create your profile"), profileContainer::add, "createProfile");
        TextField nicknameText = UIUtils.create(new TextField("Profile Nickname"), profileContainer::add, "enterNickname");
        userBinder.forField(nicknameText) //
                .asRequired()
                .withValidator(input -> input.length() > 5 && input.length() <= 32, "Please enter between 5 and 32 characters")
                .withValidator(new RegexpValidator("Please enter only Alphanumeric characters", "[A-Za-z0-9]*"))
                .bind(ProfileBean::getNickname, ProfileBean::setNickname);

        userBinder.setBean(new ProfileBean());
        Button finishButton = UIUtils.create(new Button("Finish"), profileContainer::add, "finishButton");
        finishButton.addClickListener(event -> finish());
        Optional<UserIdentity> userOpt = userBinder.getBean().selectedUser();
        startDiv.setVisible(userOpt.isPresent());
        profileContainer.setVisible(userOpt.isEmpty());

    }

    protected void finish() {
        if (userBinder.validate().isOk()) {
            userBinder.getBean().generateUser(); // give the future to  context until someone needs the user.
            UI.getCurrent().navigate(BisqEasyView.class);
        }
    }

    protected void start() {
        UI.getCurrent().navigate(BisqEasyView.class);
    }
}
