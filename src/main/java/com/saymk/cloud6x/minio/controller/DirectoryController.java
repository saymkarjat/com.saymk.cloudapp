package com.saymk.cloud6x.minio.controller;

import com.saymk.cloud6x.minio.dto.ResourceInfoResponseDTO;
import com.saymk.cloud6x.minio.service.StorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
@Tag(name = "Directory API", description = "Управление директориями в хранилище")
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

