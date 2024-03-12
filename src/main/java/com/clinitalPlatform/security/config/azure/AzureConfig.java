package com.clinitalPlatform.security.config.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;


/*@Configuration
public class AzureConfig {

    @Value(value = "${azure.storage.account-key}")
	String azureStorageToken;
    
    @Value(value = "${azure.storage.account-name}")
	String containerName;

 
    @Bean
    public BlobServiceClient blobServiceClient() {
       
            String accountName = "documentspatient";
			String accountKey = azureStorageToken;

			StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

			String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

			BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
			.endpoint(endpoint)
					.credential(credential)
					.buildClient();

             return blobServiceClient;

    }
 
    @Bean
    public BlobContainerClient blobContainerClient() {
              
       BlobContainerClient containerClient = blobServiceClient().getBlobContainerClient(containerName);
       return containerClient;

    }


    
}*/
