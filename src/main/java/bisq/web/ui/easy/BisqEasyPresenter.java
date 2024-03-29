package bisq.web.ui.easy;

import bisq.chat.*;
import bisq.chat.bisqeasy.offerbook.BisqEasyOfferbookChannel;
import bisq.chat.bisqeasy.offerbook.BisqEasyOfferbookChannelService;
import bisq.chat.bisqeasy.open_trades.BisqEasyOpenTradeChannel;
import bisq.chat.common.CommonPublicChatChannel;
import bisq.chat.priv.PrivateChatChannel;
import bisq.chat.two_party.TwoPartyPrivateChatChannel;
import bisq.common.observable.Pin;
import bisq.common.observable.collection.ObservableArray;
import bisq.common.observable.collection.ObservableSet;
import bisq.i18n.Res;
import bisq.settings.SettingsService;
import bisq.support.SupportService;
import bisq.user.identity.UserIdentity;
import bisq.user.profile.UserProfile;
import bisq.web.base.BisqContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Model-View-Presenter
 * Presenter has access to Services.
 * Presenter has no dependency to vaadin-components (but datastructures)
 * Presenter has view specify code and state.
 */
@Slf4j
public class BisqEasyPresenter {
    @Getter
    protected Optional<ChatChannel> selectedChannel;
    @Getter
    protected IBisqEasyView iBisqEasyView;
    protected Optional<Pin> selectedChannelPin = Optional.empty();
    @Getter
    protected ObservableArray<ChatMessage> chatMessages = new ObservableArray<ChatMessage>();
    @Getter
    @Setter
    protected ChatMessage replyMessage;
    protected ObservableArray<BisqEasyOfferbookChannel> visibleChannels;

    public BisqEasyPresenter(IBisqEasyView iBisqEasyView) {
        this.iBisqEasyView = iBisqEasyView;
        selectedChannel = Optional.ofNullable(
                BisqContext.get().getApplicationService().getChatService().getBisqEasyOfferbookChannelSelectionService().getSelectedChannel().get());
    }

    public ObservableArray<BisqEasyOfferbookChannel> getVisibleChannels() {
        if (visibleChannels == null) {
            visibleChannels = new ObservableArray<>();
            BisqEasyOfferbookChannelService channelService = BisqContext.get().getBisqEasyOfferbookChannelService();
            ObservableSet<String> visibleChannelIds = channelService.getVisibleChannelIds();
            visibleChannelIds.addObserver(() ->
                visibleChannels.setAll(channelService.getChannels().stream() //
                        .filter(channelService::isVisible)
                        .sorted(Comparator.comparing(BisqEasyOfferbookChannel::getDisplayString))
                        .collect(Collectors.toList()))
            );
        }
        return visibleChannels;
    }


    public void reportUser(ChatMessage chatMessage) {
        // ref bisq.desktop.primary.main.content.components.ChatMessagesListView.Controller.onReportUser
        // TODO reportUser
//        findAuthor(chatMessage).ifPresent(author ->
//                BisqContext.get().getChatService().reportUserProfile(author, ""));
    }

    public void ignoreUser(ChatMessage chatMessage) {
        findAuthor(chatMessage).ifPresent(
                BisqContext.get().getApplicationService().getUserService().getUserProfileService()::ignoreUserProfile);
    }

    public ObservableArray<PrivateChatChannel> privateTradeChannels() {
        return (ObservableArray) BisqContext.get().getPrivateChat2PService().getChannels();
    }

    public ObservableArray<BisqEasyOfferbookChannel> publicTradeChannels() {
        return BisqContext.get().getChatService().getBisqEasyOfferbookChannelService().getChannels();
    }

    public void openPrivateChat(ChatMessage message) {
        // ref bisq.desktop.primary.main.content.components.ChatMessagesComponent.Controller.createAndSelectPrivateChannel()
        findAuthor(message).ifPresent(author ->
                selectChannel(choosePrivateTradeChannel(author).orElse(null))
        );
    }

    private Optional<TwoPartyPrivateChatChannel> choosePrivateTradeChannel(UserProfile peersUserProfile) {
        UserIdentity myUserIdentity = BisqContext.get().getUserIdentity();

        SupportService supportService = BisqContext.get().getApplicationService().getSupportService();
        Optional<UserProfile> mediatorOpt = supportService.getMediationService().selectMediator(myUserIdentity.getUserProfile().getId(), peersUserProfile.getId());
        return BisqContext.get().getChatService().createAndSelectTwoPartyPrivateChatChannel(ChatChannelDomain.BISQ_EASY_PRIVATE_CHAT, peersUserProfile);
    }

