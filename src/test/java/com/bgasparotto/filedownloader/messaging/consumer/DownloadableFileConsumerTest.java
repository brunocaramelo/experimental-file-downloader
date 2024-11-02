package com.bgasparotto.filedownloader.messaging.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bgasparotto.filedownloader.message.DownloadableFile;
import com.bgasparotto.filedownloader.model.DistributedFile;
import com.bgasparotto.filedownloader.service.FileDownloaderService;
import com.bgasparotto.filedownloader.service.FilePublisherService;
import com.bgasparotto.spring.kafka.avro.test.EmbeddedKafkaAvro;
import java.nio.file.Path;
import org.apache.commons.lang3.RandomUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@ActiveProfiles("test")
@EmbeddedKafka
@DirtiesContext
public class DownloadableFileConsumerTest {

    public static final long FIVE_SECONDS = 5 * 1000;

    @Autowired
    private EmbeddedKafkaAvro embeddedKafkaAvro;

    @Value("${topics.input.downloadable-file}")
    private String topic;

    @MockBean
    private FileDownloaderService mockFileDownloaderService;

    @MockBean
    private FilePublisherService mockFilePublisherService;

    @SpyBean
    private DownloadableFileConsumer downloadableFileConsumer;

    @Captor
    private ArgumentCaptor<ConsumerRecord<String, DownloadableFile>> downloadableFileMessageCaptor;

    @Captor
    private ArgumentCaptor<String> uriToDownloadCaptor;

    @BeforeEach
    public void setUp() {
        when(mockFileDownloaderService.download(anyString()))
            .then(i -> {
                String input = i.getArgument(0, String.class);
                return new DistributedFile(Path.of(input), RandomUtils.nextInt(1024, 2048));
            });
    }

    @Test
    public void shouldDownloadTheFileAndPublishTheResultWhenAnInputMessageIsConsumed() {
        String inputMessageKey = "some-message-key";
        DownloadableFile inputMessageValue = buildTestMessage();
        embeddedKafkaAvro.produce(topic, inputMessageKey, inputMessageValue);

        assertMessageIsConsumed(inputMessageKey, inputMessageValue);
        assertFileIsDownloaded(inputMessageValue);
        assertResultIsPublished();
    }

    private DownloadableFile buildTestMessage() {
        return DownloadableFile.newBuilder()
            .setId("some-id")
            .setTitle("Some Title")
            .setUri("some.uri/path/to/file.zip")
            .build();
    }

    private void assertMessageIsConsumed(String inputMessageKey, DownloadableFile inputMessageValue) {
        verify(downloadableFileConsumer, timeout(FIVE_SECONDS).times(1))
            .consume(downloadableFileMessageCaptor.capture());

        ConsumerRecord<String, DownloadableFile> consumedRecord = downloadableFileMessageCaptor.getValue();
        assertThat(consumedRecord.key()).isEqualTo(inputMessageKey);
        assertThat(consumedRecord.value()).isEqualTo(inputMessageValue);
    }

    private void assertFileIsDownloaded(DownloadableFile downloadableFile) {
        verify(mockFileDownloaderService, timeout(FIVE_SECONDS).times(1))
            .download(uriToDownloadCaptor.capture());

        String path = uriToDownloadCaptor.getValue();
        assertThat(path).isEqualTo(downloadableFile.getUri());
    }

    private void assertResultIsPublished() {
        verify(mockFilePublisherService, timeout(FIVE_SECONDS).times(1))
            .publish(anyString(), any(DistributedFile.class));
    }
}
