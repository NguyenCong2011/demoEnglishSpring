package com.example.english.demo.configuration;

//class này tự tạo tài khỏan và mật kẩu admin

import com.example.english.demo.enums.Roles;
import com.example.english.demo.repository.UserRepository;
import com.example.english.demo.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;

    //bean này là khi mà ứng dụng start lên
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository){//cần luu nên cần repo
        return args ->{
//            vì có thể start ứng dụng nhiều lần nên cần check
            if(userRepository.findByUsername("admin").isEmpty()){
                var roles=new HashSet<String>();
                roles.add(Roles.ADMIN.name());

                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .active(true)
//                        .roles(roles)
                        .build();
                userRepository.save(user);
                log.warn("default admin has been created with default pasword please change it!");
            }
        };
    }
}
