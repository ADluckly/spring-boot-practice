package com.example.spring_boot_demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(
            @Value("${DEMO_S3_ACCESS_KEY_ID:}") String accessKey,
            @Value("${DEMO_S3_SECRET_ACCESS_KEY:}") String secretKey,
            @Value("${DEMO_S3_REGION:}") String region
    ) {
        S3ClientBuilder builder = S3Client.builder();

        if (StringUtils.hasText(region)) {
            builder.region(Region.of(region));
        }

        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
