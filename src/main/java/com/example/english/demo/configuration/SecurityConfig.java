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

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    String[] publicRoutes = {"/ws/**", "/topic/**","/app/invite","/app/accept-invite","/app/reject-invite", "/topic/invite/{userId}","/user/search-users","/user/toeic-detail/{examId}","/admin/update-question-image/{questionId}","/competition","/user/friends","/user/submit-toeic-exam","/user/show-toeic-question/{examId}","/admin/login","/user/toeic","/audio/**","/user/confirm-account/**","/admin/import-toeic-questions/{examId}","/images/**","/admin/show-toeic-question/{examId}","/admin/create-toeic-question/{examId}","/user/create","/auth/login","/login","/toeic","/online-tests","/","/admin/toeic","/user", "/auth/verify-token", "/auth/login", "/auth/logout", "/auth/refesh-token", "/toeic-exam/create","/toeic-exam/update/{examId}","/admin/create-toeic-exam"};

    //@Value("${signer.key}")
    //private String Signer_Key;
    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers(publicRoutes).permitAll()// Được phép truy cập không cần xác thực
                        .anyRequest().authenticated());  // Các request khác cần phải xác thực

        //này dùng  cấu hình cho route nào đó cần phải có token mà ch đk cấu hình bên trên
        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );
        //cái csrf này là auto nó bật nên mk phải tắt đi,thường nếu ko tắt thì sẽ có lỗi foriden 403
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    //override Bean Granted là giả dụ và giả dụ xong phải authen và nó bắt buộc phải có là admin thì mới được truy cập k cả có token hợp le
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){
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
    //trong này thì có thể ném ra file khác dùng chung
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }
}
