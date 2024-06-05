package com.clinitalPlatform.services;

import com.clinitalPlatform.services.interfaces.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileServiceImpl implements FileService {


    @Value("${file.path}")
    public String filepath;

    @Override
     public void save(MultipartFile file){
        String dir=System.getProperty("user.dir")+"/" +filepath;
        try {
            file.transferTo(new File(dir + "/"+file.getOriginalFilename()));

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }


    }

    @Override
    public Resource getFile(String fileName){
        String dir=System.getProperty("user.dir")+"/" +filepath + "/"+fileName;
        Path path = Paths.get(dir);
        try {
            Resource resource=new UrlResource(path.toUri());
            return resource;
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


}
