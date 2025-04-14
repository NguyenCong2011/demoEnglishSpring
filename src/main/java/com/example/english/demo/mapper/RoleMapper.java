package com.example.english.demo.mapper;


import com.example.english.demo.dto.request.RoleCreateRequest;
import com.example.english.demo.dto.response.RoleResponse;
import com.example.english.demo.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions",ignore = true)
    Role toRole(RoleCreateRequest roleCreateRequest);
    RoleResponse toRoleResponse(Role role);
}
