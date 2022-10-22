package bisq.web.ui.easy;

import bisq.chat.channel.Channel;
import bisq.chat.message.ChatMessage;
import bisq.chat.trade.priv.PrivateTradeChannel;
import bisq.chat.trade.pub.PublicTradeChannel;
import bisq.chat.trade.pub.PublicTradeChannelService;
import bisq.common.observable.ObservableArray;
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
    protected ListDataProvider<PrivateTradeChannel> privateTradeChannelProvider;


    public BisqEasyPresenter(IBisqEasyView iBisqEasyView) {
        this.iBisqEasyView = iBisqEasyView;
    }

    public ListDataProvider<ChatMessage> chatMessageProvider() {
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

    public ListDataProvider<PrivateTradeChannel> privateTradeChannelsProvider() {
//        privateTradeChannelProvider = observableSet2ListProvider(BisqContext.get().getChatService().getPrivateTradeChannelService().getChannels());
        privateTradeChannelProvider = observableSet2ListProvider(BisqContext.get().getChatService().getTradeChannelSelectionService().getPrivateTradeChannelService().getChannels());
        return privateTradeChannelProvider;
    }

    protected <T> ListDataProvider<T> observableSet2ListProvider(ObservableArray<T> observableSet) {
        ListDataProvider<T> provider = new ListDataProvider<>(observableSet);
        observableSet.addChangedListener(() -> iBisqEasyView.pushCallBack(provider::refreshAll));
        return provider;
    }

    public ListDataProvider<Channel<? extends ChatMessage>> activeChannelProvider() {
        PublicTradeChannelService publicTradeChannelService = BisqContext.get().getPublicTradeChannelService();
        activeChannelProvider = new ListDataProvider<Channel<? extends ChatMessage>>(
                publicTradeChannelService.getChannels().stream() //
                        .sorted(Comparator.comparing(PublicTradeChannel::getDisplayString)) //
                        .filter(publicTradeChannelService::isVisible) //
                        .collect(Collectors.toList()));
        return activeChannelProvider;
    }

    public void sendMessage(String text) {
        selectedChannel.ifPresent(channel -> {
            UserIdentity userIdentity = BisqContext.get().getUserIdentityService().getSelectedUserIdentity().get();
//            checkNotNull(userIdentity, "chatUserIdentity must not be null at onSendMessage");
//            Optional<Quotation> quotation = quotedMessageBlock.getQuotation();
            SettingsService settingsService = BisqContext.get().getApplicationService().getSettingsService();
            if (settingsService.getOffersOnly().get()) {
                settingsService.setOffersOnly(false);
                BisqContext.get().getApplicationService().getSettingsService().persist();
            }
            if (channel instanceof PublicTradeChannel) {
                BisqContext.get().getChatService().getPublicTradeChannelService().publishChatMessage(text, Optional.empty(), (PublicTradeChannel) channel, userIdentity);
            }
            if (channel instanceof PrivateTradeChannel) {
                BisqContext.get().getChatService().getPrivateTradeChannelService().sendPrivateChatMessage(text, Optional.empty(), (PrivateTradeChannel) channel);
            }
        });
        iBisqEasyView.stateChanged();
    }

    public void selectChannel(Channel channel) {
        if (selectedChannelPin != null) {
            selectedChannelPin.unbind();
        }
        selectedChannel = Optional.ofNullable(channel);
        if (selectedChannel.isPresent()) {
            showChannel(channel);
            selectedChannelPin = channel.getChatMessages().addChangedListener(iBisqEasyView.pushCallBack(() -> {
                chatMessageProvider.getItems().clear();
                chatMessageProvider.getItems().addAll(channel.getChatMessages());
                chatMessageProvider.refreshAll();
            }));
        }
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
            if (channel instanceof PublicTradeChannel) {
                BisqContext.get().getPublicTradeChannelService().hidePublicTradeChannel((PublicTradeChannel) channel);
                BisqContext.get().getPublicTradeChannelService().persist();
            }
            activeChannelProvider.getItems().remove(channel);
            activeChannelProvider.refreshAll();
            chatMessageProvider.getItems().clear();
            chatMessageProvider.refreshAll();
            selectedChannel = Optional.empty();
            iBisqEasyView.stateChanged();
        });
    }

    void showChannel(Channel ch) {
        if (ch instanceof PublicTradeChannel) {
            BisqContext.get().getChatService().getPublicTradeChannelService().showChannel((PublicTradeChannel) ch);
            BisqContext.get().getChatService().getPublicTradeChannelService().persist();
        }
    }
}
