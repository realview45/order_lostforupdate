package com.beyond.order.common.auth;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity //PreAuthorize어노테이션을 사용하기 위한 설정
public class SecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private final JwtAuthenticationHandler jwtAuthenticationHandler;
    @Autowired
    public SecurityConfig(JwtTokenFilter jwtTokenFilter, JwtAuthenticationHandler jwtAuthenticationHandler) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.jwtAuthenticationHandler = jwtAuthenticationHandler;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
//                a->AbstractHttpConfigurer(a)구리
//                csrf공격(일반적으로 쿠키를 활용한 (세션방식에서 활용파일구리) 공격)에 대한 방어 비활성화
                .csrf(AbstractHttpConfigurer::disable)
//                http basic은 email/pw를 인코딩하여 인증(전송)하는 간단한 인증방식. 비활성화.
                .httpBasic(AbstractHttpConfigurer::disable)
//                세션로그인방식 비활성화
                .sessionManagement(a->a.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //token을 검증하고, Authentication객체 생성
//                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e->e.authenticationEntryPoint(jwtAuthenticationHandler))
//                지정한 특정url을 제외한 모든 요청에 대해서 authenticated(인증처리)하겠다라는 의미
                .authorizeHttpRequests(a->a.requestMatchers(
                        "/member/doLogin", "/member/create", "/product/list", "/member/refresh-at").permitAll().anyRequest().authenticated())
                .build();
    }
    @Bean
    public PasswordEncoder pwEncoder(){
//        들어가서 Component 붙이고싶은데 안되어서 Bean사용 메서드를 통해 싱글톤객체만들구리
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
