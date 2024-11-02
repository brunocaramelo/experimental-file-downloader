package com.bgasparotto.filedownloader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

@SpringBootApplication
public class FileDownloaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileDownloaderApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public Configuration hdfsConf(@Value("${hdfs.uri}") String hdfsUri) {
        Configuration hdfsConfiguration = new Configuration();
        hdfsConfiguration.set("fs.defaultFS", hdfsUri);
        hdfsConfiguration.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        hdfsConfiguration.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

        return hdfsConfiguration;
    }

    @Bean
    public FileSystem hdfs(@Value("${hdfs.uri}") String hdfsUri, Configuration hdfsConf) throws IOException {
        return FileSystem.newInstance(URI.create(hdfsUri), hdfsConf);
    }
}
