package com.saymk.cloud6x.minio.resolver;

import com.saymk.cloud6x.minio.service.MinioFileService;
import com.saymk.cloud6x.minio.service.MinioFolderService;
import com.saymk.cloud6x.minio.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PathBasedMinioServiceResolver implements MinioServiceResolver {

    private final MinioFolderService folderService;
    private final MinioFileService fileService;

    @Override
    public MinioService resolve(String path) {
        return path.endsWith("/") ? folderService : fileService;
    }
}
