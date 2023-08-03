package bisq.web.ui.welcome;


import bisq.identity.IdentityService;
import bisq.security.DigestUtil;
import bisq.security.pow.ProofOfWork;
import bisq.security.pow.ProofOfWorkService;
import bisq.user.NymIdGenerator;
import bisq.user.identity.UserIdentity;
import bisq.web.base.BisqContext;
import bisq.web.bo.ProfileBean;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;
import java.util.Optional;

@Slf4j
public class TestCreateProfile {

    public static final String TAG = "Nickname2";

    /**
     * creating profile like in
     * bisq.desktop.primary.overlay.onboarding.profile.GenerateProfileController#onCreateUserProfile()
     */
//    @Test
//    public void createProfile() {
//        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
//        // GenerateNewProfileStep2Controller
//        IdentityService identityService = BisqContext.get().getApplicationService().getIdentityService();
//        Set<Identity> pooledIdentities = identityService.getPool();
//
//        Identity pooledIdentity = pooledIdentities.iterator().next(); // exception if no element or null
//        ProofOfWorkService proofOfWorkService = BisqContext.get().getApplicationService().getSecurityService().getProofOfWorkService();
//        ProofOfWork proofOfWork = proofOfWorkService.mintNymProofOfWork(pubKeyHash(pooledIdentity.getKeyPair())).join();
//        UserIdentity userProfile = BisqContext.get().getUserIdentityService().createAndPublishNewUserProfile(
//                pooledIdentity,
//                TAG,
//                proofOfWork,
//                "",
//                "");
//
//        boolean foundUser = BisqContext.get().getUserIdentityService().findUserIdentity(userProfile.getId()).isPresent();
//        log.info(userProfile.getUserName() + " is present " + foundUser);
//        org.junit.jupiter.api.Assertions.assertTrue(foundUser);
//    }
    @Test
    public void createProfile2() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        // GenerateNewProfileStep2Controller
        IdentityService identityService = BisqContext.get().getApplicationService().getIdentityService();
        KeyPair keyPair = BisqContext.get().getApplicationService().getKeyPairService().generateKeyPair();

        ProofOfWorkService proofOfWorkService = BisqContext.get().getApplicationService().getSecurityService().getProofOfWorkService();
        byte[] pubKeyHash = pubKeyHash(keyPair);
        ProofOfWork proofOfWork = proofOfWorkService.mintNymProofOfWork(pubKeyHash).join();
        UserIdentity userProfile = BisqContext.get().getUserIdentityService().createAndPublishNewUserProfile(
                TAG,
                NymIdGenerator.fromHash(pubKeyHash),
                keyPair,
                proofOfWork,
                "",
                "").join();

        boolean foundUser = BisqContext.get().getUserIdentityService().findUserIdentity(userProfile.getId()).isPresent();
        log.info(userProfile.getUserName() + " is present " + foundUser);
        org.junit.jupiter.api.Assertions.assertTrue(foundUser);
    }

    @Test
    public void userPresent() {
        log.info("" + BisqContext.get().getUserIdentityService().findUserIdentity(TAG).isPresent());
    }

    protected byte[] pubKeyHash(KeyPair keyPair) {
        return DigestUtil.hash(keyPair.getPublic().getEncoded());
    }

    @Test
    public void testSelecteduser() {
        System.setProperty("application.network.supportedTransportTypes.0", "CLEAR");
        System.setProperty("application.network.seedAddressByTransportType.clear.0", "127.0.0.1:8000");
        System.setProperty("application.network.seedAddressByTransportType.clear.1", "127.0.0.1:8001");
//        System.setProperty("","");
        BisqContext.startP2PNetwork("--appName=bisq2_test");
        ProfileBean generateUser = new ProfileBean();
        Optional<UserIdentity> userIdentityOptional = generateUser.selectedUser();
        UserIdentity userIdentity = userIdentityOptional.orElseGet(() ->
                generateUser.setNickname("nickname234").generateUser().join()
        );
        log.info("" + userIdentityOptional.isPresent());
        org.junit.jupiter.api.Assertions.assertTrue(userIdentity != null);

        log.info("username " + userIdentity.getUserName());
        Boolean sucstore = BisqContext.get().getUserIdentityService().persist().join();
        log.info("successful storing " + sucstore);
    }
}
