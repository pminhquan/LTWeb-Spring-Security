package vn.iotstar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

import java.time.LocalDateTime;

@SpringBootApplication
public class Example1Application {

    public static void main(String[] args) {
        SpringApplication.run(Example1Application.class, args);
    }

    @Bean
    CommandLineRunner initDemoData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${DEMO_ROLE:ROLE_USER}") String demoRole,
            @Value("${DEMO_USER_EMAIL:user01@gmail.com}") String demoEmail,
            @Value("${DEMO_USER_PASSWORD:123456}") String demoPassword,
            @Value("${DEMO_USER_NAME:Nguyen Van A}") String demoName
    ) {
        return args -> {
            Role role = roleRepository.findByName(demoRole)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(demoRole).build()));

            if (!userRepository.existsByEmailIgnoreCase(demoEmail)) {
                User demoUser = User.builder()
                        .email(demoEmail.toLowerCase())
                        .password(passwordEncoder.encode(demoPassword))
                        .fullName(demoName)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .role(role)
                        .build();
                userRepository.save(demoUser);
            }
        };
    }
}
