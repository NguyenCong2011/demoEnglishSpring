package com.example.english.demo.service;

import com.example.english.demo.dto.request.AuthenticationRequest;
import com.example.english.demo.dto.request.IntrospectRequest;
import com.example.english.demo.dto.request.LogoutRequest;
import com.example.english.demo.dto.request.RefeshRequest;
import com.example.english.demo.dto.response.AuthenticationResponse;
import com.example.english.demo.dto.response.IntrospectResponse;
import com.example.english.demo.entity.InvalidatedToken;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.repository.InvalidatedTokenRepository;
import com.example.english.demo.entity.User;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal//dùng để ánh dấu không cho inject vo constructer
    @Value("${signer.key}")
    private String Signer_Key;

    @Value("${valid-duration}")
    private long VALID_DURRATION;

    @Value("${refeshable-duration}")
    private long REFESHABLE_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .filter(User::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITSTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        boolean authenticated1 = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated1) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token =generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    //hàm này dùng bên trên
    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        //tạo payload và claim là dữ liệu của mình
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("cong.cong")
                .issueTime(new Date())  // Thời gian phát hành
                .expirationTime(new Date(Instant.now().plus(VALID_DURRATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())// này để chúng ta get thêm claim id của token trên jwt.io
                .claim("scope", buildScope(user))// Thêm claim tùy chỉnh và này là them admin vào scope trong token
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        //dđây là phần mà bằng header+payload
        JWSObject jwsObject = new JWSObject(header, payload);

        //đã tạo xong token giờ kí token và dùng cái key kia mà mình đã gen trên gg để kí
        try {
            //này là 1 khóa còn có thể gen bằng khóa công khai và public
            jwsObject.sign(new MACSigner(Signer_Key.getBytes()));
            return jwsObject.serialize();// Trả về token đã ký dưới dạng chuỗi
        } catch (JOSEException exception) {
            log.error("Cannot create token",exception);
            throw new RuntimeException(exception);
        }
    }
//    hàm này cho vào scope
    private String buildScope(User user){
        //dấu cách vì khi trả dữ liệu sẽ có các role cách nhau
        StringJoiner stringJoiner=new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_"+role.getName());//cho ROLE_ vào đây làm prefix để pha biệt Role và Permission và khi lấy trên  jwwt ta có thể thấy ROLE_ đứng trước ADMIN
                if(!CollectionUtils.isEmpty(role.getPermissions())){
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
                }
            });
        return stringJoiner.toString();
    }

    //này dùng để verrify xem có phải đúng là token mk tạo ra hay không
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token=request.getToken();
        try {
            verifyToken(token,false);
        }catch (AppException exception){
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
        //
        return IntrospectResponse.builder()
                .valid(true)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signedToken=verifyToken(request.getToken(),true);

            String jit=signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime=signedToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken=InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expiried");
        }
    }

//    sửa lại hàm này nếu chúng ta dùng thời gian để refesh token thì dùng tgian khác còn nếu dùng để authentication thì verify theo thời gian expiry của token
    private SignedJWT verifyToken(String token,boolean isRefesh) throws JOSEException, ParseException {
        //verify lại bằng MAC giống như đã tạo
        JWSVerifier verifier=new MACVerifier(Signer_Key.getBytes());

        SignedJWT signedJWT=SignedJWT.parse(token);

        Date exprityTime= (isRefesh) ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                    .toInstant().plus(REFESHABLE_DURATION,ChronoUnit.SECONDS).toEpochMilli())
                :signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified=signedJWT.verify(verifier);//trả true hoặc false\

        if(!(verified && exprityTime.after(new Date()))){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // ny để check xem token đã logout chưa ý mà
        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    // do mk đã logout mà không sử dụng đến thư viện nên mk cx sẽ custom cho refesh luôn
    public AuthenticationResponse refeshToken(RefeshRequest request) throws ParseException, JOSEException {
        var signedJWT=verifyToken(request.getToken(),true);

        var jit=signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime=signedJWT.getJWTClaimsSet().getExpirationTime();

        //logout nhé giống hàm trên chỗ logout bên trên nhé ok ok ok ok
        InvalidatedToken invalidatedToken=InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        var username=signedJWT.getJWTClaimsSet().getSubject();

        var user=userRepository.findByUsername(username).orElseThrow(()->new AppException(ErrorCode.UNAUTHENTICATED));

        //hàm này lấy bên trên gennerate tojken từ user ngay bên trên
        var token=generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    //hàm này để xác thực token gửi về email
    public String generateConfirmationToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail()) // email sẽ là chủ thể của token
                .issuer("english-app")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(15, ChronoUnit.MINUTES).toEpochMilli())) // token xác nhận email chỉ sống 15 phút
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "EMAIL_CONFIRM")
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(Signer_Key.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Unable to sign confirmation token", e);
        }
    }


}
