package bisq.web.ui.easy;

import bisq.chat.channel.Channel;
import bisq.chat.message.ChatMessage;
import bisq.chat.trade.pub.PublicTradeChannel;
import bisq.common.observable.Pin;
import bisq.settings.SettingsService;
import bisq.user.identity.UserIdentity;
import bisq.user.profile.UserProfile;
import bisq.web.base.BisqContext;
import com.vaadin.flow.data.provider.ListDataProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
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
    protected Optional<Channel<? extends ChatMessage>> selectedChannel = Optional.empty();
    @Getter
    protected IBisqEasyView iBisqEasyView;
    protected Pin selectedChannelPin;
    protected ListDataProvider<ChatMessage> chatMessageProvider;

    protected ListDataProvider<PublicTradeChannel> publicTradeChannelProvider;
    protected ListDataProvider<Channel<? extends ChatMessage>> activeChannelProvider;


    public BisqEasyPresenter(IBisqEasyView iBisqEasyView) {
        this.iBisqEasyView = iBisqEasyView;
    }

    public ListDataProvider<ChatMessage> loadChatMessageProvider() {
        chatMessageProvider = new ListDataProvider<>(new ArrayList<>());
        return chatMessageProvider;
    }


    public ListDataProvider<PublicTradeChannel> publicTradeChannelsProvider() {
        publicTradeChannelProvider = new ListDataProvider<PublicTradeChannel>(
                BisqContext.get().getPublicTradeChannelService().getChannels().stream() //
                        .sorted(Comparator.comparing(PublicTradeChannel::getDisplayString)) //
                        .collect(Collectors.toList()));
        return publicTradeChannelProvider;
    }

    public ListDataProvider<Channel<? extends ChatMessage>> activeChannelProvider() {
        activeChannelProvider = new ListDataProvider<Channel<? extends ChatMessage>>(
                BisqContext.get().getPublicTradeChannelService().getChannels().stream() //
                        .sorted(Comparator.comparing(PublicTradeChannel::getDisplayString)) //
                        .filter(BisqContext.get().getPublicTradeChannelService()::isVisible) //
                        .collect(Collectors.toList()));
        return activeChannelProvider;
    }

    public void sendMessage(String text) {
//        Channel<? extends ChatMessage> channel =  BisqContext.get().getTradeChannelSelectionService().getSelectedChannel().get(); // TODO what about multiple window surfing???
        selectedChannel.ifPresent(channel -> {
            UserIdentity userIdentity = BisqContext.get().getUserIdentityService().getSelectedUserProfile().get();
//            checkNotNull(userIdentity, "chatUserIdentity must not be null at onSendMessage");
//            Optional<Quotation> quotation = quotedMessageBlock.getQuotation();
            if (channel instanceof PublicTradeChannel) {
                SettingsService settingsService = BisqContext.get().getApplicationService().getSettingsService();
                if (settingsService.getOffersOnly().get()) {
                    settingsService.setOffersOnly(false);
                    // writing to persist??
                }
                BisqContext.get().getChatService().getPublicTradeChannelService().publishChatMessage(text, Optional.empty(), (PublicTradeChannel) channel, userIdentity);
            }
        });
        iBisqEasyView.stateChanged();
    }

    public void selectChannel(Channel channel) {
        if (selectedChannelPin != null) {
            selectedChannelPin.unbind();
        }
        selectedChannel = Optional.of(channel);
        showChannel(channel);
        selectedChannelPin = channel.getChatMessages().addChangedListener(iBisqEasyView.pushCallBack(() -> {
            chatMessageProvider.getItems().clear();
            chatMessageProvider.getItems().addAll(channel.getChatMessages());
            chatMessageProvider.refreshAll();
        }));

        iBisqEasyView.stateChanged();
    }

    public Optional<UserProfile> findAuthor(ChatMessage chatMessage) {
        return BisqContext.get().getUserProfileService().findUserProfile(chatMessage.getAuthorId());
    }

    public boolean isMyMessage(ChatMessage chatMessage) {
        return BisqContext.get().getUserIdentityService().isUserIdentityPresent(chatMessage.getAuthorId());
    }

    void hideSelectedChannel() {
        selectedChannel.ifPresent(channel -> {
            if (selectedChannelPin != null) {
                selectedChannelPin.unbind();
            }
            BisqContext.get().getPublicTradeChannelService().hidePublicTradeChannel((PublicTradeChannel) channel);//TODO check hierarchy
            BisqContext.get().getPublicTradeChannelService().persist();
            activeChannelProvider.getItems().remove(channel);
            activeChannelProvider.refreshAll();
            chatMessageProvider.getItems().clear();
            chatMessageProvider.refreshAll();
        });
    }

    void showChannel(Channel ch) {
        boolean alreadyInCollection = activeChannelProvider.getItems().add(ch);
        if (!alreadyInCollection) {
            activeChannelProvider.refreshAll();
            BisqContext.get().getChatService().getPublicTradeChannelService().showChannel((PublicTradeChannel) ch);
            //TODO check hierarchy (why do i need a classcast)?
            BisqContext.get().getChatService().getPublicTradeChannelService().persist();
        }
    }
}
