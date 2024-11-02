package com.bgasparotto.filedownloader.service;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DistributedFilePathFactory {

    @Value("${hdfs.output.path}")
    private String distributedFileSystemPath;

    public Path fromUri(String uri) {
        String fileName = shortFileName(uri);
        return Path.of(distributedFileSystemPath, fileName);
    }

    private String shortFileName(String uri) {
        if (uri.contains("/")) {
            return substringAfterLast(uri, "/");
        }
        return uri;
    }
}
