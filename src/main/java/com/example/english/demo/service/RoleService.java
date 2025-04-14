package com.example.english.demo.service;


import com.example.english.demo.repository.PermisionRepository;
import com.example.english.demo.repository.RoleRepository;
import com.example.english.demo.dto.request.RoleCreateRequest;
import com.example.english.demo.dto.response.RoleResponse;
import com.example.english.demo.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermisionRepository permisionRepository;

    public RoleResponse create(RoleCreateRequest roleCreateRequest){
        var role=roleMapper.toRole(roleCreateRequest);
        var permissions=permisionRepository.findAllById(roleCreateRequest.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role=roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll(){
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    public void delete(String role){
        roleRepository.deleteById(role);
    }
}
