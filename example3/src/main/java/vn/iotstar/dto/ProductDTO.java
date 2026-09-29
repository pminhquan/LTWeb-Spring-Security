package vn.iotstar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 500)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Giá không được để trống")
    @PositiveOrZero(message = "Giá phải >= 0")
    private BigDecimal price;

    private String imageUrl;
    private Long userId;
    private String username;
    private MultipartFile image;
}
