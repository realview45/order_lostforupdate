package com.beyond.order.product.dtos;

import com.beyond.order.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDto {
    private Long id;
    private String name;
    private String category;
    private Long price;
    private Long stockQuantity;
    private String image_path;

    public static ProductDetailDto fromEntity(Product p) {
        return ProductDetailDto.builder()
                .id(p.getId())
                .name(p.getName())
                .category(p.getCategory())
                .price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .image_path(p.getImagePath())
                .build();
    }
}
