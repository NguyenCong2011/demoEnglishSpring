package com.example.english.demo.configuration;

//file này chứa các bean để inject
import org.springframework.beans.factory.annotation.Autowired;
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
    String[] publicRoutes = {"payment-callback", "/payment/vnpay", "/course/detail/{id}", "/course/list", "/auth/refesh-token", "/ws/**", "/topic/**", "/app/invite", "/app/accept-invite", "/app/reject-invite", "/topic/invite/{userId}", "/user/search-users", "/user/toeic-detail/{examId}", "/competition", "/user/friends", "/user/submit-toeic-exam", "/user/show-toeic-question/{examId}", "/admin/login", "/user/toeic", "/audio/**", "/user/confirm-account/**", "/images/**", "/user/create", "/auth/login", "/login", "/toeic", "/online-tests", "/", "/user", "/auth/verify-token", "/auth/login", "/auth/logout", "/auth/refesh-token", "/toeic-exam/create", "/toeic-exam/update/{examId}"};
    //@Value("${signer.key}")
    //private String Signer_Key;
    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtCookieFilter jwtCookieFilter) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicRoutes).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
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



//    hàm này học trên mạng về phân quyền
//    @Bean
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .authorizeHttpRequests(authConfig -> {
//                    authConfig.requestMatchers(HttpMethod.GET, "/", "/login", "/error", "/login-error", "/logout", "/css/**").permitAll();
//                    authConfig.requestMatchers(HttpMethod.GET, "/user").hasRole("USER");
//                    authConfig.requestMatchers(HttpMethod.GET, "/admin").hasRole("ADMIN");
//                    authConfig.requestMatchers(HttpMethod.GET, "/developer").hasRole("DEVELOPER");
//                    authConfig.requestMatchers(HttpMethod.GET, "/users").hasAnyRole("DEVELOPER");
//                    authConfig.requestMatchers(HttpMethod.GET, "/authorities").hasAnyRole("DEVELOPER");
//                    authConfig.anyRequest().authenticated();
//                })
//                .formLogin(login -> {
//                            login.loginPage("/login");
//                            login.defaultSuccessUrl("/");
//                            login.failureUrl("/login-error");
//                        }
//                )
//                .logout(logout -> {
//                    logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
//                    logout.logoutSuccessUrl("/");
//                    logout.deleteCookies("JSESSIONID");
//                    logout.invalidateHttpSession(true);
//                });
//        return http.build();
//    }

    //override Bean Granted là giả dụ và giả dụ xong phải authen và nó bắt buộc phải có là admin thì mới được truy cập k cả có token hợp le
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix(""); // ⚠️ KHÔNG thêm "ROLE_" nữa vì đã có sẵn trong token
        converter.setAuthoritiesClaimName("scope"); // ✅ đọc quyền từ claim "scope"

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
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
    //trong này thì có thể ném ra file khác dùng chung
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }
}
