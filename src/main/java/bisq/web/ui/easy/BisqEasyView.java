package bisq.web.ui.easy;

import bisq.chat.channel.Channel;
import bisq.chat.message.ChatMessage;
import bisq.chat.trade.priv.PrivateTradeChannel;
import bisq.chat.trade.pub.PublicTradeChannel;
import bisq.i18n.Res;
import bisq.presentation.formatters.DateFormatter;
import bisq.user.profile.UserProfile;
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
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;

import org.vaadin.lineawesome.LineAwesomeIcon;

@Route(value = "easy", layout = MainLayout.class)
@CssImport("./styles/shared-styles.css")
@CssImport("./styles/BisqEasyView.css")
@Slf4j
public class BisqEasyView extends HorizontalLayout implements IBisqEasyView {

    public static final String CHANNEL_PARAM = "channel"; // URL parameter for choosing the channel

    protected final VerticalLayout chatColumn;
    protected final VerticalLayout channelColumn;
    protected final ComboBox<PublicTradeChannel> tradeChannelBox;
    protected final Grid<PublicTradeChannel> listTradeChannels;
    protected final Label channelLabel;
    protected final Grid<ChatMessage> chatGrid;
    protected final TextField enterField;
    protected final Grid<PrivateTradeChannel> privateChannelList;
    protected final Div replyArea;
    protected final Div replyHeader;
    protected final Div replyAuthor;
    protected final Div replyMessage;
    @Getter
    private BisqEasyPresenter presenter = new BisqEasyPresenter(this);


