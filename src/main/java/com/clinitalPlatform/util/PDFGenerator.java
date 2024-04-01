package com.clinitalPlatform.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.clinitalPlatform.payload.response.ApiResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;


public class PDFGenerator {

	 @Autowired
	 private ObjectMapper Mapper;
	 
	    public ResponseEntity<?> GenartePdfLocaly(Object obj,String filename,String Folder) throws IOException{
	        try {
	            File pdfFolder = new File("/Documents/"+Folder+"/"+filename+".pdf");
	            
	            Files.createDirectories(Paths.get("/Documents/"+Folder));
	       
	            Document document = new Document();
	            PdfWriter.getInstance(document, new FileOutputStream(pdfFolder));
	            document.open();
	            document.add(new Paragraph(Mapper.writeValueAsString(obj)));
	            document.close();
	            ResponseEntity.status(200).build();
	            
	            return ResponseEntity.ok(new ApiResponse(true, "File has been created successfully", obj)) ;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

    
}


