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

@SpringBootApplication
public class Example2Application {

    public static void main(String[] args) {
        SpringApplication.run(Example2Application.class, args);
    }

    @Bean
    CommandLineRunner init(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${DEMO_ROLE:ROLE_USER}") String demoRole,
            @Value("${DEMO_USERNAME:user01}") String demoUsername,
            @Value("${DEMO_USER_EMAIL:user01@gmail.com}") String demoEmail,
            @Value("${DEMO_USER_PASSWORD:123456}") String demoPassword,
            @Value("${DEMO_USER_NAME:Nguyen Huu Trung}") String demoFullName,
            @Value("${DEMO_USER_IMAGES:/images/user.png}") String demoImages
    ) {
        return args -> {
            Role userRole = roleRepository.findByName(demoRole)
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name(demoRole).build()
                    ));

            if (!userRepository.existsByUsername(demoUsername)) {
                User user = User.builder()
                        .username(demoUsername)
                        .email(demoEmail.toLowerCase())
                        .password(passwordEncoder.encode(demoPassword))
                        .fullName(demoFullName)
                        .images(demoImages)
                        .role(userRole)
                        .enabled(true)
                        .build();
                userRepository.save(user);
            }
        };
    }
}
