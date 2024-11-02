![build](https://github.com/bgasparotto/file-downloader/workflows/build/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bgasparotto_file-downloader&metric=alert_status)](https://sonarcloud.io/dashboard?id=bgasparotto_file-downloader)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=bgasparotto_file-downloader&metric=coverage)](https://sonarcloud.io/dashboard?id=bgasparotto_file-downloader)

# File Downloader
Microservice for streaming files from HTTP endpoints directly to HDFS. The URL's and the stream are triggered by
messages on Apache Kafka. The resulting downloaded file is place on HDFS and its path shared on another Kafka topic.

## Running the service
Run docker-compose:
```shell script
git clone https://github.com/bgasparotto-engineering/file-downloader
cd file-downloader
docker-compose up -d
```

Then run the main class `FileDownloaderApplication.java`

## Interacting with the service
1. Run the `kafka-producer.sh` script to produce messages:
```
./kafka-producer.sh message.hansard-reader.downloadable-file input/DownloadableFile.avsc
```
then paste the content:
```
{"id":"test/test-file","title":"Test File","uri":"https://bgasparotto.com/wp-content/uploads/2015/04/bgasparotto-cube.png"}
```
2. Check the logs where the consumed messages will be displayed as a result;
3. Visit http://localhost:9870/ and lookout for the Web UI file browser to view the downloaded files.

### Generating Avro source code
This project uses [Gradle Avro Plugin](https://github.com/davidmc24/gradle-avro-plugin) for generating Java classes for
schemas defined in `.avsc` files:
```shell script
./gradlew generateAvroJava
```
