package com.beyond.order.member.domain;

import com.beyond.order.common.domain.BaseTimeEntity;
import com.beyond.order.member.dtos.MemberDetailDto;
import com.beyond.order.ordering.domain.Ordering;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length=50,unique=true,nullable=false)
    private String email;
    @Column(nullable=false)
    private String password;
    private String name;

    @Builder.Default
    private Role role=Role.USER;


    @Builder.Default
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Ordering> orderingList = new ArrayList<>();

}
