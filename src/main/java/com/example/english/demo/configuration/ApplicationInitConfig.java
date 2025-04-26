package com.example.english.demo.configuration;

import com.example.english.demo.entity.Role;
import com.example.english.demo.entity.User;
import com.example.english.demo.enums.Roles;
import com.example.english.demo.repository.RoleRepository;
import com.example.english.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner applicationRunner(UserRepository userRepository,
                                               RoleRepository roleRepository) {
        return args -> {
            // ✅ Tạo Role ADMIN nếu chưa có
            Role adminRole = roleRepository.findById(Roles.ADMIN.name())
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .name(Roles.ADMIN.name())
                                .description("Administrator role")
                                .build();
                        return roleRepository.save(role);
                    });

            // ✅ Tạo user admin nếu chưa có
            if (userRepository.findByUsername("admin").isEmpty()) {
                User adminUser = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .active(true)
                        .roles(Set.of(adminRole))
                        .build();
                userRepository.save(adminUser);

                log.warn("✅ Default admin user created with username 'admin' and password 'admin'. Please change the password!");
            }
        };
    }
}