    public Optional<Validate.ValidationException> sendMessage(UserIdentity userIdentity, String text) {
        return Validate.thisCode(validate -> {
            validate.that(userIdentity != null, "UserIdentity not set");
            validate.that(text != null && !text.isBlank(), "no text to send.");
            selectedChannel.ifPresent(chatChannel -> {
                validate.that(text.length() <= ChatMessage.MAX_TEXT_LENGTH,
                        Res.get("validation.tooLong", ChatMessage.MAX_TEXT_LENGTH));
                SettingsService settingsService = BisqContext.get().getApplicationService().getSettingsService();
                ChatService chatService = BisqContext.get().getChatService();
                Optional<Citation> citation = Optional.ofNullable(replyMessage).flatMap(ChatMessage::getCitation);
                replyMessage = null;
                validate.that(!(citation.isPresent() && citation.get().getText().length() > Citation.MAX_TEXT_LENGTH),
                        Res.get("validation.tooLong", Citation.MAX_TEXT_LENGTH));

                if (chatChannel instanceof BisqEasyOfferbookChannel obChannel) {
                    if (settingsService.getOffersOnly().get()) {
                        settingsService.setOffersOnly(false);
                    }
                    chatService.getBisqEasyOfferbookChannelService().publishChatMessage(text, citation, obChannel, userIdentity);
                } else if (chatChannel instanceof BisqEasyOpenTradeChannel) {
                    chatService.getBisqEasyOpenTradeChannelService().sendTextMessage(text, citation, (BisqEasyOpenTradeChannel) chatChannel);
                } else if (chatChannel instanceof CommonPublicChatChannel cpChannel) {
                    chatService.getCommonPublicChatChannelServices().get(cpChannel.getChatChannelDomain()).publishChatMessage(text, citation, cpChannel, userIdentity);
                } else if (chatChannel instanceof TwoPartyPrivateChatChannel p2Channel) {
                    chatService.getTwoPartyPrivateChatChannelServices().get(p2Channel.getChatChannelDomain()).sendTextMessage(text, citation, p2Channel);
                }
            });
            iBisqEasyView.stateChanged();
        });
    }

    public void selectChannel(ChatChannel channel) {
        if (selectedChannel.equals(Optional.ofNullable(channel))) {
            return;
        }
        selectedChannel = Optional.ofNullable(channel);
        selectedChannelPin.ifPresent(Pin::unbind);

        selectedChannel.ifPresent(ch -> {
            BisqContext.get().getChatService().getChatChannelSelectionService(ch.getChatChannelDomain()).selectChannel(channel);
            selectedChannelPin = Optional.of(ch.getChatMessages().addObserver(() ->
                    chatMessages.setAll(ch.getChatMessages())
            ));
        });
        iBisqEasyView.stateChanged();
    }

    public Optional<UserProfile> findAuthor(ChatMessage chatMessage) {
        return BisqContext.get().getUserProfileService().findUserProfile(chatMessage.getAuthorUserProfileId());
    }

    public boolean isMyMessage(ChatMessage chatMessage) {
        return BisqContext.get().getUserIdentityService().isUserIdentityPresent(chatMessage.getAuthorUserProfileId());
    }

    public void hideSelectedChannel() {
        selectedChannel.ifPresent(channel -> {
            selectedChannelPin.ifPresent(Pin::unbind);

            BisqContext.get().findChatChannelService(channel).leaveChannel(channel);
                // persist is done by leaveChannel
            chatMessages.clear();
            selectedChannel = Optional.empty();
            iBisqEasyView.stateChanged();
        });
    }

    public void showChannel(ChatChannel channel) {
        if (channel instanceof BisqEasyOfferbookChannel obChannel) {
            BisqContext.get().getBisqEasyOfferbookChannelService().joinChannel(obChannel);
        }
    }

    public Optional<UserIdentity> myLastProfileInChannel() {
        return chatMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getDate).reversed())
                .map(ChatMessage::getAuthorUserProfileId)
                .map(BisqContext.get().getUserIdentityService()::findUserIdentity)
                .flatMap(Optional::stream)
                .findFirst();
    }

}
