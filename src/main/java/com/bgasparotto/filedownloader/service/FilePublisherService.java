package com.bgasparotto.filedownloader.service;

import com.bgasparotto.filedownloader.message.DownloadedFile;
import com.bgasparotto.filedownloader.messaging.producer.DownloadedFileProducer;
import com.bgasparotto.filedownloader.model.DistributedFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilePublisherService {

    private final DownloadedFileProducer downloadedFileProducer;

    public void publish(String fileId, DistributedFile distributedFile) {
        DownloadedFile downloadedFile = DownloadedFile.newBuilder()
            .setId(fileId)
            .setPath(distributedFile.getPathAsString())
            .build();

        downloadedFileProducer.produce(downloadedFile);
    }
}