    public BisqEasyView() {
        setSizeFull();

        channelColumn = UIUtils.create(new VerticalLayout(), this::add, "channelColumn");
        channelColumn.setWidth("30%");

        UIUtils.create(new Image("./images/logo_grey.png", "Bisq logo"), channelColumn::add);

        Label marketLabel = UIUtils.create(new Label("Market channels"), channelColumn::add, "marketLabel");
        //Res.get("social.marketChannels"));

        // combo channel select
        tradeChannelBox = UIUtils.create(new ComboBox<>(), channelColumn::add, "tradeChannelBox");
        tradeChannelBox.setItems(presenter.publicTradeChannelsProvider());
        tradeChannelBox.setItemLabelGenerator(Channel::getDisplayString);
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

        listTradeChannels = UIUtils.create(new Grid<>(), channelColumn::add, "listTradeChannels");
        listTradeChannels.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        listTradeChannels.addColumn(Channel::getDisplayString);
        listTradeChannels.setItems(presenter.activePublicTradeChannelProvider());
        listTradeChannels.setSelectionMode(Grid.SelectionMode.SINGLE);
        listTradeChannels.asSingleSelect().addValueChangeListener(ev1 -> {
            if (ev1.isFromClient()) {
                presenter.selectChannel(ev1.getValue());
            }
        });

        Hr divider = new Hr();
        channelColumn.add(divider);


        // private section  ----------------------------------------------------------
        UIUtils.create(new Label("Private channels"), channelColumn::add, "privateLabel");

        privateChannelList = UIUtils.create(new Grid<>(), channelColumn::add, "privateChannelList");
        privateChannelList.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        privateChannelList.addColumn(Channel::getDisplayString);
        privateChannelList.setItems(presenter.privateTradeChannelsProvider());
        privateChannelList.setSelectionMode(Grid.SelectionMode.SINGLE);
        privateChannelList.asSingleSelect().addValueChangeListener(ev1 -> {
            if (ev1.isFromClient()) {
                presenter.selectChannel(ev1.getValue());
                privateChannelList.getListDataView().refreshAll();
            }
        });

        // chatColumn ----------------------------------------------
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
        chatGrid.addColumn(new ComponentRenderer<Div, ChatMessage>(this::chatComponent));
        ListDataProvider<ChatMessage> chatMessageProvider = presenter.chatMessageProvider();
        chatGrid.setItems(chatMessageProvider);
        chatMessageProvider.addDataProviderListener(ev -> {
            chatGrid.scrollToEnd(); // scroll down to display latest message
        });
        chatGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        chatGrid.setSelectionMode(Grid.SelectionMode.NONE);

        // reply area ---------------------------------------
        replyArea = UIUtils.create(new Div(), chatColumn::add, "replyArea");
        replyHeader = UIUtils.create(new Div(), replyArea::add, "replyHeader");
        UIUtils.create(new Span("Replying to:"), replyHeader::add, "headerText");
        Button replyClose = UIUtils.create(new Button(LineAwesomeIcon.WINDOW_CLOSE.create()), replyHeader::add, "replyClose");
        replyClose.addClickListener(ev -> replyArea.setVisible(false));

        replyAuthor = UIUtils.create(new Div(), replyArea::add, "replyAuthor");
        replyMessage = UIUtils.create(new Div(), replyArea::add, "replyMessage");

        replyArea.setVisible(false);
        // message enter ----------------------------------------

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
            presenter.sendMessage(text);
        }
        enterField.setValue(enterField.getEmptyValue());
    }

    private Div chatComponent(ChatMessage message) {
        Div ret = new Div();
        ret.addClassName("message");
        boolean myMessage = presenter.isMyMessage(message);
        if (myMessage) {
            ret.addClassName("isMyMessage");
        }
        Div msgBorder = UIUtils.create(new Div(), ret::add, "msgBorder");
        Div nameTag = UIUtils.create(new Div(), msgBorder::add, "nameTag");
        if (myMessage) {
            nameTag.addClassName("isMyTag");
        }
        Optional<UserProfile> authorProfileOpt = presenter.findAuthor(message);
        authorProfileOpt.ifPresent(authorProfile -> {
            nameTag.add(new Span(authorProfile.getNickName()));
        });
        String date = DateFormatter.formatDateTime(new Date(message.getDate()), DateFormat.MEDIUM, DateFormat.SHORT, true, " " + Res.get("at") + " ");
        UIUtils.create(new Span(date), nameTag::add, "messDate");
        Div msgTag = UIUtils.create(new Div(), msgBorder::add, "msgTag");
        msgTag.setText(message.getText());
        Div msgActions = UIUtils.create(new Div(), ret::add, "msgActions");
        Button replyButton = UIUtils.create(new Button(LineAwesomeIcon.REPLY_SOLID.create()), msgBorder::add, "replyButton");
        replyButton.addClickListener(event -> reply(message));
        Button privateButton = UIUtils.create(new Button(LineAwesomeIcon.COMMENT_ALT.create()), msgBorder::add, "privateButton");
        Button ignoreButton = UIUtils.create(new Button(LineAwesomeIcon.USER_MINUS_SOLID.create()), msgBorder::add, "ignoreButton");
        Button petzButton = UIUtils.create(new Button(LineAwesomeIcon.USER_GRADUATE_SOLID.create()), msgBorder::add, "petzButton");
        return ret;
    }

    protected void reply(ChatMessage message) {

        // first show reply dialog over new message
        replyArea.setVisible(true);
        replyAuthor.setText(presenter.findAuthor(message).map(UserProfile::getNickName).orElse(""));
        replyMessage.setText(message.getText());
    }

    @Override
    public void stateChanged() {
        channelLabel.setText(presenter.getSelectedChannel().map(Channel::getDisplayString).orElse(""));
        listTradeChannels.select(presenter.getSelectedChannel()
                .filter(PublicTradeChannel.class::isInstance)
                .map(PublicTradeChannel.class::cast)
                .orElse(null));
        PrivateTradeChannel privateTradeChannel = (PrivateTradeChannel) presenter.getSelectedChannel()
                .filter(PrivateTradeChannel.class::isInstance)
                .orElse(null);
        privateChannelList.select(privateTradeChannel);

        if (!presenter.getSelectedChannel().isPresent()) {
            enterField.setValue(enterField.getEmptyValue());
        }
    }

    private void hideChannel() {
        presenter.hideSelectedChannel();
        tradeChannelBox.setVisible(false);
    }

    private void boxSelection() {
        tradeChannelBox.getOptionalValue().ifPresent(ch -> {
            // add channel and select
            presenter.selectChannel(ch);
        });
        tradeChannelBox.setValue(null);
        tradeChannelBox.setVisible(false);
    }

    @Override
    public Runnable pushCallBack(Runnable command) {
        final UI sourceUI = UI.getCurrent();
        return () -> sourceUI.access(() -> command.run()); //avoiding dependency to vaadin command
    }

}
