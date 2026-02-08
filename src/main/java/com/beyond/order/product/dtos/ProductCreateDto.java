package com.beyond.order.product.dtos;

import com.beyond.order.member.domain.Member;
import com.beyond.order.product.domain.Product;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateDto {
    @NotBlank
    private String name;
    @NotBlank
    private Long price;
    private String category;
    @NotBlank
    private Long stockQuantity;
    private MultipartFile productImage;

    public Product toEntity(Member member) {
        return Product.builder()
                .name(name).price(price).category(category).stockQuantity(stockQuantity).member(member)
                .imagePath("path").build();
    }
}
