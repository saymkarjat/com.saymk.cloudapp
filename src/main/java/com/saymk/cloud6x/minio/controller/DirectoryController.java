package com.saymk.cloud6x.minio.controller;

import com.saymk.cloud6x.minio.dto.ResourceInfoResponseDTO;
import com.saymk.cloud6x.minio.repository.MinioResourceRepository;
import com.saymk.cloud6x.minio.resolver.MinioServiceResolver;
import com.saymk.cloud6x.minio.service.MinioFolderService;
import com.saymk.cloud6x.minio.service.MinioService;
import com.saymk.cloud6x.minio.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final StorageService storageService;


    @PostMapping()
    public ResponseEntity<ResourceInfoResponseDTO> createDirectory(@RequestParam(name = "path") String path,
                                          @AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storageService.createFolder(path, userId));
    }

    @GetMapping()
    public ResponseEntity<List<ResourceInfoResponseDTO>> getDirectoryContents(@RequestParam(name = "path") String path,
                                                              @AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.ok(storageService.getFolderContents(path, userId));
    }

}

