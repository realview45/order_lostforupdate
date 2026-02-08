package com.beyond.order.common.auth;

import com.beyond.order.member.domain.Member;
import com.beyond.order.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secretKey}")
    private String st_secret_key;
    @Value("${jwt.secretKeyRt}")
    private String st_secret_key_rt;
    @Value("${jwt.expiration}")
    private int expiration;
    @Value("${jwt.expirationRt}")
    private int expiration_rt;
    private Key secret_key_rt;
    private Key secret_key;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String,String> redisTemplate;
    @Autowired              //Bean에 이름붙여주기
    public JwtTokenProvider(MemberRepository memberRepository, @Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate) {
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    //서버키 디코딩 PostConstruct를 이용해 Value 초기화보다 늦은 순서 보장
    @PostConstruct
    public void init(){
        secret_key = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key),
                SignatureAlgorithm.HS512.getJcaName());
        secret_key_rt = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key),
                SignatureAlgorithm.HS512.getJcaName());
    }
    public String createToken(Member member){
//        sub : abc@naver.com 형태
        Claims claims = Jwts.claims().setSubject(member.getEmail());
//        주된 키값을 제외한 나머지 정보는 put을 사용하여 key:value세팅
        claims.put("role", member.getRole().toString());
//        ex)claims.put("age", member.getAge()); 형태가능

        Date now = new Date();
//        토큰의 구성요소 : 헤더, 페이로드, 시그니처(서명부)
        String token = Jwts.builder()
//                아래 3가지 요소는 페이로드
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+expiration*60*1000L))//30분:30*60초*1000밀리초 : 밀리초형태로 변환
//              secret키를 통해 서명값(signature) 생성
                .signWith(secret_key)
                .compact();
        return token;
    }

    public String createRtToken(Member member) {
//        유효기간이 긴 rt토큰생성
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role", member.getRole().toString());
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+expiration_rt*60*1000L))//30분:30*60초*1000밀리초 : 밀리초형태로 변환
//              secret키를 통해 서명값(signature) 생성
                .signWith(secret_key_rt)
                .compact();
//        rt토큰을 redis에 저장
//        opsForValue : 일반 스트링 자료구조 opsForSet(또는 Zset 또는 List등) 존재
//        redisTemplate.opsForValue().set(member.getEmail(), token);
        redisTemplate.opsForValue().set(member.getEmail(), token, expiration_rt, TimeUnit.MINUTES);//3000분 ttl
        return token;
    }

    public Member validateRt(String refreshToken) {
        //유효기간 만료, 토큰조작시 검증에러날시 에러터뜨림
        Claims claims = null;
        try {
            claims = Jwts.parserBuilder()
                    //1.키값넣으면
                    .setSigningKey(st_secret_key_rt)
                    .build()
                    //2.파싱을해서 재암호화해서 비교해서 검증후
                    .parseClaimsJws(refreshToken).getBody();
        }catch(Exception e){//CommonException으로 간다.
            throw new IllegalArgumentException("잘못된 토큰입니다.");
        }
        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("엔티티가 없습니다."));
//        redis rt와 비교 검증(삭제하려고)
        String redisRt = redisTemplate.opsForValue().get(email);
        if(!redisRt.equals(refreshToken)){
            throw new IllegalArgumentException("잘못된 토큰입니다.");
        }

        return member;
    }
}
