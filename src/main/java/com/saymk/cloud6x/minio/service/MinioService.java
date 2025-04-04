package com.saymk.cloud6x.minio.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RequiredArgsConstructor
public abstract class MinioService {

    protected final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    protected String bucketName;

    @SneakyThrows
    public void putObject(String path, MultipartFile file) {
        @Cleanup InputStream stream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .stream(stream, file.getSize(), -1)
                        .build()
        );
    }

    @SneakyThrows
    protected InputStream getFileStream(String fullPath) {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fullPath)
                        .build()
        );
    }


    public abstract void deleteObjectByPath(String path);

    public abstract void copyObject(String sourcePath, String targetPath);

    public abstract InputStream getResourceStream(String path);

    public abstract boolean objectExists(String path);

}
