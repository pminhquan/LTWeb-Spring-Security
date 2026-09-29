package vn.iotstar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.impl.ProductServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    private ProductMapper productMapper;
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productMapper = Mappers.getMapper(ProductMapper.class);
        productService = new ProductServiceImpl(productRepository, userRepository, productMapper, cloudinaryService);
    }

    @Test
    void create_withImage_uploadsToCloudinaryAndSavesProduct() {
        ProductDTO dto = ProductDTO.builder()
                .name("Keyboard")
                .description("Mechanical Keyboard")
                .price(new BigDecimal("99.99"))
                .userId(1L)
                .build();

        User user = User.builder().id(1L).username("seller").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        MockMultipartFile image = new MockMultipartFile(
                "image", "keyboard.jpg", "image/jpeg", "image_bytes".getBytes()
        );
        when(cloudinaryService.upload(image)).thenReturn(
                new CloudinaryUploadResult("https://cloudinary.com/img.jpg", "pub_123")
        );

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductDTO result = productService.create(dto, image);
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Keyboard", result.getName());
        assertEquals("https://cloudinary.com/img.jpg|pub_123", result.getImageUrl());
        verify(cloudinaryService).upload(image);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void delete_withCloudinaryImage_deletesCloudinaryAndProduct() {
        Product product = Product.builder()
                .id(10L)
                .name("Old Product")
                .imageUrl("https://cloudinary.com/img.jpg|pub_123")
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        productService.delete(10L);

        verify(cloudinaryService).delete("pub_123");
        verify(productRepository).delete(product);
    }

    @Test
    void update_withNewImage_whenCloudinaryDeleteFails_stillUpdatesProductSuccessfully() {
        Product product = Product.builder()
                .id(10L)
                .name("Old Product")
                .imageUrl("https://cloudinary.com/img.jpg|old_pub_123")
                .build();

        ProductDTO updateDTO = ProductDTO.builder()
                .id(10L)
                .name("New Name")
                .description("New Desc")
                .price(new BigDecimal("150.00"))
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        doThrow(new RuntimeException("Cloudinary destroy failed"))
                .when(cloudinaryService).delete("old_pub_123");

        MockMultipartFile newImage = new MockMultipartFile(
                "image", "new.jpg", "image/jpeg", "new_bytes".getBytes()
        );
        when(cloudinaryService.upload(newImage)).thenReturn(
                new CloudinaryUploadResult("https://cloudinary.com/new.jpg", "new_pub_456")
        );
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDTO result = productService.update(10L, updateDTO, newImage);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("https://cloudinary.com/new.jpg|new_pub_456", result.getImageUrl());

        org.mockito.InOrder inOrder = inOrder(cloudinaryService, productRepository);
        inOrder.verify(cloudinaryService).upload(newImage);
        inOrder.verify(productRepository).save(product);
        inOrder.verify(cloudinaryService).delete("old_pub_123");
    }

    @Test
    void update_withNewImage_whenUploadThrows_doesNotDeleteOldImageAndDoesNotSaveProduct() {
        Product product = Product.builder()
                .id(10L)
                .name("Old Product")
                .description("Old Desc")
                .price(new BigDecimal("100.00"))
                .imageUrl("https://cloudinary.com/img.jpg|old_pub_123")
                .build();

        ProductDTO updateDTO = ProductDTO.builder()
                .id(10L)
                .name("New Name")
                .description("New Desc")
                .price(new BigDecimal("150.00"))
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        MockMultipartFile newImage = new MockMultipartFile(
                "image", "new.jpg", "image/jpeg", "new_bytes".getBytes()
        );
        when(cloudinaryService.upload(newImage)).thenThrow(new IllegalArgumentException("Upload failed"));

        assertThrows(IllegalArgumentException.class, () -> productService.update(10L, updateDTO, newImage));

        assertEquals("https://cloudinary.com/img.jpg|old_pub_123", product.getImageUrl());
        verify(cloudinaryService).upload(newImage);
        verify(cloudinaryService, never()).delete(anyString());
        verify(productRepository, never()).save(any());
    }

    @Test
    void update_withoutNewImage_updatesFieldsAndLeavesImageUnchangedWithoutCallingCloudinary() {
        Product product = Product.builder()
                .id(10L)
                .name("Old Product")
                .description("Old Desc")
                .price(new BigDecimal("100.00"))
                .imageUrl("https://cloudinary.com/img.jpg|old_pub_123")
                .build();

        ProductDTO updateDTO = ProductDTO.builder()
                .id(10L)
                .name("Updated Name")
                .description("Updated Desc")
                .price(new BigDecimal("200.00"))
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDTO result = productService.update(10L, updateDTO, null);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("https://cloudinary.com/img.jpg|old_pub_123", result.getImageUrl());
        verify(productRepository).save(product);
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    void delete_whenCloudinaryDeleteFails_stillDeletesProductSuccessfully() {
        Product product = Product.builder()
                .id(10L)
                .name("Old Product")
                .imageUrl("https://cloudinary.com/img.jpg|seeded_sample")
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        doThrow(new RuntimeException("Cloudinary destroy failed"))
                .when(cloudinaryService).delete("seeded_sample");

        assertDoesNotThrow(() -> productService.delete(10L));

        verify(cloudinaryService).delete("seeded_sample");
        verify(productRepository).delete(product);
    }
}
