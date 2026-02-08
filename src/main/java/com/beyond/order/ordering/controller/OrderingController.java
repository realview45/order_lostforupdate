package com.beyond.order.ordering.controller;

import com.beyond.order.member.dtos.MemberCreateDto;
import com.beyond.order.ordering.domain.Ordering;
import com.beyond.order.ordering.dtos.OrderingCreateDto;
import com.beyond.order.ordering.dtos.OrderingListDto;
import com.beyond.order.ordering.service.OrderingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;
    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderingCreateDto> dtoList){
        Long id = orderingService.create(dtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> findAll(){
        List<OrderingListDto> dtoList = orderingService.findAll();
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoList);
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/myorders")
    public ResponseEntity<?> myorders(@AuthenticationPrincipal String email){
        List<OrderingListDto> dtoList = orderingService.myorders(email);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoList);
    }
}