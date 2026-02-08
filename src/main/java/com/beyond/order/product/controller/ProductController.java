package com.beyond.order.product.controller;

import com.beyond.order.product.dtos.ProductCreateDto;
import com.beyond.order.product.dtos.ProductDetailDto;
import com.beyond.order.product.dtos.ProductListDto;
import com.beyond.order.product.dtos.ProductSearchDto;
import com.beyond.order.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    //상품등록
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> create(@ModelAttribute ProductCreateDto dto){
        Long id = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    //상품상세조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/detail/{id}")
    public ProductDetailDto findById(@PathVariable Long id){
        return productService.findById(id);
    }
    //상품목록조회(검색)
    //@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
    @GetMapping("/list")
    public Page<ProductListDto> findAll(
            Pageable pageable,
            @ModelAttribute ProductSearchDto searchDto){
        return productService.findAll(pageable, searchDto);
    }
}
