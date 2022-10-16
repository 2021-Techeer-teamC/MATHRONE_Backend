//package mathrone.backend.domain;
//
//
//import org.springframework.beans.factory.annotation.Value; //이 Value annotation을 사용해야한다.. 다른거 썼다가 null뜸
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Component //이거를 쓰지 않으면 properties에서 값을 읽어올 수가 없다
//public class googleLoginConfig {
//    @Value("${google.auth.url}") //properties 파일에 적힌 값들
//    private String googleAuthUrl;
//
//    @Value("${google.login.url}")
//    private String googleLoginUrl;
//
//    @Value("${google.redirect.uri}")
//    private String googleRedirectUrl;
//
//    @Value("${google.client.id}")
//    private String googleClientId;
//
//    @Value("${google.secret}")
//    private String googleSecret;
//
//    @Value("${google.auth.scope}")
//    private String scopes;
//
//    // Google 로그인 URL 생성 로직
//    public String googleInitUrl() {
//        System.out.println("prob 1");
//        Map<String, Object> params = new HashMap<>();
//        System.out.println("prob 2");
//        params.put("scope", getScopeUrl());
//        params.put("client_id", getGoogleClientId());
//        System.out.println(getGoogleClientId());
//        params.put("redirect_uri", getGoogleRedirectUri());
//        System.out.println(getGoogleRedirectUri());
//        params.put("response_type", "code");
//        System.out.println("prob 5");
////        params.put("scope", getScopeUrl());
////        System.out.println(getScopeUrl());
//
//        String paramStr = params.entrySet().stream()
//                .map(param -> param.getKey() + "=" + param.getValue())
//                .collect(Collectors.joining("&"));
//        System.out.println("prob 7");
//        return getGoogleLoginUrl()
//                + "/o/oauth2/v2/auth"
//                + "?"
//                + paramStr;
//    }
//
//    public String getGoogleAuthUrl() {
//        return googleAuthUrl;
//    }
//
//    public String getGoogleLoginUrl() {
//        return googleLoginUrl;
//    }
//
//    public String getGoogleClientId() {
//        return googleClientId;
//    }
//
//    public String getGoogleRedirectUri() {
//        return googleRedirectUrl;
//    }
//
//    public String getGoogleSecret() {
//        return googleSecret;
//    }
//
//    // scope의 값을 보내기 위해 띄어쓰기 값을 UTF-8로 변환하는 로직 포함
//    public String getScopeUrl() {
////        return scopes.stream().collect(Collectors.joining(","))
////                .replaceAll(",", "%20");
//        return scopes.replaceAll(",", "%20"); //띄어쓰기를 실제로 사용하면 에러이므로 ,로 저장했다가 %20으로변경
//    }
//}
