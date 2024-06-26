package mathrone.backend.service;

import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.*;
import mathrone.backend.controller.dto.OauthDTO.GoogleIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoIDToken;
import mathrone.backend.controller.dto.OauthDTO.Kakao.KakaoTokenResponseDTO;
import mathrone.backend.domain.token.KakaoRefreshTokenRedis;
import mathrone.backend.domain.token.LogoutAccessToken;
import mathrone.backend.domain.token.RefreshToken;
import mathrone.backend.domain.UserInfo;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.error.exception.UserException;
import mathrone.backend.repository.UserInfoRepository;
import mathrone.backend.repository.tokenRepository.KakaoRefreshTokenRedisRepository;
import mathrone.backend.repository.tokenRepository.LogoutAccessTokenRedisRepository;
import mathrone.backend.util.TokenProviderUtil;
import mathrone.backend.repository.tokenRepository.RefreshTokenRedisRepository;
import mathrone.backend.repository.tokenRepository.RefreshTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static mathrone.backend.domain.enums.UserResType.*;

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

    @Transactional
    public UserResponseDto signup(UserSignUpDto userSignUpDto) {
        // user account ID가 존재하는지 검사
        validateUserAccountId(userSignUpDto.getAccountId());

        UserInfo newUser = userSignUpDto.toUser(passwordEncoder,
                MATHRONE.getTypeName()); //MATHRONE user로 가입시켜주기
        return UserResponseDto.of(userinfoRepository.save(newUser));
    }

    @Transactional
    public UserResponseDto signupWithGoogle(ResponseEntity<GoogleIDToken> googleIDToken) {

        UserSignUpDto userSignUpDto = new UserSignUpDto(googleIDToken.getBody().getEmail(),
                "googleLogin", googleIDToken.getBody().getEmail()); //id와 email을 email로 채워서 만들기
        UserInfo newUser = userSignUpDto.toUser(passwordEncoder, GOOGLE.getTypeName());

        return UserResponseDto.of(userinfoRepository.save(newUser));
    }

    @Transactional
    public UserResponseDto signupWithKakao(ResponseEntity<KakaoIDToken> kakaoIDToken) {

        UserSignUpDto userSignUpDto = new UserSignUpDto(kakaoIDToken.getBody().getEmail(),
                "kakaoLogin", kakaoIDToken.getBody().getEmail()); //id와 email을 email로 채워서 만들기
        UserInfo newUser = userSignUpDto.toUser(passwordEncoder, KAKAO.getTypeName());

        return UserResponseDto.of(userinfoRepository.save(newUser));
    }

    @Transactional
    public TokenDto googleLogin(ResponseEntity<GoogleIDToken> googleIDToken) {

        //가입이 안되어 있는 경우
        if (!userinfoRepository.existsByEmailAndResType(googleIDToken.getBody().getEmail(),
                GOOGLE.getTypeName())) {
            signupWithGoogle(googleIDToken);
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
    public TokenDto kakaoLogin(ResponseEntity<KakaoTokenResponseDTO> kakaoTokenResponseDto, ResponseEntity<KakaoIDToken> kakaoIDToken) {

        //가입이 안되어 있는 경우 -> 자동가입 but accountID가 미설정되었음을 알려야함
        if (!userinfoRepository.existsByEmailAndResType(kakaoIDToken.getBody().getEmail(),
                KAKAO.getTypeName())) {
            signupWithKakao(kakaoIDToken); //카카오계정 으로 회원가입 진행
        }


        UserRequestDto userRequestDto = new UserRequestDto(kakaoIDToken.getBody().getEmail(),
                "kakaoLogin");


        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.of();


        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);

        // 3. token 생성
        TokenDto tokenDto = tokenProviderUtil.generateTokenWithSns(authentication, kakaoTokenResponseDto);



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
    public void saveKakaoRefreshToken(ResponseEntity<KakaoTokenResponseDTO> kakaoTokenResponseDto, ResponseEntity<KakaoIDToken> kakaoIdToken){


        //user id알아내기
        Optional<UserInfo> user = userinfoRepository.findByAccountId(kakaoIdToken.getBody().getEmail());


        int userId = user.get().getUserId();


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
    public TokenDto login(UserRequestDto userRequestDto) throws Exception {

        //존재하는 아이디인지
        invalidateUserAccountId(userRequestDto.getAccountId());

        TokenDto tokenDto = null;

        try {

            // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
            UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.of();

            // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
            //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
            Authentication authentication = authenticationManagerBuilder.getObject()
                    .authenticate(authenticationToken);

            // 3. token 생성
            tokenDto = tokenProviderUtil.generateToken(authentication);


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
            
        } catch (Exception e) {
            incorrectPassword(); //에러발생
        }
        
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

    public void invalidateUserAccountId(String userAccountId){
        if (!userinfoRepository.existsUserInfoByAccountId(userAccountId)){
            throw new UserException(ErrorCode.ACCOUNT_NOT_EXIST);
        }
    }


    public void incorrectPassword(){
        throw new UserException(ErrorCode.PASSWORD_NOT_CORRECT);
    }




}
