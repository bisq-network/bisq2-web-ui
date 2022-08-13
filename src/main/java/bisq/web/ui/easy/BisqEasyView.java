package bisq.web.ui.easy;

import bisq.application.DefaultApplicationService;
import bisq.chat.channel.Channel;
import bisq.chat.message.ChatMessage;
import bisq.chat.message.PublicChatMessage;
import bisq.chat.trade.pub.PublicTradeChannel;
import bisq.chat.trade.pub.PublicTradeChannelService;
import bisq.chat.trade.pub.PublicTradeChatMessage;
import bisq.common.observable.ObservableSet;
import bisq.common.observable.Pin;
import bisq.settings.SettingsService;
import bisq.user.identity.UserIdentity;
import bisq.user.profile.UserProfile;
import bisq.web.base.BisqContext;
import bisq.web.base.MainLayout;
import bisq.web.base.UIUtils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;


@Route(value = "easy", layout = MainLayout.class)
@RouteAlias("")
@CssImport("./styles/shared-styles.css")
@CssImport("./styles/BisqEasyView.css")
@Slf4j
public class BisqEasyView extends HorizontalLayout {

    public static final String CHANNEL_PARAM = "channel"; // URL parameter for choosing the channel

    protected final VerticalLayout chatColumn;
    protected final VerticalLayout channelColumn;
    protected final ComboBox<PublicTradeChannel> tradeChannelBox;
    protected final ListBox<PublicTradeChannel> listTradeChannels;
    protected final Label channelLabel;
    protected final Grid<PublicChatMessage> chatGrid;
    protected final TextField enterField;
    protected Pin selectedChannelPin;


    public BisqEasyView() {
        setSizeFull();

        channelColumn = UIUtils.create(new VerticalLayout(), this::add, "channelColumn");
        channelColumn.setWidth("30%");

        UIUtils.create(new Image("./images/logo_grey.png", "Bisq logo"), channelColumn::add);
        
        Label marketLabel = UIUtils.create(new Label("Market Channels"), channelColumn::add, "marketLabel");
        //Res.get("social.marketChannels"));


        // combo channel select
        tradeChannelBox = UIUtils.create(new ComboBox<>(), channelColumn::add, "tradeChannelBox");
        tradeChannelBox.setItems(BisqContext.get().getPublicTradeChannelService().getChannels());
        tradeChannelBox.setItemLabelGenerator(PublicTradeChannel::getDisplayString);
        tradeChannelBox.addValueChangeListener(ev -> {
            if (ev.isFromClient()) {
                boxSelection();
            }
        });
        tradeChannelBox.setVisible(false);
        tradeChannelBox.setPlaceholder("Add market channel");// Res.get("tradeChat.addMarketChannel"));

        // add / remove channel
        HorizontalLayout chButtonbar = UIUtils.create(new HorizontalLayout(), channelColumn::add);
        Button plusButton = UIUtils.create(new Button(new Icon(VaadinIcon.PLUS)), chButtonbar::add, "plusButton");
        plusButton.addClickListener(ev -> tradeChannelBox.setVisible(true));


        Button minusButton = UIUtils.create(new Button(new Icon(VaadinIcon.MINUS)), chButtonbar::add, "minusButton");
        minusButton.addClickListener(ev -> hideChannel());

        listTradeChannels = UIUtils.create(new ListBox<>(), channelColumn::add, "listTradeChannels");
        listTradeChannels.setItemLabelGenerator(PublicTradeChannel::getDisplayString);
        loadListTradeChannels();
        listTradeChannels.addValueChangeListener(ev -> selectChannel());

        Hr divider = new Hr();
        channelColumn.add(divider);


        // private section  ----------------------------------------------------------

        // chatColumn
        chatColumn = UIUtils.create(new VerticalLayout(), this::add, "chatColumn");
        chatColumn.setSizeFull();

        // header -----

        HorizontalLayout chatHeader = UIUtils.create(new HorizontalLayout(), chatColumn::add, "chatHeader");
        channelLabel = UIUtils.create(new Label(), chatHeader::add, "channelLabel");
        Checkbox offerOnlyCheck = UIUtils.create(new Checkbox("Offers only"), chatHeader::add, "offerOnlyCheck");
        TextField searchField = UIUtils.create(new TextField(), chatHeader::add, "searchField");
        searchField.setPlaceholder("Search");//Res.get("search"));

        UIUtils.create(new Hr(), chatColumn::add);

        chatGrid = UIUtils.create(new Grid(), chatColumn::add, "chatGrid");
        chatGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        chatGrid.addColumn(new ComponentRenderer<Div, PublicChatMessage>(this::chatComponent));

        HorizontalLayout messageLayout = UIUtils.create(new HorizontalLayout(), chatColumn::add, "messageLayout");
        enterField = UIUtils.create(new TextField(), messageLayout::add, "enterField");
        enterField.setPlaceholder("Type a new message");
        enterField.addKeyPressListener(Key.ENTER, ev -> send());

        Button sendButton = UIUtils.create(new Button(new Icon(VaadinIcon.CARET_RIGHT)), messageLayout::add, "sendButton");
        sendButton.addClickListener(ev -> send());
    }

