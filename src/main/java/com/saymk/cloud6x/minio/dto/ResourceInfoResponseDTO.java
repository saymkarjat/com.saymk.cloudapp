package com.saymk.cloud6x.minio.dto;

import com.saymk.cloud6x.minio.model.ResourceType;
import org.springframework.lang.Nullable;

/**
 * DTO for {@link com.saymk.cloud6x.minio.model.MinioResource}
 */

public record ResourceInfoResponseDTO(String path, String name, @Nullable Long size, ResourceType type) {
}