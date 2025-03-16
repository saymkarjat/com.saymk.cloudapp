package com.saymk.cloud6x.minio.resolver;

import com.saymk.cloud6x.minio.service.MinioService;

public interface MinioServiceResolver {
    MinioService resolve(String path);
}
