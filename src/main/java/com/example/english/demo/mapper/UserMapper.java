package com.example.english.demo.mapper;


import com.example.english.demo.dto.request.UserCreateRequest;
import com.example.english.demo.dto.request.UserUpdateRequest;
import com.example.english.demo.dto.response.UserResponse;
import com.example.english.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
   //truyền vào request rồi map ngược về User
    User toUser(UserCreateRequest request);

    //hàm này dùng khi mà 2 cái entity mà khác thuộc tính với nhau
    //@Mapping(target="lastName",ignore=true) //này là bỏ trống ko map
    //@Mapping(source="firstName",target="lastName")//dòng này tức nghĩa ta muốn khi ta gọi hàm firstName và lastName giống nhau
    //truyền vào User rồi map ngược về UserResponse
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles",ignore = true)//dòng này không map roles từ request vào useer
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
