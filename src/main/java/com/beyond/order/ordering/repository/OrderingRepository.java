package com.beyond.order.ordering.repository;

import com.beyond.order.member.domain.Member;
import com.beyond.order.ordering.domain.Ordering;
import com.beyond.order.ordering.dtos.OrderingListDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    List<Ordering> findAllByMember(Member member);
}
