package com.example.english.demo.controller;

import com.example.english.demo.dto.request.ApiResponse;
import com.example.english.demo.dto.request.PermissionCreateRequest;
import com.example.english.demo.dto.response.PermissionResponse;
import com.example.english.demo.service.PermisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/permission")
public class PermisssionController {
    private final PermisionService permisionService;
    @PostMapping
    ApiResponse<PermissionResponse> create(@RequestBody PermissionCreateRequest permissionCreateRequest){
        return ApiResponse.<PermissionResponse>builder()
                .result(permisionService.create(permissionCreateRequest))
                .message("okok")
                .build();
    }

    @GetMapping
    ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permisionService.getAll())
                .build();
    }

    @DeleteMapping("/{permission}")//này xoa theo name
    ApiResponse<Void> delete(@PathVariable String permission){
        permisionService.delete(permission);
        return ApiResponse.<Void>builder().build();
    }

}
