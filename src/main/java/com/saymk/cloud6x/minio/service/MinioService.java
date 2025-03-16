package com.saymk.cloud6x.minio.service;

import com.saymk.cloud6x.minio.dto.ResourceInfoResponseDTO;
import com.saymk.cloud6x.minio.model.MinioResource;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
public abstract class MinioService {

    protected final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    protected String bucketName;

    public abstract ResourceInfoResponseDTO objectStats(String fullPath);

    public abstract void deleteObjectByPath(String path);

    public abstract void copyObject(String sourcePath, String targetPath);

    public abstract InputStream readObject(String folderPath);

    public abstract boolean objectExists(String path);

}
