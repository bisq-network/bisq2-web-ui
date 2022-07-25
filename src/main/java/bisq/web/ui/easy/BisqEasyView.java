package bisq.web.ui.easy;

import bisq.web.base.MainLayout;
import bisq.web.bo.Offer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Route(value = "easy", layout = MainLayout.class)
@RouteAlias("")
@CssImport("./styles/shared-styles.css")
@CssImport("./styles/BisqEasyView.css")

public class BisqEasyView extends VerticalLayout {

    protected final Div channelList;
    protected final VerticalLayout offerColumn;

    public BisqEasyView() {

        setSizeFull();
        HorizontalLayout mainCols = new HorizontalLayout();
        mainCols.addClassName("mainCols");
        add(mainCols);

        VerticalLayout channelColumn = new VerticalLayout();
        channelColumn.addClassName("channelColumn");
        channelColumn.setWidth("250px");
        mainCols.add(channelColumn);

        Label mc = new Label("Market Channel");
        channelColumn.add(mc);
        channelList = new Div();

        offerColumn = new VerticalLayout();
        mainCols.add(offerColumn);
        channelColumn.add(channelList);

        updateChannels("BTC/EUR");
    }

    public void updateChannels(String... sampleChannelNames) {
        channelList.removeAll(); //remove if some existed
        for (String channelName : sampleChannelNames) {
            Button channelButton = new Button(channelName);
            channelButton.addClickListener(ev -> showChannel(channelName));
            channelList.add(channelButton);
        }
    }

    private void showChannel(String channelName) {
        //columns: picture with name and longname, date, BUY/SELL,Currency,paymentMethod Reputation, Button
        HorizontalLayout topLine = new HorizontalLayout();
        topLine.setSizeFull();
        offerColumn.add(topLine);
        offerColumn.setSizeFull();
        Span channelTitle = new Span(channelName);
        channelTitle.addClassName("channelTitle");
        topLine.add(channelTitle);

        Grid<Offer> offerGrid = new Grid<>();
        offerGrid.setSizeFull();
        offerColumn.add(offerGrid);
        offerGrid.addColumn(offer -> offer.getMaker().getNickName())//
                .setHeader("Nick Name");
        offerGrid.addColumn(offer -> offer.getMaker().getPseudo()) //
                .setHeader("Full Name");
        offerGrid.addColumn(new LocalDateTimeRenderer<>( //
                        offer -> LocalDateTime.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli(offer.getDate()), ZoneId.of("US/Eastern"))), //
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.US)))//
                .setHeader("Date");
        offerGrid.addColumn(offer -> offer.getDirection()) //
                .setHeader("BUY/SELL");
        offerGrid.addColumn(offer -> formatAmount(offer.getAmount(), offer.getMarket()))//
                .setHeader("Amount");
        offerGrid.addColumn(offer -> offer.getMarket().substring(0, 3)) //
                .setHeader("Currency");
        offerGrid.addColumn(offer -> offer.getSettlementMethodName())//
                .setHeader("Settlement");

        offerGrid.setItems(Offer.sampleOffers());
    }

    private String formatAmount(long amount, String market) {
        if (market.startsWith("BTC")) {
            String amountAsString = Long.toString(amount);
            return "0,00000000".substring(0, 10 - amountAsString.length()) + amountAsString;
        }
        return "";
    }
}
