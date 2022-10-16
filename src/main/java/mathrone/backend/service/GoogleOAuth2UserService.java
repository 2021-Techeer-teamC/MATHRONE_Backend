//package mathrone.backend.service;
//
//import lombok.RequiredArgsConstructor;
//import mathrone.backend.controller.dto.OauthDTO.OAuthAttributes;
//import mathrone.backend.controller.dto.OauthDTO.SessionUser;
//import mathrone.backend.domain.UserInfo;
//import mathrone.backend.repository.UserInfoRepository;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//
//import javax.servlet.http.HttpSession;
//import java.util.Collections;
//
//@RequiredArgsConstructor
//@Service
//public class GoogleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
//
//    private final UserInfoRepository userInfoRepository;
//    private final HttpSession httpSession;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2UserService delegate = new DefaultOAuth2UserService();
//        OAuth2User oAuth2User = delegate.loadUser(userRequest);
//
//        String registrationId = userRequest.getClientRegistration().getRegistrationId();
//        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
//
//        OAuthAttributes attributes = OAuthAttributes.
//                of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
//
//        UserInfo user = saveOrUpdate(attributes);
//        httpSession.setAttribute("user", new SessionUser(user));
//
//
//
//        return new DefaultOAuth2User(
//                Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
//                attributes.getAttributes(),
//                attributes.getNameAttributeKey());
//    }
//
//    private UserInfo saveOrUpdate(OAuthAttributes attributes) {
//        UserInfo user = userInfoRepository.findByEmail(attributes.getEmail())
//                .map(entity -> entity.update(attributes.getUserImg(),attributes.toUser().isPremium()))
//                .orElse(attributes.toUser());
//
//        return userInfoRepository.save(user);
//    }
//}
