package mathrone.backend.domain;


import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "subscription")
@Getter
public class Subscription extends BaseTime{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //JPA 사용시 필요)
    @Column(name = "sub_id")
    private int subId;

    @NotNull
    @Column(name = "user_id")
    private int userId;

    @NotNull
    private String status; // 보류중 (PENDING, COMPLETE , ERROR, CANCELED)

    @NotNull
    private String item;

    @NotNull
    private int price;


    @NotNull
    private String tid;




    @Builder
    public Subscription(int userId, String item, int price){
        this.userId= userId;
        this.item=item;
        this.price=price;
        this.status="PENDING";
        this.tid = "";
    }


    public Subscription updateStatus(String status){
        this.status = status;
        return this;
    }


    public Subscription updateTid(String tid){
        this.tid= tid;
        return this;
    }

}
