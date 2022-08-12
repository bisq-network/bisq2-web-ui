package bisq.web.ui.easy;

import bisq.chat.channel.Channel;
import bisq.chat.message.ChatMessage;
import bisq.chat.trade.pub.PublicTradeChannel;
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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;


@Route(value = "easy", layout = MainLayout.class)
@RouteAlias("")
@CssImport("./styles/shared-styles.css")
@CssImport("./styles/BisqEasyView.css")
@Slf4j
public class BisqEasyView extends HorizontalLayout implements IBisqEasyView {

    public static final String CHANNEL_PARAM = "channel"; // URL parameter for choosing the channel

    protected final VerticalLayout chatColumn;
    protected final VerticalLayout channelColumn;
    protected final ComboBox<PublicTradeChannel> tradeChannelBox;
    protected final ListBox<Channel<? extends ChatMessage>> listTradeChannels;
    protected final Label channelLabel;
    protected final Grid<ChatMessage> chatGrid;
    protected final TextField enterField;
    @Getter
    private BisqEasyPresenter presenter = new BisqEasyPresenter(this);


    public BisqEasyView() {
        setSizeFull();

        channelColumn = UIUtils.create(new VerticalLayout(), this::add, "channelColumn");
        channelColumn.setWidth("30%");

        Label marketLabel = UIUtils.create(new Label("Market Channels"), channelColumn::add, "marketLabel");
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

        listTradeChannels = UIUtils.create(new ListBox<>(), channelColumn::add);
        listTradeChannels.setItemLabelGenerator(Channel::getDisplayString);
        listTradeChannels.setItems(presenter.activeChannelProvider());
        listTradeChannels.addValueChangeListener(ev -> presenter.selectChannel(ev.getValue()));

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
        chatGrid.addColumn(new ComponentRenderer<Div, ChatMessage>(this::chatComponent));
        chatGrid.setItems(presenter.loadChatMessageProvider());

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
        if (presenter.isMyMessage(message)) {
            ret.addClassName("isMyMessage");
        }
        Div nameTag = UIUtils.create(new Div(), ret::add, "nameTag");
        Optional<UserProfile> authorProfileOpt = presenter.findAuthor(message);
        authorProfileOpt.ifPresent(authorProfile -> {
            nameTag.setText(authorProfile.getNickName());
        });
        Div msgTag = UIUtils.create(new Div(), ret::add, "msgTag");
        msgTag.setText(message.getText());
        return ret;
    }

    @Override
    public void stateChanged() {
        presenter.getSelectedChannel().ifPresent(ch -> channelLabel.setText(ch.getDisplayString()));
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
