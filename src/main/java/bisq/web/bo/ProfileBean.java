package bisq.web.bo;

import bisq.security.DigestUtil;
import bisq.security.pow.ProofOfWork;
import bisq.security.pow.ProofOfWorkService;
import bisq.user.NymIdGenerator;
import bisq.user.identity.UserIdentity;
import bisq.web.base.BisqContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.security.KeyPair;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * this aids in creating a new user and profile
 */
@Accessors(chain = true)
public class ProfileBean {
    public static final String N_A = "N/A";
    @Getter
    protected String nym;
    protected CompletableFuture<ProofOfWork> proofOfWorkFuture;
    protected KeyPair keyPair;
    @Getter
    @Setter
    protected String nickname = ""; //nulls not allowed
    @Getter
    @Setter
    protected String terms = "";
    @Getter
    @Setter
    protected String statement = "";
    @Getter
    protected String userId;
    @Getter
    protected UserIdentity userIdentity;

    /**
     * this must be called before a User can be generated. It starts the ProoOfWork in the background already.
     *
     * @return
     */
    public ProfileBean prepareUserGeneration() {
        keyPair = BisqContext.get().getApplicationService().getKeyPairService().generateKeyPair();
        ProofOfWorkService proofOfWorkService = BisqContext.get().getApplicationService().getSecurityService().getProofOfWorkService();
        byte[] pubKeyHash = pubKeyHash(keyPair);
        proofOfWorkFuture = proofOfWorkService.mintNymProofOfWork(pubKeyHash);
        nym = NymIdGenerator.fromHash(pubKeyHash);
        return this;
    }

    protected byte[] pubKeyHash(KeyPair keyPair) {
        return DigestUtil.hash(keyPair.getPublic().getEncoded());
    }

    public CompletableFuture<UserIdentity> generateUser() {
        final CompletableFuture<UserIdentity> userFuture;
        synchronized (this) {
            userFuture = BisqContext.get().getUserIdentityService().createAndPublishNewUserProfile(
                            nickname, nym, keyPair, proofOfWorkFuture.join(), terms, statement) //
                    .thenApply(userIdent -> {
                        BisqContext.get().getUserIdentityService().persist().join();
                        return userIdent;
                    });
            keyPair = null; // make sure this is not reused!
            nym = null;
            proofOfWorkFuture = null;
        }
        BisqContext.get().setUserFuture(userFuture);
        return userFuture;
    }

    /**
     * get the selected user or if no user, init() is called, so you can later
     * (once you have asked the user which nickname he wants) call generateUser(...)
     *
     * @return Optional<UserIdentity>
     */
    public Optional<UserIdentity> selectedUser() {
        UserIdentity userIdentity = BisqContext.get().getUserIdentityService().getSelectedUserIdentity().get();
        if (userIdentity == null) {
            prepareUserGeneration();
            return Optional.empty();
        } else {
            return Optional.of(userIdentity);
        }
    }

    public ProfileBean loadFromIdentity(UserIdentity userIdentity) {
        this.userIdentity = userIdentity;
        nym = userIdentity.getNym();
        userId = userIdentity.getId();
        statement = userIdentity.getUserProfile().getStatement();

        terms = userIdentity.getUserProfile().getTerms();
        nickname = userIdentity.getNickName();

        return this;
    }

    public String getProfileAge() {
        if (userIdentity == null) {
            return N_A;
        }
        return BisqContext.get().getProfileAgeService().getProfileAge(userIdentity.getUserProfile()) //
                .map(String::valueOf) //
                .orElse(N_A);
    }

    public String getReputationScore() {
        if (userIdentity == null) {
            return N_A;
        }
        return String.valueOf( //
                BisqContext.get().getApplicationService().getUserService().getReputationService() //
                        .getReputationScore(userIdentity.getUserProfile()).getTotalScore());
    }

    public void save() {
        if (userIdentity == null) {
            // need to create the user.
            loadFromIdentity(generateUser().join());
        } else {
            BisqContext.get().getUserIdentityService().editUserProfile(userIdentity, terms, statement);
        }
    }


}
