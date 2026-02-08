package com.beyond.order.member.dtos;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberLoginDto {
    @Column(length=50,unique=true,nullable=false)
    private String email;
    private String password;
}
