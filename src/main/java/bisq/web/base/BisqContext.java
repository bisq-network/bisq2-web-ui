package bisq.web.base;

import bisq.application.DefaultApplicationService;
import bisq.chat.ChatService;
import bisq.chat.trade.TradeChannelSelectionService;
import bisq.chat.trade.pub.PublicTradeChannelService;
import bisq.user.identity.UserIdentityService;
import bisq.user.profile.UserProfileService;
import lombok.Getter;

public class BisqContext {

    static BisqContext instance = new BisqContext();
    @Getter
    DefaultApplicationService applicationService;

    public BisqContext() {
    }

    public void startP2PNetwork(String... args) {
        applicationService = new DefaultApplicationService(args);
        applicationService.readAllPersisted().thenCompose(result -> applicationService.initialize()).join();
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

    public ChatService getChatService() {
        return getApplicationService().getChatService();
    }

    public TradeChannelSelectionService getTradeChannelSelectionService() {
        return getChatService().getTradeChannelSelectionService();
    }
}
