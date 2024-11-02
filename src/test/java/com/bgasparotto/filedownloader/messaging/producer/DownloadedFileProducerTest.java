package com.bgasparotto.filedownloader.messaging.producer;

import static org.assertj.core.api.Assertions.assertThat;

import com.bgasparotto.filedownloader.message.DownloadedFile;
import com.bgasparotto.spring.kafka.avro.test.EmbeddedKafkaAvro;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@ActiveProfiles("test")
@EmbeddedKafka
@DirtiesContext
public class DownloadedFileProducerTest {

    @Autowired
    private EmbeddedKafkaAvro embeddedKafkaAvro;

    @Autowired
    private DownloadedFileProducer producer;

    @Value("${topics.output.downloaded-file}")
    private String topic;

    @Test
    public void shouldProduceDownloadedFileMessage() {
        DownloadedFile testMessage = buildTestMessage();
        producer.produce(testMessage);

        ConsumerRecord<String, DownloadedFile> consumedRecord = embeddedKafkaAvro.consumeOne(topic);
        assertThat(consumedRecord.key()).isEqualTo(testMessage.getId());
        assertThat(consumedRecord.value()).isEqualTo(testMessage);
    }

    private DownloadedFile buildTestMessage() {
        return DownloadedFile.newBuilder()
            .setId("some-message-id")
            .setPath("some/storage/path/for/file.zip")
            .build();
    }
}
