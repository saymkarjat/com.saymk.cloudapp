package com.saymk.cloud6x.minio.service;

import com.saymk.cloud6x.minio.dto.ResourceInfoResponseDTO;
import com.saymk.cloud6x.minio.dto.mapper.MinioResourceMapper;
import com.saymk.cloud6x.minio.exception.FolderAlreadyExistException;
import com.saymk.cloud6x.minio.model.MinioResource;
import com.saymk.cloud6x.minio.model.ResourceType;
import com.saymk.cloud6x.minio.repository.MinioResourceRepository;
import com.saymk.cloud6x.minio.resolver.MinioServiceResolver;
import com.saymk.cloud6x.minio.util.StorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {
    private final MinioServiceResolver serviceResolver;
    private final MinioFolderService folderService;
    private final MinioResourceRepository repository;
    private final MinioResourceMapper mapper;

    public void createInitialFolderForNewUser(Long id) {
        String path = "user-"+id+"-files/";
        if (folderService.objectExists(path)){
            log.info("Папка уже существует: {}", path);
            throw new FolderAlreadyExistException();
        }
        folderService.createEmptyFolder(path);
        MinioResource resource = MinioResource.builder()
                .fullPath(path)
                .path("")
                .name(StringUtils.removeEnd(path, "/"))
                .userId(id)
                .type(ResourceType.DIRECTORY)
                .build();
        repository.save(resource);
        log.info("Создана новая папка: {}", path);
    }

    public ResourceInfoResponseDTO createFolder(String path, Long id) {
        String fullPath = "user-"+id+"-files/" + path;
        String name = StorageUtil.getFolderName(fullPath);
        String folderPathWithoutName = StorageUtil.getFolderPathWithoutName(fullPath);

        folderService.createEmptyFolder(fullPath);

        MinioResource resource = MinioResource.builder()
                .fullPath(fullPath)
                .path(folderPathWithoutName)
                .name(name)
                .userId(id)
                .type(ResourceType.DIRECTORY)
                .build();
        repository.save(resource);

        return mapper.toDto(resource);
    }

    public List<ResourceInfoResponseDTO> getFolderContents(String path, Long id) {
        String fullPath = "user-"+id+"-files/" + path;
        List<MinioResource> resources = repository.findByPath(fullPath);
        return mapper.toDtoList(resources);
    }
}
