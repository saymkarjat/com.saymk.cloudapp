package com.saymk.cloud6x.minio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "minio_data")
public class MinioResource {

    @Id
    private String id;

    @Indexed
    private String path;

    private String name;

    @Indexed(unique = true)
    private String fullPath;

    @Nullable
    private Long size;

    @Indexed
    private Long userId;

    private ResourceType type;
}
