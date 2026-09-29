package vn.iotstar.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    private UserMapper userMapper;
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        productMapper = Mappers.getMapper(ProductMapper.class);
    }

    @Test
    void userMapper_mapsEntityToDtoAndViceVersa() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .id(100L)
                .username("john_doe")
                .email("john@example.com")
                .password("secret")
                .fullName("John Doe")
                .enabled(true)
                .role(role)
                .build();

        UserDTO dto = userMapper.toDTO(user);
        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals("john_doe", dto.getUsername());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("ROLE_USER", dto.getRoleName());
        assertTrue(dto.isEnabled());

        User entity = userMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals("john_doe", entity.getUsername());
        assertEquals("john@example.com", entity.getEmail());
        assertEquals("John Doe", entity.getFullName());
        assertTrue(entity.isEnabled());
    }

    @Test
    void productMapper_mapsEntityToDtoAndViceVersa() {
        User user = User.builder().id(5L).username("owner").build();
        Product product = Product.builder()
                .id(50L)
                .name("Laptop")
                .description("Gaming Laptop")
                .price(new BigDecimal("1500.00"))
                .imageUrl("https://res.cloudinary.com/demo/image.jpg|public_123")
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        ProductDTO dto = productMapper.toDTO(product);
        assertNotNull(dto);
        assertEquals(50L, dto.getId());
        assertEquals("Laptop", dto.getName());
        assertEquals("Gaming Laptop", dto.getDescription());
        assertEquals(new BigDecimal("1500.00"), dto.getPrice());
        assertEquals(5L, dto.getUserId());
        assertEquals("owner", dto.getUsername());
        assertEquals("https://res.cloudinary.com/demo/image.jpg|public_123", dto.getImageUrl());

        Product entity = productMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals("Laptop", entity.getName());
        assertEquals("Gaming Laptop", entity.getDescription());
        assertEquals(new BigDecimal("1500.00"), entity.getPrice());
    }
}
