package bisq.web.ui.admin;

import bisq.i18n.Res;
import bisq.user.identity.UserIdentity;
import bisq.web.base.BisqContext;
import bisq.web.base.MainLayout;
import bisq.web.bo.ProfileBean;
import bisq.web.util.Popup;
import bisq.web.util.UIUtils;
import bisq.web.util.Util;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import lombok.Getter;

import static bisq.web.ui.admin.UserProfileView.NAME;

@Route(value = NAME, layout = MainLayout.class)
@CssImport("./styles/" + NAME + ".css")
@CssImport("./styles/Bisq.css")
public class UserProfileView extends Div {

    public static final String NAME = "UserProfileView";//.class.getSimpleName();
    protected final TextField statementField;
    protected final TextField termsField;
    protected final Button deleteButton;
    protected final TextField nicknameField;
    @Getter
    protected UserProfilePresenter presenter;

    Binder<ProfileBean> binder = new Binder<>();

    protected final Select<UserIdentity> profileSelection;

    public UserProfileView() {
        presenter = new UserProfilePresenter();
        addClassName(NAME);
        UIUtils.create(new H1("User Profile"), this::add, "header");
        FormLayout formLayout = UIUtils.create(new FormLayout(), this::add, "formLayout");
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("50px", 1));
        Div selectBar = UIUtils.create(new Div(), formLayout::add, "selectBar");

        profileSelection = UIUtils.create(new Select<UserIdentity>(), selectBar::add, "profileSelection");
        profileSelection.setLabel(Res.get("settings.userProfile.select"));
        profileSelection.setItemLabelGenerator(UserIdentity::getNickName);
        profileSelection.addValueChangeListener(ev -> {
            if (ev.isFromClient()) {
                getPresenter().selectProfile(new ProfileBean().loadFromIdentity(ev.getValue()));
            }
        });

        Button createButton = UIUtils.create(new Button(Res.get("settings.userProfile.createNewProfile")), selectBar::add, "createButton outlined-button");
        createButton.addClickListener(ev -> presenter.createNewProfile());


        nicknameField = UIUtils.create(new TextField(Res.get("social.chatUser.nickName")), formLayout::add, "nicknameField");
        nicknameField.setPlaceholder(Res.get("addNickName.nickName.prompt"));
        nicknameField.setVisible(false);
        binder.forField(nicknameField) //
                .withValidator(val -> nicknameField.isVisible() || (val != null && !val.isBlank()), "required") //
                .bind(ProfileBean::getNickname, ProfileBean::setNickname);

        TextField botIDField = UIUtils.create(new TextField(Res.get("social.chatUser.nymId")), formLayout::add, "botIDField");
        binder.forField(botIDField).bindReadOnly(ProfileBean::getNym);

        TextField profileIDField = UIUtils.create(new TextField(Res.get("social.chatUser.profileId")), formLayout::add, "profileIDField");
        binder.forField(profileIDField).bindReadOnly(ProfileBean::getUserId);

        TextField profileAgeField = UIUtils.create(new TextField(Res.get("social.chatUser.profileAge")), formLayout::add, "profileAgeField");
        binder.forField(profileAgeField).bindReadOnly(ProfileBean::getProfileAge);

        TextField reputationScoreField = UIUtils.create(new TextField(Res.get("social.chatUser.reputationScore")), formLayout::add, "reputationScoreField");
        binder.forField(reputationScoreField).bindReadOnly(ProfileBean::getReputationScore);

        statementField = UIUtils.create(new TextField(Res.get("social.chatUser.statement")), formLayout::add, "statementField");
        binder.forField(statementField).bind(ProfileBean::getStatement, ProfileBean::setStatement);

        termsField = UIUtils.create(new TextField(Res.get("social.chatUser.terms")), formLayout::add, "termsField");
        binder.forField(termsField).bind(ProfileBean::getTerms, ProfileBean::setTerms);

        HorizontalLayout buttonBar = UIUtils.create(new HorizontalLayout(), formLayout::add, "buttonBar");


        Button saveButton = UIUtils.create(new Button(Res.get("save")), buttonBar::add, "saveButton");
        saveButton.addClickListener(ev -> save());

        deleteButton = UIUtils.create(new Button(Res.get("settings.userProfile.deleteProfile")), buttonBar::add, "deleteButton");
        deleteButton.addClickListener(ev -> delete());
        ListDataProvider<UserIdentity> profilesProvider = UIUtils.providerFrom(getPresenter().createUserIdentityProvider(), getPresenter().getProfileDetailsChangedEvent());
        profileSelection.setItems(profilesProvider);
        // hide delete button iff only one left.
        getPresenter().getUserIdentities().addChangedListener(BisqContext.get().runInUIThread(() -> deleteButton.setEnabled(getPresenter().userIdentities.size() > 1)));

        getPresenter().getProfileOb().addObserver(profile -> BisqContext.get().runInUIThread(() -> loadProfile(profile)).run());
        getPresenter().getProfileDetailsChangedEvent().addListener(ident -> BisqContext.get().runInUIThread(() -> loadUserIdentity(ident)).run());
        getPresenter().selectProfile(new ProfileBean().loadFromIdentity(
                BisqContext.get().getUserIdentityService().getSelectedUserIdentity().get()));
    }

    private void loadProfile(ProfileBean profile) {
        binder.readBean(profile);
        loadUserIdentity(Util.nullSafe(profile, ProfileBean::getUserIdentity));
    }

    private void loadUserIdentity(UserIdentity ident) {
        profileSelection.setValue(ident);
        nicknameField.setVisible(ident == null);
    }

    public void save() {
        ProfileBean profile = getPresenter().getProfileOb().get();
        if (binder.writeBeanIfValid(profile)) {
            getPresenter().saveProfile();
        }
    }

    private void delete() {
        new Popup().warning(Res.get("settings.userProfile.deleteProfile.warning"))
                .onAction(getPresenter()::deleteProfile)
                .actionText(Res.get("settings.userProfile.deleteProfile.warning.yes"))
                .cancelButton()
                .show();
    }
}