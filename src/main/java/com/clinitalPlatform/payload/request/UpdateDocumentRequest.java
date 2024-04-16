package com.clinitalPlatform.payload.request;

import com.clinitalPlatform.models.TypeDocument;
import lombok.Data;

@Data
public class UpdateDocumentRequest {

    private Long id_doc;
    private String titre_doc;
    private TypeDocument typeDoc;

}
