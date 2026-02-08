package com.beyond.order.member.dtos;

import com.beyond.order.member.domain.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberListDto {
    private Long id;
    private String name;
    private String email;

    public static MemberListDto fromEntity(Member m) {
        return MemberListDto.builder()
                .id(m.getId()).name(m.getName()).email(m.getEmail()).build();
    }
}
