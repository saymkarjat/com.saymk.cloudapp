package com.saymk.cloud6x.minio.service;

import com.saymk.cloud6x.minio.dto.ResourceInfoResponseDTO;
import com.saymk.cloud6x.minio.exception.FolderAlreadyExistException;
import com.saymk.cloud6x.minio.model.ResourceType;
import com.saymk.cloud6x.minio.util.StorageUtil;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
@Component
@Slf4j
public class MinioFolderService extends MinioService{
    private static final InputStream EMPTY_BYTE_STREAM = new ByteArrayInputStream(new byte[0]);

    public MinioFolderService(MinioClient minioClient) {
        super(minioClient);
    }

    @SneakyThrows
    public void createEmptyFolder(String path) {
        if (objectExists(path)) {
            throw new FolderAlreadyExistException();
        }
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .stream(EMPTY_BYTE_STREAM, 0, -1)
                .build());
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
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(path)
                .maxKeys(1)
                .build());

        return results.iterator().hasNext();
    }
}
