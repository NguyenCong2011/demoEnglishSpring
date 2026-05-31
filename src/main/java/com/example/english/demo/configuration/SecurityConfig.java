package com.example.english.demo.configuration;

//file này chứa các bean để inject
import com.example.english.demo.enums.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    String[] publicRoutes = {"payment-callback", "/payment/vnpay", "/course/detail/{id}", "/course/list", "/auth/refesh-token", "/ws/**", "/topic/**", "/app/invite", "/app/accept-invite", "/app/reject-invite", "/topic/invite/{userId}", "/user/search-users", "/user/toeic-detail/{examId}", "/competition", "/user/friends", "/user/submit-toeic-exam", "/user/show-toeic-question/{examId}", "/admin/login", "/user/toeic", "/audio/**", "/user/confirm-account/**", "/images/**", "/user/create", "/auth/login", "/api/login", "/toeic", "/online-tests", "/", "/user", "/auth/verify-token", "/auth/login", "/auth/logout", "/auth/refesh-token", "/toeic-exam/create", "/toeic-exam/update/{examId}"};
    private CustomJwtDecoder customJwtDecoder;

    public SecurityConfig(CustomJwtDecoder customJwtDecoder) {
        this.customJwtDecoder = customJwtDecoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtCookieFilter jwtCookieFilter) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicRoutes).permitAll()
                        .requestMatchers("/admin/**").hasRole(Roles.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable);

        //cái này để lấy token từ cookies nhé xác định admin
        http.addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class);

        // Cấu hình lại OAuth2 Resource Server với CustomJwtDecoder
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        return http.build();
    }

    //override Bean Granted là giả dụ và giả dụ xong phải authen và nó bắt buộc phải có là admin thì mới được truy cập kể cả có token hợp le
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix(""); //  KHÔNG thêm "ROLE_" nữa vì đã có sẵn trong token
        converter.setAuthoritiesClaimName("scope"); // đọc quyền từ claim "scope"

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter111111(){

        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter=new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter=new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter1111111(){

        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter=new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter=new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }


    //bean này dùng cho bên trên
//    @Bean
//    JwtDecoder jwtDecoder(){
//        SecretKeySpec secretKeySpec=new SecretKeySpec(Signer_Key.getBytes(),"HS512");
//        return NimbusJwtDecoder
//                .withSecretKey(secretKeySpec)
//                .macAlgorithm(MacAlgorithm.HS512)
//                .build();
//    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

        //bean này dùng cho bên trên
//    @Bean
//    JwtDecoder jwtDecoder(){
//        SecretKeySpec secretKeySpec=new SecretKeySpec(Signer_Key.getBytes(),"HS512");
//        return NimbusJwtDecoder
//                .withSecretKey(secretKeySpec)
//                .macAlgorithm(MacAlgorithm.HS512)
//                .build();
//    }
}