    private void send() {
        String text = enterField.getValue();
        if (text != null && !text.isEmpty()) {
            Channel<? extends ChatMessage> channel = BisqContext.get().getTradeChannelSelectionService().getSelectedChannel().get(); // TODO what about multiple window surfing???
            UserIdentity userIdentity = BisqContext.get().getUserIdentityService().getSelectedUserProfile().get();
//            checkNotNull(userIdentity, "chatUserIdentity must not be null at onSendMessage");
//            Optional<Quotation> quotation = quotedMessageBlock.getQuotation();
            if (channel instanceof PublicTradeChannel) {
//                String dontShowAgainId = "sendMsgOfferOnlyWarn";
                SettingsService settingsService = BisqContext.get().getApplicationService().getSettingsService();
                if (settingsService.getOffersOnly().get()) {
                    settingsService.setOffersOnly(false);

                }
                BisqContext.get().getChatService().getPublicTradeChannelService().publishChatMessage(text, Optional.empty(), (PublicTradeChannel) channel, userIdentity);
            }
        }
        enterField.setValue(enterField.getEmptyValue());
    }

    private Div chatComponent(PublicChatMessage message) {
        Div ret = new Div();
        ret.addClassName("message");
        if (isMyMessage(message)) {
            ret.addClassName("isMyMessage");
        }
        Div nameTag = UIUtils.create(new Div(), ret::add, "nameTag");
        Optional<UserProfile> authorProfileOpt = BisqContext.get().getUserProfileService().findUserProfile(message.getAuthorId());
        authorProfileOpt.ifPresent(authorProfile -> {
            nameTag.setText(authorProfile.getNickName());
        });
        Div msgTag = UIUtils.create(new Div(), ret::add, "msgTag");
        msgTag.setText(message.getText());
        return ret;
    }

    private boolean isMyMessage(ChatMessage chatMessage) {
        return BisqContext.get().getUserIdentityService().isUserIdentityPresent(chatMessage.getAuthorId());
    }

    private void selectChannel() {
        if (selectedChannelPin != null) {
            selectedChannelPin.unbind();
        }
        listTradeChannels.getOptionalValue().ifPresent(ch -> {
            channelLabel.setText(ch.getDisplayString());
            final UI currentUI = UI.getCurrent();
            selectedChannelPin = ch.getChatMessages().addChangedListener(() -> {
                // https://vaadin.com/docs/latest/advanced/server-push
                currentUI.access(() -> {
                    ObservableSet<PublicTradeChatMessage> chatMessages = ch.getChatMessages();
                    log.info("async change of chatmessage , no of messages: " + chatMessages.size());
                    chatGrid.setItems(ch.getChatMessages().stream().collect(Collectors.toList()));
                });
            });
        });
    }

    private void hideChannel() {
        listTradeChannels.getOptionalValue().ifPresent(ch -> {
            if (selectedChannelPin != null) {
                selectedChannelPin.unbind();
            }
            BisqContext.get().getPublicTradeChannelService().hidePublicTradeChannel(ch);
            BisqContext.get().getPublicTradeChannelService().persist();
            loadListTradeChannels();
            tradeChannelBox.setVisible(false);
            chatGrid.setItems(Collections.emptyList());
        });
    }

    private void boxSelection() {
        tradeChannelBox.getOptionalValue().ifPresent(ch -> {
            // add channel and select
            BisqContext.get().getChatService().getPublicTradeChannelService().showChannel(ch);
            BisqContext.get().getChatService().getPublicTradeChannelService().persist();
            loadListTradeChannels();
            listTradeChannels.setValue(ch);
        });
        tradeChannelBox.setValue(null);
        tradeChannelBox.setVisible(false);
    }

    private void loadListTradeChannels() {
        listTradeChannels.setItems(BisqContext.get().getPublicTradeChannelService().getChannels().stream()//
                .filter(BisqContext.get().getPublicTradeChannelService()::isVisible) //
                .collect(Collectors.toList()));
    }


    private void printEurChannel() {
        DefaultApplicationService app = BisqContext.get().getApplicationService();
        PublicTradeChannelService ptcs = app.getChatService().getPublicTradeChannelService();
        ptcs.getVisibleChannelIds();
        PublicTradeChannel eurChannel = ptcs.getChannels().stream() //
                .filter(ch -> "EUR".equals(ch.getMarket().getQuoteCurrencyCode()))//
                .findAny() //
                .get();

        eurChannel.getChatMessages().stream().forEach(System.out::println);
    }

    private String formatAmount(long amount, String market) {
        if (market.startsWith("BTC")) {
            String amountAsString = Long.toString(amount);
            return "0,00000000".substring(0, 10 - amountAsString.length()) + amountAsString;
        }
        return "";
    }
}
