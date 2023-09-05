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


}