package mathrone.backend.service;

import static mathrone.backend.domain.enums.UserResType.GOOGLE;
import static mathrone.backend.domain.enums.UserResType.KAKAO;
import static mathrone.backend.domain.enums.UserResType.MATHRONE;
import static mathrone.backend.error.exception.ErrorCode.AlREADY_LOGOUT;
import static mathrone.backend.error.exception.ErrorCode.INVALID_REFRESH_TOKEN;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.ChangePasswordDto;
import mathrone.backend.controller.dto.FindDto;
import mathrone.backend.controller.dto.OauthDTO.GoogleIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;
import mathrone.backend.controller.dto.OauthDTO.ResponseTokenDTO;
import mathrone.backend.controller.dto.TokenDto;
import mathrone.backend.controller.dto.UserRequestDto;
import mathrone.backend.controller.dto.UserResponseDto;
import mathrone.backend.controller.dto.UserSignUpDto;
import mathrone.backend.domain.Subscription;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.domain.token.GoogleRefreshTokenRedis;
import mathrone.backend.domain.token.KakaoRefreshTokenRedis;
import mathrone.backend.domain.token.LogoutAccessToken;
import mathrone.backend.domain.token.RefreshToken;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.repository.RefreshTokenRepository;
import mathrone.backend.repository.SubscriptionRepository;
import mathrone.backend.repository.UserInfoRepository;
import mathrone.backend.repository.redisRepository.KakaoRefreshTokenRedisRepository;
import mathrone.backend.repository.redisRepository.LogoutAccessTokenRedisRepository;
import mathrone.backend.repository.redisRepository.RefreshTokenRedisRepository;
import mathrone.backend.repository.tokenRepository.GoogleRefreshTokenRedisRepository;
import mathrone.backend.util.TokenProviderUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserInfoRepository userinfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProviderUtil tokenProviderUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;
    private final KakaoRefreshTokenRedisRepository kakaoRefreshTokenRedisRepository;
    private final GoogleRefreshTokenRedisRepository googleRefreshTokenRedisRepository;
    private final MailService mailService;
    private final SubscriptionRepository subscriptionRepository;


    @Transactional
    public UserResponseDto signup(UserSignUpDto userSignUpDto) {
        // user account ID가 존재하는지 검사
        validateUserAccountId(userSignUpDto.getAccountId());

        UserInfo newUser = userSignUpDto.toUser(passwordEncoder,
            MATHRONE.getTypeName()); //MATHRONE user로 가입시켜주기
        return UserResponseDto.of(userinfoRepository.save(newUser));
    }

    @Transactional
    public UserResponseDto signupWithGoogle(ResponseEntity<GoogleIDToken> googleIDToken,
        String accountId) {
        //이미 가입된 구글계정인지 확인
        validateGoogleAccount(googleIDToken);//이미 가입기록이 있으면 여기서 에러
        //유효한 accountID인지 확인(이미 존재하는 아이디인지)
        validateUserAccountId(accountId); //이미 존재하는 아이디면 여기서 에러
        //아니면 회원가입 진행
        //입력받아온 accountID를 이용하여 회원가입
        UserSignUpDto userSignUpDto = new UserSignUpDto(googleIDToken.getBody().getEmail(),
            "googleLogin", accountId); //id와 email을 email로 채워서 만들기
        UserInfo newUser = userSignUpDto.toUser(passwordEncoder, GOOGLE.getTypeName());

        return UserResponseDto.of(userinfoRepository.save(newUser));
    }

    @Transactional
    public UserResponseDto signupWithKakao(ResponseEntity<KakaoIDToken> kakaoIDToken,
        String accountID) {

        //이미 가입된 카카오 계정인지 확인
        validateKakaoAccount(kakaoIDToken);

        //유효한 accountID인지 확인
        validateUserAccountId(accountID);

        UserSignUpDto userSignUpDto = new UserSignUpDto(kakaoIDToken.getBody().getEmail(),
            "kakaoLogin", accountID); //id와 email을 email로 채워서 만들기
        UserInfo newUser = userSignUpDto.toUser(passwordEncoder, KAKAO.getTypeName());

        return UserResponseDto.of(userinfoRepository.save(newUser));
    }

    @Transactional
    public TokenDto googleLogin(ResponseEntity<GoogleIDToken> googleIDToken,
        ResponseEntity<ResponseTokenDTO> googleResponseToken) {
        //0. 가입이 되어 있는 계정이 아니면 회원가입을 자동으로 시켜주기
        if (!userinfoRepository.existsByEmailAndResType(googleIDToken.getBody().getEmail(),
            GOOGLE.getTypeName())) {
            //타입 : 구글 && 이메일이 존재하지 않는 경우
            String tmpId;
            //@로 시작하는 랜덤 아이디를 만들어 제공
            do {
                tmpId = "@" + RandomStringUtils.random(12, true, true);
            } while (userinfoRepository.existsByAccountId(tmpId));//존재하지 않는 아이디일 때 까지 반복
            signupWithGoogle(googleIDToken, tmpId);
        }

        //active true인 경우만 로그인 가능
        UserInfo user = userinfoRepository.findByEmailAndResTypeAndActivateTrue(googleIDToken.getBody().getEmail(),
            GOOGLE.getTypeName());

        //프리미엄
        if (user.isPremium()) {
            checkPremiumUser(user.getUserId());
        }

        UserRequestDto userRequestDto = new UserRequestDto(user.getAccountId(),
            "googleLogin");

        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.of();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject()
            .authenticate(authenticationToken);

        // 3. token 생성
        TokenDto tokenDto = tokenProviderUtil.generateTokenWithSns(authentication,
            userRequestDto.getAccountId(), googleResponseToken.getBody().getAccessToken());

        // 4. refresh token 생성 ( database 및 redis 저장을 위한 refresh token )
        RefreshToken refreshToken = RefreshToken.builder()
            .userid(authentication.getName())
            .refreshToken(tokenDto.getRefreshToken())
            .expiration(tokenProviderUtil.getRefreshTokenExpireTime())
            .build();

        // 5. 토큰 저장 테이블 저장
        refreshTokenRepository.save(refreshToken);

        // 6. redis 저장
        refreshTokenRedisRepository.save(refreshToken.transferRedisToken());

        // 7. google에서 발급한 refreshToken저장
        saveGoogleRefreshToken(googleIDToken, googleResponseToken);

        return tokenDto;
    }

    @Transactional
    public void saveGoogleRefreshToken(ResponseEntity<GoogleIDToken> googleIDToken,
        ResponseEntity<ResponseTokenDTO> googleResponseToken) {

        //user id알아내기
        UserInfo user = userinfoRepository.findByEmailAndResType(googleIDToken.getBody().getEmail(),
            GOOGLE.getTypeName());
        int userId = user.getUserId();

        // google에서 발급한 refresh Token
        String refreshToken = googleResponseToken.getBody().getRefreshToken();

        //builder로 객체 생성
        GoogleRefreshTokenRedis googleRefreshTokenRedis = GoogleRefreshTokenRedis.builder()
            .id(Integer.toString(userId))
            .refreshToken(refreshToken)
            .build();

        googleRefreshTokenRedisRepository.save(googleRefreshTokenRedis);
    }

    @Transactional
    public TokenDto kakaoLogin(ResponseEntity<KakaoTokenResponseDTO> kakaoTokenResponseDto,
        ResponseEntity<KakaoIDToken> kakaoIDToken) {

        //가입이 안되어 있는 경우 -> 자동가입 but accountID가 미설정되었음을 알려야함
        if (!userinfoRepository.existsByEmailAndResType(kakaoIDToken.getBody().getEmail(),
            KAKAO.getTypeName())) {
            //타입 : 카카오 && 이메일이 존재하지 않는 경우
            String tmpId;
            //@로 시작하는 랜덤 아이디를 만들어 제공
            do {
                tmpId = "@" + RandomStringUtils.random(12, true, true);
            } while (userinfoRepository.existsByAccountId(tmpId));//존재하지 않는 아이디일 때 까지 반복
            signupWithKakao(kakaoIDToken, tmpId); //카카오계정 으로 회원가입 진행
        }
        UserInfo user = userinfoRepository.findByEmailAndResTypeAndActivateTrue(kakaoIDToken.getBody().getEmail(),
            KAKAO.getTypeName());

        //로그인 시 프리미엄 검사
        if (user.isPremium()) {
            checkPremiumUser(user.getUserId());
        }

        UserRequestDto userRequestDto = new UserRequestDto(user.getAccountId(),
            "kakaoLogin");

        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.of();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject()
            .authenticate(authenticationToken);

        // 3. token 생성
        TokenDto tokenDto = tokenProviderUtil.generateTokenWithSns(authentication,
            userRequestDto.getAccountId(), kakaoTokenResponseDto.getBody().getAccess_token());

        // 4. refresh token 생성 ( database 및 redis 저장을 위한 refresh token )
        RefreshToken refreshToken = RefreshToken.builder()
            .userid(authentication.getName())
            .refreshToken(tokenDto.getRefreshToken())
            .expiration(tokenProviderUtil.getRefreshTokenExpireTime())
            .build();

        // 5. 토큰 저장 테이블 저장
        refreshTokenRepository.save(refreshToken);
        // 6. redis 저장
        refreshTokenRedisRepository.save(refreshToken.transferRedisToken());
        // 7. kakao에서 발급한 refreshToken 저장
        saveKakaoRefreshToken(kakaoTokenResponseDto, kakaoIDToken);

        return tokenDto;
    }

    @Transactional
    public void saveKakaoRefreshToken(ResponseEntity<KakaoTokenResponseDTO> kakaoTokenResponseDto,
        ResponseEntity<KakaoIDToken> kakaoIdToken) {
        //user id알아내기
        UserInfo user = userinfoRepository.findByEmailAndResType(kakaoIdToken.getBody().getEmail(),
            KAKAO.getTypeName());
        int userId = user.getUserId();

        // kakao에서 발급한 refresh Token 및 만료시간
        String refreshToken = kakaoTokenResponseDto.getBody().getRefresh_token();
        Integer refreshTokenExpire = kakaoTokenResponseDto.getBody().getRefresh_token_expires_in();

        //builder로 객체 생성
        KakaoRefreshTokenRedis kakaoRefreshTokenRedis = KakaoRefreshTokenRedis.builder()
            .id(Integer.toString(userId))
            .refreshToken(refreshToken)
            .expiration(refreshTokenExpire)
            .build();
        kakaoRefreshTokenRedisRepository.save(kakaoRefreshTokenRedis);

    }


    @Transactional
    public void deleteUser(String accountId, String resType) {
        // resType에 대한 구분을 enum class로 다루는 방안에 대해 토의하기
        if (resType.equals(MATHRONE.getTypeName())) {
            // accountId가 존재하지 않는 경우에 대한 예외처리 작성하기
            userinfoRepository.deleteByAccountIdAndResType(accountId, resType);
        }
    }

    public List<UserInfo> allUser() {
        return userinfoRepository.findAll();
    }

    @Transactional
    public TokenDto login(UserRequestDto userRequestDto) {
        // Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.of();

        // 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject()
            .authenticate(authenticationToken);
        // token 생성
        TokenDto tokenDto = tokenProviderUtil.generateToken(authentication,
            userRequestDto.getAccountId());

        int userId = Integer.parseInt(tokenDto.getUserInfo().getUserId());
        UserInfo u = userinfoRepository.findByUserIdAndActivateTrue(userId); //active true인 경우에만
        if (u.isPremium()) {
            checkPremiumUser(userId);
        }

        // refresh token 생성 ( database 및 redis 저장을 위한 refresh token )
        RefreshToken refreshToken = RefreshToken.builder()
            .userid(authentication.getName())
            .refreshToken(tokenDto.getRefreshToken())
            .expiration(tokenProviderUtil.getRefreshTokenExpireTime())
            .build();
        // 5. 토큰 저장 테이블 저장
        refreshTokenRepository.save(refreshToken);
        // 6. redis 저장
        refreshTokenRedisRepository.save(refreshToken.transferRedisToken());
        return tokenDto;
    }

    @Transactional
    public void logout(HttpServletRequest request) {

        // Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // access token으로부터 user id 가져오기 (email x)
        String userId = tokenProviderUtil.getAuthentication(accessToken).getName();

        // logout token의 유효기간은 access token의 남은 기간동안 유지되어야 함
        long remainAccessTokenExpiration = tokenProviderUtil.getRemainExpiration(accessToken);

        // refreshToken table에 존재하는 해당 유저의 refreshToken 정보 삭제
        refreshTokenRepository.deleteByUserId(userId);

        // redis에 존재하는 refreshToken 삭제
        refreshTokenRedisRepository.deleteById(userId);

        // logout token를 redis에 저장 (이후 로그아웃된 유저의 AccessToken으로 접근 방지를 위함)
        logoutAccessTokenRedisRepository.save(
            LogoutAccessToken.of(accessToken, userId, remainAccessTokenExpiration));
    }

    @Transactional
    public void logoutWithKakao(HttpServletRequest request) {

        // Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // access token으로부터 user id 가져오기 (email x)
        String userId = tokenProviderUtil.getAuthentication(accessToken).getName();

        // logout token의 유효기간은 access token의 남은 기간동안 유지되어야 함
        long remainAccessTokenExpiration = tokenProviderUtil.getRemainExpiration(accessToken);

        // refreshToken table에 존재하는 해당 유저의 refreshToken 정보 삭제
        refreshTokenRepository.deleteByUserId(userId);

        // redis에 존재하는 refreshToken 삭제
        refreshTokenRedisRepository.deleteById(userId);

        // logout token를 redis에 저장 (이후 로그아웃된 유저의 AccessToken으로 접근 방지를 위함)
        logoutAccessTokenRedisRepository.save(
            LogoutAccessToken.of(accessToken, userId, remainAccessTokenExpiration));

        // kakaoRedisToken삭제
        kakaoRefreshTokenRedisRepository.deleteById(userId);
    }


    @Transactional
    public void logoutWithGoogle(HttpServletRequest request) {
        // Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // access token으로부터 user id 가져오기 (email x)
        String userId = tokenProviderUtil.getAuthentication(accessToken).getName();

        // logout token의 유효기간은 access token의 남은 기간동안 유지되어야 함
        long remainAccessTokenExpiration = tokenProviderUtil.getRemainExpiration(accessToken);

        // refreshToken table에 존재하는 해당 유저의 refreshToken 정보 삭제
        refreshTokenRepository.deleteByUserId(userId);

        // redis에 존재하는 refreshToken 삭제
        refreshTokenRedisRepository.deleteById(userId);

        // logout token를 redis에 저장 (이후 로그아웃된 유저의 AccessToken으로 접근 방지를 위함)
        logoutAccessTokenRedisRepository.save(
            LogoutAccessToken.of(accessToken, userId, remainAccessTokenExpiration));

        // googleRedisToken삭제
        googleRefreshTokenRedisRepository.deleteById(userId);
    }

    @Transactional
    public TokenDto reissue(HttpServletRequest request, String refreshToken) {
        // Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProviderUtil.getAuthentication(
            accessToken);

        // 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져오기
        RefreshToken storedRefreshToken = refreshTokenRepository.findByUserId(
                authentication.getName())
            .orElseThrow(() -> new CustomException(AlREADY_LOGOUT));

        // Refresh Token 일치 여부 검사
        if (!storedRefreshToken.getRefreshToken().equals(refreshToken)) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }

        UserInfo user = findUserFromRequest(request);
        // 새로운 토큰 생성
        TokenDto tokenDto = tokenProviderUtil.generateToken(authentication, user.getAccountId());
        // 저장소 정보 업데이트
        RefreshToken newRefreshToken = storedRefreshToken.updateValue(tokenDto.getRefreshToken(),
            tokenProviderUtil.getRefreshTokenExpireTime());

        // redis와 gcp에 모두 refresh token을 저장.
        refreshTokenRepository.save(newRefreshToken);
        refreshTokenRedisRepository.save(newRefreshToken.transferRedisToken());

        return tokenDto;
    }


    @Transactional
    public TokenDto kakaoReissue(HttpServletRequest request,
        ResponseEntity<KakaoTokenResponseDTO> reissue, ResponseEntity<KakaoIDToken> kakaoIdToken) {

        // 2. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);
        // 3. Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProviderUtil.getAuthentication(
            accessToken);
        // 4. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져오기
        RefreshToken storedRefreshToken = refreshTokenRepository.findByUserId(
                authentication.getName())
            .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        UserInfo user = findUserFromRequest(request);
        // 5. 새로운 토큰 생성
        TokenDto tokenDto = tokenProviderUtil.generateTokenWithSns(authentication,
            user.getAccountId(), reissue.getBody().getAccess_token());
        // 6. 저장소 정보 업데이트
        RefreshToken newRefreshToken = storedRefreshToken.updateValue(tokenDto.getRefreshToken(),
            tokenProviderUtil.getRefreshTokenExpireTime());
        // redis와 gcp에 모두 refresh token을 저장.
        refreshTokenRepository.save(newRefreshToken);
        refreshTokenRedisRepository.save(newRefreshToken.transferRedisToken());

        // 7. kakao에서 발급한 refreshToken 저장
        saveKakaoRefreshToken(reissue, kakaoIdToken);

        return tokenDto;
    }


    @Transactional
    public TokenDto googleReissue(HttpServletRequest request,
        ResponseEntity<ResponseTokenDTO> reissue, ResponseEntity<GoogleIDToken> googleIdToken) {

        // 2. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // 3. Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProviderUtil.getAuthentication(
            accessToken);

        // 4. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져오기
        RefreshToken storedRefreshToken = refreshTokenRepository.findByUserId(
                authentication.getName())
            .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        UserInfo user = findUserFromRequest(request);

        // 5. 새로운 토큰 생성
        TokenDto tokenDto = tokenProviderUtil.generateTokenWithSns(authentication,
            user.getAccountId(), reissue.getBody().getAccessToken());

        // 6. 저장소 정보 업데이트
        RefreshToken newRefreshToken = storedRefreshToken.updateValue(tokenDto.getRefreshToken(),
            tokenProviderUtil.getRefreshTokenExpireTime());

        // redis와 gcp에 모두 refresh token을 저장.
        refreshTokenRepository.save(newRefreshToken);

        refreshTokenRedisRepository.save(newRefreshToken.transferRedisToken());

        // 7. google 에서 발급한 refreshToken 저장
        saveGoogleRefreshToken(googleIdToken, reissue);
        return tokenDto;
    }

    @Transactional
    public String getUserIdFromAT(HttpServletRequest request) {
        // 1. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // 2. access token으로부터 user id 가져오기 (email x)
        return tokenProviderUtil.getAuthentication(accessToken).getName();
    }

    public void validateUserAccountId(String userAccountId) {
        if (userinfoRepository.existsUserInfoByAccountId(userAccountId)) {
            throw new CustomException(ErrorCode.ACCOUNT_IS_DUPLICATION);
        }
    }

    //가입이 진행된 구글 계정인지 확인 -> 가입이 된적 없으면 에러 (로그인 시도시)
    // 로그인 시 가입된 적 없으면 자동 가입이 진행되므로 필요 없어짐
