package com.saymk.cloud6x.minio.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class StorageUtil {

    public static String getFolderName(String path) {
        String cleanedPath = StringUtils.removeEnd(path, "/");
        return FilenameUtils.getName(cleanedPath);
    }

    public static String getFolderPathWithoutName(String path) {
        String cleanedPath = StringUtils.removeEnd(path, "/");
        return FilenameUtils.getFullPath(cleanedPath);
    }
}
