package bisq.web.base;

import bisq.application.DefaultApplicationService;
import bisq.chat.ChatService;
import bisq.chat.trade.TradeChannelSelectionService;
import bisq.chat.trade.priv.PrivateTradeChannelService;
import bisq.chat.trade.pub.PublicTradeChannelService;
import bisq.user.identity.UserIdentity;
import bisq.user.identity.UserIdentityService;
import bisq.user.profile.UserProfileService;
import bisq.user.reputation.ProfileAgeService;
import com.vaadin.flow.component.UI;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BisqContext {

    static BisqContext instance = new BisqContext();
    @Getter
    protected DefaultApplicationService applicationService;
    public BisqContext() {
    }

    public static void startP2PNetwork(String... args) {
        instance.applicationService = new DefaultApplicationService(args);
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

    public PublicTradeChannelService getPublicTradeChannelService() {
        return getApplicationService().getChatService().getPublicTradeChannelService();
    }

    public PrivateTradeChannelService getPrivateTradeChannelService() {
        return getApplicationService().getChatService().getPrivateTradeChannelService();
    }

    public ChatService getChatService() {
        return getApplicationService().getChatService();
    }

    public TradeChannelSelectionService getTradeChannelSelectionService() {
        return getChatService().getTradeChannelSelectionService();
    }

    public void setUser(UserIdentity userIdentity) {
    }

    public UserIdentity getUserIdentity() {
        if (userFutureOpt.isPresent()) {
            userFutureOpt.get().join(); // wait for it to finish.
            userFutureOpt = Optional.empty();
        }
        UserIdentity userIdentity = getUserIdentityService().getSelectedUserIdentity().get();
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

    /**
     * Use this if you need to execute code from backend in the UI. Threads will most likely not be the same!
     *
     * @param r
     * @return
     */
    public Runnable runInUIThread(Runnable r) {
        final UI ui = UI.getCurrent();
        return () -> ui.access(() -> r.run());
    }


}
