package com.bgasparotto.filedownloader.messaging.producer;

import com.bgasparotto.filedownloader.message.DownloadedFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadedFileProducer {
    private final KafkaTemplate<String, DownloadedFile> kafkaTemplate;

    @Value("${topics.output.downloaded-file}")
    private String topic;

    public void produce(DownloadedFile downloadedFile) {
        kafkaTemplate.send(topic, downloadedFile.getId(), downloadedFile);
        log.info("Produced message with downloadable file details: [{}]", downloadedFile);
    }
}
