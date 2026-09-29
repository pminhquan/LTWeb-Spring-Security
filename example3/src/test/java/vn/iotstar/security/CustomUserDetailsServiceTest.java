package vn.iotstar.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_withValidUser_returnsCustomUserDetails() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .id(10L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_pass")
                .fullName("Test User")
                .enabled(true)
                .role(role)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        CustomUserDetails userDetails = (CustomUserDetails) service.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals(10L, userDetails.getId());
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encoded_pass", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_withDisabledUser_returnsDisabledUserDetails() {
        Role role = Role.builder().id(2L).name("ROLE_ADMIN").build();
        User user = User.builder()
                .id(20L)
                .username("admin_disabled")
                .email("admin@example.com")
                .password("pass")
                .fullName("Admin User")
                .enabled(false)
                .role(role)
                .build();

        when(userRepository.findByUsername("admin_disabled")).thenReturn(Optional.of(user));

        CustomUserDetails userDetails = (CustomUserDetails) service.loadUserByUsername("admin_disabled");

        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        assertEquals("ROLE_ADMIN", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_whenUserNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                service.loadUserByUsername("unknown")
        );
    }
}
