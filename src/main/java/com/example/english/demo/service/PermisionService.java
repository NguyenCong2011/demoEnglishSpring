package com.example.english.demo.service;

import com.example.english.demo.dto.request.PermissionCreateRequest;
import com.example.english.demo.dto.response.PermissionResponse;
import com.example.english.demo.entity.Permission;
import com.example.english.demo.mapper.PermissionMapper;
import com.example.english.demo.repository.PermisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class PermisionService {
    private final PermisionRepository permisionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionCreateRequest request){
        Permission permission= permissionMapper.toPermission(request);
        permission=permisionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);

    }

    public List<PermissionResponse> getAll(){
        var permission=permisionRepository.findAll();
        return permission.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void delete(String id){
        permisionRepository.deleteById(id);
    }
}
