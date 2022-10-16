//package mathrone.backend.controller.dto.OauthDTO;
//
//
//import lombok.Builder;
//import lombok.Getter;
//import mathrone.backend.domain.UserInfo;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Map;
//
//@Getter
//public class OAuthAttributes {
//
//    private Map<String, Object> attributes;
//    private String nameAttributeKey;
//    private String email;
//    private String userImg;//profile picture
//
//    @Builder
//    public OAuthAttributes(Map<String, Object> attributes,
//                           String nameAttributeKey,
//                           String email, String userImg){
//        this.attributes = attributes;
//        this.nameAttributeKey = nameAttributeKey;
//        this.email = email;
//        this.userImg = userImg;
//    }
//
//    public static OAuthAttributes of(String registrationId,
//                                     String userNameAttributeName,
//                                     Map<String, Object> attributes ){
//        return ofGoogle(userNameAttributeName, attributes);
//    }
//
//    private static OAuthAttributes ofGoogle(String userNameAttributeName,
//                                            Map<String, Object> attributes) {
//        return OAuthAttributes.builder()
//                .email((String) attributes.get("email"))
//                .userImg((String) attributes.get("userImg"))
//                .attributes(attributes)
//                .nameAttributeKey(userNameAttributeName)
//                .build();
//    }
//
//    public UserInfo toUser(){
//        return UserInfo.builder()
//                .email(email)
//                .role("ROLE_USER")
//                .build();
//    }
//
//
//
//}
