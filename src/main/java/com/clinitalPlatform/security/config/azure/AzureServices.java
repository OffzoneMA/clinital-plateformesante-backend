package com.clinitalPlatform.security.config.azure;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/*@Component
public class AzureServices {

   @Autowired
   BlobServiceClient blobServiceClient;

   @Autowired
   BlobContainerClient blobContainerClient;

   @Value(value = "${azure.storage.account-name}")
	String containerName;
 
   // upload file to azure cloud :

   public String upload(MultipartFile multipartFile, String fileName,String folder) 
        throws IOException {

      // Todo UUID
      // CloudBlockBlob blockBlob = blob.GetBlockBlobReference("members/" + filename);
       // how to upload a file into a sub-folder in azure with java  
       //blobServiceClient().getBlobContainerClient(containerName);
       BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName+"/"+folder);
    
      BlobClient blob = containerClient
            .getBlobClient(fileName);
            
      blob.upload(multipartFile.getInputStream(),
            multipartFile.getSize(), true);
        
      return blob.getBlobUrl();
   }

   public String upload(ByteArrayInputStream fileIn,ByteArrayOutputStream OutFile, String fileName,String folder) 
        throws IOException {

       BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName+"/"+folder);
    
      BlobClient blob = containerClient
            .getBlobClient(fileName);
            
      blob.upload(fileIn,
      OutFile.size(), true);
        
      return blob.getBlobUrl();
   }
   // DownLoad file from azure cloud :

   public byte[] getFile(String fileName) 
        throws URISyntaxException {

      BlobClient blob = blobContainerClient.getBlobClient(fileName);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      blob.download(outputStream);
      final byte[] bytes = outputStream.toByteArray();
      return bytes;

   }

   // list file from azure cloud :

   public List<String> listBlobs() {

      PagedIterable<BlobItem> items = blobContainerClient.listBlobs();
      List<String> names = new ArrayList<String>();
      for (BlobItem item : items) {
         names.add(item.getName());
      }
      return names;

   }

   public Boolean deleteBlob(String blobName) {

      BlobClient blob = blobContainerClient.getBlobClient(blobName);
      blob.delete();
      return true;
   }

}*/

    

