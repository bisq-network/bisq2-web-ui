package bisq.web.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@AllArgsConstructor
public class Offer {
    private ChatUser maker;
    private String direction;
    private String market;
    private long amount;
    private long date;
    private String settlementMethodName;


    public static List<Offer> sampleOffers() {
        ChatUser user1 = new ChatUser("nicki", "Blindly-Price-Escape-2167", "", 4);
        ChatUser user2 = new ChatUser("nerf", "Backwards-born-City-2357", "", 3);
        ChatUser user3 = new ChatUser("sdfg", "Someone-here-athome-4682", "", 5);
        ArrayList<Offer> list = new ArrayList<>();
        list.add(new Offer(user1, "BUY", "BTC/EUR", 2160, System.currentTimeMillis(), "SEPA"));
        list.add(new Offer(user1, "SELL", "BTC/EUR", 70, System.currentTimeMillis(), "instant SEPA"));
        list.add(new Offer(user2, "BUY", "BTC/EUR", 34, System.currentTimeMillis(), "SEPA"));
        list.add(new Offer(user2, "BUY", "BTC/EUR", 680, System.currentTimeMillis(), "SEPA"));
        list.add(new Offer(user3, "SELL", "BTC/EUR", 445, System.currentTimeMillis(), "SEPA"));
        list.add(new Offer(user3, "BUY", "BTC/EUR", 70, System.currentTimeMillis(), "SEPA"));
        return list;
    }

    public static void main(String[] args) {
        System.out.println(ZoneId.getAvailableZoneIds());
        //https://dotnetcodr.com/2015/01/11/formatting-dates-in-java-8-using-datetimeformatter/
        ZonedDateTime d = ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("US/Eastern"));
        LocalDateTime g = LocalDateTime.from(d);
        String f = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.US).format(g);
        System.out.println(f);
    }
}