package com.saymk.cloud6x.minio.service;

import com.saymk.cloud6x.minio.exception.FolderAlreadyExistException;
import com.saymk.cloud6x.minio.util.StorageUtil;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j

public class MinioFolderService extends MinioService {
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
    @SneakyThrows
    public void deleteObjectByPath(String path) {
        List<Item> items = getResourcesByPrefix(path);
        List<DeleteObject> deleteObjectList = items.stream()
                .map(Item::objectName)
                .map(DeleteObject::new)
                .toList();

        minioClient.removeObjects(RemoveObjectsArgs
                        .builder()
                        .bucket(bucketName)
                        .objects(deleteObjectList)
                        .build())
                .forEach(del -> {
                });
    }

    @SneakyThrows
    private List<Item> getResourcesByPrefix(String path) {
        List<Item> items = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs
                .builder()
                .bucket(bucketName)
                .prefix(path)
                .recursive(true)
                .build());

        results.forEach(e -> {
            try {
                items.add(e.get());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        return items;
    }

    @Override
    @SneakyThrows
    public void copyObject(String sourcePath, String targetPath) {
        List<Item> items = getResourcesByPrefix(sourcePath);
        for (Item item : items) {
            String innerObjectSourcePath = item.objectName();
            String innerObjectTargetPath = innerObjectSourcePath.replaceFirst(sourcePath, targetPath);
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(innerObjectTargetPath)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(innerObjectSourcePath)
                                            .build()
                            )
                            .build()
            );
        }
    }


    @Override
    @SneakyThrows
    public InputStream getResourceStream(String path) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipFile = new ZipOutputStream(byteArrayOutputStream);
        List<Item> items = getResourcesByPrefix(path);

        for (Item item : items) {
            InputStream inputStream = getFileStream(item.objectName());
            zipFile.putNextEntry(new ZipEntry(StorageUtil.deleteInitialPrefix(item.objectName())));
            inputStream.transferTo(zipFile);
            zipFile.closeEntry();
            inputStream.close();
        }

        zipFile.finish();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
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
