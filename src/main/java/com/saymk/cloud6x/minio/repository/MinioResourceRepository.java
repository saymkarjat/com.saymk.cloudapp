package com.saymk.cloud6x.minio.repository;

import com.saymk.cloud6x.minio.model.MinioResource;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MinioResourceRepository extends MongoRepository<MinioResource, String> {

    Optional<MinioResource> findByFullPath(String fullPath);

    List<MinioResource> findByPath(String path);

    long deleteByFullPathIgnoreCase(String fullPath);

    long deleteByPathIgnoreCase(String path);

    List<MinioResource> findByNameIgnoreCase(String name);

    List<MinioResource> findByNameAndUserId(String name, Long userId);

}
