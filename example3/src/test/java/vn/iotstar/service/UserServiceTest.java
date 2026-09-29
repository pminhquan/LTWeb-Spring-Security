package vn.iotstar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.impl.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        userService = new UserServiceImpl(userRepository, roleRepository, userMapper, passwordEncoder);
    }

    @Test
    void findById_whenExists_returnsUserDTOWithProductCount() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .id(1L)
                .username("test")
                .email("test@example.com")
                .fullName("Test User")
                .enabled(true)
                .role(role)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.countProductsByUserId(1L)).thenReturn(3L);

        UserDTO result = userService.findById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test", result.getUsername());
        assertEquals(3L, result.getProductCount());
    }

    @Test
    void create_whenUsernameExists_throwsException() {
        UserDTO dto = UserDTO.builder()
                .username("existing")
                .email("new@example.com")
                .fullName("Name")
                .build();

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.create(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_withValidData_encodesDefaultPasswordAndSaves() {
        UserDTO dto = UserDTO.builder()
                .username("newuser")
                .email("newuser@example.com")
                .fullName("New User")
                .roleName("ROLE_USER")
                .enabled(true)
                .build();

        Role role = Role.builder().id(1L).name("ROLE_USER").build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("123456")).thenReturn("hashed123456");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        UserDTO created = userService.create(dto);
        assertNotNull(created);
        assertEquals("newuser", created.getUsername());
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void delete_whenExists_deletesUser() {
        User user = User.builder().id(5L).build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        userService.delete(5L);
        verify(userRepository).delete(user);
    }

    @Test
    void delete_whenUserHasProducts_throwsIllegalStateExceptionAndDoesNotDelete() {
        User user = User.builder().id(5L).build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.countProductsByUserId(5L)).thenReturn(2L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> userService.delete(5L));
        assertTrue(ex.getMessage().contains("sản phẩm"));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void delete_whenUserNotFound_throwsIllegalArgumentException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.delete(999L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void countByRole_returnsCount() {
        when(userRepository.countByRoleName("ROLE_ADMIN")).thenReturn(2L);
        long count = userService.countByRole("ROLE_ADMIN");
        assertEquals(2L, count);
        verify(userRepository).countByRoleName("ROLE_ADMIN");
    }
}
