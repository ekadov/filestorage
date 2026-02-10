package com.eugene.filestorage.service;

import com.eugene.filestorage.config.AppProperties;
import lombok.AllArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@AllArgsConstructor
public class S3StorageService {
    private final S3Client s3;
    private final AppProperties props;

    private static String formatFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public Mono<String> upload(FilePart filePart) {
        return Mono.fromCallable(() -> {
                    checkThatBucketExists();
                    String key = UUID.randomUUID() + "-" + formatFileName(filePart.filename());
                    Path tmp = Files.createTempFile("upload-", "-" + formatFileName(filePart.filename()));
                    try {
                        filePart.transferTo(tmp).block();

                        PutObjectRequest request = PutObjectRequest.builder()
                                .bucket(props.getS3().getBucket())
                                .key(key)
                                .contentType(filePart.headers().getContentType() == null ?
                                        null :
                                        filePart.headers().getContentType().toString())
                                .build();

                        s3.putObject(request, RequestBody.fromFile(tmp));
                        return props.getS3().getEndpoint() + "/" + props.getS3().getBucket() + "/" + key;
                    } finally {
                        try {
                            Files.deleteIfExists(tmp);
                        } catch (Exception ignored) {
                        }
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private void checkThatBucketExists() {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(props.getS3().getBucket()).build());
        } catch (NoSuchBucketException ex) {
            s3.createBucket(CreateBucketRequest.builder().bucket(props.getS3().getBucket()).build());
        }
    }
}
