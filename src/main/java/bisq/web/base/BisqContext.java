package bisq.web.base;

import bisq.application.DefaultApplicationService;
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
            instance.startP2PNetwork("--appName=bisq2_web_ui", "--data-dir=c:\\work\\bisq2\\web-ui\\ui");
        }
        return instance;
    }

    public UserIdentityService getUserIdentityService() {
        return BisqContext.get().getApplicationService().getUserService().getUserIdentityService();
    }

    public UserProfileService getUserProfileService() {
        return BisqContext.get().getApplicationService().getUserService().getUserProfileService();
    }
}
