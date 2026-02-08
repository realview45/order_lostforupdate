package com.beyond.order.member.dtos;

import com.beyond.order.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDetailDto {
    private Long id;
    private String name;
    private String email;

    public static MemberDetailDto fromEntity(Member m) {
        return MemberDetailDto.builder()
                .id(m.getId()).name(m.getName()).email(m.getEmail()).build();
    }
}
