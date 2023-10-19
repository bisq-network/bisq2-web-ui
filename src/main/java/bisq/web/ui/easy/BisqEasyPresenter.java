package bisq.web.ui.easy;

import bisq.chat.channel.Channel;
import bisq.chat.message.ChatMessage;
import bisq.chat.message.Quotation;
import bisq.chat.trade.priv.PrivateTradeChannel;
import bisq.chat.trade.pub.PublicTradeChannel;
import bisq.common.observable.ObservableArray;
import bisq.common.observable.ObservableSet;
import bisq.common.observable.Pin;
import bisq.settings.SettingsService;
import bisq.support.SupportService;
import bisq.user.identity.UserIdentity;
import bisq.user.profile.UserProfile;
import bisq.web.base.BisqContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Objects;
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
    protected Optional<Channel> selectedChannel;
    @Getter
    protected IBisqEasyView iBisqEasyView;
    protected Pin selectedChannelPin;
    @Getter
    protected ObservableArray<ChatMessage> chatMessages = new ObservableArray<>();
    @Getter
    @Setter
    protected ChatMessage replyMessage;
    protected ObservableArray<PublicTradeChannel> visibleChannels;

    public ObservableArray<PublicTradeChannel> getVisibleChannels() {
        if (visibleChannels == null) {
            ObservableSet<String> obsVisibleChannels = BisqContext.get().getPublicTradeChannelService().getVisibleChannelNames();
            visibleChannels = new ObservableArray<>();
            obsVisibleChannels.addChangedListener(() -> {
                visibleChannels.clear();
                visibleChannels.addAll(BisqContext.get().getPublicTradeChannelService().getChannels().stream() //
                        .filter(BisqContext.get().getPublicTradeChannelService()::isVisible)
                        .sorted(Comparator.comparing(PublicTradeChannel::getDisplayString))
                        .collect(Collectors.toList()));
            });
        }
        return visibleChannels;
    }

    public BisqEasyPresenter(IBisqEasyView iBisqEasyView) {
        this.iBisqEasyView = iBisqEasyView;
        selectedChannel = Optional.ofNullable(BisqContext.get().getTradeChannelSelectionService().getSelectedChannel().get());
    }


    public void reportUser(ChatMessage chatMessage) {
        // ref bisq.desktop.primary.main.content.components.ChatMessagesListView.Controller.onReportUser
        findAuthor(chatMessage).ifPresent(author ->
                BisqContext.get().getChatService().reportUserProfile(author, ""));
    }

    public void ignoreUser(ChatMessage chatMessage) {
        findAuthor(chatMessage).ifPresent(
                BisqContext.get().getApplicationService().getUserService().getUserProfileService()::ignoreUserProfile);
    }

    public ObservableArray<PrivateTradeChannel> privateTradeChannels() {
        return BisqContext.get().getChatService().getTradeChannelSelectionService().getPrivateTradeChannelService().getChannels();
    }

    public ObservableArray<PublicTradeChannel> publicTradeChannels() {
        return BisqContext.get().getPublicTradeChannelService().getChannels();
    }

    public void openPrivateChat(ChatMessage message) {
        // ref bisq.desktop.primary.main.content.components.ChatMessagesComponent.Controller.createAndSelectPrivateChannel()
        findAuthor(message).ifPresent(author ->
                selectChannel(choosePrivateTradeChannel(author))
        );
    }

    private PrivateTradeChannel choosePrivateTradeChannel(UserProfile peersUserProfile) {
        UserIdentity myUserIdentity = BisqContext.get().getUserIdentity();

        SupportService supportService = BisqContext.get().getApplicationService().getSupportService();
        Optional<UserProfile> mediatorOpt = supportService.getMediationService().selectMediator(myUserIdentity.getUserProfile().getId(), peersUserProfile.getId());
        return BisqContext.get().getPrivateTradeChannelService().traderCreatesNewChannel(myUserIdentity, peersUserProfile, mediatorOpt);
    }

    public void sendMessage(UserIdentity userIdentity, String text) {
        Objects.requireNonNull(userIdentity, "UserIdentity not set");
        Objects.requireNonNull(text, "no text to send.");
        selectedChannel.ifPresent(channel -> {
            //            Optional<Quotation> quotation = quotedMessageBlock.getQuotation();
            SettingsService settingsService = BisqContext.get().getApplicationService().getSettingsService();
            // ref bisq.desktop.primary.main.content.components.QuotedMessageBlock.getQuotation
            Optional<Quotation> quotationOptional = Optional.ofNullable(replyMessage)//
                    .filter(msg -> StringUtils.isNotBlank(msg.getText())) //
                    .flatMap(this::findAuthor) //
                    .map(authorProfile -> new Quotation(authorProfile.getNym(), authorProfile.getNickName(), authorProfile.getPubKeyHash(), replyMessage.getText()));
            replyMessage = null;

            if (settingsService.getOffersOnly().get()) {
                settingsService.setOffersOnly(false);
                BisqContext.get().getApplicationService().getSettingsService().persist();
            }
            if (channel instanceof PublicTradeChannel) {
                BisqContext.get().getChatService().getPublicTradeChannelService().publishChatMessage(text, quotationOptional, (PublicTradeChannel) channel, userIdentity);
            }
            if (channel instanceof PrivateTradeChannel) {
                BisqContext.get().getChatService().getPrivateTradeChannelService().sendPrivateChatMessage(text, quotationOptional, (PrivateTradeChannel) channel);
            }
        });
        iBisqEasyView.stateChanged();
    }

    public void selectChannel(Channel channel) {
        if (selectedChannel.equals(Optional.ofNullable(channel))) {
            return;
        }
        selectedChannel = Optional.ofNullable(channel);
        BisqContext.get().getTradeChannelSelectionService().selectChannel(channel);
        if (selectedChannelPin != null) {
            selectedChannelPin.unbind();
        }

        if (selectedChannel.isPresent()) {
            selectedChannelPin = channel.getChatMessages().addChangedListener(() -> {
                chatMessages.clear();
                chatMessages.addAll(channel.getChatMessages());
            });
        }
        iBisqEasyView.stateChanged();
    }

    public Optional<UserProfile> findAuthor(ChatMessage chatMessage) {
        return BisqContext.get().getUserProfileService().findUserProfile(chatMessage.getAuthorId());
    }

    public boolean isMyMessage(ChatMessage chatMessage) {
        return BisqContext.get().getUserIdentityService().isUserIdentityPresent(chatMessage.getAuthorId());
    }

    public void hideSelectedChannel() {
        selectedChannel.ifPresent(channel -> {
            if (selectedChannelPin != null) {
                selectedChannelPin.unbind();
            }
            if (channel instanceof PublicTradeChannel) {
                BisqContext.get().getPublicTradeChannelService().hidePublicTradeChannel((PublicTradeChannel) channel);
                BisqContext.get().getPublicTradeChannelService().persist();
            }
            if (channel instanceof PrivateTradeChannel) {
                PrivateTradeChannel privateTradeChannel = (PrivateTradeChannel) channel;
                BisqContext.get().getPrivateTradeChannelService().leaveChannel(privateTradeChannel);
                // persist is done by leaveChannel
            }
            chatMessages.clear();
            selectedChannel = Optional.empty();
            iBisqEasyView.stateChanged();
        });
    }

    public void showChannel(Channel ch) {
        if (ch instanceof PublicTradeChannel) {
            BisqContext.get().getChatService().getPublicTradeChannelService().showChannel((PublicTradeChannel) ch);
        }
    }

    //bisq.desktop.primary.main.content.components.ChatMessagesComponent.Controller.getMyUserProfilesInChannel
//    public List<UserIdentity> getMyUserProfilesInChannel() {
//        return chatMessages.stream()
//                .sorted(Comparator.comparing(ChatMessage::getDate).reversed())
//                .map(ChatMessage::getAuthorId)
//                .map(BisqContext.get().getUserIdentityService()::findUserIdentity)
//                .flatMap(Optional::stream)
//                .distinct()
//                .collect(Collectors.toList());
//    }
    public Optional<UserIdentity> myLastProfileInChannel() {
        return chatMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getDate).reversed())
                .map(ChatMessage::getAuthorId)
                .map(BisqContext.get().getUserIdentityService()::findUserIdentity)
                .flatMap(Optional::stream)
                .findFirst();
    }

}
