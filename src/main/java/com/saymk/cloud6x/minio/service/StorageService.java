package com.saymk.cloud6x.minio.service;

import com.saymk.cloud6x.minio.exception.StorageLimitExceededException;
import com.saymk.cloud6x.minio.dto.ResourceInfoResponseDTO;
import com.saymk.cloud6x.minio.dto.mapper.MinioResourceMapper;
import com.saymk.cloud6x.minio.exception.FolderAlreadyExistException;
import com.saymk.cloud6x.minio.exception.ResourceNotFoundException;
import com.saymk.cloud6x.minio.model.MinioResource;
import com.saymk.cloud6x.minio.model.ResourceType;
import com.saymk.cloud6x.minio.repository.MinioResourceRepository;
import com.saymk.cloud6x.minio.resolver.MinioServiceResolver;
import com.saymk.cloud6x.minio.util.StorageUtil;
import com.saymk.cloud6x.model.User;
import com.saymk.cloud6x.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {
    private final MinioServiceResolver serviceResolver;
    private final MinioFolderService folderService;
    private final MinioResourceRepository repository;
    private final UserRepository userRepository;
    private final MinioResourceMapper mapper;
    @Value("${minio.max_mb_capacity_for_user}")
    protected Long maxCapacity;

    public ResourceInfoResponseDTO moveResources(String from, String to, Long userId) {
        String root = "user-" + userId + "-files/";
        MinioService service = serviceResolver.resolve(from);
        String sourcePath = root + from;
        String targetPath = root + to;

        if (from.endsWith("/")) {
            service.copyObject(sourcePath, targetPath);
            service.deleteObjectByPath(sourcePath);
            return changeDirectory(sourcePath, targetPath);
        }
        service.copyObject(sourcePath, targetPath);
        service.deleteObjectByPath(sourcePath);
        MinioResource resource = repository.findByFullPath(sourcePath).orElseThrow(ResourceNotFoundException::new);
        return changeResourceData(targetPath, resource);

    }

    private ResourceInfoResponseDTO changeResourceData(String targetPath, MinioResource resource) {
        String name = "";
        String path = "";

        if (targetPath.endsWith("/")) {
            name = StorageUtil.getFolderName(targetPath);
            path = StorageUtil.getFolderPathWithoutName(targetPath);
        } else {
            name = StorageUtil.getFileName(targetPath);
            path = StorageUtil.getFilePathWithoutName(targetPath);
        }

        MinioResource changedResource = MinioResource.builder()
                .name(name)
                .path(path)
                .type(resource.getType())
                .fullPath(targetPath)
                .userId(resource.getUserId())
                .size(resource.getSize())
                .build();

        repository.delete(resource);
        repository.save(changedResource);

        return mapper.toDto(changedResource);
    }

    private ResourceInfoResponseDTO changeDirectory(String sourcePath, String targetPath) {
        List<MinioResource> resources = repository.findByPath(sourcePath);
        for (MinioResource resource : resources) {
            String fullPath = "";
            if (resource.getType() == ResourceType.DIRECTORY) {
                fullPath = targetPath + resource.getName() + "/";
            } else {
                fullPath = targetPath + resource.getName();
            }

            changeResourceData(fullPath, resource);
        }
        Optional<MinioResource> minioResource = repository.findByFullPath(sourcePath);
        MinioResource resource = minioResource.orElseThrow(ResourceNotFoundException::new);
        return changeResourceData(targetPath, resource);
    }

    public List<ResourceInfoResponseDTO> findResourcesByName(String name, Long id) {
        List<MinioResource> resources = repository.findByNameAndUserId(name, id);
        return mapper.toDtoList(resources);
    }

    public InputStream downloadResource(String path, Long userId) {
        String root = "user-"+userId+"-files/";
        MinioService service = serviceResolver.resolve(path);
        String fullPath = root+path;
        return service.getResourceStream(fullPath);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<ResourceInfoResponseDTO> uploadObjects(List<MultipartFile> files, String path, User user) {
        Long resourceSize = files.stream()
                .map(MultipartFile::getSize)
                .reduce(0L, Long::sum);
        Long sizeMB = resourceSize / (1024 * 1024);
        int updatedRows = userRepository.increaseStorageSize(user.getId(), sizeMB, maxCapacity);
        if (updatedRows == 0) {
            log.warn("Превышен лимит памяти во время обновления. Файлы не загружены.");
            throw new StorageLimitExceededException();
        }

        String sourceFolder = "user-"+user.getId()+"-files/" + path;
        List<MinioResource> list = new ArrayList<>();
        for (MultipartFile file : files) {
            String fullPath = sourceFolder + file.getOriginalFilename();
            MinioService service = serviceResolver.resolve(fullPath);
            service.putObject(fullPath, file);
            addFolderIfNotExist(fullPath, user.getId());

            MinioResource resource = MinioResource.builder()
                    .name(StorageUtil.getFileName(fullPath))
                    .path(StorageUtil.getFilePathWithoutName(fullPath))
                    .type(ResourceType.FILE)
                    .fullPath(fullPath)
                    .userId(user.getId())
                    .size(file.getSize())
                    .build();
            repository.save(resource);
            list.add(resource);
        }

        return mapper.toDtoList(list);
    }

    public ResourceInfoResponseDTO getResourceInfo(String path, Long id) {
        String root = "user-"+id+"-files/";
        Optional<MinioResource> minioResource = repository.findByFullPath(root + path);
        if (minioResource.isEmpty()) {
            throw new ResourceNotFoundException();
        }
        return mapper.toDto(minioResource.get());
    }

    public void deleteResource(String path, Long id) {
        String root = "user-"+id+"-files/";
        MinioService service = serviceResolver.resolve(path);
        if (!service.objectExists(root+path)) {
            throw new ResourceNotFoundException();
        }
        service.deleteObjectByPath(root+path);
        if (path.endsWith("/")) {
            repository.deleteByFullPathIgnoreCase(root+path);
            repository.deleteByPathIgnoreCase(root+path);
            return;
        }
        repository.deleteByFullPathIgnoreCase(root+path);
    }

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

    private void addFolderIfNotExist(String fullPath, Long userId) {
        String folderPath = StorageUtil.getFilePathWithoutName(fullPath);
        while (!StringUtils.isBlank(folderPath)) {
            Optional<MinioResource> folderResource = repository.findByFullPath(folderPath);
            if (folderResource.isEmpty()) {
                MinioResource folder = MinioResource.builder()
                        .fullPath(folderPath)
                        .path(StorageUtil.getFolderPathWithoutName(folderPath))
                        .name(StorageUtil.getFolderName(folderPath))
                        .userId(userId)
                        .type(ResourceType.DIRECTORY)
                        .build();

                repository.save(folder);
            }
            folderPath = StorageUtil.getFolderPathWithoutName(folderPath);
        }
    }

    public String getFileName(String path) {
        if (path.endsWith("/")) {
            return StorageUtil.getFolderName(path);
        }
        return StorageUtil.getFileName(path);
    }

}
