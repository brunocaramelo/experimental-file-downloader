package com.bgasparotto.filedownloader.service;

import com.bgasparotto.filedownloader.model.DistributedFile;
import com.bgasparotto.filedownloader.service.exception.DistributedFileSystemException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedFileSystemService {

    private final FileSystem hdfs;

    public DistributedFile create(Path path, InputStream inputStream) {
        try (inputStream; var outputStream = hdfs.create(hdfsPath(path))) {
            int size = IOUtils.copy(inputStream, outputStream);
            return new DistributedFile(path, size);
        } catch (IOException e) {
            throw new DistributedFileSystemException(e);
        }
    }

    public boolean exists(Path path) {
        return wrap(() -> hdfs.exists(hdfsPath(path)));
    }

    public boolean delete(Path path, boolean recursive) {
        return wrap(() -> hdfs.delete(hdfsPath(path), recursive));
    }

    private org.apache.hadoop.fs.Path hdfsPath(Path path) {
        return new org.apache.hadoop.fs.Path(path.toString());
    }

    private <T> T wrap(Callable<T> operation) {
        try {
            return operation.call();
        } catch (Exception e) {
            throw new DistributedFileSystemException(e);
        }
    }
}
