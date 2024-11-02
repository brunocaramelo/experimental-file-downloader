package com.brunocaramelo.filedownloader.service;

import com.brunocaramelo.filedownloader.message.DownloadedFile;
import com.brunocaramelo.filedownloader.messaging.producer.DownloadedFileProducer;
import com.brunocaramelo.filedownloader.model.DistributedFile;
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
