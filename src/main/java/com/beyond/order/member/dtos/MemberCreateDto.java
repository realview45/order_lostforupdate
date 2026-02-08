package com.beyond.order.member.dtos;

import com.beyond.order.member.domain.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberCreateDto {
    private String name;
    @NotBlank
    private String email;
    @NotBlank
    private String password;

    public Member toEntity(String ecodedPassword) {
        return Member.builder().name(name).email(email).password(ecodedPassword).build();
    }
}
