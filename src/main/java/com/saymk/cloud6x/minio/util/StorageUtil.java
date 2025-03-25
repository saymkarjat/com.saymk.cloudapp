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

    public static String getFileName(String path) {
        return FilenameUtils.getName(path);
    }

    public static String getFilePathWithoutName(String path) {
        return FilenameUtils.getFullPath(path);
    }

    public static String deleteInitialPrefix(String path) {
        String[] parts = path.split("/", 2);
        if (parts.length > 1) {
            path = parts[1];
        } else {
            path = "";
        }
        return path;
    }
}
