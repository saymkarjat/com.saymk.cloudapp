package com.saymk.cloud6x.minio.service;

import com.saymk.cloud6x.minio.dto.ResourceInfoResponseDTO;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
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
    public ResourceInfoResponseDTO objectStats(String fullPath) {
        return null;
    }

    @Override
    public void deleteObjectByPath(String path) {

    }

    @Override
    public void copyObject(String sourcePath, String targetPath) {

    }

    @Override
    public InputStream readObject(String folderPath) {
        return null;
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
