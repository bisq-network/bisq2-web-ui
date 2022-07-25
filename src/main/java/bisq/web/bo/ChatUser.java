package bisq.web.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ChatUser {

    private String nickName;
    private String pseudo;
    private String picture;
    private int stars;
}
