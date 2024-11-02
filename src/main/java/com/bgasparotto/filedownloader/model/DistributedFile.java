package com.bgasparotto.filedownloader.model;

import java.nio.file.Path;
import lombok.Value;
import org.apache.commons.io.FileUtils;

@Value
public class DistributedFile {

    private final Path path;
    private final int size;

    public String getPathAsString() {
        return path.toString();
    }

    public String getSizeAsString() {
        return FileUtils.byteCountToDisplaySize(size);
    }
}
