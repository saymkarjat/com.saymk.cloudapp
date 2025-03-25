package com.saymk.cloud6x.minio.controller;

import com.saymk.cloud6x.minio.service.StorageService;
import com.saymk.cloud6x.model.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Resource API", description = "Управление директориями и файлами")
public class ResourceController {
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<?> getResourceInfo(@RequestParam(name = "path") String path,
                                             @AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.ok(storageService.getResourceInfo(path, userId));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteResources(@RequestParam(name = "path") String path,
                                            @AuthenticationPrincipal(expression = "id") Long userId) {
        storageService.deleteResource(path, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<?> uploadResources(@RequestParam(name = "object") List<MultipartFile> files,
                                             @RequestParam(name = "path") String path,
                                             @AuthenticationPrincipal(expression = "user") User user) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storageService.uploadObjects(files, path, user));
    }


    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadResources(@RequestParam(name = "path") String path,
                                                                 @AuthenticationPrincipal(expression = "id") Long userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(storageService.getFileName(path)).build());

        InputStream resource = storageService.downloadResource(path, userId);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(resource));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchResources(@RequestParam(name = "query") String query,
                                                                 @AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.ok().body(storageService.findResourcesByName(query, userId));
    }

    @GetMapping("/move")
    public ResponseEntity<?> moveResources(@RequestParam(name = "from") String from,
                                           @RequestParam(name = "to") String to,
                                           @AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.ok(storageService.moveResources(from, to, userId));
    }
}
