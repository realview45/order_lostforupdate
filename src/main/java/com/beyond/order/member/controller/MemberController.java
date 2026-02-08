package com.beyond.order.member.controller;

import com.beyond.order.common.auth.JwtTokenProvider;
import com.beyond.order.member.domain.Member;
import com.beyond.order.member.dtos.*;
import com.beyond.order.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    //회원가입
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid MemberCreateDto dto){
        Long id = memberService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }
    //유저, 어드민로그인
    @PostMapping("/doLogin")
    public TokenDto login(@RequestBody @Valid MemberLoginDto dto){
        Member member = memberService.login(dto);
        String accessToken = jwtTokenProvider.createToken(member);
//        refresh생성및저장
        String refreshToken = jwtTokenProvider.createRtToken(member);
        return TokenDto.builder()
                .refresh_token(refreshToken)
                .access_token(accessToken)
                .build();
    }
    @PostMapping("/refresh-at")
    public TokenDto refreshAt(@RequestBody RefreshTokenDto dto){
        //rt검증(1.토큰 자체검증(유효기간확인) 2.redis조회 검증)
        Member member = jwtTokenProvider.validateRt(dto.getRefreshToken());
//        at신규생성 후 return
        String accessToken = jwtTokenProvider.createToken(member);
        return TokenDto.builder()
                .access_token(accessToken)
                .refresh_token(null).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public List<MemberListDto> findAll(){
        return memberService.findAll();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/myinfo")
    public MemberDetailDto myinfo(@AuthenticationPrincipal String principal){
        return memberService.myinfo(principal);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/detail/{id}")
    public MemberDetailDto findById(@PathVariable Long id){
        return memberService.findById(id);
    }
}
