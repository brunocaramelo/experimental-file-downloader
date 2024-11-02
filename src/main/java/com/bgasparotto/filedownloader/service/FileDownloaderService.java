package com.bgasparotto.filedownloader.service;

import com.bgasparotto.filedownloader.model.DistributedFile;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloaderService {

    private final RestTemplate httpClient;
    private final DistributedFilePathFactory filePathFactory;
    private final DistributedFileSystemService fileSystemService;

    public DistributedFile download(String sourceUri) {
        Path destinationPath = filePathFactory.fromUri(sourceUri);
        URI typedSourceUri = URI.create(sourceUri);

        return download(destinationPath, typedSourceUri);
    }

    public DistributedFile download(Path destinationPath, URI sourceUri) {
        ResponseExtractor<DistributedFile> responseExtractor = createStreamExtractor(destinationPath);
        RequestCallback requestCallback = createEmptyRequestCallBack();

        return httpClient.execute(sourceUri, HttpMethod.GET, requestCallback, responseExtractor);
    }

    private ResponseExtractor<DistributedFile> createStreamExtractor(Path destinationPath) {
        return response -> {
            InputStream inputStream = response.getBody();
            return fileSystemService.create(destinationPath, inputStream);
        };
    }

    private RequestCallback createEmptyRequestCallBack() {
        return null;
    }
}
