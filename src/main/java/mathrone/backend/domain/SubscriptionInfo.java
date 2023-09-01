package mathrone.backend.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Builder
public class SubscriptionInfo {

//    private boolean premium;
    private Timestamp expired_data;
    private Timestamp subscribed_date;
    private String item;
    private int price;

    @Builder
    public SubscriptionInfo (Timestamp expired_data, Timestamp subscribed_date, String item, int price){
        this.expired_data = expired_data;
        this.subscribed_date=subscribed_date;
        this.item=item;
        this.price=price;
    }

}

