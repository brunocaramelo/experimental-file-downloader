package com.bgasparotto.filedownloader.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.bgasparotto.filedownloader.model.DistributedFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class DistributedFileSystemServiceTest {

    @Autowired
    private DistributedFileSystemService fileSystemService;

    @Value("${hdfs.output.path}")
    private String testOutputPath;

    @AfterEach
    public void tearDown() {
        fileSystemService.delete(Path.of(testOutputPath), true);
    }

    @Test
    public void shouldCreateFileWhenInputStreamIsProvided() throws IOException {
        Path destinationPath = Path.of(testOutputPath, "test-file-1.png");
        File inputFile = new File("src/test/resources/file/test-file-1.png");
        InputStream inputStream = FileUtils.openInputStream(inputFile);
        InputStream spyInputStream = Mockito.spy(inputStream);

        DistributedFile distributedFile = fileSystemService.create(destinationPath, spyInputStream);

        assertThat(distributedFile.getPath()).isEqualTo(destinationPath);
        assertThat(distributedFile.getSizeAsString()).isEqualTo("11 KB");
        assertThat(fileSystemService.exists(destinationPath)).isTrue();
        verify(spyInputStream, times(1)).close();
    }

    @Test
    public void shouldReturnTrueWhenFileExists() {
        Path path = addFileToTestFolder("test-file.example");
        assertThat(fileSystemService.exists(path)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenFileDoesNotExist() {
        Path unExistingFile = Path.of(testOutputPath, "this-does-not.exist");
        assertThat(fileSystemService.exists(unExistingFile)).isFalse();
    }

    @Test
    public void shouldDeleteFile() {
        Path path = addFileToTestFolder("path/to/deletion-test-1.example");

        fileSystemService.delete(path, false);
        assertThat(fileSystemService.exists(path)).isFalse();
    }

    @Test
    public void shouldDeleteFolder() {
        Path pathOne = addFileToTestFolder("path/to/deletion-test-1.example");
        Path pathTwo = addFileToTestFolder("path/to/deletion-test-2.example");

        fileSystemService.delete(Path.of(testOutputPath, "path/to"), true);
        assertThat(fileSystemService.exists(pathOne)).isFalse();
        assertThat(fileSystemService.exists(pathTwo)).isFalse();
    }

    private Path addFileToTestFolder(String filePath) {
        Path path = Path.of(testOutputPath, filePath);
        createRandomFile(path);

        return path;
    }

    private void createRandomFile(Path path) {
        String randomContent = RandomStringUtils.randomAlphabetic(1024, 2048);
        InputStream inputStream = IOUtils.toInputStream(randomContent, Charset.defaultCharset());
        fileSystemService.create(path, inputStream);
    }
}
