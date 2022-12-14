package mathrone.backend.config.security;

import lombok.RequiredArgsConstructor;
import mathrone.backend.config.jwt.JwtAccessDeniedHandler;
import mathrone.backend.config.jwt.JwtAuthenticationEntryPoint;
import mathrone.backend.config.jwt.JwtSecurityConfig;
import mathrone.backend.repository.tokenRepository.LogoutAccessTokenRedisRepository;
//import mathrone.backend.service.GoogleOAuth2UserService;
import mathrone.backend.util.TokenProviderUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenProviderUtil tokenProviderUtil;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;
//    private final GoogleOAuth2UserService oAuth2UserService;

    private static final String[] AUTH_LIST = {
        "/v3/api-docs",
        "/swagger-resources/**",
        "/swagger-ui/**",
        "/webjars/**",
        "/book/**",
        "/main/**",
        "/user/**",
        "/problem/**",
        "/answer/**",
        "/rank/**",
        "/oauth/**"
    };

    @Bean // user password μνΈν
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web){
        // ACL(access control list)μ url μΆκ°
        web.ignoring().antMatchers("/book/**"); // λ¬Έμ  μ‘°ν testλ₯Ό μν΄ μΆκ°
        web.ignoring().antMatchers("/swagger-ui/**",
            "/swagger-resources/**", "/v3/api-docs/**"); // debugμ swagger μ¬μ©μ μν΄ μΆκ°
    }

    protected void configure(HttpSecurity http) throws Exception {

        // csrfλ‘ μΈν forbidden error λ°©μ§
        http.csrf().disable()
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            // μνλ¦¬ν°λ κΈ°λ³Έμ μΌλ‘ μΈμμ ν΅ν΄ μ μ  μ λ³΄λ€μ μ μ₯
            // νμ§λ§ redisλ₯Ό μ¬μ©νλ―λ‘ μΈμμ μ¬μ©νμ§ μμ μΈμ μ€μ μ Stateless λ‘ μ€μ 
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            // μΈμ¦μμ΄ μ κ·Ό κ°λ₯ν urlμ μ€μ , AUTH_LISTμ μΆκ°νλ©΄ μ¬μ© κ°λ₯.
            .and()
            .authorizeRequests()
            .antMatchers("/user/**").permitAll()
            .antMatchers(AUTH_LIST).permitAll()
            .anyRequest().authenticated()

            .and()
            .apply(new JwtSecurityConfig(tokenProviderUtil, logoutAccessTokenRedisRepository));

        //google login
//                .and()
//                .oauth2Login()
//                .userInfoEndpoint()
//                .userService(oAuth2UserService);
    }
}
