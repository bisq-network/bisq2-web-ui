package bisq.web.ui.admin;

import bisq.common.observable.Observable;
import bisq.common.observable.ObservableSet;
import bisq.user.identity.UserIdentity;
import bisq.web.base.BisqContext;
import bisq.web.bo.ProfileBean;
import lombok.Getter;
import org.apache.commons.lang3.event.EventListenerSupport;

import java.util.function.Consumer;

public class UserProfilePresenter {
    @Getter
    protected Observable<ProfileBean> profileOb;
    @Getter
    protected ObservableSet<UserIdentity> userIdentities;
    @Getter
    protected EventListenerSupport<Consumer<UserIdentity>> profileDetailsChangedEvent;

    public UserProfilePresenter() {
        profileOb = new Observable<>();
        profileDetailsChangedEvent = EventListenerSupport.create((Class<Consumer<UserIdentity>>) (Class<?>) Consumer.class);
    }

    public ObservableSet<UserIdentity> createUserIdentityObserver() {
        userIdentities = BisqContext.get().getUserIdentityService().getUserIdentities();
        return userIdentities;
    }

    public void selectProfile(ProfileBean profile) {
        BisqContext.get().getUserIdentityService().getSelectedUserIdentity().set(profile.getUserIdentity());
        BisqContext.get().getUserIdentityService().persist();
        profileOb.set(profile);
    }

    public void deleteProfile() {
        // there must be 2 or more Ident left
        UserIdentity userIdentity = profileOb.get().getUserIdentity();
        if (userIdentities.contains(userIdentity)) {
            BisqContext.get().getUserIdentityService().deleteUserProfile(userIdentity).join();
            //must wait until really delete from backend to not show the deleted again.
        }
        userIdentity = getUserIdentities().iterator().next();
        selectProfile(new ProfileBean().loadFromIdentity(userIdentity));
    }

    public void saveProfile() {
        profileOb.get().save();
        profileDetailsChangedEvent.fire().accept(profileOb.get().getUserIdentity());
    }

    public void createNewProfile() {
        selectProfile(new ProfileBean().prepareUserGeneration());
    }
}
