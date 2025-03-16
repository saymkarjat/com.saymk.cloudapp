package com.saymk.cloud6x.minio.dto.mapper;

import com.saymk.cloud6x.minio.dto.ResourceInfoResponseDTO;
import com.saymk.cloud6x.minio.model.MinioResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MinioResourceMapper {


    public ResourceInfoResponseDTO toDto(MinioResource resource) {
        return new ResourceInfoResponseDTO(
                removeRootDirectory(resource.getPath(), getRootDirectory(resource.getUserId())),
                resource.getName(),
                resource.getSize(),
                resource.getType());
    }

    public List<ResourceInfoResponseDTO> toDtoList(List<MinioResource> resources) {
        return resources.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private String removeRootDirectory(String path, String root) {
        if (path.startsWith(root)) {
            return path.substring(root.length());
        }
        return path;
    }

    private String getRootDirectory(Long id){
        return "user-"+id+"-files/";
    }
}

