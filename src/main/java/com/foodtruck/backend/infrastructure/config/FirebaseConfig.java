package com.foodtruck.backend.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Value("${firebase.service-account}")
    private Resource serviceAccount;

    @Bean
    public Storage firebaseStorage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount.getInputStream());
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    @Bean
    public String firebaseBucketName() {
        return bucketName;
    }
}
