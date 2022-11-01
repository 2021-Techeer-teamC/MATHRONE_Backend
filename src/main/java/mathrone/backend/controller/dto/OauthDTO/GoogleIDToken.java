package mathrone.backend.controller.dto.OauthDTO;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class GoogleIDToken {

    private String iss;
    private String azp;
    private String aud;
    private String sub;
    private String at_hash;
    private String hd;
    private String email;
    private String email_verified;
    private String name;
    private String picture;
    private String given_name;
    private String family_name;
    private String locale;
    private String iat;
    private String exp;
    private String nonce;
    private String alg;
    private String kid;
    private String typ;

}
