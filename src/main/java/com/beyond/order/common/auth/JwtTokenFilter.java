package com.beyond.order.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtTokenFilter extends GenericFilter {
    @Value("${jwt.secretKey}")
    private String st_secret_key;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException, IOException {
        try {

            HttpServletRequest req = (HttpServletRequest) servletRequest;
//        관례적으로 Bearer라는 문자열을 토큰에 붙여서 전송
            String bearerToken = req.getHeader("Authorization");
            System.out.println(bearerToken);
            if (bearerToken == null) {
//            token이 없는 경우 검증을 할수가 없으므로, filter chain으로 return
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
//            Bearer문자열을 제거한 후에 jwt token만을 검증
            String token = bearerToken.substring(7);

//        token 검증 및 3.claims 추출
            Claims claims = Jwts.parserBuilder()
                    //1.키값넣으면
                    .setSigningKey(st_secret_key)
                    .build()
                    //2.파싱을해서 재암호화해서 비교해서 검증후
                    .parseClaimsJws(token).getBody();
//      claims를 기반으로 authentication 객체생성
//        권한의 경우 다수의 권한을 가질수 있으므로 일반적으로 List로 설계
            List<GrantedAuthority> authorities = new ArrayList<>();
            //권한을 자동으로 체크해주는 어노테이션을 쓰기 위해구리
            authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));

            //        1)principal : email 2)credentials : 토큰 3)authorities : 권한묶음
            Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }catch(Exception e){
            e.printStackTrace();
        }
        //        다시 filterChain으로 돌아가는 로직
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
