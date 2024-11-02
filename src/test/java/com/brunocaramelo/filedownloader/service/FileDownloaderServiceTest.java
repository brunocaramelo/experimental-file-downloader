package com.brunocaramelo.filedownloader.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.brunocaramelo.filedownloader.model.DistributedFile;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RestClientTest(components = FileDownloaderService.class)
@ActiveProfiles("test")
public class FileDownloaderServiceTest {

    @Autowired
    private FileDownloaderService fileDownloaderService;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private DistributedFilePathFactory mockFilePathFactory;

    @MockBean
    private DistributedFileSystemService mockFileSystemService;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void shouldDownloadFromUriToFileSystem() throws Exception {
        String sourceUri = "https://some-website.uri/downloads/test-file-1.png";
        Path expectedFilePath = Path.of("/some/path/to/test-file-1.png");
        int dummyFileSize = 10000;
        DistributedFile expectedFile = new DistributedFile(expectedFilePath, dummyFileSize);

        mockRestClient(sourceUri);
        mockFilePathFactory(sourceUri, expectedFilePath);
        mockFileSystemService(expectedFilePath, expectedFile);

        DistributedFile downloadedFile = fileDownloaderService.download(sourceUri);

        assertHttpClientWasInvoked();
        assertExpectedFileWasServed(downloadedFile, expectedFile);
        assertFileNameWasProperlyResolved(sourceUri);
        assertFileWasSavedOnDistributedFileSystem(expectedFilePath);
    }

    private void mockRestClient(String sourceUri) throws IOException, URISyntaxException {
        ClassPathResource resource = new ClassPathResource("file/test-file-1.png");
        String body = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);

        mockServer.expect(ExpectedCount.once(), requestTo(new URI(sourceUri)))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.IMAGE_PNG)
                .body(body)
            );
    }

    private void mockFilePathFactory(String sourceUri, Path expectedFilePath) {
        when(mockFilePathFactory.fromUri(sourceUri)).thenReturn(expectedFilePath);
    }

    private void mockFileSystemService(Path expectedFilePath, DistributedFile distributedFile) {
        when(mockFileSystemService.create(eq(expectedFilePath), any(InputStream.class))).thenReturn(distributedFile);
    }

    private void assertHttpClientWasInvoked() {
        mockServer.verify();
    }

    private void assertExpectedFileWasServed(DistributedFile downloadedFile, DistributedFile expectedFile) {
        assertThat(downloadedFile).isEqualTo(expectedFile);
    }

    private void assertFileNameWasProperlyResolved(String sourceUri) {
        verify(mockFilePathFactory, times(1)).fromUri(sourceUri);
    }

    private void assertFileWasSavedOnDistributedFileSystem(Path expectedFilePath) {
        verify(mockFileSystemService, times(1)).create(eq(expectedFilePath), any(InputStream.class));
    }
}
