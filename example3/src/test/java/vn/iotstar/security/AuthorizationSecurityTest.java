package vn.iotstar.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import vn.iotstar.config.SecurityConfig;
import vn.iotstar.controller.ProductController;
import vn.iotstar.controller.UserController;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationSecurityTest {

    @Test
    @DisplayName("SecurityConfig enables method security and web security")
    void securityConfig_hasRequiredSecurityAnnotations() {
        assertTrue(SecurityConfig.class.isAnnotationPresent(EnableWebSecurity.class),
                "SecurityConfig must be annotated with @EnableWebSecurity");
        assertTrue(SecurityConfig.class.isAnnotationPresent(EnableMethodSecurity.class),
                "SecurityConfig must be annotated with @EnableMethodSecurity for method-level guards");
    }

    @Test
    @DisplayName("UserController is restricted to ROLE_ADMIN at class level")
    void userController_isProtectedWithRoleAdmin() {
        assertTrue(UserController.class.isAnnotationPresent(PreAuthorize.class),
                "UserController must have class-level @PreAuthorize");
        PreAuthorize preAuth = UserController.class.getAnnotation(PreAuthorize.class);
        assertEquals("hasRole('ADMIN')", preAuth.value(),
                "UserController must require ROLE_ADMIN");
    }

    @Test
    @DisplayName("ProductController mutation endpoints are restricted to ROLE_ADMIN")
    void productController_mutationMethods_requireRoleAdmin() {
        Method[] methods = ProductController.class.getDeclaredMethods();

        String[] adminRestrictedMethodNames = {"create", "edit", "delete"};

        for (String methodName : adminRestrictedMethodNames) {
            boolean found = false;
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    found = true;
                    assertTrue(method.isAnnotationPresent(PreAuthorize.class),
                            "Method " + methodName + " must be annotated with @PreAuthorize");
                    assertEquals("hasRole('ADMIN')", method.getAnnotation(PreAuthorize.class).value(),
                            "Method " + methodName + " must require ROLE_ADMIN");
                }
            }
            assertTrue(found, "Method " + methodName + " must exist on ProductController");
        }
    }

    @Test
    @DisplayName("ProductController list method is not restricted to ROLE_ADMIN")
    void productController_listMethod_allowsNonAdmin() throws NoSuchMethodException {
        Method listMethod = ProductController.class.getMethod("list", String.class, int.class, int.class, org.springframework.ui.Model.class);
        PreAuthorize preAuth = listMethod.getAnnotation(PreAuthorize.class);
        if (preAuth != null) {
            assertNotEquals("hasRole('ADMIN')", preAuth.value(),
                    "Product list method should be accessible to authenticated ROLE_USER");
        }
    }

    @Test
    @DisplayName("CustomUserDetails correctly resolves ROLE_USER and ROLE_ADMIN authorities")
    void userDetails_mapsRolesCorrectly() {
        User user = User.builder()
                .id(1L)
                .username("user01")
                .password("hash")
                .enabled(true)
                .role(Role.builder().id(1L).name("ROLE_USER").build())
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertFalse(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

        User admin = User.builder()
                .id(2L)
                .username("admin")
                .password("hash")
                .enabled(true)
                .role(Role.builder().id(2L).name("ROLE_ADMIN").build())
                .build();
        CustomUserDetails adminDetails = new CustomUserDetails(admin);
        assertTrue(adminDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
