package com.saymk.cloud6x.minio.service;

import com.saymk.cloud6x.minio.exception.FileAlreadyExistException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Slf4j
public class MinioFileService extends MinioService {

    public MinioFileService(MinioClient minioClient) {
        super(minioClient);
    }

    @Override
    @SneakyThrows
    public void deleteObjectByPath(String path) {
        minioClient.removeObject(RemoveObjectArgs
                .builder()
                .bucket(bucketName)
                .object(path)
                .build());
    }

    @Override
    @SneakyThrows
    public void copyObject(String sourcePath, String targetPath) {
        if (objectExists(targetPath)) {
            throw new FileAlreadyExistException();
        }
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucketName)
                        .object(targetPath)
                        .source(
                                CopySource.builder()
                                        .bucket(bucketName)
                                        .object(sourcePath)
                                        .build()
                        )
                        .build()
        );
    }

    @Override
    @SneakyThrows
    public InputStream getResourceStream(String path) {
        return getFileStream(path);
    }

    @Override
    public boolean objectExists(String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                log.info("Файл не найден: {}", path);
                return false;
            }
            log.error("Ошибка MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка MinIO: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при проверке файла {}", path, e);
            throw new RuntimeException("Ошибка при проверке файла: " + path, e);
        }
    }
}
