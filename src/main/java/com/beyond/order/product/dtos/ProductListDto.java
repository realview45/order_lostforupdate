package com.beyond.order.product.dtos;

import com.beyond.order.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductListDto {
    private Long id;
    private String name;
    private Long price;
    private String category;
    private Long stockQuantity;
    private String image_path;

    public static ProductListDto fromEntity(Product p) {
        return ProductListDto.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .category(p.getCategory())
                .stockQuantity(p.getStockQuantity())
                .image_path(p.getImagePath()).build();
    }
}