//    public void existGoogleAccount(ResponseEntity<GoogleIDToken> googleIDToken) {
//        if (!userinfoRepository.existsByEmailAndResType(googleIDToken.getBody().getEmail(),
//                GOOGLE.getTypeName())){
//            throw new UserException(ErrorCode.GOOGLE_ACCOUNT_NOT_FOUND);
//        }
//    }
    //가입이 진행된 구글 계정인지 확인 -> 가입 된적 있으면 에러(회원가입 시도시)
    public void validateGoogleAccount(ResponseEntity<GoogleIDToken> googleIDToken) {
        if (userinfoRepository.existsByEmailAndResType(googleIDToken.getBody().getEmail(),
            GOOGLE.getTypeName())) {
            throw new CustomException(ErrorCode.GOOGLE_ACCOUNT_IS_DUPLICATION);
        }
    }


    public void validateKakaoAccount(ResponseEntity<KakaoIDToken> kakaoIDToken) {
        if (userinfoRepository.existsByEmailAndResType(kakaoIDToken.getBody().getEmail(),
            KAKAO.getTypeName())) {
            throw new CustomException(ErrorCode.KAKAO_ACCOUNT_IS_DUPLICATION);
        }
    }


    //
    public UserInfo findUserFromRequest(HttpServletRequest request) {
        // 1. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // 2. access token으로부터 user id 가져오기 (email x)
        int userId = Integer.parseInt(
            tokenProviderUtil.getAuthentication(accessToken).getName());
        // 3. userId를 이용해 user가져오기
        return userinfoRepository.findByUserId(userId);
    }

    public void updateAccountId(String accountId, UserInfo user) {
        //정확하게 존재하는 유저가 아니라면 오류
        validateUser(user);
        //존재하는 accountID인 경우 오류
        validateUserAccountId(accountId);
        //어카운트 아이디 업데이트 진행
        UserInfo newUser = user.updateAccountId(accountId);
        userinfoRepository.save(newUser); //p key가 같은 것이 save되면 자동으로 update의 기능이 수행됨
        // return을 void로 했는데 204 + empty() 를 보내도 괜찮다는
        // 204 : No Content 클라이언트의 요청은 정상적이다. 하지만 컨텐츠를 제공하지 않습니다
    }

    public void validateUser(UserInfo user) {
        if (!userinfoRepository.existsByUserId(user.getUserId())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    // 아이디 찾기, 아이디 찾아서 이메일 발송
    public void findId(FindDto request) {
        UserInfo user = userinfoRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        mailService.sendId(user);
    }

    // 비밀번호 찾기, 임시 비밀번호 발급 후 이메일 발송
    @Transactional
    public void findPw(FindDto request) {
        String newPassword = UUID.randomUUID().toString().substring(0, 10);
        UserInfo user = userinfoRepository.findByEmailAndAccountId(request.getEmail(),
            request.getId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.changePassword(passwordEncoder, newPassword);
        mailService.sendPw(user, newPassword);
    }

    //일반적인 비밀변호 변경 로직
    @Transactional
    public void changePw(HttpServletRequest request, ChangePasswordDto newPassword) {
        String accessToken = tokenProviderUtil.resolveToken(request);

        if (!tokenProviderUtil.validateToken(accessToken, request)) {
            throw (CustomException) request.getAttribute("Exception");
        }
        int userId = Integer.parseInt(
            tokenProviderUtil.getAuthentication(accessToken).getName());

        UserInfo user = userinfoRepository.findByUserId(userId);
        user.changePassword(passwordEncoder, newPassword.getNewPassword());
    }

    //authService.java -> 로그인 하는 경우에 프리미엄 구독 만료 대상이면 만료시킴
    public void checkPremiumUser(int userId) {

        UserInfo u = userinfoRepository.findByUserId(userId);

        if (u.isPremium()) {

            Subscription s = subscriptionRepository.checkLastSubscription(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIBE_USER_NOT_FOUND));

            Date today = new Date();

            long diffDays =
                (today.getTime() - s.getLastModifiedDate().getTime()) / (24 * 60 * 60 * 1000);

            //구독 만료 대상자 -> premium = false로 변경
            if (diffDays >= 30) {
                UserInfo updatedUser = u.updatePremium(false);
                userinfoRepository.save(updatedUser);
            }

        }

    }
}
