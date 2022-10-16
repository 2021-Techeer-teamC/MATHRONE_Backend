package mathrone.backend.service;

import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.*;
import mathrone.backend.controller.dto.OauthDTO.GoogleIDToken;
import mathrone.backend.domain.token.LogoutAccessToken;
import mathrone.backend.domain.token.RefreshToken;
import mathrone.backend.domain.UserInfo;
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
    public UserResponseDto signup(UserSignUpDto userSignUpDto){
        if (userinfoRepository.existsByEmailAndResType(userSignUpDto.getEmail(),"MATHRONE")){ // && type이 MATHRONE인 경우도 같이 검사해야함.. -> 같은 이메일로 여러 sns시스템을 이용할 수 있기 때문
//            if(userinfoRepository.findByEmail(userSignUpDto.getEmail()) -> 이거에서 resType이 MATHRONE인경우)
            throw new RuntimeException("이미 가입된 유저입니다.");
        }
        UserInfo newUser = userSignUpDto.toUser(passwordEncoder,"MATHRONE"); //MATHRONE user로 가입시켜주기
        return UserResponseDto.of(userinfoRepository.save(newUser));
    }

    @Transactional
    public UserResponseDto signupWithGoogle(ResponseEntity<GoogleIDToken> googleIDToken){

        System.out.println("singup1");
        UserSignUpDto userSignUpDto = new UserSignUpDto(googleIDToken.getBody().getEmail(), "googleLogin", googleIDToken.getBody().getEmail()); //id와 email을 email로 채워서 만들기
        System.out.println("singup2");

//        if (userinfoRepository.existsByEmailAndResType(googleIDToken.getBody().getEmail(),"GOOGLE") ){
//            //이메일이 존재함 + 가입방식이 구글임! -> 해당 이메일로 다른 sns가입에 사용했을수도 있기 때문에 둘다 검사해야함
//            System.out.println("가입완료");
////            if(userinfoRepository.findByEmail(googleIDToken.getBody().getEmail())->에서 resType이 GOOGLE인 경우 equals("GOOGLE"))
//            throw new RuntimeException("이미 가입된 유저입니다.");
//        }
        System.out.println("singup3");
        UserInfo newUser = userSignUpDto.toUser(passwordEncoder,"GOOGLE");
        System.out.println("singup4");
        return UserResponseDto.of(userinfoRepository.save(newUser));
    }

    @Transactional
    public TokenDto googleLogin(ResponseEntity<GoogleIDToken> googleIDToken){

        //가입이 안되어 있는 경우
        if (!userinfoRepository.existsByEmailAndResType(googleIDToken.getBody().getEmail(), "GOOGLE")){
            System.out.println("가입이 안되어 있음");
            signupWithGoogle(googleIDToken);
        }

        System.out.println("가입이 되어 있음 ");
        System.out.println(googleIDToken.getBody().getEmail());


        UserRequestDto userRequestDto = new UserRequestDto(googleIDToken.getBody().getEmail(), "googleLogin");

        System.out.println("유저 리퀘스트 생성 성공??");

        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.of();


        System.out.println(authenticationToken.getName());
        System.out.println(authenticationToken.getCredentials());
        System.out.println(authenticationToken.getPrincipal());
        System.out.println(authenticationToken.getAuthorities());
        System.out.println(authenticationToken.getDetails());
        System.out.println(authenticationToken.getClass());
        System.out.println("1??");

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        System.out.println("2??");

        // 3. token 생성
        TokenDto tokenDto = tokenProviderUtil.generateToken(authentication);

        System.out.println("3??");

        // 4. refresh token 생성 ( database 및 redis 저장을 위한 refresh token )
        RefreshToken refreshToken = RefreshToken.builder()
                .userid(authentication.getName())
                .refreshToken(tokenDto.getRefreshToken())
                .expiration(tokenProviderUtil.getRefreshTokenExpireTime())
                .build();

        System.out.println("4??");

        // 5. 토큰 저장 테이블 저장
        refreshTokenRepository.save(refreshToken);


        System.out.println("5??");

        // 6. redis 저장
        refreshTokenRedisRepository.save(refreshToken.transferRedisToken());

        return tokenDto;
    }

    //delete by mail에도 문제가 있다 / resType추가로 변경해야함
    @Transactional
    public void deleteUser(String email) {
        userinfoRepository.deleteByEmail(email);
    }

    public List<UserInfo> allUser() {
        return userinfoRepository.findAll();
    }

    @Transactional
    public TokenDto login(UserRequestDto userRequestDto){
        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = userRequestDto.of();

        System.out.println(authenticationToken.getName());
        System.out.println(authenticationToken.getCredentials());
        System.out.println(authenticationToken.getPrincipal());
        System.out.println(authenticationToken.getAuthorities());
        System.out.println(authenticationToken.getDetails());
        System.out.println(authenticationToken.getClass());


        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);



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
    public void logout(TokenRequestDto tokenRequestDto){
        // 1. access token 유효성 검사
        if(!tokenProviderUtil.validateToken(tokenRequestDto.getAccessToken())){
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }
        String accessToken = tokenRequestDto.getAccessToken();

        // 2. access token으로부터 user id 가져오기 (email x)
        String userId = tokenProviderUtil.getAuthentication(accessToken).getName();

        // 3. logout token의 유효기간은 access token의 남은 기간동안 유지되어야 함
        long remainAccessTokenExpiration = tokenProviderUtil.getRemainExpiration(accessToken);

        // 4. refreshToken table에 존재하는 해당 유저의 refreshToken 정보 삭제
        refreshTokenRepository.deleteByUserId(userId);

        // 5. redis에 존재하는 refreshToken 삭제
        refreshTokenRedisRepository.deleteById(userId);

        // 6. logout token를 redis에 저장 (이후 로그아웃된 유저의 AccessToken으로 접근 방지를 위함)
        logoutAccessTokenRedisRepository.save(LogoutAccessToken.of(accessToken, userId, remainAccessTokenExpiration));
    }


    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // 1. Refresh token 검증
        if (!tokenProviderUtil.validateToken(tokenRequestDto.getRefreshToken())){
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // 2. Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProviderUtil.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져오기
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(authentication.getName())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치 여부 검사
        if (!refreshToken.getRefreshToken().equals(tokenRequestDto.getRefreshToken())){
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        TokenDto tokenDto = tokenProviderUtil.generateToken(authentication);

        // 6. 저장소 정보 업데이트
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken(),
                tokenProviderUtil.getRefreshTokenExpireTime());

        // redis와 gcp에 모두 refresh token을 저장.
        refreshTokenRepository.save(newRefreshToken);
        refreshTokenRedisRepository.save(newRefreshToken.transferRedisToken());

        return tokenDto;
    }

    // refresh Token table에 존재하는 refreshToken 전체 리스트 가져오기
    public List<RefreshToken> getRefreshList(){
        List<RefreshToken> list = refreshTokenRepository.findAll();
        return list;
    }


    @Transactional
    public String getUserIdFromAT(TokenRequestDto tokenRequestDto) {
        // 1. access token 유효성 검사
        if (!tokenProviderUtil.validateToken(tokenRequestDto.getAccessToken())) {
            throw new RuntimeException("Access Token 이 유효하지 않습니다.");
        }
        String accessToken = tokenRequestDto.getAccessToken();

        // 2. access token으로부터 user id 가져오기 (email x)
        String userId = tokenProviderUtil.getAuthentication(accessToken).getName();

        return userId;

    }

}
