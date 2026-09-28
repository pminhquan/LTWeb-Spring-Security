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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService service;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(userRepository);
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void loadUserByUsername_withValidUsername_returnsCustomUserDetails() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .id(10L)
                .username("user01")
                .email("user01@gmail.com")
                .password("encoded_pass")
                .fullName("Nguyen Huu Trung")
                .images("/images/user.png")
                .enabled(true)
                .role(role)
                .build();

        when(userRepository.findByUsernameOrEmail("user01", "user01")).thenReturn(Optional.of(user));

        CustomUserDetails userDetails = (CustomUserDetails) service.loadUserByUsername("user01");

        assertNotNull(userDetails);
        assertEquals("user01", userDetails.getUsername());
        assertEquals("user01@gmail.com", userDetails.getEmail());
        assertEquals("encoded_pass", userDetails.getPassword());
        assertEquals("Nguyen Huu Trung", userDetails.getFullName());
        assertEquals("/images/user.png", userDetails.getImages());
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_withValidEmail_returnsCustomUserDetails() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .id(10L)
                .username("user01")
                .email("user01@gmail.com")
                .password("encoded_pass")
                .fullName("Nguyen Huu Trung")
                .images("/images/user.png")
                .enabled(true)
                .role(role)
                .build();

        when(userRepository.findByUsernameOrEmail("user01@gmail.com", "user01@gmail.com")).thenReturn(Optional.of(user));

        CustomUserDetails userDetails = (CustomUserDetails) service.loadUserByUsername("user01@gmail.com");

        assertNotNull(userDetails);
        assertEquals("user01", userDetails.getUsername());
        assertEquals("user01@gmail.com", userDetails.getEmail());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_withDisabledUser_returnsDisabledUserDetails() {
        Role role = Role.builder().id(2L).name("USER").build();
        User user = User.builder()
                .id(20L)
                .username("disabled_user")
                .email("disabled@example.com")
                .password("pass")
                .fullName("Disabled User")
                .images(null)
                .enabled(false)
                .role(role)
                .build();

        when(userRepository.findByUsernameOrEmail("disabled_user", "disabled_user")).thenReturn(Optional.of(user));

        CustomUserDetails userDetails = (CustomUserDetails) service.loadUserByUsername("disabled_user");

        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_whenUserNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsernameOrEmail("unknown", "unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                service.loadUserByUsername("unknown")
        );
    }

    @Test
    void userMapper_mapsUserToUserDTO() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder()
                .id(10L)
                .username("user01")
                .email("user01@gmail.com")
                .password("secret")
                .fullName("Nguyen Huu Trung")
                .images("/images/user.png")
                .enabled(true)
                .role(role)
                .build();

        UserDTO dto = userMapper.toDTO(user);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("user01", dto.getUsername());
        assertEquals("user01@gmail.com", dto.getEmail());
        assertEquals("Nguyen Huu Trung", dto.getFullName());
        assertEquals("/images/user.png", dto.getImages());
        assertEquals("ROLE_USER", dto.getRoleName());
        assertTrue(dto.isEnabled());
    }
}
