package com.example.english.demo.service;

import com.example.english.demo.dto.request.UserCreateRequest;
import com.example.english.demo.dto.request.UserUpdateRequest;
import com.example.english.demo.dto.response.UserResponse;
import com.example.english.demo.entity.User;
import com.example.english.demo.enums.Roles;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.mapper.UserMapper;
import com.example.english.demo.repository.RoleRepository;
import com.example.english.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

//annotation này của lombook nhá
@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    //  trước là public User nhưng mà chúng ta không trả về tất cả các dữ liệu của User nên phải tạo UserResponse
    public UserResponse createUser(UserCreateRequest request){
        //
        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXITSTED);
        }
        //
        User user=userMapper.toUser(request);
        //
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        //
        HashSet<String> roles=new HashSet<>();
        roles.add(Roles.USER.name());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PostAuthorize("returnObject.owner == authentication.name")//dòng log bên dưới vẫn được in ra kể cả k phải admin,tức là ngược với preauthorize
    public UserResponse getUserById(String id){
        log.info("in method get by id");
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(()->new RuntimeException("User not found")));
    }

    //cái này hay được sử dụng hơn la postauthorize
    @PreAuthorize("hasRole('ADMIN')")//pre là trước làm như này tức là trước khi gọi hàm phải là admin dòng log bên dưới ko được in ra nếu k phải admin
//    @PreAuthorize("hasAuthority('create')")//này dung để sử dụng perrmision làm theo 2 cách này để có thể phân quyền api theo cả permision và role
    public List<UserResponse> getUser() {
        log.info("in method get all");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    public UserResponse updateUser(String userId,UserUpdateRequest request){
        User user=userRepository.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        userMapper.updateUser(user,request);

        //này dùng để map role vào user
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        var roles= roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));//map từ 2 dòng trên dùng convert từ list sang hashSet

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String id){
        userRepository.deleteById(id);
    }

    //hàm này để lấy thông tin chính mình
    public UserResponse getMyInfo(){
        System.out.println("okokol++++++++++++++++++okok");
        var context= SecurityContextHolder.getContext();
        String name=context.getAuthentication().getName();
        User user=userRepository.findByUsername(name).orElseThrow(()->new AppException(ErrorCode.USER_EXITSTED));

        return userMapper.toUserResponse(user);
    }
}
