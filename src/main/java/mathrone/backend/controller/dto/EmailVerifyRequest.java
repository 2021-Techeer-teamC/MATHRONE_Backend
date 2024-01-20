package mathrone.backend.controller.dto;


import lombok.*;

@Setter
@Getter
@Builder
@ToString
public class EmailVerifyRequest {

    private String email;

    private String accountId;

    public EmailVerifyRequest(){

    }

    public EmailVerifyRequest(String email, String accountId){
        this.email = email;
        this.accountId = accountId;
    }

}
