package bisq.web.base;

import bisq.bisq_easy.BisqEasyService;
import bisq.chat.ChatChannel;
import bisq.chat.ChatChannelDomain;
import bisq.chat.ChatChannelService;
import bisq.chat.ChatService;
import bisq.chat.bisqeasy.offerbook.BisqEasyOfferbookChannelService;
import bisq.chat.two_party.TwoPartyPrivateChatChannelService;
import bisq.user.identity.UserIdentity;
import bisq.user.identity.UserIdentityService;
import bisq.user.profile.UserProfile;
import bisq.user.profile.UserProfileService;
import bisq.user.reputation.ProfileAgeService;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BisqContext {

    static BisqContext instance = new BisqContext();
    @Getter
    protected WebApplicationService applicationService;
    public BisqContext() {
    }

    public static void startP2PNetwork(String... args) {
        instance.applicationService = new WebApplicationService(args);
        instance.applicationService.readAllPersisted().thenCompose(result -> instance.applicationService.initialize()).join();
    }

    public static BisqContext get() {
        if (instance.getApplicationService() == null) {
            instance.startP2PNetwork();
        }
        return instance;
    }

    public UserIdentityService getUserIdentityService() {
        return BisqContext.get().getApplicationService().getUserService().getUserIdentityService();
    }

    public UserProfileService getUserProfileService() {
        return BisqContext.get().getApplicationService().getUserService().getUserProfileService();
    }

    public ChatService getChatService() {
        return getApplicationService().getChatService();
    }

    public void setUser(UserIdentity userIdentity) {
    }

    public UserIdentity getUserIdentity() {
        if (userFutureOpt.isPresent()) {
            userFutureOpt.get().join(); // wait for it to finish.
            userFutureOpt = Optional.empty();
        }
        UserIdentity userIdentity = getUserIdentityService().getSelectedUserIdentity();
        if (userIdentity == null)
            throw new NullPointerException("UserIdentity must not be null at this point.");
        return userIdentity;
    }

    protected Optional<CompletableFuture<UserIdentity>> userFutureOpt = Optional.empty();

    public void setUserFuture(CompletableFuture<UserIdentity> userFuture) {
        this.userFutureOpt = Optional.ofNullable(userFuture);
    }

    public ProfileAgeService getProfileAgeService() {
        return getApplicationService().getUserService().getReputationService().getProfileAgeService();
    }

    public BisqEasyService getBisqEasyService() {
        return getApplicationService().getBisqEasyService();
    }

    public BisqEasyOfferbookChannelService getBisqEasyOfferbookChannelService() {
        return getApplicationService().getChatService().getBisqEasyOfferbookChannelService();
    }

    public TwoPartyPrivateChatChannelService getPrivateChat2PService() {
        return getApplicationService().getChatService().getTwoPartyPrivateChatChannelServices().get(ChatChannelDomain.BISQ_EASY_PRIVATE_CHAT);
    }

    public BisqEasyOfferbookChannelService BisqEasyOfferbookChannelService() {
        return getApplicationService().getChatService().getBisqEasyOfferbookChannelService();
    }

    public ChatChannelService findChatChannelService(ChatChannel channel) {
        return getApplicationService().getChatService().findChatChannelService(channel).get();
    }

    public String findProfileName(String authorUserProfileId) {
        return getChatService().getUserProfileService().findUserProfile(authorUserProfileId)
                .map(UserProfile::getUserName)
                .orElse("");
    }
}
