package vn.iotstar.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserMapper userMapper;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        service = new CustomUserDetailsService(userRepository, userMapper);
    }

    @Test
    void loadUserByUsername_withValidEnabledUser_returnsCustomUserDetails() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .id(10L)
                .email("test@example.com")
                .password("encoded_pass")
                .fullName("Test User")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .role(role)
                .build();

        when(userRepository.findByEmailWithRole("test@example.com")).thenReturn(Optional.of(user));

        CustomUserDetails userDetails = (CustomUserDetails) service.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encoded_pass", userDetails.getPassword());
        assertEquals("Test User", userDetails.getFullName());
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());

        UserDTO dto = userDetails.getUserDto();
        assertNotNull(dto);
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("ROLE_USER", dto.getRoleName());
    }

    @Test
    void loadUserByUsername_withDisabledUser_returnsUserDetailsWithDisabledState() {
        Role role = Role.builder().id(2L).name("USER").build();
        User user = User.builder()
                .id(20L)
                .email("disabled@example.com")
                .password("pass")
                .fullName("Disabled User")
                .enabled(false)
                .role(role)
                .build();

        when(userRepository.findByEmailWithRole("disabled@example.com")).thenReturn(Optional.of(user));

        CustomUserDetails userDetails = (CustomUserDetails) service.loadUserByUsername("disabled@example.com");

        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_whenUserNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmailWithRole("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                service.loadUserByUsername("nonexistent@example.com")
        );
    }
}
