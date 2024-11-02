package com.bgasparotto.filedownloader.messaging.consumer;

import com.bgasparotto.filedownloader.message.DownloadableFile;
import com.bgasparotto.filedownloader.model.DistributedFile;
import com.bgasparotto.filedownloader.service.FileDownloaderService;
import com.bgasparotto.filedownloader.service.FilePublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadableFileConsumer {

    private final FileDownloaderService fileDownloaderService;
    private final FilePublisherService filePublisherService;

    @KafkaListener(topics = "${topics.input.downloadable-file}")
    public void consume(ConsumerRecord<String, DownloadableFile> record) {
        DownloadableFile downloadableFile = record.value();
        log.info("Received downloadable file to process: [{}]", downloadableFile);

        String downloadableFileUri = downloadableFile.getUri();
        DistributedFile distributedFile = fileDownloaderService.download(downloadableFileUri);

        filePublisherService.publish(downloadableFile.getId(), distributedFile);
    }
}
