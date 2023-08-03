package bisq.web.ui.admin;

import bisq.common.observable.ObservableSet;
import bisq.user.identity.UserIdentity;
import bisq.web.base.BisqContext;
import bisq.web.bo.ProfileBean;
import bisq.web.util.Util;
import com.vaadin.flow.data.provider.ListDataProvider;
import lombok.Getter;

public class UserProfilePresenter {
    @Getter
    private ProfileBean selectedProfile;
    @Getter
    protected ListDataProvider<UserIdentity> profileProvider;
    @Getter
    protected ObservableSet<UserIdentity> userIdentities;

    public UserProfilePresenter() {
    }

    public ListDataProvider<UserIdentity> createUserIdentityProvider() {
        userIdentities = BisqContext.get().getUserIdentityService().getUserIdentities();
        return Util.observable2ListProvider(userIdentities);
    }

    public void selectProfile(ProfileBean profile) {
        selectedProfile = profile;
        BisqContext.get().getUserIdentityService().getSelectedUserIdentity().set(selectedProfile.getUserIdentity());
    }

    public void deleteProfile() {
        BisqContext.get().getUserIdentityService().deleteUserProfile(selectedProfile.getUserIdentity()).join();
        //must wait until really delete from backend to not show the deleted again.
        profileProvider.refreshAll();
    }

    public void saveProfile() {
        selectedProfile.save();
        profileProvider.refreshAll();
    }
}
