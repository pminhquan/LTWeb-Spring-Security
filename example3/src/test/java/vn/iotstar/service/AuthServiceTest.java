package vn.iotstar.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.impl.AuthServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, roleRepository, passwordEncoder, otpService);
    }

    @Test
    void register_withValidData_createsDisabledUserAndSendsOtp() {
        RegisterDTO dto = RegisterDTO.builder()
                .username("newuser")
                .email("new@example.com")
                .fullName("New User")
                .password("secret123")
                .confirmPassword("secret123")
                .build();

        Role role = Role.builder().id(1L).name("ROLE_USER").build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed_secret");

        authService.register(dto);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newuser")
                        && user.getEmail().equals("new@example.com")
                        && !user.isEnabled()
                        && user.getPassword().equals("hashed_secret")
        ));
        verify(otpService).sendRegisterOtp("new@example.com");
    }

    @Test
    void register_whenPasswordMismatch_throwsException() {
        RegisterDTO dto = RegisterDTO.builder()
                .username("newuser")
                .email("new@example.com")
                .fullName("New User")
                .password("secret123")
                .confirmPassword("different")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
        verify(otpService, never()).sendRegisterOtp(any());
    }

    @Test
    void verifyRegister_whenOtpValid_enablesUser() {
        User user = User.builder()
                .id(1L)
                .email("verify@example.com")
                .enabled(false)
                .build();

        when(otpService.verifyRegisterOtp("verify@example.com", "123456")).thenReturn(true);
        when(userRepository.findByEmail("verify@example.com")).thenReturn(Optional.of(user));

        boolean result = authService.verifyRegister("verify@example.com", "123456");

        assertTrue(result);
        assertTrue(user.isEnabled());
    }

    @Test
    void verifyRegister_whenOtpInvalid_returnsFalse() {
        when(otpService.verifyRegisterOtp("verify@example.com", "000000")).thenReturn(false);

        boolean result = authService.verifyRegister("verify@example.com", "000000");

        assertFalse(result);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void resetPassword_updatesPassword() {
        User user = User.builder()
                .id(2L)
                .email("reset@example.com")
                .password("old_pass")
                .build();

        when(userRepository.findByEmail("reset@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new_pass")).thenReturn("hashed_new_pass");

        authService.resetPassword("reset@example.com", "new_pass");

        assertEquals("hashed_new_pass", user.getPassword());
    }
}
