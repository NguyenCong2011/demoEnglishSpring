package com.example.english.demo.mapper;


import com.example.english.demo.dto.request.PermissionCreateRequest;
import com.example.english.demo.dto.response.PermissionResponse;
import com.example.english.demo.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionCreateRequest permissionCreateRequest);
    PermissionResponse toPermissionResponse(Permission permission);
}
