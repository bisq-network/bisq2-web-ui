package bisq.web.ui.admin;

import bisq.i18n.Res;
import bisq.user.identity.UserIdentity;
import bisq.web.base.MainLayout;
import bisq.web.bo.ProfileBean;
import bisq.web.util.Popup;
import bisq.web.util.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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
                selectProfile(new ProfileBean().loadFromIdentity(ev.getValue()));
            }
        });

        Button createButton = UIUtils.create(new Button(Res.get("settings.userProfile.createNewProfile")), selectBar::add, "createButton outlined-button");
        createButton.addClickListener(ev -> createNewProfile());


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
        profileSelection.setItems(getPresenter().getProfileProvider());
        deleteButton.setEnabled(getPresenter().userIdentities.size() > 1);
    }

    public void save() {
        ProfileBean profile = getPresenter().getSelectedProfile();
        if (binder.writeBeanIfValid(profile)) {
            getPresenter().saveProfile();
        }
    }

    private void createNewProfile() {
        selectProfile(new ProfileBean().prepareUserGeneration());
        nicknameField.setVisible(true);
    }

    private void delete() {
        new Popup().warning(Res.get("settings.userProfile.deleteProfile.warning"))
                .onAction(() -> doDelete())
                .actionText(Res.get("settings.userProfile.deleteProfile.warning.yes"))
                .cancelButton()
                .show();

    }

    public void doDelete() {
        getPresenter().deleteProfile();
        UserIdentity identity = getPresenter().getUserIdentities().iterator().next();
        selectProfile(new ProfileBean().loadFromIdentity(identity));
        deleteButton.setEnabled(getPresenter().getUserIdentities().size() > 1);

    }

    protected void selectProfile(ProfileBean profile) {
        getPresenter().selectProfile(profile);
        binder.readBean(profile);
        profileSelection.setValue(profile.getUserIdentity());
        nicknameField.setVisible(false);
    }
}
