package mathrone.backend.service;

import static mathrone.backend.domain.enums.UserResType.GOOGLE;
import static mathrone.backend.domain.enums.UserResType.MATHRONE;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.*;
import mathrone.backend.controller.dto.OauthDTO.GoogleIDToken;
import mathrone.backend.domain.token.LogoutAccessToken;
import mathrone.backend.domain.token.RefreshToken;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.error.exception.UserException;
import mathrone.backend.repository.UserInfoRepository;
import mathrone.backend.repository.tokenRepository.LogoutAccessTokenRedisRepository;
import mathrone.backend.util.TokenProviderUtil;
import mathrone.backend.repository.tokenRepository.RefreshTokenRedisRepository;
import mathrone.backend.repository.tokenRepository.RefreshTokenRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.List;

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

    @Transactional
    public UserResponseDto signup(UserSignUpDto userSignUpDto) {
        // user account ID가 존재하는지 검사
        validateUserAccountId(userSignUpDto.getAccountId());

        UserInfo newUser = userSignUpDto.toUser(passwordEncoder,
            MATHRONE.getTypeName()); //MATHRONE user로 가입시켜주기
        return UserResponseDto.of(userinfoRepository.save(newUser));
    }

    @Transactional
    public UserResponseDto signupWithGoogle(ResponseEntity<GoogleIDToken> googleIDToken, String accountID) {

        //이미 가입된 구글계정인지 확인
        validateGoogleAccount(googleIDToken);//이미 가입기록이 있으면 여기서 에러

        //유효한 accountID인지 확인(이미 존재하는 아이디인지)
        validateUserAccountId(accountID); //이미 존재하는 아이디면 여기서 에러

        //아니면 회원가입 진행
        //입력받아온 accountID를 이용하여 회원가입
        UserSignUpDto userSignUpDto = new UserSignUpDto(accountID,
                    "googleLogin", googleIDToken.getBody().getEmail()); //id와 email을 email로 채워서 만들기
        UserInfo newUser = userSignUpDto.toUser(passwordEncoder, GOOGLE.getTypeName());

        return UserResponseDto.of(userinfoRepository.save(newUser));

    }

    @Transactional
    public TokenDto googleLogin(ResponseEntity<GoogleIDToken> googleIDToken){

        //0. 가입이 되어 있는 계정이 아니면 회원가입을 자동으로 시켜주기
        if (!userinfoRepository.existsByEmailAndResType(googleIDToken.getBody().getEmail(),
                GOOGLE.getTypeName())){
            //타입 : 구글 && 이메일이 존재하지 않는 경우

            //@을 기준으로 앞부분을 임시 아이디로 사용 -> 이 또한 중복이 있으면 가입이 막혀서 좋은 방법은 아닌 것 같음..
            String[] emailSplit = googleIDToken.getBody().getEmail().split("@");
            signupWithGoogle(googleIDToken, emailSplit[0]);

        }

        UserRequestDto userRequestDto = new UserRequestDto(googleIDToken.getBody().getEmail(),
                    "googleLogin");

        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.of();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject()
                    .authenticate(authenticationToken);

        // 3. token 생성
        TokenDto tokenDto = tokenProviderUtil.generateToken(authentication);

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

        return tokenDto;


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
    public TokenDto login(UserRequestDto userRequestDto) throws Exception{



            // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
            UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.of();

            // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
            //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
            Authentication authentication = authenticationManagerBuilder.getObject()
                    .authenticate(authenticationToken);

            // 3. token 생성
            TokenDto tokenDto = tokenProviderUtil.generateToken(authentication);

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

            return tokenDto;



    }

    @Transactional
    public void logout(HttpServletRequest request) {
        // 1. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // 2. access token 유효성 검사
        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }

        // 3. access token으로부터 user id 가져오기 (email x)
        String userId = tokenProviderUtil.getAuthentication(accessToken).getName();

        // 4. logout token의 유효기간은 access token의 남은 기간동안 유지되어야 함
        long remainAccessTokenExpiration = tokenProviderUtil.getRemainExpiration(accessToken);

        // 5. refreshToken table에 존재하는 해당 유저의 refreshToken 정보 삭제
        refreshTokenRepository.deleteByUserId(userId);

        // 6. redis에 존재하는 refreshToken 삭제
        refreshTokenRedisRepository.deleteById(userId);

        // 7. logout token를 redis에 저장 (이후 로그아웃된 유저의 AccessToken으로 접근 방지를 위함)
        logoutAccessTokenRedisRepository.save(
            LogoutAccessToken.of(accessToken, userId, remainAccessTokenExpiration));
    }


    @Transactional
    public TokenDto reissue(HttpServletRequest request, String refreshToken) {

        // 1. Refresh token 검증
        if (!tokenProviderUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // 2. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // 3. Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProviderUtil.getAuthentication(
            accessToken);

        // 4. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져오기
        RefreshToken storedRefreshToken = refreshTokenRepository.findByUserId(
                authentication.getName())
            .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치 여부 검사
        if (!storedRefreshToken.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        TokenDto tokenDto = tokenProviderUtil.generateToken(authentication);

        // 6. 저장소 정보 업데이트
        RefreshToken newRefreshToken = storedRefreshToken.updateValue(tokenDto.getRefreshToken(),
            tokenProviderUtil.getRefreshTokenExpireTime());

        // redis와 gcp에 모두 refresh token을 저장.
        refreshTokenRepository.save(newRefreshToken);
        refreshTokenRedisRepository.save(newRefreshToken.transferRedisToken());

        return tokenDto;
    }

    // refresh Token table에 존재하는 refreshToken 전체 리스트 가져오기
    public List<RefreshToken> getRefreshList() {
        List<RefreshToken> list = refreshTokenRepository.findAll();
        return list;
    }


    @Transactional
    public String getUserIdFromAT(HttpServletRequest request) {
        // 1. Request Header 에서 access token 빼기
        String accessToken = tokenProviderUtil.resolveToken(request);

        // 2. access token 유효성 검사
        if (!tokenProviderUtil.validateToken(accessToken)) {
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }

        // 3. access token으로부터 user id 가져오기 (email x)
        String userId = tokenProviderUtil.getAuthentication(accessToken).getName();

        return userId;

    }

    public void validateUserAccountId(String userAccountId) {
        if (userinfoRepository.existsUserInfoByAccountId(userAccountId)){
            throw new UserException(ErrorCode.ACCOUNT_IS_DUPLICATION);
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

        if(userinfoRepository.existsByEmailAndResType(googleIDToken.getBody().getEmail(),
                GOOGLE.getTypeName())){
            throw new UserException(ErrorCode.GOOGLE_ACCOUNT_IS_DUPLICATION);
        }

    }




}
